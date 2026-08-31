package ca.qc.ircm.lanaseq;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuration for Spring.
 */
@Configuration
@EnableTransactionManagement
@EnableScheduling
public class SpringConfiguration {

  /**
   * Creates {@link MessageSource} instance.
   *
   * @return {@link MessageSource} instance.
   */
  @Bean
  public ReloadableResourceBundleMessageSource messageSource() {
    String currentDir = FilenameUtils.separatorsToUnix(System.getProperty("user.dir"));
    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setBasenames("file:" + currentDir + "/messages", "classpath:messages");
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setAlwaysUseMessageFormat(true);
    return messageSource;
  }
}
