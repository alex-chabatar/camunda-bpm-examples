package org.camunda.bpm.examples.incidents.resolver.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.examples.incidents.resolver.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ServiceTaskWithIncident implements JavaDelegate {

  @Autowired
  private TestService testService;

  @Override
  public void execute(DelegateExecution execution) {
    testService.doSomething();
  }

}
