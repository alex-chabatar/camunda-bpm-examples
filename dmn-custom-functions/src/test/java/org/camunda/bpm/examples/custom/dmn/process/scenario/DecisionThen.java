package org.camunda.bpm.examples.custom.dmn.process.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionOutputInstance;

import com.tngtech.jgiven.annotation.*;
import com.tngtech.jgiven.integration.spring.JGivenStage;

@JGivenStage
public class DecisionThen<SELF extends DecisionThen<SELF>> extends AbstractProcessStage<SELF> {

  protected enum ModelType {
    RUNTIME, HISTORY
  }

  private ModelType modelType;

  @ExpectedScenarioState
  public String decisionDefinitionKey;

  @ExpectedScenarioState
  private DmnDecisionTableResult decisionResult;

  private List<HistoricDecisionInstance> historyDecisions;

  @AfterScenario
  public void cleanUp() {
    // cleanup
    workflowHelper.deleteHistoricDecisionProcesses();
    // reset clock
    workflowHelper.resetWorkflowCurrentTime();
  }

  // -- Decision Definitions

  @As("$number deployed DMN decision definition(s)")
  public SELF deployed_decision_definitions(int number) {
    assertThat(workflowHelper.getDecisionDefinitions()).hasSize(number);
    return self();
  }

  @As("DMN decision definition of type $decisionType")
  public SELF deployed_decision_definition(@SingleQuoted String decisionType) {
    assertThat(workflowHelper.getDecisionDefinition(decisionType)).isNotNull();
    return self();
  }

  protected ModelType getModelType() {
    return modelType;
  }

  public SELF decision_result() {
    modelType = ModelType.RUNTIME;
    return self();
  }

  @As("history decision result with definition key = $decisionDefinitionKey")
  public SELF history_decision_result(@SingleQuoted String decisionDefinitionKey) {
    this.decisionDefinitionKey = decisionDefinitionKey;
    modelType = ModelType.HISTORY;
    return self();
  }

  @As("has $number decision(s)")
  public SELF has_decisions(int number) {
    if (ModelType.RUNTIME.equals(getModelType())) {
      assertThat(decisionResult).hasSize(number);
    } else if (ModelType.HISTORY.equals(getModelType())) {
      assertThat(decisionDefinitionKey)
          .as("use .history_decision_result(\"decisionDefinitionKey\") before")
          .isNotNull();
      loadHistoryDecisions();
      assertThat(historyDecisions).hasSize(number);
    }
    return self();
  }

  private void loadHistoryDecisions() {
    if (historyDecisions == null) {
      historyDecisions = workflowHelper.getHistoryDecisions(decisionDefinitionKey);
    }
  }

  @As("with $key = $value")
  public SELF with(@SingleQuoted String key, @SingleQuoted Object value) {
    if (ModelType.HISTORY.equals(getModelType())) {
      return with_value_in_history(key, value);
    }
    Optional<Map<String, Object>> actual = decisionResult.getResultList().stream()
        .filter(map -> map.containsKey(key))
        .findFirst();
    boolean result = (actual.isPresent() && actual.get().get(key).equals(value))
        || value == null;  // clearing rule will not report anything
    log.debug("found match {} = {} with result: {}", key, value, result);
    assertThat(result).as("Expected %s = %s; Actual: %s", key, value, actual.isPresent()
        ? actual.get().get(key) : null).isTrue();
    return self();
  }

  private SELF with_value_in_history(@SingleQuoted String key, @SingleQuoted Object value) {
    loadHistoryDecisions();
    Optional<HistoricDecisionOutputInstance> actual = historyDecisions.stream()
        .flatMap(historicDecision -> historicDecision.getOutputs().stream())
        .filter(output -> key.equals(output.getVariableName()))
        .filter(output -> value == null || value.equals(output.getValue()))
        .findFirst();
    boolean result = actual.isPresent() || value == null;  // clearing rule will not report anything
    log.debug("found match {} = {} with result: {}", key, value, result);
    assertThat(result).as("Expected %s = %s; Actual: %s", key, value, actual.isPresent()
        ? actual.get().getValue() : null).isTrue();
    return self();
  }

  @As("with rule = $ruleId")
  public SELF with_rule(@SingleQuoted String ruleId) {
    loadHistoryDecisions();
    List<String> rules = historyDecisions.stream()
        .flatMap(historicDecision -> historicDecision.getOutputs().stream())
        .map(HistoricDecisionOutputInstance::getRuleId)
        .toList();
    if (!rules.isEmpty()) { // clearing rule will not report anything
      assertThat(rules).containsOnly(ruleId);
    }
    return self();
  }

}
