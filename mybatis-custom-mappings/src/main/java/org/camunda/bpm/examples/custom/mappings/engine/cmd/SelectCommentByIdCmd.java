package org.camunda.bpm.examples.custom.mappings.engine.cmd;

import java.util.Map;

import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.persistence.entity.CommentEntity;

public class SelectCommentByIdCmd extends AbstractCustomCmd<CommentEntity> {

  private static final String QUERY = "custom.selectCommentById";

  private final String id;

  public SelectCommentByIdCmd(String id) {
    this.id = id;
  }

  @Override
  public CommentEntity execute(CommandContext commandContext) {
    return (CommentEntity) commandContext.getDbEntityManager()
        .selectOne(defaultOrDbSpecific(commandContext, QUERY), Map.of("id", id));
  }

}
