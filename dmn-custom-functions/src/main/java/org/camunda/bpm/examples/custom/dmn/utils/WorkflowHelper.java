package org.camunda.bpm.examples.custom.dmn.utils;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.dmn.engine.DmnDecisionTableResult;
import org.camunda.bpm.engine.*;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Getter
public class WorkflowHelper {

  private final ProcessEngineConfiguration processEngineConfiguration;
  private final RepositoryService repositoryService;
  private final HistoryService historyService;
  private final DecisionService decisionService;

  // -- Decision Definitions

  public List<DecisionDefinition> getDecisionDefinitions() {
    return repositoryService
        .createDecisionDefinitionQuery()
        .latestVersion()
        .list();
  }

  public DecisionDefinition getDecisionDefinition(String decisionDefinitionKey) {
    return repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(decisionDefinitionKey)
        .latestVersion()
        .singleResult();
  }

  // -- DMN

  public DmnDecisionTableResult evaluateDecisionTableResult(String decisionDefinitionKey,
      Map<String, Object> variables) {
    return decisionService.evaluateDecisionTableByKey(decisionDefinitionKey, variables);
  }

  public List<HistoricDecisionInstance> getHistoricDecisionInstances() {
    return historyService
        .createHistoricDecisionInstanceQuery()
        .list();
  }

  public List<HistoricDecisionInstance> getHistoryDecisions(String decisionDefinitionKey) {
    return historicDecisionInstanceQuery(decisionDefinitionKey).list();
  }

  public HistoricDecisionInstanceQuery historicDecisionInstanceQuery(String decisionDefinitionKey) {
    return historyService.createHistoricDecisionInstanceQuery()
        .decisionDefinitionKey(decisionDefinitionKey)
        .includeOutputs();
  }

  public void deleteHistoricDecisionProcess(String processId) {
    historyService.deleteHistoricDecisionInstanceByInstanceId(processId);
  }

  public void deleteHistoricDecisionProcesses() {
    getHistoricDecisionInstances()
        .forEach(process -> deleteHistoricDecisionProcess(process.getId()));
  }

  // -- Clock

  public Date getWorkflowCurrentTime() {
    return ClockUtil.getCurrentTime();
  }

  public void setWorkflowCurrentTime(Date currentTime) {
    var oldTime = getWorkflowCurrentTime();
    log.debug("Current workflow time {} -> {}", oldTime, currentTime);
    ClockUtil.setCurrentTime(currentTime);
  }

  public void resetWorkflowCurrentTime() {
    ClockUtil.reset();
  }

}
