package org.camunda.bpm.examples.liquibase;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

@SpringBootTest(classes = SpringContextTest.TestConfig.class)
class SpringContextTest {

  @Autowired
  private RuntimeService processEngineConfiguration;

  @Test
  void contextLoaded() {
    assertThat(processEngineConfiguration).isNotNull();
  }

  @SpringBootConfiguration
  @ComponentScan("org.camunda.bpm.examples")
  public static class TestConfig {
  }

}
