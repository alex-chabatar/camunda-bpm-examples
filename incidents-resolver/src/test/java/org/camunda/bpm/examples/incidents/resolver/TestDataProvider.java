package org.camunda.bpm.examples.incidents.resolver;

import java.util.UUID;

public class TestDataProvider {

  public static final String SERVICE_TASK_ERROR = "Something goes wrong";

  public static String businessKey() {
    return uuid();
  }

  public static String uuid() {
    return UUID.randomUUID().toString();
  }

}
