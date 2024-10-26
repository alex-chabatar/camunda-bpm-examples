package org.camunda.bpm.examples.usertask.reminder.listener;

import static org.camunda.bpm.examples.usertask.reminder.WorkflowParameters.*;
import static org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE;

import java.util.Map;
import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.examples.usertask.reminder.utils.WorkflowHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import lombok.Setter;

@Setter
@Service
@Scope(value = SCOPE_PROTOTYPE)
public class UserTaskReminderTaskListener implements TaskListener {

  @Autowired
  private WorkflowHelper workflowHelper;

  private Expression reminderType;
  private Expression reminderValue;

  @Override
  public void notify(DelegateTask delegateTask) {

    var taskId = delegateTask.getId();

    var reminderTypeValue = Optional.ofNullable(reminderType)
        .map(expr -> (String) expr.getValue(delegateTask))
        .orElse(null);

    var reminderValueValue = Optional.ofNullable(reminderValue)
        .map(expr -> expr.getValue(delegateTask))
        .orElse(null);

    if (reminderTypeValue != null && reminderValueValue != null) {
      workflowHelper.correlateMessage(
          MESSAGE_NEW_TASK_REMINDER, taskId, Map.of(
              PARAM_TASK_ID, taskId,
              PARAM_REMINDER_TYPE, reminderTypeValue,
              PARAM_REMINDER_VALUE, reminderValueValue
          ));
    }

  }

}
