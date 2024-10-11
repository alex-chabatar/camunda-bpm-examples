package org.camunda.bpm.examples.incidents.resolver.utils;

import static java.lang.System.currentTimeMillis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.externaltask.ExternalTask;
import org.camunda.bpm.engine.externaltask.ExternalTaskQuery;
import org.camunda.bpm.engine.externaltask.LockedExternalTask;
import org.camunda.bpm.engine.history.*;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Getter
public class WorkflowHelper {

  private final ProcessEngineConfiguration processEngineConfiguration;
  private final RepositoryService repositoryService;
  private final RuntimeService runtimeService;
  private final HistoryService historyService;
  private final ExternalTaskService externalTaskService;
  private final ManagementService managementService;

  // -- Process Definitions

  public List<ProcessDefinition> getProcessDefinitions() {
    return repositoryService
        .createProcessDefinitionQuery()
        .latestVersion()
        .list();
  }

  public ProcessDefinition getProcessDefinition(String processDefinitionKey) {
    return repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .latestVersion()
        .singleResult();
  }

  public ProcessDefinition getProcessDefinitionById(String processDefinitionId) {
    if (!ObjectUtils.isEmpty(processDefinitionId)) {
      return repositoryService
          .createProcessDefinitionQuery()
          .processDefinitionId(processDefinitionId)
          .singleResult();
    }
    return null;
  }

  public String getProcessDefinitionKeyById(String processDefinitionId) {
    var definition = getProcessDefinitionById(processDefinitionId);
    return definition != null ? definition.getKey() : null;
  }

  // -- Process Instances

  public List<ProcessInstance> getProcessInstances() {
    return createProcessInstanceQuery().list();
  }

  public List<ProcessInstance> getProcessInstances(String processDefinitionKey) {
    return createProcessInstanceQuery()
        .processDefinitionKey(processDefinitionKey)
        .list();
  }

  public ProcessInstance getProcessInstanceByKey(String processDefinitionKey) {
    var processInstances = getProcessInstances(processDefinitionKey);
    return processInstances.isEmpty() ? null : processInstances.get(0);
  }

  public ProcessInstance getProcessInstance(String processId) {
    return createProcessInstanceQuery()
        .processInstanceId(processId)
        .singleResult();
  }

  private ProcessInstanceQuery createProcessInstanceQuery() {
    return runtimeService.createProcessInstanceQuery();
  }

  public String createProcess(String processDefinitionKey, String businessKey, Map<String, Object> values) {
    return createProcessInstance(processDefinitionKey, businessKey, values).getId();
  }

  public ProcessInstance createProcessInstance(String processDefinitionKey, String businessKey,
      Map<String, Object> values) {
    var startTime = System.currentTimeMillis();
    var result = createProcessInstanceBuilder(processDefinitionKey, businessKey, values).execute();
    log.info("Created workflow process '{}' of type '{}' with businessKey '{}' and values {}, time used {} ms",
        result.getId(), processDefinitionKey, businessKey, values, System.currentTimeMillis() - startTime);
    return result;
  }

  private ProcessInstantiationBuilder createProcessInstanceBuilder(String processDefinitionKey, String businessKey,
      Map<String, Object> values) {
    return createProcessInstanceBuilder(processDefinitionKey, values)
        .businessKey(businessKey);
  }

  private ProcessInstantiationBuilder createProcessInstanceBuilder(String processDefinitionKey,
      Map<String, Object> values) {
    return runtimeService.createProcessInstanceByKey(processDefinitionKey)
        .setVariables(values);
  }

  public List<HistoricProcessInstance> getHistoricProcessInstances() {
    return createHistoricProcessInstanceQuery().list();
  }

  public List<HistoricProcessInstance> getHistoricProcessInstances(String processDefinitionKey) {
    return createHistoricProcessInstanceQuery()
        .processDefinitionKey(processDefinitionKey)
        .list();
  }

  private HistoricProcessInstanceQuery createHistoricProcessInstanceQuery() {
    return historyService.createHistoricProcessInstanceQuery();
  }

  // --- Variables

  public Map<String, Object> getProcessVariables(String executionId) {
    Map<String, Object> variables = new HashMap<>();
    variableInstanceQuery()
        .executionIdIn(executionId)
        .list()
        .stream()
        .forEach(item -> variables.put(item.getName(), item.getValue()));
    return variables;
  }

  private VariableInstanceQuery variableInstanceQuery() {
    return runtimeService.createVariableInstanceQuery();
  }

  public Map<String, Object> getProcessHistoryVariables(String executionId) {
    Map<String, Object> variables = new HashMap<>();
    historyService.createHistoricVariableInstanceQuery()
        .executionIdIn(executionId)
        .list()
        .stream()
        .forEach(item -> variables.put(item.getName(), item.getValue()));
    return variables;
  }

