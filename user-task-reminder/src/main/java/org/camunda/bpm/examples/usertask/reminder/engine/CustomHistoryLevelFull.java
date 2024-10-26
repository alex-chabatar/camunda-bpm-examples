package org.camunda.bpm.examples.usertask.reminder.engine;

import static org.camunda.bpm.engine.impl.history.event.HistoryEventTypes.*;
import static org.camunda.bpm.examples.usertask.reminder.WorkflowParameters.MESSAGE_CANCEL_TASK_REMINDER;
import static org.camunda.bpm.examples.usertask.reminder.WorkflowParameters.PARAM_TASK_ID;

import java.util.*;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.history.HistoryLevelFull;
import org.camunda.bpm.engine.impl.history.event.HistoryEventType;
import org.camunda.bpm.engine.impl.persistence.entity.TaskEntity;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Setter
public class CustomHistoryLevelFull extends HistoryLevelFull {

  public static final String NAME = "custom-full";

  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public boolean isHistoryEventProduced(HistoryEventType eventType, Object entity) {
    if (TASK_INSTANCE_COMPLETE.equals(eventType) && entity instanceof TaskEntity task) {
      taskEntityCompleted(task);
    }
    return super.isHistoryEventProduced(eventType, entity);
  }

  private void taskEntityCompleted(TaskEntity task) {
    runtimeService().createMessageCorrelation(MESSAGE_CANCEL_TASK_REMINDER)
        .processInstanceVariablesEqual(Map.of(PARAM_TASK_ID, task.getId()))
        .correlateAll();
  }

  private ProcessEngine processEngine() {
    return Context.getCommandContext()
        .getProcessEngineConfiguration()
        .getProcessEngine();
  }

  private RuntimeService runtimeService() {
    return processEngine().getRuntimeService();
  }

}