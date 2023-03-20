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

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.REMOVE;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.UPLOAD;
import static ca.qc.ircm.lanaseq.protocol.ProtocolFileProperties.FILENAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NOTE;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.DELETE_HEADER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.DELETE_MESSAGE;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES_ERROR;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.HEADER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.ID;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.MAXIMUM_FILES_COUNT;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.MAXIMUM_FILES_SIZE;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.REMOVE_BUTTON;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.functions;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.rendererTemplate;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateEquals;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.englishUploadI18N;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.frenchUploadI18N;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
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
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link ProtocolDialog}.
 */
@ServiceTestAnnotations
@WithMockUser
public class ProtocolDialogTest extends AbstractKaribuTestCase {
  private ProtocolDialog dialog;
  @Mock
  private ProtocolDialogPresenter presenter;
  @Mock
  private ComponentEventListener<SavedEvent<ProtocolDialog>> savedListener;
  @Mock
  private ComponentEventListener<DeletedEvent<ProtocolDialog>> deletedListener;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Anchor, ProtocolFile>> anchorComponentRendererCaptor;
  @Captor
  private ArgumentCaptor<LitRenderer<ProtocolFile>> litRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<ProtocolFile>> comparatorCaptor;
  @Autowired
  private ProtocolRepository repository;
  @Autowired
  private ProtocolFileRepository fileRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(ProtocolDialog.class, locale);
  private AppResources protocolResources = new AppResources(Protocol.class, locale);
  private AppResources protocolFileResources = new AppResources(ProtocolFile.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  @Mock
  private Protocol protocol;
  private List<ProtocolFile> protocolFiles;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    dialog = new ProtocolDialog(presenter);
    dialog.init();
    protocolFiles = fileRepository.findAll();
  }

