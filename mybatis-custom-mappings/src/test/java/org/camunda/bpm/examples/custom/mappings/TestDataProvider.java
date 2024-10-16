package org.camunda.bpm.examples.custom.mappings;

import java.util.UUID;

public class TestDataProvider {

  public static String businessKey() {
    return UUID.randomUUID().toString();
  }

  public static String userId() {
    return "john.doe";
  }

}
