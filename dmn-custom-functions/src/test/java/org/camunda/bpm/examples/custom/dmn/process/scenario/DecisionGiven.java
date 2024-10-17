package org.camunda.bpm.examples.custom.dmn.process.scenario;

import java.util.HashMap;
import java.util.Map;

import com.tngtech.jgiven.annotation.As;
import com.tngtech.jgiven.annotation.ProvidedScenarioState;
import com.tngtech.jgiven.annotation.SingleQuoted;
import com.tngtech.jgiven.integration.spring.JGivenStage;

@JGivenStage
public class DecisionGiven<SELF extends DecisionGiven<SELF>> extends AbstractProcessStage<SELF> {

  @ProvidedScenarioState
  private Map<String, Object> decisionInput;

  public SELF a_decision_engine() {
    return self();
  }

  public SELF a_decision_input() {
    decisionInput = new HashMap<>();
    return self();
  }

  @As("with $key = $value")
  public SELF with(@SingleQuoted String key, @SingleQuoted Object value) {
    decisionInput.put(key, value);
    return self();
  }

  @As("with $values")
  public SELF with(Map<String, Object> values) {
    decisionInput.putAll(values);
    return self();
  }

}
