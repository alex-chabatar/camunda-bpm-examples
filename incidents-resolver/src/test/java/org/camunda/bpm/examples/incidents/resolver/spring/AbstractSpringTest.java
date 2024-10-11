package org.camunda.bpm.examples.incidents.resolver.spring;

import org.camunda.bpm.examples.incidents.resolver.IncidentsResolverApp;
import org.camunda.bpm.examples.incidents.resolver.spring.configuration.ScenarioConfiguration;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.tngtech.jgiven.Stage;
import com.tngtech.jgiven.integration.spring.JGivenSpringConfiguration;
import com.tngtech.jgiven.integration.spring.junit5.SpringScenarioTest;

@SpringBootTest(classes = {
    IncidentsResolverApp.class,
    ScenarioConfiguration.class,
    JGivenSpringConfiguration.class
})
public abstract class AbstractSpringTest<GIVEN extends Stage, WHEN extends Stage, THEN extends Stage>
    extends SpringScenarioTest<GIVEN, WHEN, THEN> {

  protected void setProxyField(Object proxy, String field, Object value) throws Exception {
    ReflectionTestUtils.setField(unwrapProxy(proxy), field, value);
  }

  // http://forum.springsource.org/showthread.php?60216-Need-to-unwrap-a-proxy-to-get-the-object-being-proxied
  protected Object unwrapProxy(Object bean) throws Exception {
    if (AopUtils.isAopProxy(bean) && bean instanceof Advised advised) {
      bean = advised.getTargetSource().getTarget();
    }
    return bean;
  }

}