  // --- Delete

  public void deleteProcess(String processId, String reason) {
    runtimeService.deleteProcessInstance(processId, reason);
  }

  public void deleteProcesses(String reason) {
    getProcessInstances().forEach(process -> deleteProcess(process.getId(), reason));
  }

  public void deleteHistoryProcess(String processId) {
    historyService.deleteHistoricProcessInstance(processId);
  }

  public void deleteHistoryProcesses() {
    getHistoricProcessInstances().forEach(process -> deleteHistoryProcess(process.getId()));
  }

  public void deleteProcesses() {
    deleteProcesses("cleanup");
    deleteHistoryProcesses();
  }

  // -- External tasks

  public List<ExternalTask> getExternalTasks() {
    return externalTaskQuery()
        .list();
  }

  public ExternalTask getExternalTask(String externalTaskId) {
    return externalTaskQuery()
        .externalTaskId(externalTaskId)
        .singleResult();
  }

  public List<HistoricExternalTaskLog> getExternalTaskLogs(String externalTaskId) {
    return historyService
        .createHistoricExternalTaskLogQuery()
        .externalTaskId(externalTaskId)
        .list();
  }

  public List<ExternalTask> getExternalTasks(String topic) {
    return externalTaskQuery()
        .topicName(topic)
        .list();
  }

  private ExternalTaskQuery externalTaskQuery() {
    return externalTaskService.createExternalTaskQuery();
  }

  public void completeExternalTasksWithFailure(String topic, String errorMessage) {
    completeExternalTasksWithFailure(topic, errorMessage, 0, 0);
  }

  public void completeExternalTasksWithFailure(String topic, String errorMessage, int retries, long retryTimeout) {
    String workerId = workerId(topic);
    fetchAndLock(topic, workerId)
        .forEach(task -> completeExternalTaskWithFailure(task.getId(), workerId, errorMessage, retries, retryTimeout));
  }

  public void completeExternalTaskWithFailure(String taskId, String workerId, String errorMessage, int retries,
      long retryTimeout) {
    externalTaskService.handleFailure(taskId, workerId, errorMessage, retries, retryTimeout);
  }

  private String workerId(String topic) {
    return String.join("-", "externalWorker", topic, String.valueOf(currentTimeMillis()));
  }

  private List<LockedExternalTask> fetchAndLock(String topic, String workerId) {
    return externalTaskService.fetchAndLock(10, workerId)
        .topic(topic, 60L * 1000L)
        .execute();
  }

  // -- Incidents

  public List<Incident> getIncidents() {
    return incidentQuery().list();
  }

  public Incident getIncident(String incidentId) {
    return incidentQuery()
        .incidentId(incidentId)
        .singleResult();
  }

  public List<Incident> getIncidents(String processId) {
    return incidentQuery()
        .processInstanceId(processId)
        .list();
  }

  public boolean hasIncidents(String processId) {
    return !getIncidents(processId).isEmpty();
  }

  private IncidentQuery incidentQuery() {
    return runtimeService.createIncidentQuery();
  }

  public List<HistoricIncident> getHistoryIncidents(String processId) {
    return historicIncidentQuery()
        .processInstanceId(processId)
        .list();
  }

  public HistoricIncidentQuery historicIncidentQuery() {
    return historyService.createHistoricIncidentQuery();
  }

  public void retryIncident(String incidentId) {
    retryIncident(getIncident(incidentId));
  }

  public void retryIncident(Incident incident) {
    if (incident == null || incident.getProcessInstanceId() == null) { // not found or batch migration job
      return; // skip
    }
    if (Incident.FAILED_JOB_HANDLER_TYPE.equals(incident.getIncidentType())) {
      var job = managementService.createJobQuery()
          .processInstanceId(incident.getProcessInstanceId())
          .executionId(incident.getExecutionId())
          .failedActivityId(incident.getFailedActivityId())
          .noRetriesLeft()
          .singleResult();
      managementService.setJobRetries(job.getId(), 1);
    } else if (Incident.EXTERNAL_TASK_HANDLER_TYPE.equals(incident.getIncidentType())) {
      var task = externalTaskService.createExternalTaskQuery()
          .processInstanceId(incident.getProcessInstanceId())
          .executionId(incident.getExecutionId())
          .activityId(incident.getActivityId())
          .noRetriesLeft()
          .singleResult();
      externalTaskService.setRetries(task.getId(), 1);
    }
  }

  // -- Clock

  public void resetWorkflowCurrentTime() {
    ClockUtil.reset();
  }

}
