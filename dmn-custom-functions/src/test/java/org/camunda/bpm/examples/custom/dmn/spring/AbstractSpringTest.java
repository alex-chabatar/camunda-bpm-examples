package org.camunda.bpm.examples.custom.dmn.spring;

import org.camunda.bpm.examples.custom.dmn.CustomDmnFunctionsApp;
import org.camunda.bpm.examples.custom.dmn.spring.configuration.ScenarioConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.integration.spring.JGivenSpringConfiguration;
import com.tngtech.jgiven.integration.spring.junit5.SpringScenarioTest;

@SpringBootTest(classes = {
    CustomDmnFunctionsApp.class,
    ScenarioConfiguration.class,
    JGivenSpringConfiguration.class
})
public abstract class AbstractSpringTest<GIVEN extends Stage, WHEN extends Stage, THEN extends Stage>
    extends SpringScenarioTest<GIVEN, WHEN, THEN> {
}
