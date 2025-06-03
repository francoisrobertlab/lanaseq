package ca.qc.ircm.lanaseq.protocol.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.protocol.ProtocolFileProperties.FILENAME;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.FILES;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.HEADER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.ID;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.RECOVER;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.RECOVERED;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.RECOVER_BUTTON;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.functions;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.rendererTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolFileRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.testbench.unit.MetaKeys;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link ProtocolHistoryDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class ProtocolHistoryDialogTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(ProtocolHistoryDialog.class);
  private static final String PROTOCOL_FILE_PREFIX = messagePrefix(ProtocolFile.class);
  private ProtocolHistoryDialog dialog;
  @MockitoBean
  private ProtocolService service;
  @Mock
  private ComponentEventListener<SavedEvent<ProtocolDialog>> savedListener;
  @Autowired
  private ProtocolRepository repository;
  @Autowired
  private ProtocolFileRepository fileRepository;
  private final Locale locale = Locale.ENGLISH;
  private List<ProtocolFile> protocolFiles;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    protocolFiles = fileRepository.findAll();
    when(service.all()).thenReturn(repository.findAll());
    when(service.get(anyLong())).then(i -> repository.findById(i.getArgument(0)));
    when(service.deletedFiles(any())).then(i -> {
      Protocol protocol = i.getArgument(0);
      if (protocol != null && protocol.getId() != 0) {
        return fileRepository.findByProtocolAndDeletedTrue(protocol);
      } else {
        return new ArrayList<>();
      }
    });
    UI.getCurrent().setLocale(locale);
    ProtocolsView view = navigate(ProtocolsView.class);
    test(view.protocols).clickRow(2, new MetaKeys().alt());
    dialog = $(ProtocolHistoryDialog.class).first();
  }

  private ProtocolFile filename(String filename) {
    ProtocolFile file = new ProtocolFile();
    file.setFilename(filename);
    return file;
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(FILES), dialog.files.getId().orElse(""));
  }

  @Test
  public void labels() {
    Protocol protocol = repository.findById(dialog.getProtocolId()).orElseThrow();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, protocol.getName()),
        dialog.getHeaderTitle());
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(PROTOCOL_FILE_PREFIX + FILENAME),
        headerRow.getCell(dialog.filename).getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + RECOVER),
        headerRow.getCell(dialog.recover).getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    Protocol protocol = repository.findById(dialog.getProtocolId()).orElseThrow();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, protocol.getName()),
        dialog.getHeaderTitle());
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(PROTOCOL_FILE_PREFIX + FILENAME),
        headerRow.getCell(dialog.filename).getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + RECOVER),
        headerRow.getCell(dialog.recover).getText());
  }

  @Test
  public void files() {
    assertEquals(2, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
    assertNotNull(dialog.files.getColumnByKey(RECOVER));
    assertInstanceOf(SelectionModel.Single.class, dialog.files.getSelectionModel());
  }

  @Test
  public void files_ColumnsValueProvider() {
    for (ProtocolFile file : protocolFiles) {
      ComponentRenderer<Anchor, ProtocolFile> filenameRenderer = (ComponentRenderer<Anchor, ProtocolFile>) dialog.filename.getRenderer();
      Anchor anchor = filenameRenderer.createComponent(file);
      assertEquals(file.getFilename(), anchor.getText());
      assertEquals(file.getFilename(), anchor.getElement().getAttribute("download"));
      assertTrue(anchor.getHref().startsWith("VAADIN/dynamic/resource"));
      LitRenderer<ProtocolFile> recoverRenderer = (LitRenderer<ProtocolFile>) dialog.recover.getRenderer();
      assertEquals(RECOVER_BUTTON, rendererTemplate(recoverRenderer));
      assertTrue(functions(recoverRenderer).containsKey("recoverFile"));
      assertNotNull(functions(recoverRenderer).get("recoverFile"));
    }
  }

  @Test
  public void files_RecoverFile() {
    ProtocolFile file = dialog.files.getListDataView().getItems().findFirst().orElseThrow();
    LitRenderer<ProtocolFile> recoverRenderer = (LitRenderer<ProtocolFile>) dialog.recover.getRenderer();
    functions(recoverRenderer).get("recoverFile").accept(file, null);
    verify(service).recover(file);
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + RECOVERED, file.getFilename()),
        test(notification).getText());
    dialog.showNotification(dialog.getTranslation(MESSAGE_PREFIX + RECOVERED, file.getFilename()));
    assertTrue(items(dialog.files).isEmpty());
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
  public void getProtocolId() {
    assertEquals(3L, dialog.getProtocolId());
  }

  @Test
  public void setProtocolId_Protocol() {
    Protocol protocol = repository.findById(3L).orElseThrow();

    dialog.setProtocolId(3L);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, protocol.getName()),
        dialog.getHeaderTitle());
    assertEquals(1, dialog.files.getListDataView().getItemCount());
  }

  @Test
  public void setProtocol_NoFiles() {
    Protocol protocol = repository.findById(2L).orElseThrow();

    dialog.setProtocolId(2L);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, protocol.getName()),
        dialog.getHeaderTitle());
    assertEquals(0, dialog.files.getListDataView().getItemCount());
  }

  @Test
  public void setProtocol_0() {
    assertThrows(NoSuchElementException.class, () -> dialog.setProtocolId(0));
  }
}
