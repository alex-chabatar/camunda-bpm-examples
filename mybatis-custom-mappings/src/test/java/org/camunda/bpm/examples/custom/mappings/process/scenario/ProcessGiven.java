package org.camunda.bpm.examples.custom.mappings.process.scenario;

import com.tngtech.jgiven.integration.spring.JGivenStage;

@JGivenStage
public class ProcessGiven<SELF extends ProcessGiven<SELF>> extends AbstractProcessStage<SELF> {

  public SELF a_process_engine() {
    return self();
  }

}
