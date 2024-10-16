package org.camunda.bpm.examples.custom.mappings.engine.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.Date;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cmd.AddCommentCmd;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.CommentEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.task.Comment;
import org.camunda.bpm.engine.task.Event;

public class AddCommentExtendedCmd extends AddCommentCmd {

  private Date time;

  public AddCommentExtendedCmd(String taskId, String processInstanceId, String message) {
    super(taskId, processInstanceId, message);
  }

  public AddCommentExtendedCmd(String taskId, String processInstanceId, String message, Date time) {
    super(taskId, processInstanceId, message);
    this.time = time;
  }

  @Override
  public Comment execute(CommandContext commandContext) {

    if (processInstanceId == null && taskId == null) {
      throw new ProcessEngineException("Process instance id and task id is null");
    }

    ensureNotNull("Message", message);

    var userId = commandContext.getAuthenticatedUserId();
    var comment = new CommentEntity();
    comment.setUserId(userId);
    comment.setType(CommentEntity.TYPE_COMMENT);
    comment.setTime(
        time != null ? time : ClockUtil.getCurrentTime()); // statt comment.setTime(ClockUtil.getCurrentTime());
    comment.setTaskId(taskId);
    comment.setProcessInstanceId(processInstanceId);
    comment.setAction(Event.ACTION_ADD_COMMENT);

    var execution = getExecution(commandContext);
    if (execution != null) {
      comment.setRootProcessInstanceId(execution.getRootProcessInstanceId());
    }

    if (isHistoryRemovalTimeStrategyStart()) {
      provideRemovalTime(comment);
    }

    var eventMessage = message.replaceAll("\\s+", " ");
    if (eventMessage.length() > 163) {
      eventMessage = eventMessage.substring(0, 160) + "...";
    }
    comment.setMessage(eventMessage);

    comment.setFullMessage(message);

    commandContext
        .getCommentManager()
        .insert(comment);

    var task = getTask(commandContext);
    if (task != null) {
      task.triggerUpdateEvent();
    }

    return comment;

  }

}
