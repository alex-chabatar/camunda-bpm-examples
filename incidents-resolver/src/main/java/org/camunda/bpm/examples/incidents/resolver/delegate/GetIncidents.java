package org.camunda.bpm.examples.incidents.resolver.delegate;

import static org.camunda.bpm.examples.incidents.resolver.ProcessConstants.PARAM_INCIDENT_IDS;

import java.util.List;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.examples.incidents.resolver.utils.WorkflowHelper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * Loads the list of incidents
 */
@Service
@RequiredArgsConstructor
public class GetIncidents implements JavaDelegate {

  private final WorkflowHelper workflowHelper;

  @Override
  public void execute(DelegateExecution execution) {

    List<String> incidentIds = workflowHelper.getIncidents()
        .stream()
        .filter(incident -> incident.getProcessInstanceId() != null)
        .map(Incident::getId)
        .toList();

    execution.setVariable(PARAM_INCIDENT_IDS, incidentIds);

  }

}
