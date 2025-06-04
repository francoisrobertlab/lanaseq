package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.REMOVE;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.UPLOAD;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.protocol.ProtocolFileProperties.FILENAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NAME;
import static ca.qc.ircm.lanaseq.protocol.ProtocolProperties.NOTE;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.DELETED;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.DELETE_HEADER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.DELETE_MESSAGE;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES_ERROR;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES_IOEXCEPTION;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES_OVER_MAXIMUM;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.FILES_REQUIRED;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.HEADER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.ID;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.MAXIMUM_FILES_COUNT;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.MAXIMUM_FILES_SIZE;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.REMOVE_BUTTON;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.SAVED;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.functions;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.rendererTemplate;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateEquals;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.englishUploadI18N;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.frenchUploadI18N;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolFileRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.WarningNotification;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link ProtocolDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ProtocolDialogTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(ProtocolDialog.class);
  private static final String PROTOCOL_PREFIX = messagePrefix(Protocol.class);
  private static final String PROTOCOL_FILE_PREFIX = messagePrefix(ProtocolFile.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private ProtocolDialog dialog;
  @MockitoBean
  private ProtocolService service;
  @Mock
  private ComponentEventListener<SavedEvent<ProtocolDialog>> savedListener;
  @Mock
  private ComponentEventListener<DeletedEvent<ProtocolDialog>> deletedListener;
  @Captor
  private ArgumentCaptor<Protocol> protocolCaptor;
  @Captor
  private ArgumentCaptor<Collection<ProtocolFile>> filesCaptor;
  @Autowired
  private ProtocolRepository repository;
  @Autowired
  private ProtocolFileRepository fileRepository;
  @Autowired
  private AuthenticatedUser authenticatedUser;
  private final Locale locale = Locale.ENGLISH;
  private List<ProtocolFile> protocolFiles;
  private final String name = "test protocol";
  private final String note = "test note\nsecond line";
  private final String filename = "test file";
  private final byte[] fileContent = new byte[5120];
  private final Random random = new Random();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    protocolFiles = fileRepository.findAll();
    when(service.all()).thenReturn(repository.findAll());
    when(service.get(anyLong())).then(i -> repository.findById(i.getArgument(0)));
    when(service.files(any())).then(i -> {
      Protocol protocol = i.getArgument(0);
      return protocol.getId() != 0 ? fileRepository.findByProtocolAndDeletedFalse(protocol)
          : new ArrayList<>();
    });
    random.nextBytes(fileContent);
    UI.getCurrent().setLocale(locale);
    ProtocolsView view = navigate(ProtocolsView.class);
    test(view.protocols).doubleClickRow(0);
    dialog = $(ProtocolDialog.class).first();
  }

  private void fillFields() {
    dialog.name.setValue(name);
    dialog.note.setValue(note);
    ProtocolFile file = new ProtocolFile();
    file.setFilename(filename);
    file.setContent(fileContent);
    dialog.files.getListDataView().addItem(file);
  }

  private ProtocolFile filename(String filename) {
    ProtocolFile file = new ProtocolFile();
    file.setFilename(filename);
    return file;
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
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
    assertEquals("auto", dialog.delete.getStyle().get("margin-inline-end"));
    assertTrue(dialog.delete.hasThemeName(ButtonVariant.LUMO_ERROR.getVariantName()));
    validateIcon(VaadinIcon.TRASH.create(), dialog.delete.getIcon());
    assertEquals(id(CONFIRM), dialog.confirm.getId().orElse(""));
    assertEquals("true", dialog.confirm.getElement().getProperty("cancelButtonVisible"));
    assertTrue(dialog.confirm.getElement().getProperty("confirmTheme")
        .contains(ButtonVariant.LUMO_ERROR.getVariantName()));
    assertTrue(dialog.confirm.getElement().getProperty("confirmTheme")
        .contains(ButtonVariant.LUMO_PRIMARY.getVariantName()));
  }

  @Test
  public void labels() {
    Protocol protocol = repository.findById(dialog.getProtocolId()).orElseThrow();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, protocol.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(PROTOCOL_PREFIX + NAME), dialog.name.getLabel());
    assertEquals(dialog.getTranslation(PROTOCOL_PREFIX + NOTE), dialog.note.getLabel());
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(PROTOCOL_FILE_PREFIX + FILENAME),
        headerRow.getCell(dialog.filename).getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + REMOVE),
        headerRow.getCell(dialog.remove).getText());
    validateEquals(englishUploadI18N(), dialog.upload.getI18n());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + SAVE), dialog.save.getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + CANCEL), dialog.cancel.getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + DELETE), dialog.delete.getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + DELETE),
        dialog.confirm.getElement().getProperty("confirmText"));
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + CANCEL),
        dialog.confirm.getElement().getProperty("cancelText"));
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    Protocol protocol = repository.findById(dialog.getProtocolId()).orElseThrow();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, protocol.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(PROTOCOL_PREFIX + NAME), dialog.name.getLabel());
    assertEquals(dialog.getTranslation(PROTOCOL_PREFIX + NOTE), dialog.note.getLabel());
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(PROTOCOL_FILE_PREFIX + FILENAME),
        headerRow.getCell(dialog.filename).getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + REMOVE),
        headerRow.getCell(dialog.remove).getText());
    validateEquals(frenchUploadI18N(), dialog.upload.getI18n());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + SAVE), dialog.save.getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + CANCEL), dialog.cancel.getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + DELETE), dialog.delete.getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + DELETE),
        dialog.confirm.getElement().getProperty("confirmText"));
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + CANCEL),
        dialog.confirm.getElement().getProperty("cancelText"));
  }

  @Test
  public void upload() {
    assertEquals(MAXIMUM_FILES_COUNT, dialog.upload.getMaxFiles());
    assertEquals(MAXIMUM_FILES_SIZE, dialog.upload.getMaxFileSize());
  }

  @Test
  public void upload_File() {
    String mimeType = "text/plain";

    test(dialog.upload).upload(filename, mimeType, fileContent);

    assertFalse($(Notification.class).exists());
    List<ProtocolFile> files = items(dialog.files);
    assertEquals(2, files.size());
    ProtocolFile file = files.get(1);
    assertEquals(filename, file.getFilename());
    assertArrayEquals(fileContent, file.getContent());
  }

  @Test
  public void upload_Fail() {
    String mimeType = "text/plain";

    test(dialog.upload).uploadFailed(filename, mimeType);

    Notification notification = $(Notification.class).first();
    assertInstanceOf(WarningNotification.class, notification);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + FILES_IOEXCEPTION, filename),
        ((WarningNotification) notification).getText());
    List<ProtocolFile> files = items(dialog.files);
    assertEquals(1, files.size());
  }

  @Test
  public void upload_OverMaximum() {
    String mimeType = "text/plain";

    test(dialog.upload).upload(filename + "1", mimeType, fileContent);
    test(dialog.upload).upload(filename + "2", mimeType, fileContent);
    test(dialog.upload).upload(filename + "3", mimeType, fileContent);
    test(dialog.upload).upload(filename + "4", mimeType, fileContent);
    test(dialog.upload).upload(filename + "5", mimeType, fileContent);
    test(dialog.upload).upload(filename + "6", mimeType, fileContent);

    Notification notification = $(Notification.class).first();
    assertInstanceOf(WarningNotification.class, notification);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + FILES_OVER_MAXIMUM, MAXIMUM_FILES_COUNT),
        ((WarningNotification) notification).getText());
    assertEquals(MAXIMUM_FILES_COUNT, items(dialog.files).size());
    assertEquals(filename + "1", items(dialog.files).get(1).getFilename());
    assertEquals(filename + "2", items(dialog.files).get(2).getFilename());
    assertEquals(filename + "3", items(dialog.files).get(3).getFilename());
    assertEquals(filename + "4", items(dialog.files).get(4).getFilename());
    assertEquals(filename + "5", items(dialog.files).get(5).getFilename());
  }

  @Test
  public void files() {
    assertEquals(2, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
    assertNotNull(dialog.files.getColumnByKey(REMOVE));
    assertInstanceOf(SelectionModel.Single.class, dialog.files.getSelectionModel());
  }

  @Test
  public void files_ColumnsValueProvider() {
    dialog.files.setItems(new ArrayList<>(protocolFiles));
    for (int i = 0; i < protocolFiles.size(); i++) {
      ProtocolFile file = protocolFiles.get(i);
      ComponentRenderer<Anchor, ProtocolFile> filenameRenderer = (ComponentRenderer<Anchor, ProtocolFile>) dialog.filename.getRenderer();
      Anchor anchor = filenameRenderer.createComponent(file);
      assertEquals(file.getFilename(), anchor.getText());
      assertEquals(file.getFilename(), anchor.getElement().getAttribute("download"));
      assertTrue(anchor.getHref().startsWith("VAADIN/dynamic/resource"));
      LitRenderer<ProtocolFile> removeRenderer = (LitRenderer<ProtocolFile>) dialog.remove.getRenderer();
      assertEquals(REMOVE_BUTTON, rendererTemplate(removeRenderer));
      assertTrue(functions(removeRenderer).containsKey("removeFile"));
      functions(removeRenderer).get("removeFile").accept(file, null);
      assertEquals(protocolFiles.size() - i - 1, items(dialog.files).size());
    }
  }

  @Test
  public void files_FilenameColumnComparator() {
    Comparator<ProtocolFile> comparator = dialog.filename.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(filename("éê"), filename("ee")));
    assertTrue(comparator.compare(filename("a"), filename("e")) < 0);
    assertTrue(comparator.compare(filename("a"), filename("é")) < 0);
    assertTrue(comparator.compare(filename("e"), filename("a")) > 0);
    assertTrue(comparator.compare(filename("é"), filename("a")) > 0);
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
  public void getProtocolId() {
    assertEquals(1L, dialog.getProtocolId());
  }

  @Test
  public void setProtocolId() {
    Protocol protocol = repository.findById(1L).orElseThrow();

    dialog.setProtocolId(1L);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, protocol.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_MESSAGE, protocol.getName()),
        dialog.confirm.getElement().getProperty("message"));
    assertEquals(protocol.getName(), dialog.name.getValue());
    assertFalse(dialog.name.isReadOnly());
    assertEquals(protocol.getNote(), dialog.note.getValue());
    assertFalse(dialog.note.isReadOnly());
    assertTrue(dialog.upload.isVisible());
    assertTrue(dialog.remove.isVisible());
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
    List<ProtocolFile> files = items(dialog.files);
    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertEquals(1, file.getId());
  }

  @Test
  public void setProtocolId_ReadOnly() {
    Protocol protocol = repository.findById(2L).orElseThrow();

    dialog.setProtocolId(2L);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, protocol.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_MESSAGE, protocol.getName()),
        dialog.confirm.getElement().getProperty("message"));
    assertEquals(protocol.getName(), dialog.name.getValue());
    assertTrue(dialog.name.isReadOnly());
    assertEquals(protocol.getNote() != null ? protocol.getNote() : "", dialog.note.getValue());
    assertTrue(dialog.note.isReadOnly());
    assertFalse(dialog.upload.isVisible());
    assertFalse(dialog.remove.isVisible());
    assertFalse(dialog.save.isVisible());
    assertFalse(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
    List<ProtocolFile> files = items(dialog.files);
    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertEquals(2, file.getId());
  }

  @Test
  public void setProtocolId_Deletable() {
    when(service.isDeletable(any())).thenReturn(true);
    Protocol protocol = repository.findById(1L).orElseThrow();

    dialog.setProtocolId(1L);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, protocol.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_MESSAGE, protocol.getName()),
        dialog.confirm.getElement().getProperty("message"));
    assertEquals(protocol.getName(), dialog.name.getValue());
    assertFalse(dialog.name.isReadOnly());
    assertEquals(protocol.getNote(), dialog.note.getValue());
    assertFalse(dialog.note.isReadOnly());
    assertTrue(dialog.upload.isVisible());
    assertTrue(dialog.remove.isVisible());
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertTrue(dialog.delete.isVisible());
    List<ProtocolFile> files = items(dialog.files);
    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertEquals(1, file.getId());
  }

  @Test
  public void setProtocolId_DeletedFiles() {
    Protocol protocol = repository.findById(3L).orElseThrow();

    dialog.setProtocolId(3L);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, protocol.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_MESSAGE, protocol.getName()),
        dialog.confirm.getElement().getProperty("message"));
    assertEquals(protocol.getName(), dialog.name.getValue());
    assertTrue(dialog.name.isReadOnly());
    assertEquals(protocol.getNote() != null ? protocol.getNote() : "", dialog.note.getValue());
    assertTrue(dialog.note.isReadOnly());
    assertFalse(dialog.upload.isVisible());
    assertFalse(dialog.remove.isVisible());
    assertFalse(dialog.save.isVisible());
    assertFalse(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
    List<ProtocolFile> files = items(dialog.files);
    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertEquals(4, file.getId());
  }

  @Test
  public void setProtocolId_0() {
    dialog.setProtocolId(0);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 0), dialog.getHeaderTitle());
    assertEquals("", dialog.name.getValue());
    assertFalse(dialog.name.isReadOnly());
    assertEquals("", dialog.note.getValue());
    assertFalse(dialog.note.isReadOnly());
    assertTrue(dialog.upload.isVisible());
    assertTrue(dialog.remove.isVisible());
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
    assertEquals(0, items(dialog.files).size());
  }

  @Test
  public void save_EmptyName() {
    dialog.addSavedListener(savedListener);
    fillFields();
    dialog.name.setValue("");

    clickButton(dialog.save);

    BinderValidationStatus<Protocol> status = dialog.validateProtocol();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError = findValidationStatusByField(status,
        dialog.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(dialog.getTranslation(CONSTANTS_PREFIX + REQUIRED)),
        error.getMessage());
    verify(service, never()).save(any(), any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_NameExistsNewProtocol() {
    when(service.nameExists(any())).thenReturn(true);
    dialog.addSavedListener(savedListener);
    dialog.setProtocolId(0);
    fillFields();

    clickButton(dialog.save);

    BinderValidationStatus<Protocol> status = dialog.validateProtocol();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError = findValidationStatusByField(status,
        dialog.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(dialog.getTranslation(CONSTANTS_PREFIX + ALREADY_EXISTS)),
        error.getMessage());
    verify(service, atLeastOnce()).nameExists(name);
    verify(service, never()).save(any(), any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_NameExistsSameProtocol() {
    Protocol protocol = repository.findById(dialog.getProtocolId()).orElseThrow();
    when(service.nameExists(any())).thenReturn(true);
    dialog.addSavedListener(savedListener);
    fillFields();
    dialog.name.setValue(protocol.getName());

    clickButton(dialog.save);

    BinderValidationStatus<Protocol> status = dialog.validateProtocol();
    assertTrue(status.isOk());
    verify(service, atLeastOnce()).nameExists(protocol.getName());
    verify(service, atLeastOnce()).get(protocol.getId());
    verify(service).save(any(), any());
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, protocol.getName()),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void save_NameExistsSameProtocolDifferentCase() {
    Protocol protocol = repository.findById(dialog.getProtocolId()).orElseThrow();
    when(service.nameExists(any())).thenReturn(true);
    dialog.addSavedListener(savedListener);
    fillFields();
    dialog.name.setValue(protocol.getName().toLowerCase());

    clickButton(dialog.save);

    BinderValidationStatus<Protocol> status = dialog.validateProtocol();
    assertTrue(status.isOk());
    verify(service, atLeastOnce()).nameExists(protocol.getName());
    verify(service, atLeastOnce()).get(protocol.getId());
    verify(service).save(any(), any());
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, protocol.getName()),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void save_NameExistsDifferentProtocol() {
    Protocol protocol = repository.findById(dialog.getProtocolId()).orElseThrow();
    when(service.nameExists(any())).thenReturn(true);
    dialog.addSavedListener(savedListener);
    fillFields();

    clickButton(dialog.save);

    BinderValidationStatus<Protocol> status = dialog.validateProtocol();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError = findValidationStatusByField(status,
        dialog.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(dialog.getTranslation(CONSTANTS_PREFIX + ALREADY_EXISTS)),
        error.getMessage());
    verify(service, atLeastOnce()).nameExists(name);
    verify(service, atLeastOnce()).get(protocol.getId());
    verify(service, never()).save(any(), any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_EmptyNote() {
    Protocol protocol = repository.findById(dialog.getProtocolId()).orElseThrow();
    dialog.addSavedListener(savedListener);
    fillFields();
    dialog.note.setValue("");

    clickButton(dialog.save);

    BinderValidationStatus<Protocol> status = dialog.validateProtocol();
    assertTrue(status.isOk());
    verify(service).save(any(), any());
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, protocol.getName()),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void save_NoFile() {
    dialog.addSavedListener(savedListener);
    fillFields();
    dialog.files.setItems(new ArrayList<>());

    clickButton(dialog.save);

    dialog.filesError.setVisible(true);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + FILES_REQUIRED),
        dialog.filesError.getText());
    verify(service, never()).save(any(), any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_NoFileErrorClear() {
    dialog.addSavedListener(savedListener);
    fillFields();
    dialog.files.setItems(new ArrayList<>());
    clickButton(dialog.save);
    fillFields();
    clickButton(dialog.save);

    dialog.filesError.setVisible(false);
  }

  @Test
  public void save_NewProtocol() {
    dialog.addSavedListener(savedListener);
    dialog.setProtocolId(0);
    fillFields();

    clickButton(dialog.save);

    verify(service).save(protocolCaptor.capture(), filesCaptor.capture());
    Protocol protocol = protocolCaptor.getValue();
    assertEquals(0, protocol.getId());
    assertEquals(name, protocol.getName());
    assertEquals(note, protocol.getNote());
    assertTrue(LocalDateTime.now().minusMinutes(1).isBefore(protocol.getCreationDate())
        && LocalDateTime.now().plusMinutes(1).isAfter(protocol.getCreationDate()));
    assertEquals(authenticatedUser.getUser().orElseThrow(), protocol.getOwner());
    List<ProtocolFile> files = new ArrayList<>(filesCaptor.getValue());
    assertEquals(1, files.size());
    ProtocolFile file = files.get(0);
    assertEquals(0, file.getId());
    assertEquals(filename, file.getFilename());
    assertArrayEquals(fileContent, file.getContent());
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, protocol.getName()),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void save_UpdateProtocol() throws Throwable {
    dialog.addSavedListener(savedListener);
    fillFields();

    clickButton(dialog.save);

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
    byte[] fileContent = Files.readAllBytes(Paths.get(
        Objects.requireNonNull(getClass().getResource("/protocol/FLAG_Protocol.docx")).toURI()));
    assertArrayEquals(fileContent, file.getContent());
    file = files.get(1);
    assertEquals(0, file.getId());
    assertEquals(filename, file.getFilename());
    assertArrayEquals(this.fileContent, file.getContent());
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, protocol.getName()),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void save_UpdateProtocolRemoveFile() {
    dialog.addSavedListener(savedListener);
    fillFields();
    ProtocolFile removeFile = dialog.files.getListDataView().getItems().toList().get(0);
    dialog.files.getListDataView().removeItem(removeFile);

    clickButton(dialog.save);

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
    assertEquals(0, file.getId());
    assertEquals(filename, file.getFilename());
    assertArrayEquals(this.fileContent, file.getContent());
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, protocol.getName()),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void cancel() {
    clickButton(dialog.cancel);
    assertFalse(dialog.isOpened());
  }

  @Test
  public void delete_Confirm() {
    Protocol protocol = repository.findById(dialog.getProtocolId()).orElseThrow();
    dialog.addDeletedListener(deletedListener);

    clickButton(dialog.delete);

    assertTrue(dialog.confirm.isOpened());
    ConfirmDialog.ConfirmEvent event = new ConfirmDialog.ConfirmEvent(dialog.confirm, false);
    fireEvent(dialog.confirm, event);
    verify(service, never()).save(any(), any());
    verify(service).delete(protocol);
    assertFalse(dialog.isOpened());
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETED, protocol.getName()),
        test(notification).getText());
    verify(deletedListener).onComponentEvent(any());
  }

  @Test
  public void delete_Cancel() {
    dialog.addDeletedListener(deletedListener);

    clickButton(dialog.delete);

    assertTrue(dialog.confirm.isOpened());
    ConfirmDialog.CancelEvent event = new ConfirmDialog.CancelEvent(dialog.confirm, false);
    fireEvent(dialog.confirm, event);
    verify(service, never()).save(any(), any());
    verify(service, never()).delete(any());
    assertTrue(dialog.isOpened());
    assertFalse($(Notification.class).exists());
    verify(deletedListener, never()).onComponentEvent(any());
  }
}
