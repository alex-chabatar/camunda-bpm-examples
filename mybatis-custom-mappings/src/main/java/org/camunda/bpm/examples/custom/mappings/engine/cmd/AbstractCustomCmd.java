package org.camunda.bpm.examples.custom.mappings.engine.cmd;

import java.io.Serializable;
import java.util.Date;

import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.oplog.UserOperationLogContext;
import org.camunda.bpm.engine.impl.util.ClockUtil;

abstract class AbstractCustomCmd<T> implements Command<T>, Serializable {

  protected String defaultOrDbSpecific(CommandContext commandContext, String queryId) {

    var processEngineConfiguration = commandContext.getProcessEngineConfiguration();
    var queryIdKey = String.join("_", queryId, processEngineConfiguration.getDatabaseType());

    return processEngineConfiguration.getDbSqlSessionFactory().getSqlSessionFactory().getConfiguration()
        .getMappedStatementNames().stream()
        .filter(queryIdKey::equals)
        .findAny()
        .orElse(queryId);

  }

  protected void logUserOperations(CommandContext commandContext, UserOperationLogContext context) {
    commandContext.getOperationLogManager().logUserOperations(context);
  }

  protected Date currentTime() {
    return ClockUtil.getCurrentTime();
  }

}
