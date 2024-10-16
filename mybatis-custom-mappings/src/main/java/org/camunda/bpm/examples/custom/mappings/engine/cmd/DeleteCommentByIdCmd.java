package org.camunda.bpm.examples.custom.mappings.engine.cmd;

import static org.camunda.bpm.engine.history.UserOperationLogEntry.*;
import static org.camunda.bpm.engine.impl.persistence.entity.PropertyChange.EMPTY_CHANGE;
import static org.camunda.bpm.examples.custom.mappings.engine.cmd.EntityTypesExtended.ENTITY_TYPE_COMMENT;

import java.util.Optional;

import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContext;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContextEntryBuilder;
import org.camunda.bpm.engine.impl.persistence.entity.CommentEntity;

public class DeleteCommentByIdCmd extends AbstractCustomCmd<Void> {

  private static final String QUERY = "custom.deleteCommentById";

  private final String id;
  private final String userId;

  public DeleteCommentByIdCmd(String id, String userId) {
    this.id = id;
    this.userId = userId;
  }

  @Override
  public Void execute(CommandContext commandContext) {

    var entity = new SelectCommentByIdCmd(id).execute(commandContext);

    commandContext.getDbEntityManager()
        .delete(CommentEntity.class, defaultOrDbSpecific(commandContext, QUERY), entity);

    var processInstance = commandContext.getHistoricProcessInstanceManager()
        .findHistoricProcessInstance(entity.getProcessInstanceId());

    var operationId = commandContext.getOperationId(); // default by Camunda

    var logContext = new UserOperationLogContext();
    logContext.setUserId(userId);
    logContext.setOperationId(operationId);
    logContext.addEntry(UserOperationLogContextEntryBuilder.entry(OPERATION_TYPE_DELETE, ENTITY_TYPE_COMMENT)
        .processInstanceId(entity.getProcessInstanceId())
        .taskId(entity.getTaskId())
        .category(entity.getTaskId() != null ? CATEGORY_TASK_WORKER : CATEGORY_OPERATOR)
        .processDefinitionKey(Optional.ofNullable(processInstance)
            .map(HistoricProcessInstance::getProcessDefinitionKey)
            .orElse(null))
        .processDefinitionId(Optional.ofNullable(processInstance)
            .map(HistoricProcessInstance::getProcessDefinitionId)
            .orElse(null))
        .propertyChanges(EMPTY_CHANGE)
        .create());

    logUserOperations(commandContext, logContext);

    return null;

  }

}
