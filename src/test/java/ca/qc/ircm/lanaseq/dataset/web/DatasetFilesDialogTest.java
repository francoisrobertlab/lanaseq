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

package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILENAME;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.id;
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
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.EditableFile;
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
import java.io.File;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
@WithMockUser
public class DatasetFilesDialogTest extends AbstractKaribuTestCase {
  @Autowired
  private DatasetFilesDialog dialog;
  @MockBean
  private DatasetFilesDialogPresenter presenter;
  @Mock
  private Dataset dataset;
  @Captor
  private ArgumentCaptor<ValueProvider<EditableFile, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Button, EditableFile>> buttonRendererCaptor;
  @Captor
  private ArgumentCaptor<EditorCloseListener<EditableFile>> closeListenerCaptor;
  @Mock
  private ComponentEventListener<SavedEvent<DatasetDialog>> savedListener;
  @Mock
  private ComponentEventListener<DeletedEvent<DatasetDialog>> deletedListener;
  @Autowired
  private DatasetRepository datasetRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetFilesDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<File> files = new ArrayList<>();

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    ui.setLocale(locale);
    files.add(new File("dataset", "dataset_R1.fastq"));
    files.add(new File("dataset", "dataset_R2.fastq"));
    files.add(new File("dataset", "dataset.bw"));
    files.add(new File("dataset", "dataset.png"));
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
    verify(dialog.delete).setHeader(webResources.message(DELETE));
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    mockColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(DatasetFilesDialog.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    ui.setLocale(locale);
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
    ValueProvider<EditableFile, String> valueProvider = valueProviderCaptor.getValue();
    for (File file : files) {
      EditableFile efile = new EditableFile(file);
      assertEquals(efile.getFilename(), valueProvider.apply(efile));
    }
    verify(dialog.filename).setEditorComponent(dialog.filenameEdit);
    verify(dialog.files).addColumn(buttonRendererCaptor.capture(), eq(DELETE));
    ComponentRenderer<Button, EditableFile> buttonRenderer = buttonRendererCaptor.getValue();
    for (File file : files) {
      EditableFile efile = new EditableFile(file);
      Button button = buttonRenderer.createComponent(efile);
      assertTrue(button.hasClassName(DELETE));
      assertTrue(button.hasThemeName(ButtonVariant.LUMO_ERROR.getVariantName()));
      validateIcon(VaadinIcon.TRASH.create(), button.getIcon());
      assertEquals("", button.getText());
      button.click();
      verify(presenter).deleteFile(efile);
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
  public void getDataset() {
    when(presenter.getDataset()).thenReturn(dataset);
    assertEquals(dataset, dialog.getDataset());
    verify(presenter).getDataset();
  }

  @Test
  public void setDataset_NewDataset() {
    Dataset dataset = new Dataset();
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(dataset);

    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER), dialog.header.getText());
  }

  @Test
  public void setDataset_NewDatasetWithName() {
    Dataset dataset = new Dataset();
    dataset.setName("my dataset");
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(dataset);

    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER, dataset.getName()), dialog.header.getText());
  }

  @Test
  public void setDataset_Dataset() {
    Dataset dataset = datasetRepository.findById(2L).get();
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(dataset);

    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER, dataset.getName()), dialog.header.getText());
  }

  @Test
  public void setDataset_BeforeLocaleChange() {
    Dataset dataset = datasetRepository.findById(2L).get();
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.setDataset(dataset);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER, dataset.getName()), dialog.header.getText());
  }

  @Test
  public void setDataset_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(null);

    verify(presenter).setDataset(null);
    assertEquals(resources.message(HEADER), dialog.header.getText());
  }

  @Test
  public void add() {
    dialog.add.click();

    verify(presenter).add();
  }
}
