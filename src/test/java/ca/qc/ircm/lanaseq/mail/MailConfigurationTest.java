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

package ca.qc.ircm.lanaseq.mail;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/**
 * Tests for {@link MailConfiguration}.
 */
@NonTransactionalTestAnnotations
public class MailConfigurationTest {
  @Autowired
  private MailConfiguration mailConfiguration;
  @Autowired
  private JavaMailSenderImpl mailSender;

  @Test
  public void defaultValues() {
    assertEquals(true, mailConfiguration.isEnabled());
    assertEquals("lanaseq@ircm.qc.ca", mailConfiguration.getFrom());
    assertEquals("christian.poitras@ircm.qc.ca", mailConfiguration.getTo());
    assertEquals("lanaseq", mailConfiguration.getSubject());
    assertEquals("localhost", mailSender.getHost());
  }
}
