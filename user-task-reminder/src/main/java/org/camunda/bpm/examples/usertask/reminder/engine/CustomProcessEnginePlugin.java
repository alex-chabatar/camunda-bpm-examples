package org.camunda.bpm.examples.usertask.reminder.engine;

import java.util.ArrayList;

import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomProcessEnginePlugin extends AbstractProcessEnginePlugin {

  @Override
  public void preInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

    // -- History

    var customHistoryLevels = processEngineConfiguration.getCustomHistoryLevels();
    if (customHistoryLevels == null) {
      customHistoryLevels = new ArrayList<>();
      processEngineConfiguration.setCustomHistoryLevels(customHistoryLevels);
    }
    var customHistoryLevel = new CustomHistoryLevelFull();
    customHistoryLevels.add(customHistoryLevel);

    processEngineConfiguration.setHistory(CustomHistoryLevelFull.NAME);

  }

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {

    // -- History

    processEngineConfiguration.getHistoryLevels()
        .removeIf(historyLevel -> HistoryLevel.HISTORY_LEVEL_FULL.getName().equals(historyLevel.getName()));

  }

}
