package org.camunda.bpm.examples.incidents.resolver.process;

import static org.camunda.bpm.examples.incidents.resolver.ProcessConstants.INCIDENT_RESOLVER;

import org.camunda.bpm.examples.incidents.resolver.AbstractIncidentsTest;
import org.junit.jupiter.api.Test;

class DeploymentTest extends AbstractIncidentsTest {

  @Test
  void ensureDeployment() {

    given()
        .a_process_engine();

    then()
        .deployed_process_definitions(2)
        .deployed_process_definition(INCIDENT_RESOLVER)
        .deployed_process_definition(TEST_PROCESS);

  }

}
