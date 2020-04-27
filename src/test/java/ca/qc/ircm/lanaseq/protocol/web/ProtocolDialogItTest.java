package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.ID;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.SAVED;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolsView.VIEW_NAME;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.Laboratory;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TestTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ProtocolDialogItTest extends AbstractTestBenchTestCase {
  @Autowired
  private ProtocolRepository repository;
  private String name = "test protocol";
  private Path file1;
  private Path file2;

  @Before
  public void beforeTest() throws Throwable {
    file1 = Paths.get(getClass().getResource("/protocol/FLAG_Protocol.docx").toURI());
    file2 = Paths.get(getClass().getResource("/protocol/Histone_FLAG_Protocol.docx").toURI());
  }

  private void open() {
    openView(VIEW_NAME);
  }

  private void setFields(ProtocolDialogElement dialog) {
    dialog.name().setValue(name);
    dialog.upload().upload(file1.toFile());
    dialog.upload().upload(file2.toFile());
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).id(ProtocolsView.ID);
    view.doubleClickProtocol(0);
    ProtocolDialogElement dialog = $(ProtocolDialogElement.class).id(ID);
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.name()).isPresent());
    assertTrue(optional(() -> dialog.upload()).isPresent());
    assertTrue(optional(() -> dialog.files()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
    assertTrue(optional(() -> dialog.cancel()).isPresent());
  }

  @Test
  public void save_New() throws Throwable {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).id(ProtocolsView.ID);
    view.add().click();
    ProtocolDialogElement dialog = $(ProtocolDialogElement.class).id(ID);
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(ProtocolDialog.class);
    assertEquals(resources.message(SAVED, name), notification.getText());
    Protocol protocol =
        repository.findByNameAndOwnerLaboratory(name, new Laboratory(2L)).orElse(null);
    assertNotNull(protocol);
    assertEquals(name, protocol.getName());
    assertEquals(2, protocol.getFiles().size());
    ProtocolFile file = protocol.getFiles().get(0);
    assertEquals("FLAG_Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(file1), file.getContent());
    file = protocol.getFiles().get(1);
    assertEquals("Histone_FLAG_Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(file2), file.getContent());
  }

  @Test
  public void save_Update() throws Throwable {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).id(ProtocolsView.ID);
    view.doubleClickProtocol(0);
    ProtocolDialogElement dialog = $(ProtocolDialogElement.class).id(ID);
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(ProtocolDialog.class);
    assertEquals(resources.message(SAVED, name), notification.getText());
    Protocol protocol = repository.findById(1L).get();
    assertEquals(name, protocol.getName());
    assertEquals(3, protocol.getFiles().size());
    ProtocolFile file = protocol.getFiles().get(0);
    assertEquals("FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(getClass().getResource("/protocol/FLAG_Protocol.docx").toURI())),
        file.getContent());
    file = protocol.getFiles().get(1);
    assertEquals("FLAG_Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(file1), file.getContent());
    file = protocol.getFiles().get(2);
    assertEquals("Histone_FLAG_Protocol.docx", file.getFilename());
    assertArrayEquals(Files.readAllBytes(file2), file.getContent());
  }

  @Test
  public void cancel() throws Throwable {
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).id(ProtocolsView.ID);
    view.doubleClickProtocol(0);
    ProtocolDialogElement dialog = $(ProtocolDialogElement.class).id(ID);
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.cancel().click();
    TestTransaction.end();

    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
    Protocol protocol = repository.findById(1L).get();
    assertEquals("FLAG", protocol.getName());
    assertEquals(1, protocol.getFiles().size());
    ProtocolFile file = protocol.getFiles().get(0);
    assertEquals("FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(getClass().getResource("/protocol/FLAG_Protocol.docx").toURI())),
        file.getContent());
  }
}
