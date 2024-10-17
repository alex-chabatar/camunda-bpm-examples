package org.camunda.bpm.examples.custom.dmn.process.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;

import com.tngtech.jgiven.annotation.ExpectedScenarioState;
import com.tngtech.jgiven.annotation.IntroWord;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.SingleQuoted;
import com.tngtech.jgiven.integration.spring.JGivenStage;

@JGivenStage
public class DecisionWhen<SELF extends DecisionWhen<SELF>> extends AbstractProcessStage<SELF> {

  @ExpectedScenarioState
  private Map<String, Object> decisionInput;

  @ProvidedScenarioState
  public String decisionDefinitionKey;

  @ProvidedScenarioState
  public DmnDecisionTableResult decisionResult;

  @Override
  @IntroWord
  public SELF when() {
    return super.when();
  }

  public SELF evaluate_decision(@SingleQuoted String decisionDefinitionKey) {
    this.decisionDefinitionKey = decisionDefinitionKey;
    decisionResult = workflowHelper.evaluateDecisionTableResult(decisionDefinitionKey, decisionInput);
    assertThat(decisionResult).isNotEmpty();

    log.info("Evaluated decision {} with result {}", decisionDefinitionKey, decisionResult);
    waitForJobExecutorToProcessAllJobs();

    return self();
  }

}
