package org.camunda.bpm.examples.incidents.resolver.process.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import com.tngtech.jgiven.annotation.*;
import com.tngtech.jgiven.integration.spring.JGivenStage;

@JGivenStage
public class ProcessWhen<SELF extends ProcessWhen<SELF>> extends AbstractProcessStage<SELF> {

  @ExpectedScenarioState
  protected String businessKey;

  @ExpectedScenarioState
  private Map<String, Object> model;

  @ProvidedScenarioState
  protected String processId;

  @ProvidedScenarioState
  protected final Map<String, String> processNameToId = new HashMap<>();

  // -- Process

  public SELF create_a_process(@SingleQuoted String processDefinitionKey) {
    return create_a_process(processDefinitionKey, businessKey);
  }

  @As("Create a process of type $processDefinitionKey with businessKey=$businessKey")
  public SELF create_a_process(@SingleQuoted String processDefinitionKey, @SingleQuoted String businessKey) {

    processId = workflowHelper.createProcess(processDefinitionKey, businessKey, model);

    assertThat(processId).isNotNull();

    log.info("Created process {}", processId);
    waitForJobExecutorToProcessAllJobs();

    return self();
  }

  public SELF referenced_as(@SingleQuoted String processName) {
    processNameToId.put(processName, processId);
    return self();
  }

  // -- External Tasks

  @As("complete all active external tasks for $topic topic with error $errorMessage")
  public SELF complete_external_tasks_with_failure(@SingleQuoted String topic, @SingleQuoted String errorMessage) {
    workflowHelper.completeExternalTasksWithFailure(topic, errorMessage);
    waitForJobExecutorToProcessAllJobs();
    return self();
  }

}
