package ca.qc.ircm.lanaseq.test.config;

import static ca.qc.ircm.lanaseq.UsedBy.SPRING;

import ca.qc.ircm.lanaseq.UsedBy;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;

/**
 * Ignores scheduled tasks.
 */
@Configuration
@UsedBy(SPRING)
public class UnregisterScheduledProcessor implements BeanFactoryPostProcessor {

  @Override
  public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory)
      throws BeansException {
    for (String beanName : beanFactory.getBeanNamesForType(
        ScheduledAnnotationBeanPostProcessor.class)) {
      ((DefaultListableBeanFactory) beanFactory).removeBeanDefinition(beanName);
    }
  }
}
