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

package ca.qc.ircm.lanaseq.protocol.web;

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
import ca.qc.ircm.lanaseq.protocol.ProtocolFileRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.Download;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.flow.component.html.testbench.AnchorElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TestTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ProtocolDialogItTest extends AbstractTestBenchTestCase {
  @Autowired
  private ProtocolRepository repository;
  @Autowired
  private ProtocolFileRepository fileRepository;
  @Value("${download-home}")
  protected Path downloadHome;
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
    ProtocolDialogElement dialog = view.dialog();
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
    ProtocolDialogElement dialog = view.dialog();
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(ProtocolDialog.class);
    assertEquals(resources.message(SAVED, name), notification.getText());
    Protocol protocol = repository.findByName(name).orElse(null);
    assertNotNull(protocol);
    assertNotNull(protocol.getId());
    assertEquals(name, protocol.getName());
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(protocol.getDate()));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(protocol.getDate()));
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
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).id(ProtocolsView.ID);
    view.doubleClickProtocol(0);
    ProtocolDialogElement dialog = view.dialog();
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(ProtocolDialog.class);
    assertEquals(resources.message(SAVED, name), notification.getText());
    Protocol protocol = repository.findById(1L).get();
    assertEquals(name, protocol.getName());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getDate());
    assertEquals((Long) 3L, protocol.getOwner().getId());
    List<ProtocolFile> files = fileRepository.findByProtocol(protocol);
    assertEquals(3, files.size());
    ProtocolFile file = files.get(0);
    assertEquals("FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(getClass().getResource("/protocol/FLAG_Protocol.docx").toURI())),
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
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).id(ProtocolsView.ID);
    view.doubleClickProtocol(0);
    ProtocolDialogElement dialog = view.dialog();
    setFields(dialog);

    TestTransaction.flagForCommit();
    dialog.cancel().click();
    TestTransaction.end();

    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
    Protocol protocol = repository.findById(1L).get();
    assertEquals("FLAG", protocol.getName());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getDate());
    assertEquals((Long) 3L, protocol.getOwner().getId());
    List<ProtocolFile> files = fileRepository.findByProtocol(protocol);
    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertEquals("FLAG Protocol.docx", file.getFilename());
    assertArrayEquals(
        Files.readAllBytes(
            Paths.get(getClass().getResource("/protocol/FLAG_Protocol.docx").toURI())),
        file.getContent());
  }

  @Test
  @Download
  public void downloadFile() throws Throwable {
    Files.createDirectories(downloadHome);
    Path downloaded = downloadHome.resolve("FLAG Protocol.docx");
    Files.deleteIfExists(downloaded);
    Path source = Paths.get(getClass().getResource("/protocol/FLAG_Protocol.docx").toURI());
    open();
    ProtocolsViewElement view = $(ProtocolsViewElement.class).id(ProtocolsView.ID);
    view.doubleClickProtocol(0);
    ProtocolDialogElement dialog = view.dialog();
    AnchorElement filename = dialog.filename(0);
    filename.click();

    // Wait for file to download.
    Thread.sleep(2000);
    assertTrue(Files.exists(downloaded));
    try {
      assertArrayEquals(Files.readAllBytes(source), Files.readAllBytes(downloaded));
    } finally {
      Files.delete(downloaded);
    }
  }
}
