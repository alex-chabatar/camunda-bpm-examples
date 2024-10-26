package org.camunda.bpm.examples.usertask.reminder.utils;

import java.util.Date;
import java.util.Map;

import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Getter
public class WorkflowHelper {

  private final RuntimeService runtimeService;

  // -- Messages

  public synchronized ProcessInstance correlateMessage(String message, String businessKey,
      Map<String, Object> payload) {
    var builder = messageCorrelationBuilder(message, payload);
    if (!ObjectUtils.isEmpty(businessKey)) { // ensureNotNull("businessKey", businessKey) in Camunda
      builder.processInstanceBusinessKey(businessKey);
    }
    var startTime = getWorkflowCurrentTime().getTime();
    var result = builder.correlateStartMessage();
    log.info("Correlated start message '{}' to process {} with businessKey {} and payload {}, time used {} ms",
        message, result.getId(), businessKey, payload, getWorkflowCurrentTime().getTime() - startTime);
    return result;
  }

  private MessageCorrelationBuilder messageCorrelationBuilder(String message, Map<String, Object> payload) {
    return runtimeService
        .createMessageCorrelation(message)
        .setVariables(payload);
  }

  // -- Clock

  public Date getWorkflowCurrentTime() {
    return ClockUtil.getCurrentTime();
  }

}
