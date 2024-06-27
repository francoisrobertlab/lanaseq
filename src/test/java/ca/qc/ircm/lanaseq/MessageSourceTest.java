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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.security.test.context.support.WithUserDetails;

@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class MessageSourceTest {
  @Autowired
  private MessageSource messageSource;

  @Test
  public void configuration() {
    assertTrue(messageSource instanceof ReloadableResourceBundleMessageSource);
    ReloadableResourceBundleMessageSource reloadableMessageSource =
        (ReloadableResourceBundleMessageSource) messageSource;
    List<String> basenames =
        reloadableMessageSource.getBasenameSet().stream().collect(Collectors.toList());
    assertEquals(2, basenames.size());
    String currentDir = FilenameUtils.separatorsToUnix(System.getProperty("user.dir"));
    assertEquals("file:" + currentDir + "/messages", basenames.get(0));
    assertEquals("classpath:messages", basenames.get(1));
  }
}
