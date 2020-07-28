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

import ca.qc.ircm.lanaseq.mail.MailConfiguration;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
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
  @Autowired
  private MailConfiguration mailConfiguration;

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

  /**
   * Mail sender.
   *
   * @return mail sender
   */
  @Bean
  public JavaMailSender mailSender() {
    JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(mailConfiguration.getHost());
    return mailSender;
  }

  /**
   * Template message.
   *
   * @return template message
   * @throws MessagingException
   *           could not create template message
   */
  @Bean
  public MimeMessage templateMessage() throws MessagingException {
    MimeMessage message = mailSender().createMimeMessage();
    MimeMessageHelper helper = new MimeMessageHelper(message);
    helper.setFrom(mailConfiguration.getFrom());
    helper.setSubject(mailConfiguration.getSubject());
    helper.setText("");
    return message;
  }

  /**
   * Template message.
   *
   * @return template message
   * @throws MessagingException
   *           could not create template message
   */
  @Bean
  @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
  public MimeMessageHelper emailHelper() throws MessagingException {
    MimeMessage message = new MimeMessage(templateMessage());
    MimeMessageHelper helper = new MimeMessageHelper(message);
    return helper;
  }
}
