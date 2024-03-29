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

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.REMOVE;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETED;
import static ca.qc.ircm.lanaseq.protocol.ProtocolFileProperties.FILENAME;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES_IOEXCEPTION;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES_OVER_MAXIMUM;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES_REQUIRED;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.MAXIMUM_FILES_COUNT;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.SAVED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolFileRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.provider.DataProviderListener;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link ProtocolDialogPresenter}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ProtocolDialogPresenterTest extends AbstractKaribuTestCase {
  @Autowired
  private ProtocolDialogPresenter presenter;
  @Mock
  private ProtocolDialog dialog;
  @MockBean
  private ProtocolService service;
  @MockBean
  private AuthenticatedUser authenticatedUser;
  @Mock
  private ProtocolFile file;
  @Mock
  private DataProviderListener<ProtocolFile> filesDataProviderListener;
  @Captor
  private ArgumentCaptor<Protocol> protocolCaptor;
  @Captor
  private ArgumentCaptor<Collection<ProtocolFile>> filesCaptor;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(ProtocolDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  @Autowired
  private ProtocolRepository repository;
  @Autowired
  private ProtocolFileRepository fileRepository;
  private Protocol protocol;
  private String name = "test protocol";
  private String note = "test note\nsecond line";
  private String filename = "test file";
  private byte[] fileContent = new byte[5120];
  private Random random = new Random();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    dialog.name = new TextField();
    dialog.note = new TextArea();
    dialog.uploadBuffer = new MultiFileMemoryBuffer();
    dialog.upload = new Upload();
    dialog.files = new Grid<>();
    dialog.filename = dialog.files.addColumn(file -> file.getId(), FILENAME);
    dialog.remove = dialog.files.addColumn(file -> file.getId(), REMOVE);
    dialog.filesError = new Div();
    dialog.save = new Button();
    dialog.cancel = new Button();
    dialog.delete = new Button();
    presenter.init(dialog);
    presenter.localeChange(locale);
    protocol = repository.findById(1L).get();
    random.nextBytes(fileContent);
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    when(service.files(any())).then(i -> {
      Protocol protocol = i.getArgument(0);
      if (protocol != null && protocol.getId() != null) {
        return fileRepository.findByProtocolAndDeletedFalse(protocol);
      } else {
        return new ArrayList<>();
      }
    });
  }

  private void fillFields() {
    dialog.name.setValue(name);
    dialog.note.setValue(note);
    presenter.addFile(filename, new ByteArrayInputStream(fileContent));
  }

  @Test
  public void addFile() {
    dialog.files.getDataProvider().addDataProviderListener(filesDataProviderListener);
    presenter.addFile(filename, new ByteArrayInputStream(fileContent));
    List<ProtocolFile> files = items(dialog.files);
    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertEquals(filename, file.getFilename());
    assertArrayEquals(fileContent, file.getContent());
    verify(filesDataProviderListener).onDataChange(any());
  }

  @Test
  public void addFile_IoException() throws Throwable {
    dialog.files.getDataProvider().addDataProviderListener(filesDataProviderListener);
    InputStream input = mock(InputStream.class);
    when(input.read()).thenThrow(new IOException());
    when(input.read(any())).thenThrow(new IOException());
    when(input.read(any(), anyInt(), anyInt())).thenThrow(new IOException());
    presenter.addFile(filename, input);
    verify(dialog).showNotification(resources.message(FILES_IOEXCEPTION, filename));
    assertTrue(items(dialog.files).isEmpty());
    verify(filesDataProviderListener, never()).onDataChange(any());
  }

  @Test
  public void addFile_OverMaximum() throws Throwable {
    presenter.addFile(filename, new ByteArrayInputStream(fileContent));
    presenter.addFile(filename + "1", new ByteArrayInputStream(fileContent));
    presenter.addFile(filename + "2", new ByteArrayInputStream(fileContent));
    presenter.addFile(filename + "3", new ByteArrayInputStream(fileContent));
    presenter.addFile(filename + "4", new ByteArrayInputStream(fileContent));
    presenter.addFile(filename + "5", new ByteArrayInputStream(fileContent));
    dialog.files.getDataProvider().addDataProviderListener(filesDataProviderListener);
    presenter.addFile(filename + "6", new ByteArrayInputStream(fileContent));
    verify(dialog).showNotification(resources.message(FILES_OVER_MAXIMUM, MAXIMUM_FILES_COUNT));
    assertEquals(MAXIMUM_FILES_COUNT, items(dialog.files).size());
    assertEquals(filename, items(dialog.files).get(0).getFilename());
    assertEquals(filename + "1", items(dialog.files).get(1).getFilename());
    assertEquals(filename + "2", items(dialog.files).get(2).getFilename());
    assertEquals(filename + "3", items(dialog.files).get(3).getFilename());
    assertEquals(filename + "4", items(dialog.files).get(4).getFilename());
    assertEquals(filename + "5", items(dialog.files).get(5).getFilename());
    verify(filesDataProviderListener, never()).onDataChange(any());
  }

  @Test
  public void removeFile() {
    items(dialog.files).add(file);
    dialog.files.getDataProvider().addDataProviderListener(filesDataProviderListener);
    presenter.removeFile(file);
    assertTrue(items(dialog.files).isEmpty());
    verify(filesDataProviderListener).onDataChange(any());
  }

  @Test
  public void save_EmptyName() {
    fillFields();
    dialog.name.setValue("");

    presenter.save();

    BinderValidationStatus<Protocol> status = presenter.validateProtocol();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any(), any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_NameExistsNewProtocol() {
    when(service.nameExists(any())).thenReturn(true);
    fillFields();

    presenter.save();

    BinderValidationStatus<Protocol> status = presenter.validateProtocol();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(ALREADY_EXISTS)), error.getMessage());
    verify(service, atLeastOnce()).nameExists(name);
    verify(service, never()).save(any(), any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_NameExistsSameProtocol() {
    when(service.nameExists(any())).thenReturn(true);
    when(service.get(any())).thenReturn(Optional.of(protocol));
    presenter.setProtocol(protocol);
    fillFields();
    dialog.name.setValue(protocol.getName());

    presenter.save();

    BinderValidationStatus<Protocol> status = presenter.validateProtocol();
    assertTrue(status.isOk());
    verify(service, atLeastOnce()).nameExists(protocol.getName());
    verify(service, atLeastOnce()).get(protocol.getId());
    verify(service).save(any(), any());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_NameExistsDifferentProtocol() {
    when(service.nameExists(any())).thenReturn(true);
    when(service.get(any())).thenReturn(Optional.of(protocol));
    presenter.setProtocol(protocol);
    fillFields();

    presenter.save();

    BinderValidationStatus<Protocol> status = presenter.validateProtocol();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(ALREADY_EXISTS)), error.getMessage());
    verify(service, atLeastOnce()).nameExists(name);
    verify(service, atLeastOnce()).get(protocol.getId());
    verify(service, never()).save(any(), any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_EmptyNote() {
    fillFields();
    dialog.note.setValue("");

    presenter.save();

    BinderValidationStatus<Protocol> status = presenter.validateProtocol();
    assertTrue(status.isOk());
    verify(service).save(any(), any());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_NoFile() {
    fillFields();
    presenter.removeFile(items(dialog.files).get(0));

    presenter.save();

    dialog.filesError.setVisible(true);
    assertEquals(resources.message(FILES_REQUIRED), dialog.filesError.getText());
    verify(service, never()).save(any(), any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_NoFileErrorClear() {
    fillFields();
    presenter.removeFile(items(dialog.files).get(0));
    presenter.save();
    fillFields();
    presenter.save();

    dialog.filesError.setVisible(false);
  }

  @Test
  public void save_NewProtocol() {
    fillFields();

    presenter.save();

    verify(service).save(protocolCaptor.capture(), filesCaptor.capture());
    Protocol protocol = protocolCaptor.getValue();
    assertNull(protocol.getId());
    assertEquals(name, protocol.getName());
    assertEquals(note, protocol.getNote());
    assertNull(protocol.getCreationDate());
    assertNull(protocol.getOwner());
    List<ProtocolFile> files = new ArrayList<>(filesCaptor.getValue());
    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertNull(file.getId());
    assertEquals(filename, file.getFilename());
    assertArrayEquals(fileContent, file.getContent());
    verify(dialog).showNotification(resources.message(SAVED, name));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateProtocol() throws Throwable {
    presenter.setProtocol(protocol);
    fillFields();

    presenter.save();

    verify(service).save(protocolCaptor.capture(), filesCaptor.capture());
    Protocol protocol = protocolCaptor.getValue();
    assertEquals((Long) 1L, protocol.getId());
    assertEquals(name, protocol.getName());
    assertEquals(note, protocol.getNote());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getCreationDate());
    assertEquals((Long) 3L, protocol.getOwner().getId());
    List<ProtocolFile> files = new ArrayList<>(filesCaptor.getValue());
    assertEquals(2, files.size());
    ProtocolFile file = files.get(0);
    assertEquals((Long) 1L, file.getId());
    assertEquals("FLAG Protocol.docx", file.getFilename());
    byte[] fileContent = Files
        .readAllBytes(Paths.get(getClass().getResource("/protocol/FLAG_Protocol.docx").toURI()));
    assertArrayEquals(fileContent, file.getContent());
    file = files.get(1);
    assertNull(file.getId());
    assertEquals(filename, file.getFilename());
    assertArrayEquals(this.fileContent, file.getContent());
    verify(dialog).showNotification(resources.message(SAVED, name));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateProtocolRemoveFile() throws Throwable {
    presenter.setProtocol(protocol);
    fillFields();
    presenter.removeFile(items(dialog.files).get(0));

    presenter.save();

    verify(service).save(protocolCaptor.capture(), filesCaptor.capture());
    Protocol protocol = protocolCaptor.getValue();
    assertEquals((Long) 1L, protocol.getId());
    assertEquals(name, protocol.getName());
    assertEquals(note, protocol.getNote());
    assertEquals(LocalDateTime.of(2018, 10, 20, 11, 28, 12), protocol.getCreationDate());
    assertEquals((Long) 3L, protocol.getOwner().getId());
    List<ProtocolFile> files = new ArrayList<>(filesCaptor.getValue());
    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertNull(file.getId());
    assertEquals(filename, file.getFilename());
    assertArrayEquals(this.fileContent, file.getContent());
    verify(dialog).showNotification(resources.message(SAVED, name));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void cancel() {
    presenter.cancel();
    verify(service, never()).save(any(), any());
    verify(dialog, never()).showNotification(any());
    verify(dialog).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void getProtocol_New() {
    when(service.files(any())).thenReturn(new ArrayList<>());
    Protocol protocol = presenter.getProtocol();
    assertNotNull(protocol);
    assertNull(protocol.getId());
    assertNull(protocol.getName());
    assertNull(protocol.getNote());
    assertTrue(items(dialog.files).isEmpty());
  }

  @Test
  public void getProtocol_Set() {
    presenter.setProtocol(protocol);
    Protocol protocol = presenter.getProtocol();
    assertSame(this.protocol, protocol);
  }

  @Test
  public void setProtocol() {
    presenter.setProtocol(protocol);
    assertEquals(protocol.getName(), dialog.name.getValue());
    assertEquals(protocol.getNote(), dialog.note.getValue());
    List<ProtocolFile> expectedFiles = fileRepository.findByProtocol(protocol);
    List<ProtocolFile> files = items(dialog.files);
    assertEquals(expectedFiles.size(), files.size());
    for (int i = 0; i < expectedFiles.size(); i++) {
      assertEquals(expectedFiles.get(i), files.get(i));
    }
    assertFalse(dialog.name.isReadOnly());
    assertTrue(dialog.upload.isVisible());
    assertTrue(dialog.remove.isVisible());
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void setProtocol_CannotWrite() {
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(false);
    presenter.setProtocol(protocol);
    assertEquals(protocol.getName(), dialog.name.getValue());
    assertEquals(protocol.getNote(), dialog.note.getValue());
    List<ProtocolFile> files = items(dialog.files);
    List<ProtocolFile> expectedFiles = fileRepository.findByProtocol(protocol);
    assertEquals(expectedFiles.size(), files.size());
    for (int i = 0; i < expectedFiles.size(); i++) {
      assertEquals(expectedFiles.get(i), files.get(i));
    }
    assertTrue(dialog.name.isReadOnly());
    assertFalse(dialog.upload.isVisible());
    assertFalse(dialog.remove.isVisible());
    assertFalse(dialog.save.isVisible());
    assertFalse(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void setProtocol_CannotWriteBeforeLocaleChange() {
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(false);
    presenter.setProtocol(protocol);
    presenter.localeChange(locale);
    assertEquals(protocol.getName(), dialog.name.getValue());
    assertEquals(protocol.getNote(), dialog.note.getValue());
    List<ProtocolFile> files = items(dialog.files);
    List<ProtocolFile> expectedFiles = fileRepository.findByProtocol(protocol);
    assertEquals(expectedFiles.size(), files.size());
    for (int i = 0; i < expectedFiles.size(); i++) {
      assertEquals(expectedFiles.get(i), files.get(i));
    }
    assertTrue(dialog.name.isReadOnly());
    assertFalse(dialog.upload.isVisible());
    assertFalse(dialog.remove.isVisible());
    assertFalse(dialog.save.isVisible());
    assertFalse(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void setProtocol_Deletable() {
    when(service.isDeletable(any())).thenReturn(true);
    presenter.setProtocol(protocol);
    assertEquals(protocol.getName(), dialog.name.getValue());
    assertEquals(protocol.getNote(), dialog.note.getValue());
    List<ProtocolFile> expectedFiles = fileRepository.findByProtocol(protocol);
    List<ProtocolFile> files = items(dialog.files);
    assertEquals(expectedFiles.size(), files.size());
    for (int i = 0; i < expectedFiles.size(); i++) {
      assertEquals(expectedFiles.get(i), files.get(i));
    }
    assertFalse(dialog.name.isReadOnly());
    assertTrue(dialog.upload.isVisible());
    assertTrue(dialog.remove.isVisible());
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertTrue(dialog.delete.isVisible());
  }

  @Test
  public void setProtocol_Null() {
    presenter.setProtocol(null);
    assertEquals("", dialog.name.getValue());
    assertEquals("", dialog.note.getValue());
    assertTrue(items(dialog.files).isEmpty());
    assertFalse(dialog.name.isReadOnly());
    assertTrue(dialog.upload.isVisible());
    assertTrue(dialog.remove.isVisible());
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void setProtocol_NoFiles() {
    when(service.files(any())).thenReturn(new ArrayList<>());
    presenter.setProtocol(new Protocol());
    assertEquals("", dialog.name.getValue());
    assertEquals("", dialog.note.getValue());
    assertTrue(items(dialog.files).isEmpty());
    assertFalse(dialog.name.isReadOnly());
    assertTrue(dialog.upload.isVisible());
    assertTrue(dialog.remove.isVisible());
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void setProtocol_DeletedFiles() {
    Protocol protocol = repository.findById(3L).get();
    presenter.setProtocol(protocol);
    assertEquals(protocol.getName(), dialog.name.getValue());
    assertEquals("", dialog.note.getValue());
    List<ProtocolFile> expectedFiles = fileRepository.findByProtocol(protocol).stream()
        .filter(file -> !file.isDeleted()).collect(Collectors.toList());
    List<ProtocolFile> files = items(dialog.files);
    assertEquals(expectedFiles.size(), files.size());
    for (int i = 0; i < expectedFiles.size(); i++) {
      assertEquals(expectedFiles.get(i), files.get(i));
    }
    assertFalse(dialog.name.isReadOnly());
    assertTrue(dialog.upload.isVisible());
    assertTrue(dialog.remove.isVisible());
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void delete() {
    Protocol protocol = repository.findById(4L).get();
    presenter.setProtocol(protocol);

    presenter.delete();

    verify(service, never()).save(any(), any());
    verify(service).delete(protocol);
    verify(dialog).close();
    verify(dialog).showNotification(resources.message(DELETED, protocol.getName()));
    verify(dialog, never()).fireSavedEvent();
    verify(dialog).fireDeletedEvent();
  }
}
