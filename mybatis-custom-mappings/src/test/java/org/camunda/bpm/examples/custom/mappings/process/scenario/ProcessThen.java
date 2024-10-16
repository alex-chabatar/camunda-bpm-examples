package org.camunda.bpm.examples.custom.mappings.process.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;
import java.util.Objects;

import org.springframework.util.ObjectUtils;

import com.tngtech.jgiven.annotation.*;
import com.tngtech.jgiven.integration.spring.JGivenStage;

@JGivenStage
public class ProcessThen<SELF extends ProcessThen<SELF>> extends AbstractProcessStage<SELF> {

  @ExpectedScenarioState
  protected String processId;

  @ProvidedScenarioState
  public String taskId;

  @AfterScenario
  @SuppressWarnings("unused") // AfterScenario
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

  @As("$number completed BPMN process(es) of type $processType")
  public SELF completed_processes(int number, @SingleQuoted String processType) {
    var historicProcessInstances = workflowHelper.getHistoricProcessInstances(processType);
    var processInstances = workflowHelper.getProcessInstances(processType);
    assertThat(historicProcessInstances.size() - processInstances.size()).isEqualTo(number);
    return self();
  }

  // -- User tasks

  @As("$number active task(s) of type $taskType")
  public SELF active_tasks(int number, @SingleQuoted String taskType) {
    assertThat(workflowHelper.getTasksByKey(taskType)).hasSize(number);
    return self();
  }

  @As("active task of type $taskType")
  public SELF active_task(@SingleQuoted String taskType) {
    active_tasks(1, taskType);
    taskId = workflowHelper.getTasksByKey(taskType).get(0).getId();
    return self();
  }

  @As("$number history task(s)")
  public SELF completed_tasks(int number) {
    assertThat(workflowHelper.getHistoryTasks().stream()
        .filter(task -> Objects.nonNull(task.getEndTime())))
        .hasSize(number);
    return self();
  }

  @As("$number history task(s) of type $taskType")
  public SELF completed_tasks(int number, @SingleQuoted String taskType) {
    assertThat(workflowHelper.getHistoryTasksByKey(taskType).stream()
        .filter(task -> Objects.nonNull(task.getEndTime())))
        .hasSize(number);
    return self();
  }

  // -- Comments

  public SELF processes_of_type_$_have_comment(@SingleQuoted String processType, @SingleQuoted String comment,
      @SingleQuoted Date time) {
    var historicProcessInstances = workflowHelper.getHistoricProcessInstances(processType);
    assertThat(historicProcessInstances).isNotEmpty();
    historicProcessInstances.forEach(process -> assertThat(workflowHelper.getProcessComments(process.getId()))
        .anyMatch(item -> comment.equals(item.getFullMessage()) && time.equals(item.getTime())));
    return self();
  }

  public SELF processes_of_type_$_have_no_comment(@SingleQuoted String processType, @SingleQuoted String comment) {
    var historicProcessInstances = workflowHelper.getHistoricProcessInstances(processType);
    assertThat(historicProcessInstances).isNotEmpty();
    historicProcessInstances.forEach(process -> assertThat(workflowHelper.getProcessComments(process.getId()))
        .noneMatch(item -> comment.equals(item.getFullMessage())));
    return self();
  }

  public SELF tasks_of_type_$_have_comment(@SingleQuoted String taskType, @SingleQuoted String comment,
      @SingleQuoted Date time) {
    var historicTaskInstances = workflowHelper.getHistoryTasksByKey(taskType);
    assertThat(historicTaskInstances).isNotEmpty();
    historicTaskInstances.forEach(task -> {
      if (!ObjectUtils.isEmpty(comment)) {
        assertThat(workflowHelper.getTaskComments(task.getId()))
            .anyMatch(item -> comment.equals(item.getFullMessage()) && time.equals(item.getTime()));
      }
    });
    return self();
  }

  public SELF tasks_of_type_$_have_no_comment(@SingleQuoted String taskType, @SingleQuoted String comment) {
    var historicTaskInstances = workflowHelper.getHistoryTasksByKey(taskType);
    assertThat(historicTaskInstances).isNotEmpty();
    historicTaskInstances.forEach(task -> assertThat(workflowHelper.getTaskComments(task.getId()))
        .noneMatch(item -> comment.equals(item.getFullMessage())));
    return self();
  }

  // -- User Operation Log

  @As("User Operation Log for process contains entry with entityType $entityType and operationType $operationType ")
  public SELF user_operation_log_for_process_contains(@SingleQuoted String entityType, @SingleQuoted String operationType) {
    assertThat(workflowHelper.userOperationLogQuery()
        .processInstanceId(processId)
        .entityType(entityType)
        .operationType(operationType)
        .list())
        .isNotEmpty();
    return self();
  }

  @As("User Operation Log for task contains entry with entityType=$entityType and operationType=$operationType ")
  public SELF user_operation_log_for_task_contains(@SingleQuoted String entityType, @SingleQuoted String operationType) {
    assertThat(workflowHelper.userOperationLogQuery()
        .taskId(taskId)
        .entityType(entityType)
        .operationType(operationType)
        .list())
        .isNotEmpty();
    return self();
  }

}
