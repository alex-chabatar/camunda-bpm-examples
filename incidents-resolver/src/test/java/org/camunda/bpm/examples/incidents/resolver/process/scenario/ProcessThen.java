package org.camunda.bpm.examples.incidents.resolver.process.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;

import org.camunda.bpm.engine.history.HistoricIncident;
import org.camunda.bpm.engine.runtime.Incident;

import com.tngtech.jgiven.annotation.*;
import com.tngtech.jgiven.integration.spring.JGivenStage;

@JGivenStage
public class ProcessThen<SELF extends ProcessThen<SELF>> extends AbstractProcessStage<SELF> {

  protected enum ModelType {
    RUNTIME, HISTORY
  }

  @ExpectedScenarioState
  protected String processId;

  @ExpectedScenarioState
  private Map<String, String> processNameToId;

  @ScenarioState
  protected String businessKey;

  @ProvidedScenarioState
  public Map<String, Object> runtimeModel;

  @ProvidedScenarioState
  public Map<String, Object> historyModel;

  @ProvidedScenarioState
  public String externalTaskId;

  @ProvidedScenarioState
  public String incidentId;

  private ModelType modelType;

  @AfterScenario
  public void cleanUp() {
    // cleanup
    workflowHelper.deleteProcesses();
    // reset clock
    workflowHelper.resetWorkflowCurrentTime();
  }

  // -- Process Definitions

  @As("$number deployed BPMN process definition(s)")
  public SELF deployed_process_definitions(int number) {
    assertThat(workflowHelper.getProcessDefinitions()).hasSize(number);
    return self();
  }

  @As("BPMN process definition of type $processType")
  public SELF deployed_process_definition(@SingleQuoted String processType) {
    assertThat(workflowHelper.getProcessDefinition(processType)).isNotNull();
    return self();
  }

  // -- Processes

  @As("$number active BPMN process(es) of type $processType")
  public SELF active_processes(int number, @SingleQuoted String processType) {
    assertThat(workflowHelper.getProcessInstances(processType)).hasSize(number);
    return self();
  }

  @As("1 active BPMN process of type $processType referenced as $processName")
  public SELF active_process_of_type_$_referenced_as_$(@SingleQuoted String processType,
      @SingleQuoted String processName) {
    var process = workflowHelper.getProcessInstanceByKey(processType);
    assertThat(process).isNotNull();
    processNameToId.put(processName, process.getId());
    return self();
  }

  // -- Process runtime

  @As("process runtime for $processName")
  public SELF process_runtime(@SingleQuoted String processName) {
    assertThat(processNameToId).containsKey(processName);
    modelType = ModelType.RUNTIME;
    runtimeModel = workflowHelper.getProcessVariables(processNameToId.get(processName));
    return self();
  }

  // -- Process history

  @As("process history for $processName")
  public SELF process_history(@SingleQuoted String processName) {
    assertThat(processNameToId).containsKey(processName);
    modelType = ModelType.HISTORY;
    historyModel = workflowHelper.getProcessHistoryVariables(processNameToId.get(processName));
    return self();
  }

  @As("$number completed BPMN process(es) of type $processType")
  public SELF completed_processes(int number, @SingleQuoted String processType) {
    var historicProcessInstances = workflowHelper.getHistoricProcessInstances(processType);
    var processInstances = workflowHelper.getProcessInstances(processType);
    assertThat(historicProcessInstances.size() - processInstances.size()).isEqualTo(number);
    return self();
  }

  // -- External Tasks

  @As("1 active external task with $topic topic")
  public SELF active_external_task(@SingleQuoted String topic) {
    active_external_tasks(1, topic);
    externalTaskId = workflowHelper.getExternalTasks(topic).get(0).getId();
    return self();
  }

  @As("$number active external task(s)")
  public SELF active_external_tasks(int number) {
    assertThat(workflowHelper.getExternalTasks()).hasSize(number);
    return self();
  }

  @As("$number active external task(s) with $topic topic")
  public SELF active_external_tasks(int number, @SingleQuoted String topic) {
    assertThat(workflowHelper.getExternalTasks(topic)).hasSize(number);
    return self();
  }

  public SELF with_external_task_error_message(@SingleQuoted String errorMessage) {
    assertThat(externalTaskId).isNotNull();
    assertThat(workflowHelper.getExternalTask(externalTaskId).getErrorMessage()).isEqualTo(errorMessage);
    return self();
  }

  public SELF with_external_task_retries(@SingleQuoted int retries) {
    assertThat(externalTaskId).isNotNull();
    assertThat(workflowHelper.getExternalTask(externalTaskId).getRetries()).isEqualTo(retries);
    return self();
  }

  // -- Incidents

  @As("1 active incident of type $incidentType")
  public SELF active_incident(@SingleQuoted String incidentType) {
    with_incidents(1, incidentType);
    incidentId = getIncidents(incidentType).get(0).getId();
    return self();
  }

  public SELF with_incident_message(@SingleQuoted String incidentMessage) {
    assertThat(incidentId).isNotNull();
    assertThat(workflowHelper.getIncident(incidentId).getIncidentMessage()).isEqualTo(incidentMessage);
    return self();
  }

  public SELF with_incidents(int number) {
    assertThat(workflowHelper.getIncidents(processId)).hasSize(number);
    return self();
  }

  @As("$number incidents of type $incidentType")
  public SELF with_incidents(int number, @SingleQuoted String incidentType) {
    assertThat(getIncidents(incidentType)).hasSize(number);
    return self();
  }

  private List<Incident> getIncidents(String incidentType) {
    return workflowHelper.getIncidents(processId).stream()
        .filter(incident -> incidentType.equals(incident.getIncidentType()))
        .toList();
  }

  public SELF with_no_incidents() {
    assertThat(workflowHelper.hasIncidents(processId)).isFalse();
    return self();
  }

  public SELF with_no_incidents(@SingleQuoted String processName) {
    assertThat(processNameToId).containsKey(processName);
    assertThat(workflowHelper.hasIncidents(processNameToId.get(processName))).isFalse();
    return self();
  }

  @As("$number history incidents of type $incidentType")
  public SELF with_history_incidents(int number, @SingleQuoted String incidentType, @SingleQuoted String processName) {
    if (processName != null) {
      assertThat(processNameToId).containsKey(processName);
      assertThat(getHistoryIncidents(processNameToId.get(processName), incidentType)).hasSize(number);
    } else {
      assertThat(getHistoryIncidents(processId, incidentType)).hasSize(number);
    }
    return self();
  }

  private List<HistoricIncident> getHistoryIncidents(String processId, String incidentType) {
    return workflowHelper.getHistoryIncidents(processId).stream()
        .filter(incident -> incidentType.equals(incident.getIncidentType()))
        .toList();
  }

}
