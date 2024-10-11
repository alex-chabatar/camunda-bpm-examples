package org.camunda.bpm.examples.incidents.resolver.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService {
  @Override
  public String doSomething() {
    // do something and send back the result
    return UUID.randomUUID().toString();
  }
}
