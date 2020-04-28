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

import com.vaadin.flow.server.StreamResourceWriter;
import com.vaadin.flow.server.VaadinSession;
import java.io.IOException;
import java.io.OutputStream;
import org.springframework.util.FileCopyUtils;

/**
 * A {@link StreamResourceWriter} that sends a byte array.
 */
public class ByteArrayStreamResourceWriter implements StreamResourceWriter {
  private static final long serialVersionUID = 673747187193922551L;
  private final byte[] content;

  public ByteArrayStreamResourceWriter(byte[] content) {
    this.content = content;
  }

  @Override
  public void accept(OutputStream stream, VaadinSession session) throws IOException {
    FileCopyUtils.copy(content, stream);
  }
}
