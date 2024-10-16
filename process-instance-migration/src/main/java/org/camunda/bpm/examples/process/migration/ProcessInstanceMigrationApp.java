package org.camunda.bpm.examples.process.migration;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.camunda.bpm.spring.boot.starter.event.PostDeployEvent;
import org.camunda.bpm.spring.boot.starter.event.PreUndeployEvent;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication(scanBasePackages = "org.camunda.bpm.examples")
@EnableProcessApplication
public class ProcessInstanceMigrationApp {

  public static void main(String... args) {
    SpringApplication.run(ProcessInstanceMigrationApp.class, args);
  }

  @EventListener
  public void onPostDeploy(PostDeployEvent event) {
    log.info("Process engine '{}' deployed", event.getProcessEngine().getName());
  }

  @EventListener
  public void onPreUndeploy(PreUndeployEvent event) {
    log.info("Process engine '{}' undeployed", event.getProcessEngine().getName());
  }

}
