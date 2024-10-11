package org.camunda.bpm.examples.incidents.resolver.process.scenario;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.examples.incidents.resolver.utils.WorkflowHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.tngtech.jgiven.Stage;

public abstract class AbstractProcessStage<SELF extends AbstractProcessStage<SELF>> extends Stage<SELF> {

  protected Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  protected WorkflowHelper workflowHelper;

  protected void waitForJobExecutorToProcessAllJobs() {
    waitForJobExecutorToProcessAllJobs(60 * 1000L, 25L);
  }

  private void waitForJobExecutorToProcessAllJobs(long maxMillisToWait, long intervalMillis) {
    TestHelper.waitForJobExecutorToProcessAllJobs(
        (ProcessEngineConfigurationImpl) workflowHelper.getProcessEngineConfiguration(),
        maxMillisToWait,
        intervalMillis);
  }

}
