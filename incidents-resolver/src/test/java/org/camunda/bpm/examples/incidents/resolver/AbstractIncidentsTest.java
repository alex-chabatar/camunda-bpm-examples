package org.camunda.bpm.examples.incidents.resolver;

import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.MockitoAnnotations.openMocks;

import org.camunda.bpm.examples.incidents.resolver.delegate.ServiceTaskWithIncident;
import org.camunda.bpm.examples.incidents.resolver.process.scenario.IncidentsGiven;
import org.camunda.bpm.examples.incidents.resolver.process.scenario.IncidentsThen;
import org.camunda.bpm.examples.incidents.resolver.process.scenario.IncidentsWhen;
import org.camunda.bpm.examples.incidents.resolver.service.TestService;
import org.camunda.bpm.examples.incidents.resolver.spring.AbstractSpringTest;
import org.camunda.bpm.examples.incidents.resolver.spring.configuration.ScenarioConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import com.tngtech.jgiven.integration.spring.JGivenSpringConfiguration;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = {
    IncidentsResolverApp.class,
    ScenarioConfiguration.class,
    JGivenSpringConfiguration.class
})
public abstract class AbstractIncidentsTest extends AbstractSpringTest<IncidentsGiven, IncidentsWhen, IncidentsThen> {

  protected static final String TEST_PROCESS = "TestProcess";

  protected static final String TEST_TOPIC = "TestTopic";
  protected static final String TEST_TOPIC_ERROR = "TestTopic Error";

  @Autowired
  private ServiceTaskWithIncident serviceTaskWithIncident;

  @Mock
  protected TestService testService;

  @BeforeEach
  public void setUp() {
    openMocks(this);
    try {
      setProxyField(serviceTaskWithIncident, "testService", testService);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @AfterEach
  public void validate() {
    validateMockitoUsage();
  }

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
