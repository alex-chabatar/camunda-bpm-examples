package org.camunda.bpm.examples.custom.mappings.engine;

import java.util.List;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.util.ReflectUtil;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomProcessEnginePlugin extends AbstractProcessEnginePlugin {

  public static final List<String> customMappings = List.of(
      "mapping/Comment.xml"
  );

  @Override
  public void postInit(ProcessEngineConfigurationImpl processEngineConfiguration) {
    // -- Custom mappings
    addCustomMappings(processEngineConfiguration.getDbSqlSessionFactory().getSqlSessionFactory().getConfiguration());
  }

  private void addCustomMappings(Configuration configuration) {
    customMappings.forEach(resourceName -> {
      log.info("loading {}", resourceName);
      var inputStream = ReflectUtil.getResourceAsStream(resourceName);
      var mapperParser = new XMLMapperBuilder(inputStream, configuration, resourceName,
          configuration.getSqlFragments());
      mapperParser.parse();
    });
  }

}
