package org.camunda.bpm.examples.process.migration.engine;

import static java.util.Collections.emptyList;

import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.deploy.Deployer;
import org.camunda.bpm.engine.impl.persistence.entity.DeploymentEntity;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomDeployer implements Deployer {

  private static final Logger LOG = LoggerFactory.getLogger(CustomDeployer.class);

  @Override
  public void deploy(DeploymentEntity deploymentEntity) {

    if (deploymentEntity == null || !deploymentEntity.isNew()) {
      return; // null or already processed...
    }

    Optional.ofNullable(deploymentEntity.getDeployedProcessDefinitions()).orElse(emptyList())
        .forEach(processDefinition -> {
          String targetProcessDefinitionId = processDefinition.getId();
          ProcessDefinition processDefinitionBefore = processDefinitionBefore(processDefinition);
          if (processDefinitionBefore != null) { // something to migrate...
            String sourceProcessDefinitionId = processDefinitionBefore.getId();
            long instancesToMigrate = runtimeService().createProcessInstanceQuery()
                .processDefinitionId(sourceProcessDefinitionId)
                .count();
            if (instancesToMigrate > 0) {
              withAuthenticatedUser(null, () -> {
                Batch batch = runtimeService()
                    .newMigration(
                        runtimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
                            .mapEqualActivities()
                            // .updateEventTriggers()
                            .build())
                    .processInstanceQuery(
                        runtimeService().createProcessInstanceQuery()
                            .processDefinitionId(sourceProcessDefinitionId))
                    .executeAsync();
                LOG.info("Scheduled a batch '{}' to migrate '{}' process instances of '{}' to '{}'",
                    batch.getId(), instancesToMigrate, sourceProcessDefinitionId, targetProcessDefinitionId);
                return batch;
              });
            }
          }
        });

  }

  private ProcessDefinition processDefinitionBefore(ProcessDefinition processDefinition) {
    return repositoryService().createProcessDefinitionQuery()
        .processDefinitionKey(processDefinition.getKey())
        .orderByProcessDefinitionVersion().desc()
        .list().stream()
        .filter(item -> item.getVersion() != processDefinition.getVersion())
        .findFirst().orElse(null);
  }

  private void withAuthenticatedUser(String userId, Supplier<?> supplier) {
    if (StringUtils.isNotEmpty(userId)) {
      identityService().setAuthenticatedUserId(userId);
      supplier.get();
      identityService().setAuthenticatedUserId(null);
    } else {
      supplier.get();
    }
  }

  private ProcessEngine processEngine() {
    return Context.getCommandContext()
        .getProcessEngineConfiguration()
        .getProcessEngine();
  }

  private RuntimeService runtimeService() {
    return processEngine().getRuntimeService();
  }

  private RepositoryService repositoryService() {
    return processEngine().getRepositoryService();
  }

  private IdentityService identityService() {
    return processEngine().getIdentityService();
  }

}
