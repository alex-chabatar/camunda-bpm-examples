package org.camunda.bpm.examples.custom.mappings.utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.*;
import org.camunda.bpm.engine.impl.ServiceImpl;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstantiationBuilder;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.task.TaskQuery;
import org.camunda.bpm.examples.custom.mappings.engine.cmd.AddCommentExtendedCmd;
import org.camunda.bpm.examples.custom.mappings.engine.cmd.DeleteCommentByIdCmd;
import org.camunda.bpm.examples.custom.mappings.engine.cmd.SelectCommentByIdCmd;
import org.camunda.bpm.examples.custom.mappings.engine.cmd.UpdateCommentCmd;
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
  private final TaskService taskService;
  private final ExternalTaskService externalTaskService;
  private final ManagementService managementService;
  private final IdentityService identityService;

  // -- Process Definitions

  public List<ProcessDefinition> getProcessDefinitions() {
    return processDefinitionQuery()
        .latestVersion()
        .list();
  }

  public ProcessDefinition getProcessDefinition(String processDefinitionKey) {
    return processDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .latestVersion()
        .singleResult();
  }

  private ProcessDefinitionQuery processDefinitionQuery() {
    return repositoryService.createProcessDefinitionQuery();
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

  private ProcessInstanceQuery createProcessInstanceQuery() {
    return runtimeService.createProcessInstanceQuery();
  }

  public String createProcess(String processDefinitionKey, String businessKey, Map<String, Object> values) {
    return createProcessInstance(processDefinitionKey, businessKey, values).getId();
  }

  public ProcessInstance createProcessInstance(String processDefinitionKey, String businessKey,
      Map<String, Object> values) {
    var startTime = getWorkflowCurrentTime().getTime();
    var result = createProcessInstanceBuilder(processDefinitionKey, businessKey, values).execute();
    log.info("Created workflow process '{}' of type '{}' with businessKey '{}' and values {}, time used {} ms",
        result.getId(), processDefinitionKey, businessKey, values, getWorkflowCurrentTime().getTime() - startTime);
    return result;
  }

  private ProcessInstantiationBuilder createProcessInstanceBuilder(String processDefinitionKey, String businessKey,
      Map<String, Object> values) {
    return createProcessInstanceBuilder(processDefinitionKey, values).businessKey(businessKey);
  }

  private ProcessInstantiationBuilder createProcessInstanceBuilder(String processDefinitionKey,
      Map<String, Object> values) {
    return runtimeService.createProcessInstanceByKey(processDefinitionKey).setVariables(values);
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

  // -- User tasks

  public List<Task> getTasksByKey(String taskDefinitionKey) {
    return tasksByKeyQuery(taskDefinitionKey).list();
  }

  private TaskQuery tasksByKeyQuery(String taskDefinitionKey) {
    return tasksQuery().taskDefinitionKey(taskDefinitionKey);
  }

  private TaskQuery tasksQuery() {
    return taskService
        .createTaskQuery()
        .initializeFormKeys();
  }

  public void completeTask(String taskId, Map<String, Object> variables) {
    taskService.complete(taskId, variables);
  }

  public List<HistoricTaskInstance> getHistoryTasks() {
    return historyService
        .createHistoricTaskInstanceQuery()
        .list();
  }

  public List<HistoricTaskInstance> getHistoryTasksByKey(String taskDefinitionKey) {
    return getHistoryTasks(taskDefinitionKey, null);
  }

  public List<HistoricTaskInstance> getHistoryTasks(String taskDefinitionKey, Map<String, Object> taskLocalVariables) {
    return getHistoryTasksQuery(taskDefinitionKey, taskLocalVariables).list();
  }

  private HistoricTaskInstanceQuery getHistoryTasksQuery(String taskDefinitionKey,
      Map<String, Object> taskLocalVariables) {
    HistoricTaskInstanceQuery query = getHistoryTasksQuery().taskDefinitionKey(taskDefinitionKey);
    if (!ObjectUtils.isEmpty(taskLocalVariables)) {
      taskLocalVariables.entrySet().stream()
          .forEach(variable -> query.taskVariableValueEquals(variable.getKey(), variable.getValue()));
    }
    return query;
  }

  public HistoricTaskInstanceQuery getHistoryTasksQuery() {
    return historyService.createHistoricTaskInstanceQuery();
  }

  // -- Comments

  public Comment getCommentById(String commentId) {
    return getCommandExecutor().execute(new SelectCommentByIdCmd(commentId));
  }

  public List<Comment> getProcessComments(String processId) {
    return taskService.getProcessInstanceComments(processId);
  }

  public Comment addProcessComment(String processId, String comment) {
    return addProcessComment(processId, null, comment);
  }

  public Comment addProcessComment(String processId, String userId, String comment) {
    return withAuthenticatedUser(userId, () -> taskService.createComment(null, processId, comment));
  }

  public Comment addProcessComment(String processId, String comment, Date time) {
    return addProcessComment(processId, null, comment, time);
  }

  public Comment addProcessComment(String processId, String userId, String comment, Date time) {
    return addComment(null, processId, userId, comment, time);
  }

  public List<Comment> getTaskComments(String taskId) {
    return taskService.getTaskComments(taskId);
  }

  public Comment addTaskComment(String taskId, String comment) {
    return addTaskComment(taskId, null, comment);
  }

  public Comment addTaskComment(String taskId, String userId, String comment) {
    return withAuthenticatedUser(userId, () -> taskService.createComment(taskId, null, comment));
  }

  public Comment addTaskComment(String taskId, String comment, Date time) {
    return addTaskComment(taskId, null, comment, time);
  }

  public Comment addTaskComment(String taskId, String userId, String comment, Date time) {
    return addComment(taskId, null, userId, comment, time);
  }

  public Comment addComment(String taskId, String processId, String userId, String comment) {
    return withAuthenticatedUser(userId, () -> taskService.createComment(taskId, processId, comment));
  }

  public Comment addComment(String taskId, String processId, String userId, String comment, Date time) {
    return withAuthenticatedUser(userId,
        () -> getCommandExecutor().execute(new AddCommentExtendedCmd(taskId, processId, comment, time)));
  }

  public void updateComment(String commentId, String userId, String comment) {
    withAuthenticatedUser(userId, () -> getCommandExecutor().execute(new UpdateCommentCmd(commentId, userId, comment)));
  }

  public void updateComment(String commentId, String userId, String comment, Date time) {
    withAuthenticatedUser(userId,
        () -> getCommandExecutor().execute(new UpdateCommentCmd(commentId, userId, comment, time)));
  }

  public void deleteComment(String commentId, String userId) {
    withAuthenticatedUser(userId, () -> getCommandExecutor().execute(new DeleteCommentByIdCmd(commentId, userId)));
  }

  private <T> T withAuthenticatedUser(String userId, Supplier<T> supplier) {
    if (!ObjectUtils.isEmpty(userId)) {
      identityService.setAuthenticatedUserId(userId);
      T result = supplier.get();
      identityService.setAuthenticatedUserId(null);
      return result;
    } else {
      return supplier.get();
    }
  }

  // -- Operation Log

  public UserOperationLogQuery userOperationLogQuery() {
    return historyService.createUserOperationLogQuery();
  }

  // -- Execution

  public CommandExecutor getCommandExecutor() {
    return ((ServiceImpl) taskService).getCommandExecutor();
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

  // -- Clock

  public Date getWorkflowCurrentTime() {
    return ClockUtil.getCurrentTime();
  }

  public void resetWorkflowCurrentTime() {
    ClockUtil.reset();
  }

}
