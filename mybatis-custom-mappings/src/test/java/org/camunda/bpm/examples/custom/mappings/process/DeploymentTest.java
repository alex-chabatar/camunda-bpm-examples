package org.camunda.bpm.examples.custom.mappings.process;

import org.camunda.bpm.examples.custom.mappings.process.scenario.ProcessGiven;
import org.camunda.bpm.examples.custom.mappings.process.scenario.ProcessThen;
import org.camunda.bpm.examples.custom.mappings.process.scenario.ProcessWhen;
import org.camunda.bpm.examples.custom.mappings.spring.AbstractSpringTest;
import org.junit.jupiter.api.Test;

@SuppressWarnings("squid:S2699") // Tests should include assertions
class DeploymentTest extends AbstractSpringTest<ProcessGiven<?>, ProcessWhen<?>, ProcessThen<?>> {

  @Test
  void ensureDeployment() {

    given()
        .a_process_engine();

    then()
        .deployed_process_definitions(1)
        .deployed_process_definition(TEST_PROCESS);

  }

}
