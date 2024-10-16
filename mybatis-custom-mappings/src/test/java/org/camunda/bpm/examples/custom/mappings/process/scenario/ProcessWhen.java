package org.camunda.bpm.examples.custom.mappings.process.scenario;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.examples.custom.mappings.TestDataProvider.businessKey;
import static org.camunda.bpm.examples.custom.mappings.TestDataProvider.userId;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.task.Task;

import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.SingleQuoted;
import com.tngtech.jgiven.integration.spring.JGivenStage;

@JGivenStage
public class ProcessWhen<SELF extends ProcessWhen<SELF>> extends AbstractProcessStage<SELF> {

  @ProvidedScenarioState
  protected String processId;

  // -- Process

  @As("Create a process of type $processDefinitionKey")
  public SELF create_a_process(@SingleQuoted String processDefinitionKey) {

    processId = workflowHelper.createProcess(processDefinitionKey, businessKey(), emptyMap());

    assertThat(processId).isNotNull();

    log.info("Created process {}", processId);
    waitForJobExecutorToProcessAllJobs();

    return self();
  }

  // -- User tasks

  @As("complete active task of type $taskDefinitionKey")
  public SELF complete_task(@SingleQuoted String taskDefinitionKey) {
    return complete_task(taskDefinitionKey, emptyMap());
  }

  @As("complete active task of type $taskDefinitionKey with $variables")
  public SELF complete_task(@SingleQuoted String taskDefinitionKey, Map<String, Object> variables) {
    var tasks = workflowHelper.getTasksByKey(taskDefinitionKey);
    assertThat(tasks).hasSize(1);
    workflowHelper.completeTask(tasks.get(0).getId(), variables);
    waitForJobExecutorToProcessAllJobs();
    return self();
  }

  // -- Process/Task comments

  @As("comment on process with $comment at $time")
  public SELF comment_on_process(@SingleQuoted String comment, @SingleQuoted Date time) {
    workflowHelper.addProcessComment(processId, userId(), comment, time);
    return self();
  }

  @As("update process comment with $comment")
  public SELF update_process_comment(@SingleQuoted String comment) {
    workflowHelper.getProcessComments(processId)
        .forEach(item -> workflowHelper.updateComment(item.getId(), userId(), comment));
    return self();
  }

  @As("update process comment with $time")
  public SELF update_process_comment(@SingleQuoted Date time) {
    workflowHelper.getProcessComments(processId)
        .forEach(item -> workflowHelper.updateComment(item.getId(), userId(), item.getFullMessage(), time));
    return self();
  }

  @As("delete all process comments")
  public SELF delete_process_comments() {
    workflowHelper.getProcessComments(processId)
        .forEach(item -> workflowHelper.deleteComment(item.getId(), userId()));
    return self();
  }

  @As("comment on all active tasks of type $taskDefinitionKey with $comment at $time")
  public SELF comment_on_tasks(@SingleQuoted String taskDefinitionKey, @SingleQuoted String comment,
      @SingleQuoted Date time) {
    List<Task> tasks = workflowHelper.getTasksByKey(taskDefinitionKey);
    assertThat(tasks).isNotEmpty();
    tasks.forEach(task -> workflowHelper.addTaskComment(task.getId(), userId(), comment, time));
    return self();
  }

  @As("update all comments for tasks of type $taskDefinitionKey with $comment")
  public SELF update_task_comment(@SingleQuoted String taskDefinitionKey, @SingleQuoted String comment) {
    List<Task> tasks = workflowHelper.getTasksByKey(taskDefinitionKey);
    assertThat(tasks).isNotEmpty();
    tasks.forEach(task -> workflowHelper.getTaskComments(task.getId())
        .forEach(item -> workflowHelper.updateComment(item.getId(), userId(), comment)));
    return self();
  }

  @As("update all comments for tasks of type $taskDefinitionKey with $time")
  public SELF update_task_comment(@SingleQuoted String taskDefinitionKey, @SingleQuoted Date time) {
    List<Task> tasks = workflowHelper.getTasksByKey(taskDefinitionKey);
    assertThat(tasks).isNotEmpty();
    tasks.forEach(task -> workflowHelper.getTaskComments(task.getId())
        .forEach(item -> workflowHelper.updateComment(item.getId(), userId(), item.getFullMessage(), time)));
    return self();
  }

  @As("delete all comments for tasks of type $taskDefinitionKey")
  public SELF delete_task_comments(@SingleQuoted String taskDefinitionKey) {
    List<Task> tasks = workflowHelper.getTasksByKey(taskDefinitionKey);
    assertThat(tasks).isNotEmpty();
    tasks.forEach(task -> workflowHelper.getTaskComments(task.getId())
        .forEach(item -> workflowHelper.deleteComment(item.getId(), userId())));
    return self();
  }

}
