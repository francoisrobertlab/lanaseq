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
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILENAME;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.HEADER;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.ID;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.SampleFile;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.grid.editor.EditorCloseEvent;
import com.vaadin.flow.component.grid.editor.EditorCloseListener;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class SampleFilesDialogTest extends AbstractViewTestCase {
  private SampleFilesDialog dialog;
  @Mock
  private SampleFilesDialogPresenter presenter;
  @Mock
  private AddSampleFilesDialog addFilesDialog;
  @Mock
  private Sample sample;
  @Captor
  private ArgumentCaptor<ValueProvider<SampleFile, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Button, SampleFile>> buttonRendererCaptor;
  @Captor
  private ArgumentCaptor<EditorCloseListener<SampleFile>> closeListenerCaptor;
  @Mock
  private ComponentEventListener<SavedEvent<SampleDialog>> savedListener;
  @Mock
  private ComponentEventListener<DeletedEvent<SampleDialog>> deletedListener;
  @Autowired
  private SampleRepository sampleRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SampleFilesDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Path> files = new ArrayList<>();

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    dialog = new SampleFilesDialog(addFilesDialog, presenter);
    dialog.init();
    files.add(Paths.get("sample", "sample_R1.fastq"));
    files.add(Paths.get("sample", "sample_R2.fastq"));
    files.add(Paths.get("sample", "sample.bw"));
    files.add(Paths.get("sample", "sample.png"));
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
    when(dialog.filename.setComparator(any(Comparator.class))).thenReturn(dialog.filename);
    when(dialog.filename.setWidth(any())).thenReturn(dialog.filename);
    when(dialog.filename.setHeader(any(String.class))).thenReturn(dialog.filename);
    dialog.delete = mock(Column.class);
    when(dialog.files.addColumn(any(ComponentRenderer.class), eq(DELETE)))
        .thenReturn(dialog.delete);
    when(dialog.delete.setKey(any())).thenReturn(dialog.delete);
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
    when(ui.getLocale()).thenReturn(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), dialog.header.getText());
    verify(dialog.filename).setHeader(resources.message(FILENAME));
    verify(dialog.delete).setHeader(webResources.message(DELETE));
    verify(presenter).localeChange(locale);
  }

  @Test
  public void files() {
    assertEquals(2, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
    assertNotNull(dialog.files.getColumnByKey(DELETE));
  }

  @Test
  public void files_ColumnsValueProvider() {
    mockColumns();
    dialog.init();
    verify(dialog.files).addColumn(valueProviderCaptor.capture(), eq(FILENAME));
    ValueProvider<SampleFile, String> valueProvider = valueProviderCaptor.getValue();
    for (Path path : files) {
      SampleFile file = new SampleFile(path);
      assertEquals(file.getFilename(), valueProvider.apply(file));
    }
    verify(dialog.filename).setEditorComponent(dialog.filenameEdit);
    verify(dialog.files).addColumn(buttonRendererCaptor.capture(), eq(DELETE));
    ComponentRenderer<Button, SampleFile> buttonRenderer = buttonRendererCaptor.getValue();
    for (Path path : files) {
      SampleFile file = new SampleFile(path);
      Button button = buttonRenderer.createComponent(file);
      assertTrue(button.hasClassName(DELETE));
      assertTrue(button.hasThemeName(ButtonVariant.LUMO_ERROR.getVariantName()));
      validateIcon(VaadinIcon.TRASH.create(), button.getIcon());
      assertEquals("", button.getText());
      button.click();
      verify(presenter).deleteFile(file, locale);
    }
  }

  @Test
  public void renameFile() {
    mockColumns();
    dialog.init();
    SampleFile file = new SampleFile(files.get(0));
    verify(dialog.files.getEditor()).addCloseListener(closeListenerCaptor.capture());
    EditorCloseListener<SampleFile> listener = closeListenerCaptor.getValue();
    listener.onEditorClose(new EditorCloseEvent<>(dialog.files.getEditor(), file));
    verify(presenter).rename(file, locale);
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
