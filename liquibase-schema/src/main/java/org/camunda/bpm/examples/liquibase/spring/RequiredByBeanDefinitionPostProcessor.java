package org.camunda.bpm.examples.liquibase.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class RequiredByBeanDefinitionPostProcessor implements BeanDefinitionRegistryPostProcessor {

  @Override
  public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
    for (var beanName : registry.getBeanDefinitionNames()) {
      var beanDefinition = registry.getBeanDefinition(beanName);
      if (beanDefinition.getBeanClassName() == null) {
        continue;
      }
      try {
        var beanClass = Class.forName(beanDefinition.getBeanClassName());
        if (beanClass.isAnnotationPresent(RequiredBy.class)) {
          var dependantBeanNames = beanClass.getAnnotation(RequiredBy.class).value();
          for (var dependantBeanName : dependantBeanNames) {
            var dependantBeanDefinition = registry.getBeanDefinition(dependantBeanName);
            dependantBeanDefinition.setDependsOn(beanName);
          }
        }
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    // empty
  }

}