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

import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.DOWNLOAD;
import static ca.qc.ircm.lanaseq.Constants.UPLOAD;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.SAMPLES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.ADD_LARGE_FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILENAME;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FILE_COUNT;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.FOLDERS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MAXIMUM_SMALL_FILES_COUNT;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MAXIMUM_SMALL_FILES_SIZE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetFilesDialog.id;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateEquals;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.englishUploadI18N;
import static ca.qc.ircm.lanaseq.web.UploadInternationalization.frenchUploadI18N;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog;
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
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.server.StreamResource;
import java.io.ByteArrayInputStream;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link DatasetFilesDialog}.
 */
@ServiceTestAnnotations
@WithMockUser
public class DatasetFilesDialogTest extends AbstractKaribuTestCase {
  private DatasetFilesDialog dialog;
  @MockBean
  private DatasetFilesDialogPresenter presenter;
  @MockBean
  private AddDatasetFilesDialog addFilesDialog;
  @MockBean
  private SampleFilesDialog sampleFilesDialog;
  @Mock
  private Dataset dataset;
  @Captor
  private ArgumentCaptor<ValueProvider<EditableFile, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Anchor, EditableFile>> anchorRendererCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Button, EditableFile>> buttonRendererCaptor;
  @Captor
  private ArgumentCaptor<EditorCloseListener<EditableFile>> closeListenerCaptor;
  @Captor
  private ArgumentCaptor<ValueProvider<Sample, String>> sampleNameProviderCaptor;
  @Captor
  private ArgumentCaptor<ValueProvider<Sample, Integer>> sampleFileCountProviderCaptor;
  @Mock
  private ComponentEventListener<SavedEvent<DatasetDialog>> savedListener;
  @Mock
  private ComponentEventListener<DeletedEvent<DatasetDialog>> deletedListener;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private SampleRepository sampleRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetFilesDialog.class, locale);
  private AppResources sampleResources = new AppResources(Sample.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<File> files = new ArrayList<>();
  private List<Sample> samples = new ArrayList<>();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    dialog = new DatasetFilesDialog(presenter, addFilesDialog, sampleFilesDialog);
    dialog.init();
    files.add(new File("dataset", "dataset_R1.fastq"));
    files.add(new File("dataset", "dataset_R2.fastq"));
    files.add(new File("dataset", "dataset.bw"));
    files.add(new File("dataset", "dataset.png"));
    samples = sampleRepository.findAll();
    when(presenter.download(any())).thenAnswer(i -> {
      EditableFile efile = i.getArgument(0);
      return new StreamResource(efile.getFile().getName(), (output, session) -> {
      });
    });
    when(presenter.isArchive(any())).thenAnswer(i -> {
      EditableFile file = i.getArgument(0);
      return files.contains(file.getFile()) && files.indexOf(file.getFile()) >= 2;
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
    when(dialog.filename.setHeader(any(String.class))).thenReturn(dialog.filename);
    when(dialog.filename.setFlexGrow(anyInt())).thenReturn(dialog.filename);
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
    Element samplesElement = dialog.samples.getElement();
    dialog.samples = mock(Grid.class);
    when(dialog.samples.getElement()).thenReturn(samplesElement);
    dialog.name = mock(Column.class);
    when(dialog.samples.addColumn(any(ValueProvider.class), eq(NAME))).thenReturn(dialog.name);
    when(dialog.name.setKey(any())).thenReturn(dialog.name);
    when(dialog.name.setHeader(any(String.class))).thenReturn(dialog.name);
    when(dialog.name.setFlexGrow(anyInt())).thenReturn(dialog.name);
    dialog.fileCount = mock(Column.class);
    when(dialog.samples.addColumn(any(ValueProvider.class), eq(FILE_COUNT)))
        .thenReturn(dialog.fileCount);
    when(dialog.fileCount.setKey(any())).thenReturn(dialog.fileCount);
    when(dialog.fileCount.setHeader(any(String.class))).thenReturn(dialog.fileCount);
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
    assertEquals(id(FOLDERS), dialog.folders.getId().orElse(""));
    assertEquals(id(FILES), dialog.files.getId().orElse(""));
    assertEquals(id(FILENAME), dialog.filenameEdit.getId().orElse(""));
    assertEquals(id(SAMPLES), dialog.samples.getId().orElse(""));
    assertEquals(id(UPLOAD), dialog.upload.getId().orElse(""));
    assertEquals(id(ADD_LARGE_FILES), dialog.addLargeFiles.getId().orElse(""));
    validateIcon(VaadinIcon.PLUS.create(), dialog.addLargeFiles.getIcon());
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
    verify(dialog.name).setHeader(sampleResources.message(NAME));
    verify(dialog.fileCount).setHeader(resources.message(FILE_COUNT));
    validateEquals(englishUploadI18N(), dialog.upload.getI18n());
    assertEquals(resources.message(ADD_LARGE_FILES), dialog.addLargeFiles.getText());
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
    verify(dialog.download).setHeader(webResources.message(DOWNLOAD));
    verify(dialog.delete).setHeader(webResources.message(DELETE));
    verify(dialog.name).setHeader(sampleResources.message(NAME));
    verify(dialog.fileCount).setHeader(resources.message(FILE_COUNT));
    validateEquals(frenchUploadI18N(), dialog.upload.getI18n());
    assertEquals(resources.message(ADD_LARGE_FILES), dialog.addLargeFiles.getText());
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
    for (File file : files) {
      EditableFile efile = new EditableFile(file);
      assertEquals(efile.getFilename(), valueProvider.apply(efile));
    }
    verify(dialog.filename).setEditorComponent(dialog.filenameEdit);
    verify(dialog.files).addColumn(anchorRendererCaptor.capture(), eq(DOWNLOAD));
    ComponentRenderer<Anchor, EditableFile> anchorRenderer = anchorRendererCaptor.getValue();
    for (File file : files) {
      EditableFile efile = new EditableFile(file);
      Anchor anchor = anchorRenderer.createComponent(efile);
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
      verify(presenter).download(efile);
    }
    verify(dialog.files).addColumn(buttonRendererCaptor.capture(), eq(DELETE));
    ComponentRenderer<Button, EditableFile> buttonRenderer = buttonRendererCaptor.getValue();
    for (File file : files) {
      EditableFile efile = new EditableFile(file);
      Button button = buttonRenderer.createComponent(efile);
      assertTrue(button.hasClassName(DELETE));
      assertTrue(button.hasThemeName(ButtonVariant.LUMO_ERROR.getVariantName()));
      assertEquals(presenter.isArchive(efile), !button.isEnabled());
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
  public void samples() {
    assertEquals(2, dialog.samples.getColumns().size());
    assertNotNull(dialog.samples.getColumnByKey(NAME));
    assertNotNull(dialog.samples.getColumnByKey(FILE_COUNT));
  }

  @Test
  public void samples_ColumnsValueProvider() {
    mockColumns();
    dialog.init();
    verify(dialog.samples).addColumn(sampleNameProviderCaptor.capture(), eq(NAME));
    ValueProvider<Sample, String> valueProvider = sampleNameProviderCaptor.getValue();
    for (Sample sample : samples) {
      assertEquals(sample.getName(), valueProvider.apply(sample));
    }
    verify(dialog.samples).addColumn(sampleFileCountProviderCaptor.capture(), eq(FILE_COUNT));
    ValueProvider<Sample, Integer> fileCountProvider = sampleFileCountProviderCaptor.getValue();
    when(presenter.fileCount(any())).then(new Answer<Integer>() {
      int count = 0;

      @Override
      public Integer answer(InvocationOnMock invocation) throws Throwable {
        return count++;
      }
    });
    for (Sample sample : samples) {
      assertEquals((Integer) samples.indexOf(sample), fileCountProvider.apply(sample));
      verify(presenter).fileCount(sample);
    }
  }

  @Test
  public void samples_ViewFiles() {
    Sample sample = samples.get(0);
    doubleClickItem(dialog.samples, sample);
    verify(presenter).viewFiles(sample);
  }

  @Test
  public void upload() {
    assertEquals(MAXIMUM_SMALL_FILES_COUNT, dialog.upload.getMaxFiles());
    assertEquals(MAXIMUM_SMALL_FILES_SIZE, dialog.upload.getMaxFileSize());
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
    verify(presenter).addSmallFile(filename, input);
    verify(dialog.uploadBuffer).getInputStream(filename);
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
    Dataset dataset = repository.findById(2L).get();
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(dataset);

    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER, dataset.getName()), dialog.header.getText());
  }

  @Test
  public void setDataset_BeforeLocaleChange() {
    Dataset dataset = repository.findById(2L).get();
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
  public void addLargeFiles() {
    dialog.addLargeFiles.click();

    verify(presenter).addLargeFiles();
  }
}