  @SuppressWarnings("unchecked")
  private void mockColumns() {
    Element filesElement = dialog.files.getElement();
    dialog.files = mock(Grid.class);
    when(dialog.files.getElement()).thenReturn(filesElement);
    dialog.filename = mock(Column.class);
    when(dialog.files.addColumn(any(ComponentRenderer.class), eq(FILENAME)))
        .thenReturn(dialog.filename);
    when(dialog.filename.setKey(any())).thenReturn(dialog.filename);
    when(dialog.filename.setComparator(any(Comparator.class))).thenReturn(dialog.filename);
    when(dialog.filename.setHeader(any(String.class))).thenReturn(dialog.filename);
    when(dialog.filename.setFlexGrow(anyInt())).thenReturn(dialog.filename);
    dialog.remove = mock(Column.class);
    when(dialog.files.addColumn(any(LitRenderer.class), eq(REMOVE))).thenReturn(dialog.remove);
    when(dialog.remove.setKey(any())).thenReturn(dialog.remove);
    when(dialog.remove.setHeader(any(String.class))).thenReturn(dialog.remove);
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(dialog);
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(HEADER), dialog.header.getId().orElse(""));
    assertEquals(id(NAME), dialog.name.getId().orElse(""));
    assertEquals(id(NOTE), dialog.note.getId().orElse(""));
    assertEquals(id(UPLOAD), dialog.upload.getId().orElse(""));
    assertEquals(id(FILES), dialog.files.getId().orElse(""));
    assertEquals(id(FILES_ERROR), dialog.filesError.getId().orElse(""));
    assertTrue(dialog.filesError.hasClassName(ERROR_TEXT));
    assertEquals(id(SAVE), dialog.save.getId().orElse(""));
    assertTrue(dialog.save.hasThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName()));
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
    assertEquals(id(CANCEL), dialog.cancel.getId().orElse(""));
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel.getIcon());
    assertEquals(id(DELETE), dialog.delete.getId().orElse(""));
    assertTrue(dialog.delete.hasThemeName(ButtonVariant.LUMO_ERROR.getVariantName()));
    validateIcon(VaadinIcon.TRASH.create(), dialog.delete.getIcon());
    assertEquals(id(CONFIRM), dialog.confirm.getId().orElse(""));
    assertEquals("true", dialog.confirm.getElement().getProperty("cancel"));
    assertTrue(dialog.confirm.getElement().getProperty("confirmTheme")
        .contains(ButtonVariant.LUMO_ERROR.getVariantName()));
    assertTrue(dialog.confirm.getElement().getProperty("confirmTheme")
        .contains(ButtonVariant.LUMO_PRIMARY.getVariantName()));
  }

  @Test
  public void labels() {
    mockColumns();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(protocolResources.message(NAME), dialog.name.getLabel());
    assertEquals(protocolResources.message(NOTE), dialog.note.getLabel());
    verify(dialog.filename).setHeader(protocolFileResources.message(FILENAME));
    verify(dialog.remove).setHeader(webResources.message(REMOVE));
    validateEquals(englishUploadI18N(), dialog.upload.getI18n());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    assertEquals(webResources.message(DELETE), dialog.delete.getText());
    assertEquals(resources.message(DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(webResources.message(DELETE),
        dialog.confirm.getElement().getProperty("confirmText"));
    assertEquals(webResources.message(CANCEL),
        dialog.confirm.getElement().getProperty("cancelText"));
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    mockColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(ProtocolDialog.class, locale);
    final AppResources protocolResources = new AppResources(Protocol.class, locale);
    final AppResources protocolFileResources = new AppResources(ProtocolFile.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    ui.setLocale(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(protocolResources.message(NAME), dialog.name.getLabel());
    assertEquals(protocolResources.message(NOTE), dialog.note.getLabel());
    verify(dialog.filename).setHeader(protocolFileResources.message(FILENAME));
    verify(dialog.remove).setHeader(webResources.message(REMOVE));
    validateEquals(frenchUploadI18N(), dialog.upload.getI18n());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    assertEquals(webResources.message(DELETE), dialog.delete.getText());
    assertEquals(resources.message(DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(webResources.message(DELETE),
        dialog.confirm.getElement().getProperty("confirmText"));
    assertEquals(webResources.message(CANCEL),
        dialog.confirm.getElement().getProperty("cancelText"));
    verify(presenter).localeChange(locale);
  }

  @Test
  public void upload() {
    assertEquals(MAXIMUM_FILES_COUNT, dialog.upload.getMaxFiles());
    assertEquals(MAXIMUM_FILES_SIZE, dialog.upload.getMaxFileSize());
  }

  @Test
  public void upload_File() {
    dialog.uploadBuffer = mock(MultiFileMemoryBuffer.class);
    ByteArrayInputStream input = new ByteArrayInputStream(new byte[0]);
    when(dialog.uploadBuffer.getInputStream(any())).thenReturn(input);
    String filename = "test_file.txt";
    String mimeType = "text/plain";
    long filesize = 84325;
    SucceededEvent event = new SucceededEvent(dialog.upload, filename, mimeType, filesize);
    fireEvent(dialog.upload, event);
    verify(presenter).addFile(filename, input);
    verify(dialog.uploadBuffer).getInputStream(filename);
  }

  @Test
  public void files() {
    assertEquals(2, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
    assertNotNull(dialog.files.getColumnByKey(REMOVE));
    assertTrue(dialog.files.getSelectionModel() instanceof SelectionModel.Single);
  }

  @Test
  public void files_ColumnsValueProvider() {
    dialog = new ProtocolDialog(presenter);
    mockColumns();
    dialog.init();
    verify(dialog.files).addColumn(anchorComponentRendererCaptor.capture(), eq(FILENAME));
    ComponentRenderer<Anchor, ProtocolFile> anchorComponentRenderer =
        anchorComponentRendererCaptor.getValue();
    for (ProtocolFile file : protocolFiles) {
      Anchor anchor = anchorComponentRenderer.createComponent(file);
      assertEquals(file.getFilename(), anchor.getText());
      assertEquals(file.getFilename(), anchor.getElement().getAttribute("download"));
      assertTrue(anchor.getHref().startsWith("VAADIN/dynamic/resource"));
    }
    verify(dialog.filename).setComparator(comparatorCaptor.capture());
    Comparator<ProtocolFile> comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (ProtocolFile file : protocolFiles) {
      assertEquals(file.getFilename(),
          ((NormalizedComparator<ProtocolFile>) comparator).getConverter().apply(file));
    }
    verify(dialog.files).addColumn(litRendererCaptor.capture(), eq(REMOVE));
    LitRenderer<ProtocolFile> litRenderer = litRendererCaptor.getValue();
    for (ProtocolFile file : protocolFiles) {
      assertEquals(REMOVE_BUTTON, rendererTemplate(litRenderer));
      assertTrue(functions(litRenderer).containsKey("removeFile"));
      functions(litRenderer).get("removeFile").accept(file, null);
      verify(presenter).removeFile(file);
    }
  }

  @Test
  public void savedListener() {
    dialog.addSavedListener(savedListener);
    dialog.fireSavedEvent();
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void savedListener_Remove() {
    dialog.addSavedListener(savedListener).remove();
    dialog.fireSavedEvent();
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void deletedListener() {
    dialog.addDeletedListener(deletedListener);
    dialog.fireDeletedEvent();
    verify(deletedListener).onComponentEvent(any());
  }

  @Test
  public void deletedListener_Remove() {
    dialog.addDeletedListener(deletedListener).remove();
    dialog.fireDeletedEvent();
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void getProtocol() {
    when(presenter.getProtocol()).thenReturn(protocol);
    assertEquals(protocol, dialog.getProtocol());
    verify(presenter).getProtocol();
  }

  @Test
  public void setProtocol_NewProtocol() {
    Protocol protocol = new Protocol();
    when(presenter.getProtocol()).thenReturn(protocol);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setProtocol(protocol);

    verify(presenter).setProtocol(protocol);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void setProtocol_Protocol() {
    Protocol protocol = repository.findById(2L).get();
    when(presenter.getProtocol()).thenReturn(protocol);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setProtocol(protocol);

    verify(presenter).setProtocol(protocol);
    assertEquals(resources.message(HEADER, 1, protocol.getName()), dialog.header.getText());
    assertEquals(resources.message(DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(resources.message(DELETE_MESSAGE, protocol.getName()),
        dialog.confirm.getElement().getProperty("message"));
  }

  @Test
  public void setProtocol_BeforeLocaleChange() {
    Protocol protocol = repository.findById(2L).get();
    when(presenter.getProtocol()).thenReturn(protocol);

    dialog.setProtocol(protocol);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(presenter).setProtocol(protocol);
    assertEquals(resources.message(HEADER, 1, protocol.getName()), dialog.header.getText());
    assertEquals(resources.message(DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(resources.message(DELETE_MESSAGE, protocol.getName()),
        dialog.confirm.getElement().getProperty("message"));
  }

  @Test
  public void setProtocol_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setProtocol(null);

    verify(presenter).setProtocol(null);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void save() {
    dialog.save.click();
    verify(presenter).save();
  }

  @Test
  public void cancel() {
    clickButton(dialog.cancel);
    verify(presenter).cancel();
  }

  @Test
  public void delete_Confirm() {
    clickButton(dialog.delete);

    assertTrue(dialog.confirm.isOpened());
    ConfirmDialog.ConfirmEvent event = new ConfirmDialog.ConfirmEvent(dialog.confirm, false);
    fireEvent(dialog.confirm, event);
    verify(presenter).delete();
  }

  @Test
  public void delete_Cancel() {
    clickButton(dialog.delete);

    assertTrue(dialog.confirm.isOpened());
    ConfirmDialog.CancelEvent event = new ConfirmDialog.CancelEvent(dialog.confirm, false);
    fireEvent(dialog.confirm, event);
    verify(presenter, never()).delete();
  }
}
