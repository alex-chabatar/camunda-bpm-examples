package org.camunda.bpm.examples.liquibase.config;

import org.camunda.bpm.examples.liquibase.spring.RequiredBy;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import liquibase.integration.spring.SpringLiquibase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@RequiredBy("repositoryService")
public class LiquibaseConfig {

  private final SpringLiquibase liquibase;

  @PostConstruct
  private void init() {
    log.info("Liquibase initialized: {}", liquibase.getChangeLog());
  }

}
