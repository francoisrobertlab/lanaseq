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

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.DOWNLOAD;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILENAME;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.HEADER;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.ID;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.EditableFile;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.grid.editor.EditorCloseEvent;
import com.vaadin.flow.component.grid.editor.EditorCloseListener;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.server.StreamResource;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

@ServiceTestAnnotations
@WithMockUser
public class SampleFilesDialogTest extends AbstractKaribuTestCase {
  @Autowired
  private SampleFilesDialog dialog;
  @MockBean
  private SampleFilesDialogPresenter presenter;
  @Mock
  private Sample sample;
  @Captor
  private ArgumentCaptor<ValueProvider<EditableFile, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Anchor, EditableFile>> anchorRendererCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Button, EditableFile>> buttonRendererCaptor;
  @Captor
  private ArgumentCaptor<EditorCloseListener<EditableFile>> closeListenerCaptor;
  @Mock
  private ComponentEventListener<SavedEvent<SampleDialog>> savedListener;
  @Mock
  private ComponentEventListener<DeletedEvent<SampleDialog>> deletedListener;
  @Autowired
  private SampleRepository sampleRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SampleFilesDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<File> files = new ArrayList<>();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    files.add(new File("sample", "sample_R1.fastq"));
    files.add(new File("sample", "sample_R2.fastq"));
    files.add(new File("sample", "sample.bw"));
    files.add(new File("sample", "sample.png"));
    when(presenter.download(any())).thenAnswer(i -> {
      EditableFile efile = i.getArgument(0);
      return new StreamResource(efile.getFile().getName(), (output, session) -> {
      });
    });
  }

  @SuppressWarnings("unchecked")
  private void mockColumns() {
    Element filesElement = dialog.files.getElement();
    dialog.files = mock(Grid.class);
    when(dialog.files.getEditor()).thenReturn(mock(Editor.class));
    when(dialog.files.getElement()).thenReturn(filesElement);
    dialog.filename = mock(Column.class);
    when(dialog.files.addColumn(any(ValueProvider.class), eq(FILENAME)))
        .thenReturn(dialog.filename);
    when(dialog.filename.setKey(any())).thenReturn(dialog.filename);
    when(dialog.filename.setSortable(anyBoolean())).thenReturn(dialog.filename);
    when(dialog.filename.setComparator(any(Comparator.class))).thenReturn(dialog.filename);
    when(dialog.filename.setWidth(any())).thenReturn(dialog.filename);
    when(dialog.filename.setHeader(any(String.class))).thenReturn(dialog.filename);
    dialog.download = mock(Column.class);
    when(dialog.files.addColumn(any(ComponentRenderer.class), eq(DOWNLOAD)))
        .thenReturn(dialog.download);
    when(dialog.download.setKey(any())).thenReturn(dialog.download);
    when(dialog.download.setSortable(anyBoolean())).thenReturn(dialog.download);
    when(dialog.download.setComparator(any(Comparator.class))).thenReturn(dialog.download);
    when(dialog.download.setHeader(any(String.class))).thenReturn(dialog.download);
    dialog.delete = mock(Column.class);
    when(dialog.files.addColumn(any(ComponentRenderer.class), eq(DELETE)))
        .thenReturn(dialog.delete);
    when(dialog.delete.setKey(any())).thenReturn(dialog.delete);
    when(dialog.delete.setSortable(anyBoolean())).thenReturn(dialog.delete);
    when(dialog.delete.setComparator(any(Comparator.class))).thenReturn(dialog.delete);
    when(dialog.delete.setHeader(any(String.class))).thenReturn(dialog.delete);
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(dialog);
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(HEADER), dialog.header.getId().orElse(""));
    assertEquals(id(MESSAGE), dialog.message.getId().orElse(""));
    assertEquals(id(FILES), dialog.files.getId().orElse(""));
    assertEquals(id(FILENAME), dialog.filenameEdit.getId().orElse(""));
    assertEquals(id(ADD), dialog.add.getId().orElse(""));
    validateIcon(VaadinIcon.PLUS.create(), dialog.add.getIcon());
  }

  @Test
  public void labels() {
    mockColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), dialog.header.getText());
    verify(dialog.filename).setHeader(resources.message(FILENAME));
    verify(dialog.download).setHeader(webResources.message(DOWNLOAD));
    verify(dialog.delete).setHeader(webResources.message(DELETE));
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    mockColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(SampleFilesDialog.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    ui.setLocale(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), dialog.header.getText());
    verify(dialog.filename).setHeader(resources.message(FILENAME));
    verify(dialog.download).setHeader(webResources.message(DOWNLOAD));
    verify(dialog.delete).setHeader(webResources.message(DELETE));
    verify(presenter).localeChange(locale);
  }

  @Test
  public void files() {
    assertEquals(3, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
    assertTrue(dialog.files.getColumnByKey(FILENAME).isSortable());
    assertNotNull(dialog.files.getColumnByKey(DOWNLOAD));
    assertFalse(dialog.files.getColumnByKey(DOWNLOAD).isSortable());
    assertNotNull(dialog.files.getColumnByKey(DELETE));
    assertFalse(dialog.files.getColumnByKey(DELETE).isSortable());
  }

  @Test
  public void files_ColumnsValueProvider() {
    mockColumns();
    dialog.init();
    verify(dialog.files).addColumn(valueProviderCaptor.capture(), eq(FILENAME));
    ValueProvider<EditableFile, String> valueProvider = valueProviderCaptor.getValue();
    for (File path : files) {
      EditableFile file = new EditableFile(path);
      assertEquals(file.getFilename(), valueProvider.apply(file));
    }
    verify(dialog.filename).setEditorComponent(dialog.filenameEdit);
    verify(dialog.files).addColumn(anchorRendererCaptor.capture(), eq(DOWNLOAD));
    ComponentRenderer<Anchor, EditableFile> anchorRenderer = anchorRendererCaptor.getValue();
    for (File path : files) {
      EditableFile file = new EditableFile(path);
      Anchor anchor = anchorRenderer.createComponent(file);
      assertTrue(anchor.hasClassName(DOWNLOAD));
      assertTrue(anchor.getElement().hasAttribute("download"));
      assertEquals("", anchor.getElement().getAttribute("download"));
      assertNotEquals("", anchor.getHref());
      assertEquals(1, anchor.getChildren().toArray().length);
      Component child = anchor.getChildren().findFirst().get();
      assertTrue(child instanceof Button);
      Button button = (Button) child;
      validateIcon(VaadinIcon.DOWNLOAD.create(), button.getIcon());
      assertEquals("", button.getText());
      verify(presenter).download(file);
    }
    verify(dialog.files).addColumn(buttonRendererCaptor.capture(), eq(DELETE));
    ComponentRenderer<Button, EditableFile> buttonRenderer = buttonRendererCaptor.getValue();
    for (File path : files) {
      EditableFile file = new EditableFile(path);
      Button button = buttonRenderer.createComponent(file);
      assertTrue(button.hasClassName(DELETE));
      assertTrue(button.hasThemeName(ButtonVariant.LUMO_ERROR.getVariantName()));
      validateIcon(VaadinIcon.TRASH.create(), button.getIcon());
      assertEquals("", button.getText());
      button.click();
      verify(presenter).deleteFile(file);
    }
  }

  @Test
  public void renameFile() {
    mockColumns();
    dialog.init();
    EditableFile file = new EditableFile(files.get(0));
    verify(dialog.files.getEditor()).addCloseListener(closeListenerCaptor.capture());
    EditorCloseListener<EditableFile> listener = closeListenerCaptor.getValue();
    listener.onEditorClose(new EditorCloseEvent<>(dialog.files.getEditor(), file));
    verify(presenter).rename(file);
  }

  @Test
  public void getSample() {
    when(presenter.getSample()).thenReturn(sample);
    assertEquals(sample, dialog.getSample());
    verify(presenter).getSample();
  }

  @Test
  public void setSample_NewSample() {
    Sample sample = new Sample();
    when(presenter.getSample()).thenReturn(sample);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setSample(sample);

    verify(presenter).setSample(sample);
    assertEquals(resources.message(HEADER), dialog.header.getText());
  }

  @Test
  public void setSample_NewSampleWithName() {
    Sample sample = new Sample();
    sample.setName("my sample");
    when(presenter.getSample()).thenReturn(sample);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setSample(sample);

    verify(presenter).setSample(sample);
    assertEquals(resources.message(HEADER, sample.getName()), dialog.header.getText());
  }

  @Test
  public void setSample_Sample() {
    Sample sample = sampleRepository.findById(2L).get();
    when(presenter.getSample()).thenReturn(sample);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setSample(sample);

    verify(presenter).setSample(sample);
    assertEquals(resources.message(HEADER, sample.getName()), dialog.header.getText());
  }

  @Test
  public void setSample_BeforeLocaleChange() {
    Sample sample = sampleRepository.findById(2L).get();
    when(presenter.getSample()).thenReturn(sample);

    dialog.setSample(sample);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(presenter).setSample(sample);
    assertEquals(resources.message(HEADER, sample.getName()), dialog.header.getText());
  }

  @Test
  public void setSample_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setSample(null);

    verify(presenter).setSample(null);
    assertEquals(resources.message(HEADER), dialog.header.getText());
  }

  @Test
  public void add() {
    dialog.add.click();

    verify(presenter).add();
  }
}
