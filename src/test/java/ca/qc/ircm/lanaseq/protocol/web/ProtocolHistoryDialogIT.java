package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.RECOVERED;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolFileRepository;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link ProtocolHistoryDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("francois.robert@ircm.qc.ca")
public class ProtocolHistoryDialogIT extends SpringBrowserlessTest {

  private static final String MESSAGE_PREFIX = messagePrefix(ProtocolHistoryDialog.class);
  @Autowired
  private ProtocolFileRepository fileRepository;
  @Autowired
  private MessageSource messageSource;

  @Test
  public void recover() throws Throwable {
    ProtocolsView view = navigate(ProtocolsView.class);
    test(view.protocols).select(2);
    test(view.history).click();
    ProtocolHistoryDialog dialog = $(ProtocolHistoryDialog.class).first();

    test(dialog.files).invokeLitRendererFunction(0, dialog.recover.getKey(), "recoverFile");

    Notification notification = $(Notification.class).first();
    Assertions.assertEquals(messageSource.getMessage(MESSAGE_PREFIX + RECOVERED,
            new Object[]{"Histone FLAG Protocol.docx"}, UI.getCurrent().getLocale()),
        test(notification).getText());
    ProtocolFile file = fileRepository.findById(3L).orElseThrow();
    Assertions.assertEquals("Histone FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(Paths.get(
        Objects.requireNonNull(getClass().getResource("/protocol/Histone_FLAG_Protocol.docx"))
            .toURI())), file.getContent());
    assertFalse(file.isDeleted());
    Assertions.assertEquals(LocalDateTime.of(2018, 10, 20, 9, 58, 12), file.getCreationDate());
  }
}
