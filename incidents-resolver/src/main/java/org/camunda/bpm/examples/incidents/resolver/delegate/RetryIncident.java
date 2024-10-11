package org.camunda.bpm.examples.incidents.resolver.delegate;

import static org.camunda.bpm.examples.incidents.resolver.ProcessConstants.*;

import java.util.Optional;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.examples.incidents.resolver.utils.WorkflowHelper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RetryIncident implements JavaDelegate {

  private final WorkflowHelper workflowHelper;

  @Override
  public void execute(DelegateExecution execution) {

    var incidentId = (String) execution.getVariable(PARAM_INCIDENT_ID);

    log.info("Retry incident {}", incidentId);

    var incident = workflowHelper.getIncident(incidentId);

    if (incident != null) {

      // 1. retry
      workflowHelper.retryIncident(incidentId);

      // 2. protocol
      execution.setVariableLocal(PARAM_INCIDENT_TYPE, incident.getIncidentType());
      execution.setVariableLocal(PARAM_INCIDENT_TIMESTAMP, incident.getIncidentTimestamp());
      execution.setVariableLocal(PARAM_INCIDENT_MESSAGE, incident.getIncidentMessage());
      execution.setVariableLocal(PARAM_BUSINESS_KEY,
          Optional.ofNullable(workflowHelper.getProcessInstance(incident.getProcessInstanceId()))
              .map(ProcessInstance::getBusinessKey)
              .orElse(null));
      execution.setVariableLocal(PARAM_PROCESS_ID, incident.getProcessInstanceId());
      execution.setVariableLocal(PARAM_PROCESS_DEFINITION_KEY,
          workflowHelper.getProcessDefinitionKeyById(incident.getProcessDefinitionId()));
      execution.setVariableLocal(PARAM_ACTIVITY_ID, incident.getActivityId());
      execution.setVariableLocal(PARAM_FAILED_ACTIVITY_ID, incident.getFailedActivityId());

    }

  }

}
