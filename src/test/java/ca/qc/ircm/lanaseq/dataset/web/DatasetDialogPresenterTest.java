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

import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAVED;
import static ca.qc.ircm.lanaseq.sample.SampleType.IMMUNO_PRECIPITATION;
import static ca.qc.ircm.lanaseq.sample.SampleType.INPUT;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.sample.web.SampleDialog;
import ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.SelectedEvent;
import ca.qc.ircm.lanaseq.web.TagsField;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.provider.ListDataProvider;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link DatasetDialogPresenter}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetDialogPresenterTest extends AbstractKaribuTestCase {
  @Autowired
  private DatasetDialogPresenter presenter;
  @Mock
  private DatasetDialog dialog;
  @MockBean
  private DatasetService service;
  @MockBean
  private AuthenticatedUser authenticatedUser;
  @Autowired
  private ObjectFactory<SelectSampleDialog> selectSampleDialogFactory;
  @MockBean
  private SelectSampleDialog selectSampleDialog;
  @Mock
  private Sample sample;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<SampleDialog>>> savedListenerCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<DeletedEvent<SampleDialog>>> deletedListenerCaptor;
  @Captor
  private ArgumentCaptor<
      ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>>> selectListenerCaptor;
  @Captor
  private ArgumentCaptor<ListDataProvider<Sample>> samplesDataProviderCaptor;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private SampleRepository sampleRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<String> topTags = new ArrayList<>();
  private DateTimeFormatter nameDateFormatter = DateTimeFormatter.BASIC_ISO_DATE;
  private String namePrefix = "ChIPseq_IP_polr3a_yFR20_WT_37C_testsample1-testsample2";
  private String tag1 = "Tag 1";
  private String tag2 = "Tag 2";
  private LocalDate date = LocalDate.of(2020, 7, 20);
  private String note = "test note\nsecond line";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    dialog.header = new H3();
    dialog.namePrefix = new TextField();
    dialog.tags = new TagsField();
    dialog.protocol = new TextField();
    dialog.assay = new TextField();
    dialog.type = new TextField();
    dialog.target = new TextField();
    dialog.strain = new TextField();
    dialog.strainDescription = new TextField();
    dialog.treatment = new TextField();
    dialog.note = new TextArea();
    dialog.date = new DatePicker();
    dialog.samples = new Grid<>();
    dialog.addSample = new Button();
    dialog.error = new Div();
    dialog.save = new Button();
    dialog.cancel = new Button();
    dialog.delete = new Button();
    dialog.selectSampleDialogFactory = selectSampleDialogFactory;
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(true);
    topTags.add("input");
    topTags.add("chip");
    when(service.topTags(anyInt())).thenReturn(topTags);
    presenter.init(dialog);
    presenter.localeChange(locale);
  }

  private void fillForm() {
    dialog.namePrefix.setValue(namePrefix);
    dialog.tags.setValue(Stream.of(tag1, tag2).collect(Collectors.toSet()));
    dialog.date.setValue(date);
    dialog.note.setValue(note);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void init() throws Throwable {
    Field newTagField = TagsField.class.getDeclaredField("newTag");
    newTagField.setAccessible(true);
    ComboBox<String> newTag = (ComboBox<String>) newTagField.get(dialog.tags);
    List<String> items = items(newTag);
    assertEquals(topTags, items);
  }

  @Test
  public void error() {
    assertFalse(dialog.error.isVisible());
  }

  @Test
  public void getDataset() {
    Dataset dataset = new Dataset();
    presenter.setDataset(dataset);
    assertEquals(dataset, presenter.getDataset());
  }

  @Test
  public void setDataset_NewDataset() {
    Dataset dataset = new Dataset();

    presenter.setDataset(dataset);

    assertEquals("", dialog.namePrefix.getValue());
    assertFalse(dialog.namePrefix.isReadOnly());
    assertTrue(dialog.tags.getValue().isEmpty());
    assertFalse(dialog.tags.isReadOnly());
    assertEquals(LocalDate.now(), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    assertEquals("", dialog.note.getValue());
    assertFalse(dialog.note.isReadOnly());
    assertEquals("", dialog.protocol.getValue());
    assertTrue(dialog.protocol.isReadOnly());
    assertEquals("", dialog.assay.getValue());
    assertTrue(dialog.assay.isReadOnly());
    assertEquals("", dialog.type.getValue());
    assertTrue(dialog.type.isReadOnly());
    assertEquals("", dialog.target.getValue());
    assertTrue(dialog.target.isReadOnly());
    assertEquals("", dialog.strain.getValue());
    assertTrue(dialog.strain.isReadOnly());
    assertEquals("", dialog.strainDescription.getValue());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertEquals("", dialog.treatment.getValue());
    assertTrue(dialog.treatment.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(0, samples.size());
  }

  @Test
  public void setDataset_Dataset() {
    Dataset dataset = repository.findById(1L).get();

    presenter.setDataset(dataset);

    assertEquals("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3", dialog.namePrefix.getValue());
    assertFalse(dialog.namePrefix.isReadOnly());
    assertEquals(2, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("mnase"));
    assertTrue(dialog.tags.getValue().contains("ip"));
    assertFalse(dialog.tags.isReadOnly());
    assertEquals(LocalDate.of(2018, 10, 20), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    assertEquals("robtools version 2", dialog.note.getValue());
    assertFalse(dialog.note.isReadOnly());
    assertEquals("FLAG", dialog.protocol.getValue());
    assertTrue(dialog.protocol.isReadOnly());
    assertEquals("MNase-seq", dialog.assay.getValue());
    assertTrue(dialog.assay.isReadOnly());
    assertEquals("IP", dialog.type.getValue());
    assertTrue(dialog.type.isReadOnly());
    assertEquals("polr2a", dialog.target.getValue());
    assertTrue(dialog.target.isReadOnly());
    assertEquals("yFR100", dialog.strain.getValue());
    assertTrue(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertEquals("Rappa", dialog.treatment.getValue());
    assertTrue(dialog.treatment.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertTrue(find(samples, 2L).isPresent());
    assertTrue(find(samples, 3L).isPresent());
  }

  @Test
  public void setDataset_DatasetNoPrefix() {
    Dataset dataset = repository.findById(3L).get();

    presenter.setDataset(dataset);

    assertEquals("", dialog.namePrefix.getValue());
  }

  @Test
  public void setDataset_CannotWrite() {
    when(authenticatedUser.hasPermission(any(), any())).thenReturn(false);
    Dataset dataset = repository.findById(1L).get();

    presenter.setDataset(dataset);

    assertEquals("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3", dialog.namePrefix.getValue());
    assertTrue(dialog.namePrefix.isReadOnly());
    assertEquals(2, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("mnase"));
    assertTrue(dialog.tags.getValue().contains("ip"));
    assertTrue(dialog.tags.isReadOnly());
    assertEquals(LocalDate.of(2018, 10, 20), dialog.date.getValue());
    assertTrue(dialog.date.isReadOnly());
    assertEquals("robtools version 2", dialog.note.getValue());
    assertTrue(dialog.note.isReadOnly());
    assertEquals("FLAG", dialog.protocol.getValue());
    assertTrue(dialog.protocol.isReadOnly());
    assertEquals("MNase-seq", dialog.assay.getValue());
    assertTrue(dialog.assay.isReadOnly());
    assertEquals("IP", dialog.type.getValue());
    assertTrue(dialog.type.isReadOnly());
    assertEquals("polr2a", dialog.target.getValue());
    assertTrue(dialog.target.isReadOnly());
    assertEquals("yFR100", dialog.strain.getValue());
    assertTrue(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertEquals("Rappa", dialog.treatment.getValue());
    assertTrue(dialog.treatment.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertTrue(find(samples, 2L).isPresent());
    assertTrue(find(samples, 3L).isPresent());
  }

  @Test
  public void setDataset_NotEditable() {
    Dataset dataset = repository.findById(5L).get();

    presenter.setDataset(dataset);

    assertEquals("ChIPseq_IP_polr2b_yBC103_WT_BC1", dialog.namePrefix.getValue());
    assertTrue(dialog.namePrefix.isReadOnly());
    assertEquals(1, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("chipseq"));
    assertTrue(dialog.tags.isReadOnly());
    assertEquals(LocalDate.of(2018, 12, 05), dialog.date.getValue());
    assertTrue(dialog.date.isReadOnly());
    assertEquals("", dialog.note.getValue());
    assertTrue(dialog.note.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
    assertEquals("BioID", dialog.protocol.getValue());
    assertTrue(dialog.protocol.isReadOnly());
    assertEquals("ChIP-seq", dialog.assay.getValue());
    assertTrue(dialog.assay.isReadOnly());
    assertEquals("IP", dialog.type.getValue());
    assertTrue(dialog.type.isReadOnly());
    assertEquals("polr2b", dialog.target.getValue());
    assertTrue(dialog.target.isReadOnly());
    assertEquals("yBC103", dialog.strain.getValue());
    assertTrue(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertEquals("", dialog.treatment.getValue());
    assertTrue(dialog.treatment.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(1, samples.size());
    assertTrue(find(samples, 8L).isPresent());
  }

  @Test
  public void setDataset_SampleWithDifferentFields() {
    Dataset dataset = repository.findById(1L).get();
    Sample sample = sampleRepository.findById(4L).get();
    sample.setType(SampleType.INPUT);
    sample.setTreatment("Heat shock");
    dataset.getSamples().add(sample);

    presenter.setDataset(dataset);

    assertEquals("FLAG, Histone FLAG", dialog.protocol.getValue());
    assertEquals("MNase-seq" + ", " + "ChIP-seq", dialog.assay.getValue());
    assertEquals(IMMUNO_PRECIPITATION.getLabel(locale) + ", " + INPUT.getLabel(locale),
        dialog.type.getValue());
    assertEquals("polr2a, Spt16", dialog.target.getValue());
    assertEquals("yFR100, yFR101", dialog.strain.getValue());
    assertEquals("WT, G24D", dialog.strainDescription.getValue());
    assertEquals("Rappa, Heat shock", dialog.treatment.getValue());
  }

  @Test
  public void setDataset_SampleWithEmtpyFields() {
    Dataset dataset = repository.findById(1L).get();
    Sample sample = dataset.getSamples().get(0);
    sample.setProtocol(null);
    sample.setAssay(null);
    sample.setType(null);
    sample.setTarget(null);
    sample.setStrain(null);
    sample.setStrainDescription(null);
    sample.setTreatment(null);

    presenter.setDataset(dataset);

    assertEquals("FLAG", dialog.protocol.getValue());
    assertEquals("MNase-seq", dialog.assay.getValue());
    assertEquals(IMMUNO_PRECIPITATION.getLabel(locale), dialog.type.getValue());
    assertEquals("polr2a", dialog.target.getValue());
    assertEquals("yFR100", dialog.strain.getValue());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertEquals("Rappa", dialog.treatment.getValue());
  }

  @Test
  public void setDataset_Null() {
    presenter.setDataset(null);

    assertEquals("", dialog.namePrefix.getValue());
    assertFalse(dialog.namePrefix.isReadOnly());
    assertTrue(dialog.tags.getValue().isEmpty());
    assertFalse(dialog.tags.isReadOnly());
    assertEquals(LocalDate.now(), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    assertEquals("", dialog.note.getValue());
    assertFalse(dialog.note.isReadOnly());
    assertEquals("", dialog.protocol.getValue());
    assertTrue(dialog.protocol.isReadOnly());
    assertEquals("", dialog.assay.getValue());
    assertTrue(dialog.assay.isReadOnly());
    assertEquals("", dialog.type.getValue());
    assertTrue(dialog.type.isReadOnly());
    assertEquals("", dialog.target.getValue());
    assertTrue(dialog.target.isReadOnly());
    assertEquals("", dialog.strain.getValue());
    assertTrue(dialog.strain.isReadOnly());
    assertEquals("", dialog.strainDescription.getValue());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertEquals("", dialog.treatment.getValue());
    assertTrue(dialog.treatment.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(0, samples.size());
  }

  @Test
  public void requiredIndicator() {
    assertTrue(dialog.namePrefix.isRequiredIndicatorVisible());
    assertFalse(dialog.tags.isRequiredIndicatorVisible());
    assertTrue(dialog.date.isRequiredIndicatorVisible());
    assertFalse(dialog.note.isRequiredIndicatorVisible());
  }

  @Test
  public void generateName() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    dialog.namePrefix.setValue("test");
    dialog.date.setValue(LocalDate.of(2022, 05, 02));
    assertEquals("test", dialog.namePrefix.getValue());

    presenter.generateName();

    dialog.namePrefix.setValue("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3");
  }

  @Test
  public void generateName_NewSample() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    dialog.namePrefix.setValue("test");
    dialog.date.setValue(LocalDate.of(2022, 05, 02));
    presenter.addSample();
    verify(selectSampleDialog).addSelectedListener(selectListenerCaptor.capture());
    verify(selectSampleDialog).open();
    ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>> selectListener =
        selectListenerCaptor.getValue();
    Sample sample = sampleRepository.findById(4L).get();
    selectListener.onComponentEvent(new SelectedEvent<>(selectSampleDialog, false, sample));
    assertEquals("test", dialog.namePrefix.getValue());

    presenter.generateName();

    dialog.namePrefix.setValue("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3-JS1");
  }

  @Test
  public void addSample() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(3, items(dialog.samples).size());
    presenter.addSample();
    verify(selectSampleDialog).addSelectedListener(selectListenerCaptor.capture());
    verify(selectSampleDialog).open();
    ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>> selectListener =
        selectListenerCaptor.getValue();
    selectListener.onComponentEvent(new SelectedEvent<>(selectSampleDialog, false, sample));
    List<Sample> samples = items(dialog.samples);
    assertEquals(4, samples.size());
    assertEquals(sample, samples.get(samples.size() - 1));
    assertEquals("FLAG", dialog.protocol.getValue());
    assertEquals("MNase-seq", dialog.assay.getValue());
    assertEquals("IP", dialog.type.getValue());
    assertEquals("polr2a", dialog.target.getValue());
    assertEquals("yFR100", dialog.strain.getValue());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertEquals("Rappa", dialog.treatment.getValue());
  }

  @Test
  public void addSample_SampleWithDifferentInfo() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    assertEquals(3, items(dialog.samples).size());
    presenter.addSample();
    verify(selectSampleDialog).addSelectedListener(selectListenerCaptor.capture());
    verify(selectSampleDialog).open();
    ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>> selectListener =
        selectListenerCaptor.getValue();
    Sample sample = sampleRepository.findById(4L).get();
    selectListener.onComponentEvent(new SelectedEvent<>(selectSampleDialog, false, sample));
    List<Sample> samples = items(dialog.samples);
    assertEquals(4, samples.size());
    assertEquals(sample, samples.get(samples.size() - 1));
    assertEquals("FLAG, Histone FLAG", dialog.protocol.getValue());
    assertEquals("MNase-seq, ChIP-seq", dialog.assay.getValue());
    assertEquals("IP", dialog.type.getValue());
    assertEquals("polr2a, Spt16", dialog.target.getValue());
    assertEquals("yFR100, yFR101", dialog.strain.getValue());
    assertEquals("WT, G24D", dialog.strainDescription.getValue());
    assertEquals("Rappa", dialog.treatment.getValue());
  }

  @Test
  public void addSample_AlreadyInDataset() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    final Sample sample = dataset.getSamples().get(0);
    assertEquals(3, items(dialog.samples).size());
    presenter.addSample();
    verify(selectSampleDialog).addSelectedListener(selectListenerCaptor.capture());
    verify(selectSampleDialog).open();
    ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>> selectListener =
        selectListenerCaptor.getValue();
    selectListener.onComponentEvent(new SelectedEvent<>(selectSampleDialog, false, sample));
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
  }

  @Test
  public void addSample_NotEditableSamples() {
    Dataset dataset = repository.findById(5L).get();
    dataset.setEditable(true);
    dataset.getSamples().get(0).setEditable(false);
    presenter.setDataset(dataset);
    final Sample sample = sampleRepository.findById(1L).get();

    presenter.addSample();
    verify(selectSampleDialog).addSelectedListener(selectListenerCaptor.capture());
    verify(selectSampleDialog).open();
    ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>> selectListener =
        selectListenerCaptor.getValue();
    selectListener.onComponentEvent(new SelectedEvent<>(selectSampleDialog, false, sample));

    List<Sample> samples = items(dialog.samples);
    assertEquals(2, samples.size());
    assertTrue(find(samples, 8L).isPresent());
    assertTrue(find(samples, 1L).isPresent());
  }

  @Test
  public void addSample_AddNotEditableSample() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    final Sample sample = sampleRepository.findById(8L).get();

    presenter.addSample();
    verify(selectSampleDialog).addSelectedListener(selectListenerCaptor.capture());
    verify(selectSampleDialog).open();
    ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>> selectListener =
        selectListenerCaptor.getValue();
    selectListener.onComponentEvent(new SelectedEvent<>(selectSampleDialog, false, sample));

    List<Sample> samples = items(dialog.samples);
    assertEquals(4, samples.size());
    assertTrue(find(samples, 1L).isPresent());
    assertTrue(find(samples, 2L).isPresent());
    assertTrue(find(samples, 3L).isPresent());
    assertTrue(find(samples, 8L).isPresent());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void removeSample() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    dialog.samples = mock(Grid.class);
    when(dialog.samples.getDataProvider()).thenReturn(mock(ListDataProvider.class));
    List<Sample> samples = new ArrayList<>(dataset.getSamples());
    Sample sample = samples.get(0);
    presenter.removeSample(sample);
    verify(dialog.samples.getDataProvider()).refreshAll();
    List<Sample> items = presenter.getSamples();
    assertEquals(samples.size() - 1, items.size());
    for (int i = 1; i < samples.size(); i++) {
      assertEquals(samples.get(i), items.get(i - 1));
    }
    assertEquals("FLAG", dialog.protocol.getValue());
    assertEquals("MNase-seq", dialog.assay.getValue());
    assertEquals("IP", dialog.type.getValue());
    assertEquals("polr2a", dialog.target.getValue());
    assertEquals("yFR100", dialog.strain.getValue());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertEquals("Rappa", dialog.treatment.getValue());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void removeSample_SampleWithDifferentInfo() {
    Dataset dataset = repository.findById(1L).get();
    dataset.getSamples().add(sampleRepository.findById(4L).get());
    presenter.setDataset(dataset);
    dialog.samples = mock(Grid.class);
    when(dialog.samples.getDataProvider()).thenReturn(mock(ListDataProvider.class));
    List<Sample> samples = new ArrayList<>(dataset.getSamples());
    Sample sample = samples.get(samples.size() - 1);
    presenter.removeSample(sample);
    verify(dialog.samples.getDataProvider()).refreshAll();
    List<Sample> items = presenter.getSamples();
    assertEquals(samples.size() - 1, items.size());
    for (int i = 0; i < samples.size() - 1; i++) {
      assertEquals(samples.get(i), items.get(i));
    }
    assertEquals("FLAG", dialog.protocol.getValue());
    assertEquals("MNase-seq", dialog.assay.getValue());
    assertEquals("IP", dialog.type.getValue());
    assertEquals("polr2a", dialog.target.getValue());
    assertEquals("yFR100", dialog.strain.getValue());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertEquals("Rappa", dialog.treatment.getValue());
  }

  @Test
  public void removeSample_NotEditable() {
    Dataset dataset = repository.findById(1L).get();
    dataset.getSamples().forEach(sample -> sample.setEditable(false));
    dataset.getSamples().get(0).setEditable(true);
    presenter.setDataset(dataset);

    presenter.removeSample(dataset.getSamples().get(0));

    assertEquals(2, dialog.tags.getValue().size());
    assertTrue(dialog.tags.getValue().contains("mnase"));
    assertTrue(dialog.tags.getValue().contains("ip"));
    assertFalse(dialog.tags.isReadOnly());
    assertEquals("robtools version 2", dialog.note.getValue());
    assertFalse(dialog.note.isReadOnly());
    assertEquals(LocalDate.of(2018, 10, 20), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    assertNotNull(dialog.protocol.getValue());
    assertEquals("FLAG", dialog.protocol.getValue());
    assertTrue(dialog.protocol.isReadOnly());
    assertEquals("MNase-seq", dialog.assay.getValue());
    assertTrue(dialog.assay.isReadOnly());
    assertEquals("IP", dialog.type.getValue());
    assertTrue(dialog.type.isReadOnly());
    assertEquals("polr2a", dialog.target.getValue());
    assertTrue(dialog.target.isReadOnly());
    assertEquals("yFR100", dialog.strain.getValue());
    assertTrue(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertEquals("Rappa", dialog.treatment.getValue());
    assertTrue(dialog.treatment.isReadOnly());
    List<Sample> samples = items(dialog.samples);
    assertEquals(2, samples.size());
    assertTrue(find(samples, 2L).isPresent());
    assertTrue(find(samples, 3L).isPresent());
  }

  @Test
  public void dropSample_AboveMoveFarther() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    presenter.dropSample(dataset.getSamples().get(0), dataset.getSamples().get(2),
        GridDropLocation.ABOVE);
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertEquals((Long) 2L, samples.get(0).getId());
    assertEquals((Long) 1L, samples.get(1).getId());
    assertEquals((Long) 3L, samples.get(2).getId());
  }

  @Test
  public void dropSample_BelowMoveFarther() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    presenter.dropSample(dataset.getSamples().get(0), dataset.getSamples().get(2),
        GridDropLocation.BELOW);
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertEquals((Long) 2L, samples.get(0).getId());
    assertEquals((Long) 3L, samples.get(1).getId());
    assertEquals((Long) 1L, samples.get(2).getId());
  }

  @Test
  public void dropSample_AboveMoveNearer() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    presenter.dropSample(dataset.getSamples().get(2), dataset.getSamples().get(0),
        GridDropLocation.ABOVE);
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertEquals((Long) 3L, samples.get(0).getId());
    assertEquals((Long) 1L, samples.get(1).getId());
    assertEquals((Long) 2L, samples.get(2).getId());
  }

  @Test
  public void dropSample_BelowMoveNearer() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    presenter.dropSample(dataset.getSamples().get(2), dataset.getSamples().get(0),
        GridDropLocation.BELOW);
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertEquals((Long) 1L, samples.get(0).getId());
    assertEquals((Long) 3L, samples.get(1).getId());
    assertEquals((Long) 2L, samples.get(2).getId());
  }

  @Test
  public void dropSample_Save() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);
    presenter.dropSample(dataset.getSamples().get(0), dataset.getSamples().get(2),
        GridDropLocation.ABOVE);
    List<Sample> samples = items(dialog.samples);
    assertEquals(3, samples.size());
    assertEquals((Long) 2L, samples.get(0).getId());
    assertEquals((Long) 1L, samples.get(1).getId());
    assertEquals((Long) 3L, samples.get(2).getId());

    presenter.save();

    verify(service).save(datasetCaptor.capture());
    dataset = datasetCaptor.getValue();
    samples = dataset.getSamples();
    assertEquals(3, samples.size());
    assertEquals((Long) 2L, samples.get(0).getId());
    assertEquals((Long) 1L, samples.get(1).getId());
    assertEquals((Long) 3L, samples.get(2).getId());
  }

  @Test
  public void save_NamePrefixEmpty() {
    fillForm();
    dialog.namePrefix.setValue("");

    presenter.save();

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.namePrefix);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_TagsEmpty() {
    fillForm();
    dialog.tags.setValue(new HashSet<>());

    presenter.save();

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertTrue(dataset.getTags().isEmpty());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_NoteEmpty() {
    fillForm();
    dialog.note.setValue("");

    presenter.save();

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_DateEmpty() {
    fillForm();
    dialog.date.setValue(null);

    presenter.save();

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.date);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_NameExists() {
    Dataset dataset = repository.findById(2L).get();
    when(service.exists(any())).thenReturn(true);
    presenter.setDataset(dataset);

    presenter.save();

    verify(service).exists("ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022");
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_NameExistsSameDataset() {
    Dataset dataset = repository.findById(2L).get();
    when(service.exists(any())).thenReturn(true);
    when(service.get(any())).thenReturn(Optional.of(dataset));
    presenter.setDataset(dataset);

    presenter.save();

    verify(service).exists("ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022");
    verify(service).get(2L);
    verify(service).save(any());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_NewDataset() {
    fillForm();

    presenter.save();

    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertEquals(namePrefix + "_" + nameDateFormatter.format(date), dataset.getName());
    assertEquals(2, dataset.getTags().size());
    assertTrue(dataset.getTags().contains(tag1));
    assertTrue(dataset.getTags().contains(tag2));
    assertEquals(note, dataset.getNote());
    assertEquals(date, dataset.getDate());
    assertEquals(0, dataset.getSamples().size());
    verify(dialog).showNotification(resources.message(SAVED, dataset.getName()));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateDataset() {
    Dataset dataset = repository.findById(2L).get();
    presenter.setDataset(dataset);
    fillForm();

    presenter.save();

    verify(service).save(datasetCaptor.capture());
    dataset = datasetCaptor.getValue();
    assertEquals(namePrefix + "_" + nameDateFormatter.format(date), dataset.getName());
    assertEquals(2, dataset.getTags().size());
    assertTrue(dataset.getTags().contains(tag1));
    assertTrue(dataset.getTags().contains(tag2));
    assertEquals(note, dataset.getNote());
    assertEquals(date, dataset.getDate());
    assertEquals(2, dataset.getSamples().size());
    Sample expectedSample = sampleRepository.findById(4L).get();
    Sample sample = dataset.getSamples().get(0);
    assertEquals(expectedSample.getSampleId(), sample.getSampleId());
    assertEquals(expectedSample.getReplicate(), sample.getReplicate());
    assertEquals(expectedSample.getProtocol().getId(), sample.getProtocol().getId());
    assertEquals(expectedSample.getAssay(), sample.getAssay());
    assertEquals(expectedSample.getType(), sample.getType());
    assertEquals(expectedSample.getTarget(), sample.getTarget());
    assertEquals(expectedSample.getStrain(), sample.getStrain());
    assertEquals(expectedSample.getStrainDescription(), sample.getStrainDescription());
    assertEquals(expectedSample.getTreatment(), sample.getTreatment());
    assertEquals(expectedSample.getNote(), sample.getNote());
    assertEquals(expectedSample.getDate(), sample.getDate());
    expectedSample = sampleRepository.findById(5L).get();
    sample = dataset.getSamples().get(1);
    assertEquals(expectedSample.getSampleId(), sample.getSampleId());
    assertEquals(expectedSample.getReplicate(), sample.getReplicate());
    assertEquals(expectedSample.getProtocol().getId(), sample.getProtocol().getId());
    assertEquals(expectedSample.getAssay(), sample.getAssay());
    assertEquals(expectedSample.getType(), sample.getType());
    assertEquals(expectedSample.getTarget(), sample.getTarget());
    assertEquals(expectedSample.getStrain(), sample.getStrain());
    assertEquals(expectedSample.getStrainDescription(), sample.getStrainDescription());
    assertEquals(expectedSample.getTreatment(), sample.getTreatment());
    assertEquals(expectedSample.getNote(), sample.getNote());
    assertEquals(expectedSample.getDate(), sample.getDate());
    verify(dialog).showNotification(resources.message(SAVED, dataset.getName()));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void cancel_Close() {
    presenter.cancel();

    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void delete() {
    Dataset dataset = repository.findById(1L).get();
    presenter.setDataset(dataset);

    presenter.delete();

    verify(service, never()).save(any());
    verify(service).delete(dataset);
    verify(dialog).close();
    verify(dialog).showNotification(resources.message(DELETED, dataset.getName()));
    verify(dialog, never()).fireSavedEvent();
    verify(dialog).fireDeletedEvent();
  }
}
