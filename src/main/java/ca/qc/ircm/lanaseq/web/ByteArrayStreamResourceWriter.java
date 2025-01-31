package ca.qc.ircm.lanaseq.web;

import com.vaadin.flow.server.StreamResourceWriter;
import com.vaadin.flow.server.VaadinSession;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serial;
import org.springframework.util.FileCopyUtils;

/**
 * A {@link StreamResourceWriter} that sends a byte array.
 */
public class ByteArrayStreamResourceWriter implements StreamResourceWriter {

  @Serial
  private static final long serialVersionUID = 673747187193922551L;
  private final byte[] content;

  public ByteArrayStreamResourceWriter(byte[] content) {
    this.content = content.clone();
  }

  @Override
  public void accept(OutputStream stream, VaadinSession session) throws IOException {
    FileCopyUtils.copy(content, stream);
  }
}
