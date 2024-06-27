/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lanaseq;

import org.apache.commons.io.FilenameUtils;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.messageresolver.StandardMessageResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

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
    ReloadableResourceBundleMessageSource messageSource =
        new ReloadableResourceBundleMessageSource();
    messageSource.setBasenames("file:" + currentDir + "/messages", "classpath:messages");
    messageSource.setDefaultEncoding("UTF-8");
    return messageSource;
  }

  /**
   * Creates Thymeleaf's template engine.
   *
   * @return Thymeleaf's template engine
   */
  @Bean
  public TemplateEngine emailTemplateEngine() {
    TemplateEngine templateEngine = new TemplateEngine();
    templateEngine.setTemplateResolver(new ClassLoaderTemplateResolver());
    templateEngine.setMessageResolver(new StandardMessageResolver());
    return templateEngine;
  }
}
