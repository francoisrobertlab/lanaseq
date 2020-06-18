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
import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.PLACEHOLDER;
import static ca.qc.ircm.lanaseq.Constants.REMOVE;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TAGS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.ADD_SAMPLE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETE_HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETE_MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.FILENAME;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAMPLES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.id;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ASSAY;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN_DESCRIPTION;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TARGET;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TREATMENT;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TYPE;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DatasetFile;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.sample.Assay;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleProperties;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog.CancelEvent;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog.ConfirmEvent;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.FooterRow.FooterCell;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.grid.editor.EditorCloseEvent;
import com.vaadin.flow.component.grid.editor.EditorCloseListener;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
@WithMockUser
public class DatasetDialogTest extends AbstractKaribuTestCase {
  private DatasetDialog dialog;
  @Mock
  private DatasetDialogPresenter presenter;
  @Mock
  private Dataset dataset;
  @Mock
  private Sample sample;
  @Captor
  private ArgumentCaptor<ValueProvider<Sample, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<TextField, Sample>> textFieldRendererCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Button, Sample>> buttonRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<Sample>> comparatorCaptor;
  @Captor
  private ArgumentCaptor<ValueProvider<DatasetFile, String>> fileValueProviderCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Button, DatasetFile>> fileButtonRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<DatasetFile>> fileComparatorCaptor;
  @Captor
  private ArgumentCaptor<EditorCloseListener<DatasetFile>> fileCloseListenerCaptor;
  @Mock
  private ComponentEventListener<SavedEvent<DatasetDialog>> savedListener;
  @Mock
  private ComponentEventListener<DeletedEvent<DatasetDialog>> deletedListener;
  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Autowired
  private SampleRepository sampleRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetDialog.class, locale);
  private AppResources datasetResources = new AppResources(Dataset.class, locale);
  private AppResources sampleResources = new AppResources(Sample.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Sample> samples;
  private List<Path> files = new ArrayList<>();

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    ui.setLocale(locale);
    dialog = new DatasetDialog(presenter);
    dialog.init();
    samples = sampleRepository.findAll();
    files.add(Paths.get("dataset", "dataset.png"));
    files.add(Paths.get("sample", "sample_R1.fastq"));
    files.add(Paths.get("sample", "sample_R2.fastq"));
    files.add(Paths.get("sample", "sample.bw"));
    files.add(Paths.get("sample", "sample.png"));
  }

  @SuppressWarnings("unchecked")
  private void mockSamplesColumns() {
    Element samplesElement = new Grid<>().getElement();
    dialog.samples = mock(Grid.class);
    when(dialog.samples.getElement()).thenReturn(samplesElement);
    dialog.sampleId = mock(Column.class);
    when(dialog.samples.addColumn(any(ComponentRenderer.class), eq(SampleProperties.SAMPLE_ID)))
        .thenReturn(dialog.sampleId);
    when(dialog.sampleId.setKey(any())).thenReturn(dialog.sampleId);
    when(dialog.sampleId.setComparator(any(Comparator.class))).thenReturn(dialog.sampleId);
    when(dialog.sampleId.setHeader(any(String.class))).thenReturn(dialog.sampleId);
    dialog.sampleReplicate = mock(Column.class);
    when(dialog.samples.addColumn(any(ComponentRenderer.class), eq(SampleProperties.REPLICATE)))
        .thenReturn(dialog.sampleReplicate);
    when(dialog.sampleReplicate.setKey(any())).thenReturn(dialog.sampleReplicate);
    when(dialog.sampleReplicate.setComparator(any(Comparator.class)))
        .thenReturn(dialog.sampleReplicate);
    when(dialog.sampleReplicate.setHeader(any(String.class))).thenReturn(dialog.sampleReplicate);
    dialog.sampleName = mock(Column.class);
    when(dialog.samples.addColumn(any(ValueProvider.class), eq(SampleProperties.NAME)))
        .thenReturn(dialog.sampleName);
    when(dialog.sampleName.setKey(any())).thenReturn(dialog.sampleName);
    when(dialog.sampleName.setComparator(any(Comparator.class))).thenReturn(dialog.sampleName);
    when(dialog.sampleName.setHeader(any(String.class))).thenReturn(dialog.sampleName);
    dialog.sampleRemove = mock(Column.class);
    when(dialog.samples.addColumn(any(ComponentRenderer.class), eq(REMOVE)))
        .thenReturn(dialog.sampleRemove);
    when(dialog.sampleRemove.setKey(any())).thenReturn(dialog.sampleRemove);
    when(dialog.sampleRemove.setSortable(anyBoolean())).thenReturn(dialog.sampleRemove);
    when(dialog.sampleRemove.setComparator(any(Comparator.class))).thenReturn(dialog.sampleRemove);
    when(dialog.sampleRemove.setHeader(any(String.class))).thenReturn(dialog.sampleRemove);
    FooterRow footerRow = mock(FooterRow.class);
    when(dialog.samples.appendFooterRow()).thenReturn(footerRow);
    FooterCell sampleIdFooterCell = mock(FooterCell.class);
    when(footerRow.getCell(dialog.sampleId)).thenReturn(sampleIdFooterCell);
    FooterCell replicateFooterCell = mock(FooterCell.class);
    when(footerRow.getCell(dialog.sampleReplicate)).thenReturn(replicateFooterCell);
    FooterCell nameFooterCell = mock(FooterCell.class);
    when(footerRow.getCell(dialog.sampleName)).thenReturn(nameFooterCell);
    FooterCell removeFooterCell = mock(FooterCell.class);
    when(footerRow.getCell(dialog.sampleRemove)).thenReturn(removeFooterCell);
  }

  @SuppressWarnings("unchecked")
  private void mockFilesColumns() {
    Element filesElement = dialog.files.getElement();
    dialog.files = mock(Grid.class);
    when(dialog.files.getEditor()).thenReturn(mock(Editor.class));
    when(dialog.files.getElement()).thenReturn(filesElement);
    dialog.filename = mock(Column.class);
    when(dialog.files.addColumn(any(ValueProvider.class), eq(FILENAME)))
        .thenReturn(dialog.filename);
    when(dialog.filename.setKey(any())).thenReturn(dialog.filename);
    when(dialog.filename.setComparator(any(Comparator.class))).thenReturn(dialog.filename);
    when(dialog.filename.setHeader(any(String.class))).thenReturn(dialog.filename);
    dialog.deleteFile = mock(Column.class);
    when(dialog.files.addColumn(any(ComponentRenderer.class), eq(DELETE)))
        .thenReturn(dialog.deleteFile);
    when(dialog.deleteFile.setKey(any())).thenReturn(dialog.deleteFile);
    when(dialog.deleteFile.setComparator(any(Comparator.class))).thenReturn(dialog.deleteFile);
    when(dialog.deleteFile.setHeader(any(String.class))).thenReturn(dialog.deleteFile);
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(dialog);
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(HEADER), dialog.header.getId().orElse(""));
    assertEquals(id(TAGS), dialog.tags.getId().orElse(""));
    assertEquals(id(PROTOCOL), dialog.protocol.getId().orElse(""));
    assertEquals(id(ASSAY), dialog.assay.getId().orElse(""));
    assertEquals(id(TYPE), dialog.type.getId().orElse(""));
    assertEquals(id(TARGET), dialog.target.getId().orElse(""));
    assertEquals(id(STRAIN), dialog.strain.getId().orElse(""));
    assertEquals(id(STRAIN_DESCRIPTION), dialog.strainDescription.getId().orElse(""));
    assertEquals(id(TREATMENT), dialog.treatment.getId().orElse(""));
    assertEquals(id(SAMPLES), dialog.samples.getId().orElse(""));
    assertEquals(id(ADD_SAMPLE), dialog.addSample.getId().orElse(""));
    assertEquals(id(FILES), dialog.files.getId().orElse(""));
    assertEquals(id(FILENAME), dialog.filenameEdit.getId().orElse(""));
    assertEquals(id(SAVE), dialog.save.getId().orElse(""));
    assertTrue(dialog.save.hasThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName()));
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
    assertEquals(id(CANCEL), dialog.cancel.getId().orElse(""));
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel.getIcon());
    assertEquals(id(DELETE), dialog.delete.getId().orElse(""));
    assertTrue(dialog.delete.hasThemeName(ButtonVariant.LUMO_ERROR.getVariantName()));
    validateIcon(VaadinIcon.TRASH.create(), dialog.delete.getIcon());
    assertEquals("true", dialog.confirm.getElement().getProperty("cancel"));
    assertTrue(dialog.confirm.getElement().getProperty("confirmTheme")
        .contains(ButtonVariant.LUMO_ERROR.getVariantName()));
    assertTrue(dialog.confirm.getElement().getProperty("confirmTheme")
        .contains(ButtonVariant.LUMO_PRIMARY.getVariantName()));
  }

  @Test
  public void labels() {
    mockSamplesColumns();
    mockFilesColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(datasetResources.message(TAGS), dialog.tags.getLabel());
    assertEquals(sampleResources.message(PROTOCOL), dialog.protocol.getLabel());
    assertEquals(sampleResources.message(ASSAY), dialog.assay.getLabel());
    assertEquals(sampleResources.message(TYPE), dialog.type.getLabel());
    assertEquals(sampleResources.message(TARGET), dialog.target.getLabel());
    assertEquals(sampleResources.message(property(TARGET, PLACEHOLDER)),
        dialog.target.getPlaceholder());
    assertEquals(sampleResources.message(STRAIN), dialog.strain.getLabel());
    assertEquals(sampleResources.message(property(STRAIN, PLACEHOLDER)),
        dialog.strain.getPlaceholder());
    assertEquals(sampleResources.message(STRAIN_DESCRIPTION), dialog.strainDescription.getLabel());
    assertEquals(sampleResources.message(property(STRAIN_DESCRIPTION, PLACEHOLDER)),
        dialog.strainDescription.getPlaceholder());
    assertEquals(sampleResources.message(TREATMENT), dialog.treatment.getLabel());
    assertEquals(sampleResources.message(property(TREATMENT, PLACEHOLDER)),
        dialog.treatment.getPlaceholder());
    verify(dialog.sampleId).setHeader(sampleResources.message(SampleProperties.SAMPLE_ID));
    verify(dialog.sampleName).setHeader(sampleResources.message(SampleProperties.NAME));
    verify(dialog.sampleReplicate).setHeader(sampleResources.message(SampleProperties.REPLICATE));
    verify(dialog.filename).setHeader(resources.message(FILENAME));
    assertEquals(webResources.message(ADD), dialog.addSample.getText());
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
    mockSamplesColumns();
    mockFilesColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(DatasetDialog.class, locale);
    final AppResources datasetResources = new AppResources(Dataset.class, locale);
    final AppResources sampleResources = new AppResources(Sample.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    ui.setLocale(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(datasetResources.message(TAGS), dialog.tags.getLabel());
    assertEquals(sampleResources.message(PROTOCOL), dialog.protocol.getLabel());
    assertEquals(sampleResources.message(ASSAY), dialog.assay.getLabel());
    assertEquals(sampleResources.message(TYPE), dialog.type.getLabel());
    assertEquals(sampleResources.message(TARGET), dialog.target.getLabel());
    assertEquals(sampleResources.message(property(TARGET, PLACEHOLDER)),
        dialog.target.getPlaceholder());
    assertEquals(sampleResources.message(STRAIN), dialog.strain.getLabel());
    assertEquals(sampleResources.message(property(STRAIN, PLACEHOLDER)),
        dialog.strain.getPlaceholder());
    assertEquals(sampleResources.message(STRAIN_DESCRIPTION), dialog.strainDescription.getLabel());
    assertEquals(sampleResources.message(property(STRAIN_DESCRIPTION, PLACEHOLDER)),
        dialog.strainDescription.getPlaceholder());
    assertEquals(sampleResources.message(TREATMENT), dialog.treatment.getLabel());
    assertEquals(sampleResources.message(property(TREATMENT, PLACEHOLDER)),
        dialog.treatment.getPlaceholder());
    verify(dialog.sampleId).setHeader(sampleResources.message(SampleProperties.SAMPLE_ID));
    verify(dialog.sampleName).setHeader(sampleResources.message(SampleProperties.NAME));
    verify(dialog.sampleReplicate).setHeader(sampleResources.message(SampleProperties.REPLICATE));
    verify(dialog.filename).setHeader(resources.message(FILENAME));
    assertEquals(webResources.message(ADD), dialog.addSample.getText());
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
  public void protocol() {
    for (Protocol protocol : protocolRepository.findAll()) {
      assertEquals(protocol.getName(), dialog.protocol.getItemLabelGenerator().apply(protocol));
    }
  }

  @Test
  public void assay() {
    List<Assay> assays = items(dialog.assay);
    assertArrayEquals(Assay.values(), assays.toArray(new Assay[0]));
    for (Assay assay : assays) {
      assertEquals(assay.getLabel(locale), dialog.assay.getItemLabelGenerator().apply(assay));
    }
  }

  @Test
  public void type() {
    List<SampleType> types = items(dialog.type);
    assertArrayEquals(SampleType.values(), types.toArray(new SampleType[0]));
    for (SampleType type : types) {
      assertEquals(type.getLabel(locale), dialog.type.getItemLabelGenerator().apply(type));
    }
  }

  @Test
  public void samples() {
    assertEquals(4, dialog.samples.getColumns().size());
    assertNotNull(dialog.samples.getColumnByKey(SampleProperties.SAMPLE_ID));
    assertTrue(dialog.sampleId.isSortable());
    assertNotNull(dialog.samples.getColumnByKey(SampleProperties.REPLICATE));
    assertTrue(dialog.sampleReplicate.isSortable());
    assertNotNull(dialog.samples.getColumnByKey(SampleProperties.NAME));
    assertTrue(dialog.sampleName.isSortable());
    assertNotNull(dialog.samples.getColumnByKey(REMOVE));
    assertFalse(dialog.sampleRemove.isSortable());
    assertTrue(dialog.samples.getSelectionModel() instanceof SelectionModel.Single);
  }

  @Test
  public void samples_ColumnsValueProvider() {
    mockSamplesColumns();
    mockFilesColumns();
    dialog.init();
    verify(dialog.samples).addColumn(textFieldRendererCaptor.capture(),
        eq(SampleProperties.SAMPLE_ID));
    ComponentRenderer<TextField, Sample> textFieldRenderer = textFieldRendererCaptor.getValue();
    for (Sample sample : samples) {
      TextField textField = textFieldRenderer.createComponent(sample);
      assertTrue(textField.hasClassName(SampleProperties.SAMPLE_ID));
      assertSame(textField, dialog.sampleIdField(sample));
    }
    verify(dialog.sampleId).setComparator(comparatorCaptor.capture());
    Comparator<Sample> comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Sample sample : samples) {
      assertEquals(sample.getSampleId(),
          ((NormalizedComparator<Sample>) comparator).getConverter().apply(sample));
    }
    verify(dialog.samples).addColumn(textFieldRendererCaptor.capture(),
        eq(SampleProperties.REPLICATE));
    textFieldRenderer = textFieldRendererCaptor.getValue();
    for (Sample sample : samples) {
      TextField textField = textFieldRenderer.createComponent(sample);
      assertTrue(textField.hasClassName(SampleProperties.REPLICATE));
      assertSame(textField, dialog.sampleReplicateField(sample));
    }
    verify(dialog.sampleReplicate).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Sample sample : samples) {
      assertEquals(sample.getReplicate(),
          ((NormalizedComparator<Sample>) comparator).getConverter().apply(sample));
    }
    verify(dialog.samples).addColumn(valueProviderCaptor.capture(), eq(SampleProperties.NAME));
    ValueProvider<Sample, String> valueProvider = valueProviderCaptor.getValue();
    for (Sample sample : samples) {
      assertEquals(sample.getName(), valueProvider.apply(sample));
    }
    verify(dialog.sampleName).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Sample sample : samples) {
      assertEquals(sample.getName(),
          ((NormalizedComparator<Sample>) comparator).getConverter().apply(sample));
    }
    verify(dialog.samples).addColumn(buttonRendererCaptor.capture(), eq(REMOVE));
    ComponentRenderer<Button, Sample> buttonRenderer = buttonRendererCaptor.getValue();
    for (Sample sample : samples) {
      Button button = buttonRenderer.createComponent(sample);
      assertTrue(button.hasClassName(REMOVE));
      clickButton(button);
      verify(presenter).removeSample(sample);
    }
    verify(dialog.sampleRemove, never()).setComparator(comparatorCaptor.capture());
  }

  @Test
  public void files() {
    assertEquals(2, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
    assertNotNull(dialog.files.getColumnByKey(DELETE));
  }

  @Test
  public void files_ColumnsValueProvider() {
    mockSamplesColumns();
    mockFilesColumns();
    dialog.init();
    verify(dialog.files).addColumn(fileValueProviderCaptor.capture(), eq(FILENAME));
    ValueProvider<DatasetFile, String> valueProvider = fileValueProviderCaptor.getValue();
    for (Path path : files) {
      DatasetFile file = new DatasetFile(path);
      assertEquals(file.getFilename(), valueProvider.apply(file));
    }
    verify(dialog.filename).setEditorComponent(dialog.filenameEdit);
    verify(dialog.filename).setComparator(fileComparatorCaptor.capture());
    Comparator<DatasetFile> comparator = fileComparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Path path : files) {
      DatasetFile file = new DatasetFile(path);
      assertEquals(file.getFilename(),
          ((NormalizedComparator<DatasetFile>) comparator).getConverter().apply(file));
    }
    verify(dialog.files).addColumn(fileButtonRendererCaptor.capture(), eq(DELETE));
    ComponentRenderer<Button, DatasetFile> buttonRenderer = fileButtonRendererCaptor.getValue();
    for (Path path : files) {
      DatasetFile file = new DatasetFile(path);
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
    mockSamplesColumns();
    mockFilesColumns();
    dialog.init();
    DatasetFile file = new DatasetFile(files.get(0));
    verify(dialog.files.getEditor()).addCloseListener(fileCloseListenerCaptor.capture());
    EditorCloseListener<DatasetFile> listener = fileCloseListenerCaptor.getValue();
    listener.onEditorClose(new EditorCloseEvent<>(dialog.files.getEditor(), file));
    verify(presenter).rename(file, locale);
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

    verify(presenter).setDataset(dataset, locale);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void setDataset_Dataset() {
    Dataset dataset = datasetRepository.findById(2L).get();
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(dataset);

    verify(presenter).setDataset(dataset, locale);
    assertEquals(resources.message(HEADER, 1, dataset.getName()), dialog.header.getText());
    assertEquals(resources.message(DELETE_MESSAGE, dataset.getName()),
        dialog.confirm.getElement().getProperty("message"));
  }

  @Test
  public void setDataset_BeforeLocaleChange() {
    Dataset dataset = datasetRepository.findById(2L).get();
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.setDataset(dataset);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(presenter).setDataset(dataset, locale);
    assertEquals(resources.message(HEADER, 1, dataset.getName()), dialog.header.getText());
    assertEquals(resources.message(DELETE_MESSAGE, dataset.getName()),
        dialog.confirm.getElement().getProperty("message"));
  }

  @Test
  public void setDataset_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(null);

    verify(presenter).setDataset(null, locale);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void addSample() {
    clickButton(dialog.addSample);

    verify(presenter).addSample(locale);
  }

  @Test
  public void save() {
    clickButton(dialog.save);

    verify(presenter).save(locale);
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
    ConfirmEvent event = new ConfirmEvent(dialog.confirm, false);
    fireEvent(dialog.confirm, event);
    verify(presenter).delete(locale);
  }

  @Test
  public void delete_Cancel() {
    clickButton(dialog.delete);

    assertTrue(dialog.confirm.isOpened());
    CancelEvent event = new CancelEvent(dialog.confirm, false);
    fireEvent(dialog.confirm, event);
    verify(presenter, never()).delete(any());
  }
}
