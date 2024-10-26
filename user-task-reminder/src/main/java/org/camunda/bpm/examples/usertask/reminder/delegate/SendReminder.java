package org.camunda.bpm.examples.usertask.reminder.delegate;

import static org.camunda.bpm.examples.usertask.reminder.WorkflowParameters.*;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SendReminder implements JavaDelegate {

  @Override
  public void execute(DelegateExecution execution) {

    var taskId = execution.getVariable(PARAM_TASK_ID);
    var reminderType = execution.getVariable(PARAM_REMINDER_TYPE);
    var reminderValue = execution.getVariable(PARAM_REMINDER_VALUE);

    log.info("Sending reminder with type '{}' and value '{}' for taskId '{}'",
        reminderType, reminderValue, taskId);

  }

}
