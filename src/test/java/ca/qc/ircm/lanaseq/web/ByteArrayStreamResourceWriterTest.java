package ca.qc.ircm.lanaseq.web;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import com.vaadin.flow.server.VaadinSession;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * Tests for {@link ByteArrayStreamResourceWriter}.
 */
@NonTransactionalTestAnnotations
public class ByteArrayStreamResourceWriterTest {

  private ByteArrayStreamResourceWriter writer;
  private final byte[] content = new byte[5120];
  @Mock
  private VaadinSession session;
  private final Random random = new Random();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
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
