package org.camunda.bpm.examples.custom.mappings.spring;

import org.camunda.bpm.examples.custom.mappings.CustomMappingsApp;
import org.camunda.bpm.examples.custom.mappings.spring.configuration.ScenarioConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.integration.spring.JGivenSpringConfiguration;
import com.tngtech.jgiven.integration.spring.junit5.SpringScenarioTest;

@SpringBootTest(classes = {
    CustomMappingsApp.class,
    ScenarioConfiguration.class,
    JGivenSpringConfiguration.class
})
public abstract class AbstractSpringTest<GIVEN extends Stage, WHEN extends Stage, THEN extends Stage>
    extends SpringScenarioTest<GIVEN, WHEN, THEN> {

  protected static final String TEST_PROCESS = "TestProcess";

  protected static final String USER_TASK_PROCESS_DATA = "UserTask_ProcessData";

}
