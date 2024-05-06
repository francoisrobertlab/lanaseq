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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link ProtocolHistoryDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("lanaseq@ircm.qc.ca")
public class ProtocolHistoryDialogTest extends SpringUIUnitTest {
  private ProtocolHistoryDialog dialog;
  @MockBean
  private ProtocolService service;
  @Mock
  private ComponentEventListener<SavedEvent<ProtocolDialog>> savedListener;
  @Autowired
  private ProtocolRepository repository;
  @Autowired
  private ProtocolFileRepository fileRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(ProtocolHistoryDialog.class, locale);
  private AppResources protocolFileResources = new AppResources(ProtocolFile.class, locale);
  private List<ProtocolFile> protocolFiles;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    protocolFiles = fileRepository.findAll();
    when(service.all()).thenReturn(repository.findAll());
    when(service.get(any())).then(
        i -> i.getArgument(0) != null ? repository.findById(i.getArgument(0)) : Optional.empty());
    when(service.deletedFiles(any())).then(i -> {
      Protocol protocol = i.getArgument(0);
      if (protocol != null && protocol.getId() != null) {
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
    Protocol protocol = repository.findById(dialog.getProtocolId()).get();
    assertEquals(resources.message(HEADER, protocol.getName()), dialog.getHeaderTitle());
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(protocolFileResources.message(FILENAME),
        headerRow.getCell(dialog.filename).getText());
    assertEquals(resources.message(RECOVER), headerRow.getCell(dialog.recover).getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(ProtocolHistoryDialog.class, locale);
    final AppResources protocolFileResources = new AppResources(ProtocolFile.class, locale);
    UI.getCurrent().setLocale(locale);
    Protocol protocol = repository.findById(dialog.getProtocolId()).get();
    assertEquals(resources.message(HEADER, protocol.getName()), dialog.getHeaderTitle());
    HeaderRow headerRow = dialog.files.getHeaderRows().get(0);
    assertEquals(protocolFileResources.message(FILENAME),
        headerRow.getCell(dialog.filename).getText());
    assertEquals(resources.message(RECOVER), headerRow.getCell(dialog.recover).getText());
  }

  @Test
  public void files() {
    assertEquals(2, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
    assertNotNull(dialog.files.getColumnByKey(RECOVER));
    assertTrue(dialog.files.getSelectionModel() instanceof SelectionModel.Single);
  }

  @Test
  public void files_ColumnsValueProvider() {
    for (ProtocolFile file : protocolFiles) {
      ComponentRenderer<Anchor, ProtocolFile> filenameRenderer =
          (ComponentRenderer<Anchor, ProtocolFile>) dialog.filename.getRenderer();
      Anchor anchor = filenameRenderer.createComponent(file);
      assertEquals(file.getFilename(), anchor.getText());
      assertEquals(file.getFilename(), anchor.getElement().getAttribute("download"));
      assertTrue(anchor.getHref().startsWith("VAADIN/dynamic/resource"));
      LitRenderer<ProtocolFile> recoverRenderer =
          (LitRenderer<ProtocolFile>) dialog.recover.getRenderer();
      assertEquals(RECOVER_BUTTON, rendererTemplate(recoverRenderer));
      assertTrue(functions(recoverRenderer).containsKey("recoverFile"));
      assertNotNull(functions(recoverRenderer).get("recoverFile"));
    }
  }

  @Test
  public void files_RecoverFile() {
    ProtocolFile file = dialog.files.getListDataView().getItems().findFirst().get();
    LitRenderer<ProtocolFile> recoverRenderer =
        (LitRenderer<ProtocolFile>) dialog.recover.getRenderer();
    functions(recoverRenderer).get("recoverFile").accept(file, null);
    verify(service).recover(file);
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(RECOVERED, file.getFilename()), test(notification).getText());
    dialog.showNotification(resources.message(RECOVERED, file.getFilename()));
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
    Protocol protocol = repository.findById(3L).get();

    dialog.setProtocolId(3L);

    assertEquals(resources.message(HEADER, protocol.getName()), dialog.getHeaderTitle());
    assertEquals(1, dialog.files.getListDataView().getItemCount());
  }

  @Test
  public void setProtocol_NoFiles() {
    Protocol protocol = repository.findById(2L).get();

    dialog.setProtocolId(2L);

    assertEquals(resources.message(HEADER, protocol.getName()), dialog.getHeaderTitle());
    assertEquals(0, dialog.files.getListDataView().getItemCount());
  }

  @Test
  public void setProtocol_Null() {
    assertThrows(NoSuchElementException.class, () -> {
      dialog.setProtocolId(null);
    });
  }
}
