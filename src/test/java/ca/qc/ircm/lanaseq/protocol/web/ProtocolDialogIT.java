package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.DELETED;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.SAVED;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolFileRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.notification.Notification;
import jakarta.persistence.EntityManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

/**
 * Integration tests for {@link ProtocolDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ProtocolDialogIT extends SpringBrowserlessTest {

  private static final String MESSAGE_PREFIX = messagePrefix(ProtocolDialog.class);
  @MockitoSpyBean
  private ProtocolService service;
  @Autowired
  private ProtocolRepository repository;
  @Autowired
  private ProtocolFileRepository fileRepository;
  @Autowired
  private MessageSource messageSource;
  @Autowired
  private EntityManager entityManager;
  private final String name = "test protocol";
  private final String note = "test note\nsecond line";
  private Path file1;
  private Path file2;

  @BeforeEach
  public void beforeTest() throws Throwable {
    file1 = Paths.get(
        Objects.requireNonNull(getClass().getResource("/protocol/FLAG_Protocol.docx")).toURI());
    file2 = Paths.get(
        Objects.requireNonNull(getClass().getResource("/protocol/Histone_FLAG_Protocol.docx"))
            .toURI());
  }

  private void setFields(ProtocolDialog dialog) {
    test(dialog.name).setValue(name);
    test(dialog.note).setValue(note);
    test(dialog.upload).upload(file1.toFile());
    test(dialog.upload).upload(file2.toFile());
  }

  private void detachOnServiceGet() {
    when(service.get(anyLong())).then(a -> {
      @SuppressWarnings("unchecked") Optional<Protocol> optionalProtocol = (Optional<Protocol>) a.callRealMethod();
      optionalProtocol.ifPresent(d -> entityManager.detach(d));
      return optionalProtocol;
    });
  }

  @Test
  public void save_New() throws Throwable {
    ProtocolsView view = navigate(ProtocolsView.class);
    test(view.add).click();
    ProtocolDialog dialog = $(ProtocolDialog.class).first();
    setFields(dialog);

    test(dialog.save).click();

    Notification notification = $(Notification.class).first();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{name},
        UI.getCurrent().getLocale()), test(notification).getText());
    Protocol protocol = repository.findByName(name).orElseThrow();
    assertNotEquals(0, protocol.getId());
    assertEquals(name, protocol.getName());
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(protocol.getCreationDate()));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(protocol.getCreationDate()));
    assertEquals((Long) 3L, protocol.getOwner().getId());
    List<ProtocolFile> files = fileRepository.findByProtocol(protocol);
    assertEquals(2, files.size());
    ProtocolFile file = files.get(0);
    assertEquals("FLAG_Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(file1), file.getContent());
    file = files.get(1);
    assertEquals("Histone_FLAG_Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(file2), file.getContent());
  }

  @Test
  public void save_Update() throws Throwable {
    ProtocolsView view = navigate(ProtocolsView.class);
    test(view.protocols).select(0);
    test(view.edit).click();
    ProtocolDialog dialog = $(ProtocolDialog.class).first();
    setFields(dialog);

    test(dialog.save).click();

    Notification notification = $(Notification.class).first();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + SAVED, new Object[]{name},
        UI.getCurrent().getLocale()), test(notification).getText());
    Protocol protocol = repository.findById(1L).orElseThrow();
    assertEquals(name, protocol.getName());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getCreationDate());
    assertEquals((Long) 3L, protocol.getOwner().getId());
    List<ProtocolFile> files = fileRepository.findByProtocol(protocol);
    assertEquals(3, files.size());
    ProtocolFile file = files.get(0);
    assertEquals("FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(Paths.get(
            Objects.requireNonNull(getClass().getResource("/protocol/FLAG_Protocol.docx")).toURI())),
        file.getContent());
    file = files.get(1);
    assertEquals("FLAG_Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(file1), file.getContent());
    file = files.get(2);
    assertEquals("Histone_FLAG_Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(file2), file.getContent());
  }

  @Test
  public void cancel() throws Throwable {
    detachOnServiceGet();
    ProtocolsView view = navigate(ProtocolsView.class);
    test(view.protocols).select(0);
    test(view.edit).click();
    ProtocolDialog dialog = $(ProtocolDialog.class).first();
    setFields(dialog);

    test(dialog.cancel).click();

    assertFalse($(Notification.class).exists());
    Protocol protocol = repository.findById(1L).orElseThrow();
    assertEquals("FLAG", protocol.getName());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getCreationDate());
    assertEquals((Long) 3L, protocol.getOwner().getId());
    List<ProtocolFile> files = fileRepository.findByProtocol(protocol);
    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertEquals("FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(Paths.get(
            Objects.requireNonNull(getClass().getResource("/protocol/FLAG_Protocol.docx")).toURI())),
        file.getContent());
  }

  @Test
  @WithUserDetails("francois.robert@ircm.qc.ca")
  public void delete() {
    detachOnServiceGet();
    Protocol protocol = repository.findById(4L).get();
    ProtocolsView view = navigate(ProtocolsView.class);
    test(view.protocols).select(3);
    test(view.edit).click();
    ProtocolDialog dialog = $(ProtocolDialog.class).first();
    final String name = protocol.getName();

    test(dialog.delete).click();
    test($(ConfirmDialog.class).first()).confirm();

    Notification notification = $(Notification.class).first();
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + DELETED, new Object[]{name},
        UI.getCurrent().getLocale()), test(notification).getText());
    assertFalse(repository.findById(4L).isPresent());
  }
}
