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
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.RECOVER_BUTTON;
import static ca.qc.ircm.lanaseq.protocol.web.ProtocolHistoryDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.functions;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.rendererTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolFile;
import ca.qc.ircm.lanaseq.protocol.ProtocolFileRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link ProtocolHistoryDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ProtocolHistoryDialogTest extends AbstractKaribuTestCase {
  private ProtocolHistoryDialog dialog;
  @Mock
  private ProtocolHistoryDialogPresenter presenter;
  @Mock
  private ComponentEventListener<SavedEvent<ProtocolDialog>> savedListener;
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
  private AppResources resources = new AppResources(ProtocolHistoryDialog.class, locale);
  private AppResources protocolFileResources = new AppResources(ProtocolFile.class, locale);
  @Mock
  private Protocol protocol;
  private List<ProtocolFile> protocolFiles;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    dialog = new ProtocolHistoryDialog(presenter);
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
    dialog.recover = mock(Column.class);
    when(dialog.files.addColumn(any(LitRenderer.class), eq(RECOVER))).thenReturn(dialog.recover);
    when(dialog.recover.setKey(any())).thenReturn(dialog.recover);
    when(dialog.recover.setHeader(any(String.class))).thenReturn(dialog.recover);
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(dialog);
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(HEADER), dialog.header.getId().orElse(""));
    assertEquals(id(FILES), dialog.files.getId().orElse(""));
  }

  @Test
  public void labels() {
    mockColumns();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, ""), dialog.header.getText());
    verify(dialog.filename).setHeader(protocolFileResources.message(FILENAME));
    verify(dialog.recover).setHeader(resources.message(RECOVER));
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    mockColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(ProtocolHistoryDialog.class, locale);
    final AppResources protocolFileResources = new AppResources(ProtocolFile.class, locale);
    ui.setLocale(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, ""), dialog.header.getText());
    verify(dialog.filename).setHeader(protocolFileResources.message(FILENAME));
    verify(dialog.recover).setHeader(resources.message(RECOVER));
    verify(presenter).localeChange(locale);
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
    verify(dialog.files).addColumn(litRendererCaptor.capture(), eq(RECOVER));
    LitRenderer<ProtocolFile> litRenderer = litRendererCaptor.getValue();
    for (ProtocolFile file : protocolFiles) {
      assertEquals(RECOVER_BUTTON, rendererTemplate(litRenderer));
      assertTrue(functions(litRenderer).containsKey("recoverFile"));
      functions(litRenderer).get("recoverFile").accept(file, null);
      verify(presenter).recoverFile(file);
    }
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
    assertEquals(resources.message(HEADER, ""), dialog.header.getText());
  }

  @Test
  public void setProtocol_Protocol() {
    Protocol protocol = repository.findById(2L).get();
    when(presenter.getProtocol()).thenReturn(protocol);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setProtocol(protocol);

    verify(presenter).setProtocol(protocol);
    assertEquals(resources.message(HEADER, protocol.getName()), dialog.header.getText());
  }

  @Test
  public void setProtocol_BeforeLocaleChange() {
    Protocol protocol = repository.findById(2L).get();
    when(presenter.getProtocol()).thenReturn(protocol);

    dialog.setProtocol(protocol);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(presenter).setProtocol(protocol);
    assertEquals(resources.message(HEADER, protocol.getName()), dialog.header.getText());
  }

  @Test
  public void setProtocol_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setProtocol(null);

    verify(presenter).setProtocol(null);
    assertEquals(resources.message(HEADER, ""), dialog.header.getText());
  }
}
