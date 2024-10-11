package org.camunda.bpm.examples.incidents.resolver.process;

import static org.camunda.bpm.engine.runtime.Incident.EXTERNAL_TASK_HANDLER_TYPE;
import static org.camunda.bpm.engine.runtime.Incident.FAILED_JOB_HANDLER_TYPE;
import static org.camunda.bpm.examples.incidents.resolver.ProcessConstants.INCIDENT_RESOLVER;
import static org.camunda.bpm.examples.incidents.resolver.TestDataProvider.SERVICE_TASK_ERROR;
import static org.camunda.bpm.examples.incidents.resolver.TestDataProvider.businessKey;
import static org.mockito.Mockito.times;

import org.camunda.bpm.examples.incidents.resolver.AbstractIncidentsTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class IncidentResolvingTest extends AbstractIncidentsTest {

  @Test
  void ensureFailedJobsResolved() {

    String testProcess = "Test Process with failedJob";

    // service not available
    Mockito.when(testService.doSomething())
        .thenThrow(new IllegalStateException(SERVICE_TASK_ERROR));

    given()
        .a_process_engine()
        .a_business_key(businessKey());

    when()
        .create_a_process(TEST_PROCESS)
        .referenced_as(testProcess);

    then()
        .active_processes(1, TEST_PROCESS)
        .active_external_tasks(1)
        .active_external_tasks(1, TEST_TOPIC);

    then()
        .with_incidents(1).comment("in runtime")
        .with_incidents(1, FAILED_JOB_HANDLER_TYPE)
        .active_incident(FAILED_JOB_HANDLER_TYPE)
        .with_incident_message(SERVICE_TASK_ERROR);

    Mockito.verify(testService, times(3)).doSomething();

    // service is back
    Mockito.reset(testService);

    when()
        .create_a_process(INCIDENT_RESOLVER);

    then()
        .completed_processes(1, INCIDENT_RESOLVER)
        .active_processes(1, TEST_PROCESS)
        .active_process_of_type_$_referenced_as_$(TEST_PROCESS, testProcess)
        .process_runtime(testProcess)
        .with_no_incidents(testProcess)
        .process_history(testProcess)
        .with_history_incidents(1, FAILED_JOB_HANDLER_TYPE, testProcess);

    Mockito.verify(testService, times(1)).doSomething();

  }

  @Test
  void ensureFailedExternalTaskResolved() {

    String testProcess = "Test Process with failedJob";

    given()
        .a_process_engine()
        .a_business_key(businessKey());

    when()
        .create_a_process(TEST_PROCESS)
        .referenced_as(testProcess);

    then()
        .active_processes(1, TEST_PROCESS)
        .active_external_tasks(1)
        .active_external_tasks(1, TEST_TOPIC)
        .with_no_incidents();

    when()
        .complete_external_tasks_with_failure(TEST_TOPIC, TEST_TOPIC_ERROR);

    then()
        .with_incidents(1).comment("in runtime")
        .with_incidents(1, EXTERNAL_TASK_HANDLER_TYPE)
        .active_incident(EXTERNAL_TASK_HANDLER_TYPE)
        .with_incident_message(TEST_TOPIC_ERROR)
        .active_external_task(TEST_TOPIC).comment("external task")
        .with_external_task_retries(0)
        .with_external_task_error_message(TEST_TOPIC_ERROR);

    when()
        .create_a_process(INCIDENT_RESOLVER);

    then()
        .completed_processes(1, INCIDENT_RESOLVER)
        .active_processes(1, TEST_PROCESS)
        .active_process_of_type_$_referenced_as_$(TEST_PROCESS, testProcess)
        .process_runtime(testProcess)
        .with_no_incidents(testProcess)
        .process_history(testProcess)
        .with_history_incidents(1, EXTERNAL_TASK_HANDLER_TYPE, testProcess)
        .active_external_task(TEST_TOPIC)
        .comment("task is still active, needs to be processed by external client")
        .with_external_task_retries(1).comment("externalTaskService.setRetries +1")
        .with_external_task_error_message(TEST_TOPIC_ERROR);

  }

}
