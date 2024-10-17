package org.camunda.bpm.examples.custom.dmn.process;

import static org.camunda.bpm.examples.custom.dmn.ProcessConstants.TEST_DECISION_CONTAINS_ANY_OF;

import org.camunda.bpm.examples.custom.dmn.process.scenario.*;
import org.camunda.bpm.examples.custom.dmn.spring.AbstractSpringTest;
import org.junit.jupiter.api.Test;

@SuppressWarnings("squid:S2699") // Tests should include assertions
class DeploymentTest extends AbstractSpringTest<DecisionGiven<?>, DecisionWhen<?>, DecisionThen<?>> {

  @Test
  void ensureDeployment() {

    given()
        .a_decision_engine();

    then()
        .deployed_decision_definitions(1)
        .deployed_decision_definition(TEST_DECISION_CONTAINS_ANY_OF);

  }

}
