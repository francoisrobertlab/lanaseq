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

package ca.qc.ircm.lanaseq.web;

import static org.junit.Assert.assertArrayEquals;

import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

@NonTransactionalTestAnnotations
public class ByteArrayStreamResourceWriterTest {
  private ByteArrayStreamResourceWriter writer;
  private byte[] content = new byte[5120];
  @Mock
  private VaadinSession session;
  private Random random = new Random();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() throws Throwable {
    random.nextBytes(content);
    writer = new ByteArrayStreamResourceWriter(content);
  }

  @Test
  public void accept() throws Throwable {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    writer.accept(output, session);
    assertArrayEquals(content, output.toByteArray());
  }
}
