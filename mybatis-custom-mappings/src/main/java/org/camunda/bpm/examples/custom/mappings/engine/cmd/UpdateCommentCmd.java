package org.camunda.bpm.examples.custom.mappings.engine.cmd;

import static org.camunda.bpm.engine.history.UserOperationLogEntry.*;
import static org.camunda.bpm.examples.custom.mappings.engine.cmd.EntityTypesExtended.ENTITY_TYPE_COMMENT;

import java.util.Date;
import java.util.Optional;

import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContext;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContextEntryBuilder;
import org.camunda.bpm.engine.impl.persistence.entity.CommentEntity;
import org.camunda.bpm.engine.impl.persistence.entity.PropertyChange;

public class UpdateCommentCmd extends AbstractCustomCmd<Void> {

  private static final String QUERY = "custom.updateComment";

  private final String id;
  private final String userId;
  private final String message;
  private Date time;

  public UpdateCommentCmd(String id, String userId, String message) {
    this.id = id;
    this.userId = userId;
    this.message = message;
  }

  public UpdateCommentCmd(String id, String userId, String message, Date time) {
    this(id, userId, message);
    this.time = time;
  }

  @Override
  public Void execute(CommandContext commandContext) {

    var entity = new SelectCommentByIdCmd(id).execute(commandContext);

    var newEntity = new CommentEntity();
    newEntity.setId(id);
    newEntity.setUserId(userId);
    newEntity.setMessage(message);
    newEntity.setFullMessage(message);
    newEntity.setTime(time != null ? time : currentTime());

    commandContext.getDbEntityManager()
        .update(CommentEntity.class, defaultOrDbSpecific(commandContext, QUERY), newEntity);

    var processInstance = commandContext.getHistoricProcessInstanceManager()
        .findHistoricProcessInstance(entity.getProcessInstanceId());

    var operationId = commandContext.getOperationId(); // default by Camunda

    var logContext = new UserOperationLogContext();
    logContext.setUserId(userId);
    logContext.setOperationId(operationId);
    logContext.addEntry(UserOperationLogContextEntryBuilder.entry(OPERATION_TYPE_UPDATE, ENTITY_TYPE_COMMENT)
        .processInstanceId(entity.getProcessInstanceId())
        .taskId(entity.getTaskId())
        .category(entity.getTaskId() != null ? CATEGORY_TASK_WORKER : CATEGORY_OPERATOR)
        .processDefinitionKey(Optional.ofNullable(processInstance)
            .map(HistoricProcessInstance::getProcessDefinitionKey)
            .orElse(null))
        .processDefinitionId(Optional.ofNullable(processInstance)
            .map(HistoricProcessInstance::getProcessDefinitionId)
            .orElse(null))
        .propertyChanges(new PropertyChange("message", entity.getMessage(), message))
        .create());

    logUserOperations(commandContext, logContext);

    return null;

  }

}
