package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.HELPER;
import static ca.qc.ircm.lanaseq.Constants.REMOVE;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.FILENAMES;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.KEYWORDS;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NOTE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.SAMPLES;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.ADD_SAMPLE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETE_HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.DELETE_MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.GENERATE_NAME;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.NAME_PREFIX;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.NAME_PREFIX_REGEX_ERROR;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAMPLES_HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAVED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.id;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ASSAY;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN_DESCRIPTION;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TARGET;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TREATMENT;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TYPE;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateEquals;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.web.DatePickerInternationalization.englishDatePickerI18n;
import static ca.qc.ircm.lanaseq.web.DatePickerInternationalization.frenchDatePickerI18n;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleProperties;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridNoneSelectionModel;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.dnd.GridDragEndEvent;
import com.vaadin.flow.component.grid.dnd.GridDragStartEvent;
import com.vaadin.flow.component.grid.dnd.GridDropEvent;
import com.vaadin.flow.component.grid.dnd.GridDropLocation;
import com.vaadin.flow.component.grid.dnd.GridDropMode;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import elemental.json.impl.JsonUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link DatasetDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetDialogTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(DatasetDialog.class);
  private static final String DATASET_PREFIX = messagePrefix(Dataset.class);
  private static final String SAMPLE_PREFIX = messagePrefix(Sample.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private DatasetDialog dialog;
  @MockitoBean
  private DatasetService service;
  @Mock
  private ComponentEventListener<SavedEvent<DatasetDialog>> savedListener;
  @Mock
  private ComponentEventListener<DeletedEvent<DatasetDialog>> deletedListener;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Autowired
  private SampleRepository sampleRepository;
  private final Locale locale = Locale.ENGLISH;
  private List<Sample> samples;
  private final List<String> topKeywords = new ArrayList<>();
  private final DateTimeFormatter nameDateFormatter = DateTimeFormatter.BASIC_ISO_DATE;
  private final String namePrefix = "ChIPseq_IP_polr3a_yFR20_WT_37C_testsample1-testsample2";
  private final String keyword1 = "Keyword 1";
  private final String keyword2 = "Keyword 2";
  private final String filename1 = "OF_20241120_ROB_01";
  private final String filename2 = "OF_20241120_ROB_02";
  private final LocalDate date = LocalDate.of(2020, 7, 20);
  private final String note = "test note\nsecond line";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(service.get(anyLong())).then(i -> repository.findById(i.getArgument(0)));
    samples = sampleRepository.findAll();
    topKeywords.add("input");
    topKeywords.add("chip");
    when(service.topKeywords(anyInt())).thenReturn(topKeywords);
    UI.getCurrent().setLocale(locale);
    DatasetsView view = navigate(DatasetsView.class);
    view.datasets.setItems(repository.findAll());
    test(view.datasets).doubleClickRow(1);
    dialog = $(DatasetDialog.class).first();
  }

  private Sample name(String name) {
    Sample sample = new Sample();
    sample.setName(name);
    return sample;
  }

  private void fillForm() {
    dialog.namePrefix.setValue(namePrefix);
    dialog.date.setValue(date);
    dialog.keywords.setValue(Stream.of(keyword1, keyword2).collect(Collectors.toSet()));
    dialog.filenames.setValue(Stream.of(filename1, filename2).collect(Collectors.toSet()));
    dialog.note.setValue(note);
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(NAME_PREFIX), dialog.namePrefix.getId().orElse(""));
    assertEquals(id(GENERATE_NAME), dialog.generateName.getId().orElse(""));
    validateIcon(VaadinIcon.MAGIC.create(), dialog.generateName.getIcon());
    assertEquals(id(KEYWORDS), dialog.keywords.getId().orElse(""));
    assertEquals(id(FILENAMES), dialog.filenames.getId().orElse(""));
    assertEquals(id(PROTOCOL), dialog.protocol.getId().orElse(""));
    assertEquals(id(ASSAY), dialog.assay.getId().orElse(""));
    assertEquals(id(TYPE), dialog.type.getId().orElse(""));
    assertEquals(id(TARGET), dialog.target.getId().orElse(""));
    assertEquals(id(STRAIN), dialog.strain.getId().orElse(""));
    assertEquals(id(STRAIN_DESCRIPTION), dialog.strainDescription.getId().orElse(""));
    assertEquals(id(TREATMENT), dialog.treatment.getId().orElse(""));
    assertEquals(id(NOTE), dialog.note.getId().orElse(""));
    assertEquals(id(SAMPLES_HEADER), dialog.samplesHeader.getId().orElse(""));
    assertEquals(id(SAMPLES), dialog.samples.getId().orElse(""));
    assertEquals(id(ADD_SAMPLE), dialog.addSample.getId().orElse(""));
    validateIcon(VaadinIcon.PLUS.create(), dialog.addSample.getIcon());
    assertEquals(id(ERROR_TEXT), dialog.error.getId().orElse(""));
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
    Dataset dataset = service.get(2L).orElseThrow();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, dataset.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + NAME_PREFIX), dialog.namePrefix.getLabel());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + GENERATE_NAME),
        dialog.generateName.getText());
    assertEquals(dialog.getTranslation(DATASET_PREFIX + KEYWORDS), dialog.keywords.getLabel());
    assertEquals(dialog.getTranslation(DATASET_PREFIX + FILENAMES), dialog.filenames.getLabel());
    assertEquals(dialog.getTranslation(DATASET_PREFIX + property(FILENAMES, HELPER)),
        dialog.filenames.getHelperText());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + PROTOCOL), dialog.protocol.getLabel());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + ASSAY), dialog.assay.getLabel());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + TYPE), dialog.type.getLabel());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + TARGET), dialog.target.getLabel());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + STRAIN), dialog.strain.getLabel());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + STRAIN_DESCRIPTION),
        dialog.strainDescription.getLabel());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + TREATMENT), dialog.treatment.getLabel());
    assertEquals(dialog.getTranslation(DATASET_PREFIX + NOTE), dialog.note.getLabel());
    assertEquals(dialog.getTranslation(DATASET_PREFIX + DATE), dialog.date.getLabel());
    validateEquals(englishDatePickerI18n(), dialog.date.getI18n());
    assertEquals(Locale.CANADA, dialog.date.getLocale());
    assertEquals(dialog.getTranslation(DATASET_PREFIX + SAMPLES), dialog.samplesHeader.getText());
    HeaderRow headerRow = dialog.samples.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + SampleProperties.NAME),
        headerRow.getCell(dialog.sampleName).getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + ADD_SAMPLE), dialog.addSample.getText());
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
    Dataset dataset = service.get(2L).orElseThrow();
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, dataset.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + NAME_PREFIX), dialog.namePrefix.getLabel());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + GENERATE_NAME),
        dialog.generateName.getText());
    assertEquals(dialog.getTranslation(DATASET_PREFIX + KEYWORDS), dialog.keywords.getLabel());
    assertEquals(dialog.getTranslation(DATASET_PREFIX + FILENAMES), dialog.filenames.getLabel());
    assertEquals(dialog.getTranslation(DATASET_PREFIX + property(FILENAMES, HELPER)),
        dialog.filenames.getHelperText());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + PROTOCOL), dialog.protocol.getLabel());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + ASSAY), dialog.assay.getLabel());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + TYPE), dialog.type.getLabel());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + TARGET), dialog.target.getLabel());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + STRAIN), dialog.strain.getLabel());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + STRAIN_DESCRIPTION),
        dialog.strainDescription.getLabel());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + TREATMENT), dialog.treatment.getLabel());
    assertEquals(dialog.getTranslation(DATASET_PREFIX + NOTE), dialog.note.getLabel());
    assertEquals(dialog.getTranslation(DATASET_PREFIX + DATE), dialog.date.getLabel());
    validateEquals(frenchDatePickerI18n(), dialog.date.getI18n());
    assertEquals(Locale.CANADA, dialog.date.getLocale());
    assertEquals(dialog.getTranslation(DATASET_PREFIX + SAMPLES), dialog.samplesHeader.getText());
    HeaderRow headerRow = dialog.samples.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + SampleProperties.NAME),
        headerRow.getCell(dialog.sampleName).getText());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + ADD_SAMPLE), dialog.addSample.getText());
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
  public void date_NoValue() {
    dialog.date.setValue(null);
    assertNull(dialog.date.getValue());
    assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1-JS2", dialog.namePrefix.getValue());
  }

  @Test
  public void keywords() {
    List<String> keywords = dialog.keywords.getSuggestions();
    assertEquals(topKeywords.size(), keywords.size());
    for (String keyword : topKeywords) {
      assertTrue(keywords.contains(keyword));
    }
  }

  @Test
  public void filenames() {
    List<String> filenames = dialog.filenames.getSuggestions();
    assertTrue(filenames.isEmpty());
  }

  @Test
  public void samples() {
    assertEquals(2, dialog.samples.getColumns().size());
    assertNotNull(dialog.samples.getColumnByKey(SampleProperties.NAME));
    assertTrue(dialog.sampleName.isSortable());
    assertNotNull(dialog.samples.getColumnByKey(REMOVE));
    assertFalse(dialog.sampleRemove.isSortable());
    assertInstanceOf(GridNoneSelectionModel.class, dialog.samples.getSelectionModel());
    assertTrue(dialog.samples.isRowsDraggable());
    Dataset dataset = service.get(2L).orElseThrow();
    assertEquals(dataset.getSamples().size(), dialog.samples.getListDataView().getItemCount());
    for (Sample sample : dataset.getSamples()) {
      assertTrue(dialog.samples.getListDataView().contains(sample));
    }
  }

  @Test
  public void samples_ColumnsValueProvider() {
    dialog.samples.setItems(samples);
    for (int i = 0; i < samples.size(); i++) {
      Sample sample = samples.get(i);
      assertEquals(sample.getName(), test(dialog.samples).getCellText(i,
          dialog.samples.getColumns().indexOf(dialog.sampleName)));
      ComponentRenderer<Button, Sample> buttonRenderer = (ComponentRenderer<Button, Sample>) dialog.sampleRemove.getRenderer();
      Button button = buttonRenderer.createComponent(sample);
      assertTrue(button.hasClassName(REMOVE));
    }
  }

  @Test
  public void samples_NameColumnComparator() {
    Comparator<Sample> comparator = dialog.sampleName.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(name("éê"), name("ee")));
    assertTrue(comparator.compare(name("a"), name("e")) < 0);
    assertTrue(comparator.compare(name("a"), name("é")) < 0);
    assertTrue(comparator.compare(name("e"), name("a")) > 0);
    assertTrue(comparator.compare(name("é"), name("a")) > 0);
  }

  @Test
  public void samples_Remove() {
    dialog.protocol.setValue("Histone FLAG, BioID");
    dialog.assay.setValue("ChIP-seq, 2");
    dialog.type.setValue("Input, 2");
    dialog.target.setValue("Spt16, polr2c");
    dialog.strain.setValue("yFR101, yBC201");
    dialog.strainDescription.setValue("G24D, WT");
    dialog.treatment.setValue("1, 2");
    Dataset dataset = service.get(2L).orElseThrow();
    Sample sample = dataset.getSamples().get(0);
    ComponentRenderer<Button, Sample> buttonRenderer = (ComponentRenderer<Button, Sample>) dialog.sampleRemove.getRenderer();
    Button button = buttonRenderer.createComponent(sample);
    clickButton(button);
    assertEquals(1, dialog.samples.getListDataView().getItemCount());
    List<Sample> samples = dialog.samples.getListDataView().getItems().toList();
    assertTrue(find(samples, 5L).isPresent());
    assertEquals("Histone FLAG", dialog.protocol.getValue());
    assertEquals("ChIP-seq", dialog.assay.getValue());
    assertEquals("", dialog.type.getValue());
    assertEquals("Spt16", dialog.target.getValue());
    assertEquals("yFR101", dialog.strain.getValue());
    assertEquals("G24D", dialog.strainDescription.getValue());
    assertEquals("", dialog.treatment.getValue());
  }

  @Test
  public void samples_DragAndDrop_Above() {
    dialog.samples.setItems(new ArrayList<>(samples));
    Sample sample = samples.get(0);
    Sample droppedSample = samples.get(2);
    GridDragStartEvent<Sample> dragStartEvent = new GridDragStartEvent<>(dialog.samples, false,
        JsonUtil.parse(
            "{'draggedItems':[{'key':'" + dialog.samples.getDataCommunicator().getKeyMapper()
                .key(sample) + "'}]}"));
    fireEvent(dialog.samples, dragStartEvent);
    assertEquals(GridDropMode.BETWEEN, dialog.samples.getDropMode());
    GridDropEvent<Sample> dropEvent = new GridDropEvent<>(dialog.samples, false, JsonUtil.parse(
        "{'key':'" + dialog.samples.getDataCommunicator().getKeyMapper().key(droppedSample) + "'}"),
        GridDropLocation.ABOVE.getClientName(), JsonUtil.parse("[]"));
    fireEvent(dialog.samples, dropEvent);
    List<Sample> samples = dialog.samples.getListDataView().getItems().toList();
    assertEquals(this.samples.size(), samples.size());
    assertEquals(this.samples.get(1), samples.get(0));
    assertEquals(this.samples.get(0), samples.get(1));
    for (int i = 2; i < samples.size(); i++) {
      assertEquals(this.samples.get(i), samples.get(i));
    }
    GridDragEndEvent<Sample> dragEndEvent = new GridDragEndEvent<>(dialog.samples, false);
    fireEvent(dialog.samples, dragEndEvent);
    assertNull(dialog.samples.getDropMode());

    clickButton(dialog.save);
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    samples = dataset.getSamples();
    assertEquals(this.samples.size(), samples.size());
    assertEquals(this.samples.get(1), samples.get(0));
    assertEquals(this.samples.get(0), samples.get(1));
    for (int i = 2; i < samples.size(); i++) {
      assertEquals(this.samples.get(i), samples.get(i));
    }
  }

  @Test
  public void samples_DragAndDrop_Below() {
    dialog.samples.setItems(new ArrayList<>(samples));
    Sample sample = samples.get(0);
    Sample droppedSample = samples.get(2);
    GridDragStartEvent<Sample> dragStartEvent = new GridDragStartEvent<>(dialog.samples, false,
        JsonUtil.parse(
            "{'draggedItems':[{'key':'" + dialog.samples.getDataCommunicator().getKeyMapper()
                .key(sample) + "'}]}"));
    fireEvent(dialog.samples, dragStartEvent);
    assertEquals(GridDropMode.BETWEEN, dialog.samples.getDropMode());
    GridDropEvent<Sample> dropEvent = new GridDropEvent<>(dialog.samples, false, JsonUtil.parse(
        "{'key':'" + dialog.samples.getDataCommunicator().getKeyMapper().key(droppedSample) + "'}"),
        GridDropLocation.BELOW.getClientName(), JsonUtil.parse("[]"));
    fireEvent(dialog.samples, dropEvent);
    List<Sample> samples = dialog.samples.getListDataView().getItems().toList();
    assertEquals(this.samples.size(), samples.size());
    assertEquals(this.samples.get(1), samples.get(0));
    assertEquals(this.samples.get(2), samples.get(1));
    assertEquals(this.samples.get(0), samples.get(2));
    for (int i = 3; i < samples.size(); i++) {
      assertEquals(this.samples.get(i), samples.get(i));
    }
    GridDragEndEvent<Sample> dragEndEvent = new GridDragEndEvent<>(dialog.samples, false);
    fireEvent(dialog.samples, dragEndEvent);
    assertNull(dialog.samples.getDropMode());

    clickButton(dialog.save);
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    samples = dataset.getSamples();
    assertEquals(this.samples.size(), samples.size());
    assertEquals(this.samples.get(1), samples.get(0));
    assertEquals(this.samples.get(2), samples.get(1));
    assertEquals(this.samples.get(0), samples.get(2));
    for (int i = 3; i < samples.size(); i++) {
      assertEquals(this.samples.get(i), samples.get(i));
    }
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
  public void getDatasetId() {
    assertEquals(2L, dialog.getDatasetId());
  }

  @Test
  public void setDatasetId_Dataset() {
    Dataset dataset = repository.findById(2L).orElseThrow();

    dialog.setDatasetId(2L);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, dataset.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_MESSAGE, dataset.getName()),
        dialog.confirm.getElement().getProperty("message"));
    assertEquals("ChIPseq_Spt16_yFR101_G24D_JS1-JS2", dialog.namePrefix.getValue());
    assertFalse(dialog.namePrefix.isReadOnly());
    assertTrue(dialog.generateName.isVisible());
    assertFalse(dialog.keywords.isReadOnly());
    assertFalse(dialog.filenames.isReadOnly());
    assertTrue(dialog.protocol.isReadOnly());
    assertTrue(dialog.assay.isReadOnly());
    assertTrue(dialog.type.isReadOnly());
    assertTrue(dialog.target.isReadOnly());
    assertTrue(dialog.strain.isReadOnly());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertTrue(dialog.treatment.isReadOnly());
    assertEquals(LocalDate.of(2018, 10, 22), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    assertEquals("", dialog.note.getValue());
    assertFalse(dialog.note.isReadOnly());
    assertEquals(2, dialog.samples.getListDataView().getItemCount());
    List<Sample> samples = dialog.samples.getListDataView().getItems().toList();
    assertTrue(find(samples, 4L).isPresent());
    assertTrue(find(samples, 5L).isPresent());
    assertTrue(dialog.sampleRemove.isVisible());
    assertTrue(dialog.addSample.isVisible());
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertTrue(dialog.delete.isVisible());
  }

  @Test
  public void setDatasetId_NoPrefix() {
    dialog.setDatasetId(3L);

    assertEquals("", dialog.namePrefix.getValue());
  }

  @Test
  public void setDatasetId_CannotWrite() {
    Dataset dataset = repository.findById(4L).orElseThrow();

    dialog.setDatasetId(4L);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, dataset.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_MESSAGE, dataset.getName()),
        dialog.confirm.getElement().getProperty("message"));
    assertEquals("ChIPseq_IP_yBC102_R103S_BC1-BC2", dialog.namePrefix.getValue());
    assertTrue(dialog.namePrefix.isReadOnly());
    assertFalse(dialog.generateName.isVisible());
    assertTrue(dialog.keywords.isReadOnly());
    assertTrue(dialog.filenames.isReadOnly());
    assertTrue(dialog.protocol.isReadOnly());
    assertTrue(dialog.assay.isReadOnly());
    assertTrue(dialog.type.isReadOnly());
    assertTrue(dialog.target.isReadOnly());
    assertTrue(dialog.strain.isReadOnly());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertTrue(dialog.treatment.isReadOnly());
    assertEquals(LocalDate.of(2018, 11, 18), dialog.date.getValue());
    assertTrue(dialog.date.isReadOnly());
    assertEquals("", dialog.note.getValue());
    assertTrue(dialog.note.isReadOnly());
    assertEquals(2, dialog.samples.getListDataView().getItemCount());
    List<Sample> samples = dialog.samples.getListDataView().getItems().toList();
    assertTrue(find(samples, 6L).isPresent());
    assertTrue(find(samples, 7L).isPresent());
    assertFalse(dialog.sampleRemove.isVisible());
    assertFalse(dialog.addSample.isVisible());
    assertFalse(dialog.save.isVisible());
    assertFalse(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void setDatasetId_NotEditable() {
    Dataset dataset = repository.findById(5L).orElseThrow();

    dialog.setDatasetId(5L);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 1, dataset.getName()),
        dialog.getHeaderTitle());
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETE_MESSAGE, dataset.getName()),
        dialog.confirm.getElement().getProperty("message"));
    assertEquals("ChIPseq_IP_polr2b_yBC103_WT_BC1", dialog.namePrefix.getValue());
    assertTrue(dialog.namePrefix.isReadOnly());
    assertFalse(dialog.generateName.isVisible());
    assertTrue(dialog.keywords.isReadOnly());
    assertTrue(dialog.filenames.isReadOnly());
    assertTrue(dialog.protocol.isReadOnly());
    assertTrue(dialog.assay.isReadOnly());
    assertTrue(dialog.type.isReadOnly());
    assertTrue(dialog.target.isReadOnly());
    assertTrue(dialog.strain.isReadOnly());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertTrue(dialog.treatment.isReadOnly());
    assertEquals(LocalDate.of(2018, 12, 5), dialog.date.getValue());
    assertTrue(dialog.date.isReadOnly());
    assertEquals("", dialog.note.getValue());
    assertTrue(dialog.note.isReadOnly());
    assertEquals(1, dialog.samples.getListDataView().getItemCount());
    List<Sample> samples = dialog.samples.getListDataView().getItems().toList();
    assertTrue(find(samples, 8L).isPresent());
    assertFalse(dialog.sampleRemove.isVisible());
    assertFalse(dialog.addSample.isVisible());
    assertFalse(dialog.save.isVisible());
    assertFalse(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void setDatasetId_SampleWithDifferentFields() {
    Dataset dataset = repository.findById(1L).orElseThrow();
    Sample sample = sampleRepository.findById(4L).orElseThrow();
    sample.setType("Input");
    sample.setTreatment("Heat shock");
    dataset.getSamples().add(sample);

    dialog.setDatasetId(1L);

    assertEquals("FLAG, Histone FLAG", dialog.protocol.getValue());
    assertEquals("MNase-seq" + ", " + "ChIP-seq", dialog.assay.getValue());
    assertEquals("IP, Input", dialog.type.getValue());
    assertEquals("polr2a, Spt16", dialog.target.getValue());
    assertEquals("yFR100, yFR101", dialog.strain.getValue());
    assertEquals("WT, G24D", dialog.strainDescription.getValue());
    assertEquals("Rappa, Heat shock", dialog.treatment.getValue());
  }

  @Test
  public void setDatasetId_SampleWithEmtpyFields() {
    Dataset dataset = repository.findById(1L).orElseThrow();
    Sample sample = dataset.getSamples().get(0);
    sample.setType(null);
    sample.setTarget(null);
    sample.setStrainDescription(null);
    sample.setTreatment(null);

    dialog.setDatasetId(1L);

    assertEquals("FLAG", dialog.protocol.getValue());
    assertEquals("MNase-seq", dialog.assay.getValue());
    assertEquals("IP", dialog.type.getValue());
    assertEquals("polr2a", dialog.target.getValue());
    assertEquals("yFR100", dialog.strain.getValue());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertEquals("Rappa", dialog.treatment.getValue());
  }

  @Test
  public void setDatasetId_0() {
    dialog.setDatasetId(0);

    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + HEADER, 0), dialog.getHeaderTitle());
    assertEquals("", dialog.namePrefix.getValue());
    assertFalse(dialog.namePrefix.isReadOnly());
    assertTrue(dialog.generateName.isVisible());
    assertFalse(dialog.keywords.isReadOnly());
    assertFalse(dialog.filenames.isReadOnly());
    assertTrue(dialog.protocol.isReadOnly());
    assertTrue(dialog.assay.isReadOnly());
    assertTrue(dialog.type.isReadOnly());
    assertTrue(dialog.target.isReadOnly());
    assertTrue(dialog.strain.isReadOnly());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertTrue(dialog.treatment.isReadOnly());
    assertTrue(LocalDate.now().isEqual(dialog.date.getValue()));
    assertFalse(dialog.date.isReadOnly());
    assertEquals("", dialog.note.getValue());
    assertFalse(dialog.note.isReadOnly());
    assertEquals(0, dialog.samples.getListDataView().getItemCount());
    assertTrue(dialog.sampleRemove.isVisible());
    assertTrue(dialog.addSample.isVisible());
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertTrue(dialog.delete.isVisible());
  }

  @Test
  public void requiredIndicator() {
    assertTrue(dialog.namePrefix.isRequiredIndicatorVisible());
    assertFalse(dialog.keywords.isRequiredIndicatorVisible());
    assertFalse(dialog.filenames.isRequiredIndicatorVisible());
    assertTrue(dialog.date.isRequiredIndicatorVisible());
    assertFalse(dialog.note.isRequiredIndicatorVisible());
  }

  @Test
  public void generateName() {
    dialog.namePrefix.setValue("test");
    dialog.date.setValue(LocalDate.of(2022, 5, 2));
    assertEquals("test", dialog.namePrefix.getValue());

    clickButton(dialog.generateName);

    dialog.namePrefix.setValue("ChIPseq_Spt16_yFR101_G24D_JS1-JS2");
  }

  @Test
  public void generateName_NewSample() {
    dialog.namePrefix.setValue("test");
    dialog.date.setValue(LocalDate.of(2022, 5, 2));
    dialog.samples.getListDataView().addItem(samples.get(0));
    assertEquals("test", dialog.namePrefix.getValue());

    dialog.generateName();

    dialog.namePrefix.setValue("ChIPseq_Spt16_yFR101_G24D_JS1-JS2-FR1");
  }

  @Test
  public void addSample() {
    clickButton(dialog.addSample);

    @SuppressWarnings("unchecked") Grid<Sample> selectSampleDialogGrid = $(Grid.class).id(
        SelectSampleDialog.id(SelectSampleDialog.SAMPLES));
    selectSampleDialogGrid.sort(
        GridSortOrder.desc(selectSampleDialogGrid.getColumnByKey(SampleProperties.DATE)).build());
    TextField ownerFilter = (TextField) selectSampleDialogGrid.getHeaderRows().get(1)
        .getCell(selectSampleDialogGrid.getColumnByKey(SampleProperties.OWNER)).getComponent();
    ownerFilter.setValue("");
    test(selectSampleDialogGrid).doubleClickRow(2);

    assertEquals(3, dialog.samples.getListDataView().getItemCount());
    List<Sample> samples = dialog.samples.getListDataView().getItems().toList();
    assertTrue(find(samples, 4L).isPresent());
    assertTrue(find(samples, 5L).isPresent());
    assertTrue(find(samples, 9L).isPresent());
    assertEquals("Histone FLAG, BioID", dialog.protocol.getValue());
    assertEquals("ChIP-seq", dialog.assay.getValue());
    assertEquals("Input", dialog.type.getValue());
    assertEquals("Spt16, polr2c", dialog.target.getValue());
    assertEquals("yFR101, yBC201", dialog.strain.getValue());
    assertEquals("G24D, WT", dialog.strainDescription.getValue());
    assertEquals("", dialog.treatment.getValue());
  }

  @Test
  public void addSample_AlreadyInDataset() {
    clickButton(dialog.addSample);

    @SuppressWarnings("unchecked") Grid<Sample> selectSampleDialogGrid = $(Grid.class).id(
        SelectSampleDialog.id(SelectSampleDialog.SAMPLES));
    selectSampleDialogGrid.sort(
        GridSortOrder.desc(selectSampleDialogGrid.getColumnByKey(SampleProperties.DATE)).build());
    test(selectSampleDialogGrid).doubleClickRow(3);

    assertEquals(2, dialog.samples.getListDataView().getItemCount());
    List<Sample> samples = dialog.samples.getListDataView().getItems().toList();
    assertTrue(find(samples, 4L).isPresent());
    assertTrue(find(samples, 5L).isPresent());
  }

  @Test
  public void save_NamePrefixEmpty() {
    dialog.addSavedListener(savedListener);
    fillForm();
    dialog.namePrefix.setValue("");

    clickButton(dialog.save);

    BinderValidationStatus<Dataset> status = dialog.validateDataset();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError = findValidationStatusByField(status,
        dialog.namePrefix);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(dialog.getTranslation(CONSTANTS_PREFIX + REQUIRED)),
        error.getMessage());
    verify(service, never()).save(any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_NamePrefixInvalid() {
    save_NamePrefixInvalid("MyDataset#Cool");
    save_NamePrefixInvalid("MyDataset%Cool");
    save_NamePrefixInvalid("MyDataset&Cool");
    save_NamePrefixInvalid("MyDataset{Cool");
    save_NamePrefixInvalid("MyDataset}Cool");
    save_NamePrefixInvalid("MyDataset\\Cool");
    save_NamePrefixInvalid("MyDataset<Cool");
    save_NamePrefixInvalid("MyDataset>Cool");
    save_NamePrefixInvalid("MyDataset*Cool");
    save_NamePrefixInvalid("MyDataset?Cool");
    save_NamePrefixInvalid("MyDataset/Cool");
    save_NamePrefixInvalid("MyDataset Cool");
    save_NamePrefixInvalid("MyDataset$Cool");
    save_NamePrefixInvalid("MyDataset!Cool");
    save_NamePrefixInvalid("MyDataset'Cool");
    save_NamePrefixInvalid("MyDataset\"Cool");
    save_NamePrefixInvalid("MyDataset:Cool");
    save_NamePrefixInvalid("MyDataset@Cool");
    save_NamePrefixInvalid("MyDataset+Cool");
    save_NamePrefixInvalid("MyDataset`Cool");
    save_NamePrefixInvalid("MyDataset|Cool");
    save_NamePrefixInvalid("MyDataset=Cool");
  }

  private void save_NamePrefixInvalid(String namePrefix) {
    dialog.addSavedListener(savedListener);
    fillForm();
    dialog.namePrefix.setValue(namePrefix);

    clickButton(dialog.save);

    BinderValidationStatus<Dataset> status = dialog.validateDataset();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError = findValidationStatusByField(status,
        dialog.namePrefix);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(dialog.getTranslation(MESSAGE_PREFIX + NAME_PREFIX_REGEX_ERROR)),
        error.getMessage());
    verify(service, never()).save(any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_KeywordsEmpty() {
    dialog.addSavedListener(savedListener);
    fillForm();
    dialog.keywords.setValue(new HashSet<>());

    clickButton(dialog.save);

    BinderValidationStatus<Dataset> status = dialog.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertTrue(dataset.getKeywords().isEmpty());
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, dataset.getName()),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void save_FilenamesEmpty() {
    dialog.addSavedListener(savedListener);
    fillForm();
    dialog.filenames.setValue(new HashSet<>());

    clickButton(dialog.save);

    BinderValidationStatus<Dataset> status = dialog.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertTrue(dataset.getFilenames().isEmpty());
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, dataset.getName()),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void save_NoteEmpty() {
    dialog.addSavedListener(savedListener);
    fillForm();
    dialog.note.setValue("");

    clickButton(dialog.save);

    BinderValidationStatus<Dataset> status = dialog.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, dataset.getName()),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void save_DateEmpty() {
    dialog.addSavedListener(savedListener);
    fillForm();
    dialog.date.setValue(null);

    clickButton(dialog.save);

    BinderValidationStatus<Dataset> status = dialog.validateDataset();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError = findValidationStatusByField(status,
        dialog.date);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(dialog.getTranslation(CONSTANTS_PREFIX + REQUIRED)),
        error.getMessage());
    verify(service, never()).save(any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_NameExists() {
    dialog.addSavedListener(savedListener);
    when(service.exists(any())).thenReturn(true);
    when(service.get(anyLong())).thenReturn(repository.findById(2L))
        .thenReturn(repository.findById(1L));
    dialog.setDatasetId(2L);

    clickButton(dialog.save);

    verify(service).exists("ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022");
    verify(service, never()).save(any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_NameExistsSameDataset() {
    dialog.addSavedListener(savedListener);
    Dataset dataset = repository.findById(2L).orElseThrow();
    when(service.exists(any())).thenReturn(true);
    when(service.get(anyLong())).thenReturn(Optional.of(dataset));
    dialog.setDatasetId(2L);

    clickButton(dialog.save);

    verify(service).exists("ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022");
    verify(service, atLeastOnce()).get(2L);
    verify(service).save(any());
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, dataset.getName()),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void save_NewDataset() {
    dialog.addSavedListener(savedListener);
    dialog.setDatasetId(0);
    fillForm();

    clickButton(dialog.save);

    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertEquals(namePrefix + "_" + nameDateFormatter.format(date), dataset.getName());
    assertEquals(2, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains(keyword1));
    assertTrue(dataset.getKeywords().contains(keyword2));
    assertEquals(2, dataset.getFilenames().size());
    assertTrue(dataset.getFilenames().contains(filename1));
    assertTrue(dataset.getFilenames().contains(filename2));
    assertEquals(note, dataset.getNote());
    assertEquals(date, dataset.getDate());
    assertEquals(0, dataset.getSamples().size());
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, dataset.getName()),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void save_UpdateDataset() {
    dialog.addSavedListener(savedListener);
    dialog.setDatasetId(2L);
    fillForm();

    clickButton(dialog.save);

    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertEquals(namePrefix + "_" + nameDateFormatter.format(date), dataset.getName());
    assertEquals(2, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains(keyword1));
    assertTrue(dataset.getKeywords().contains(keyword2));
    assertEquals(2, dataset.getFilenames().size());
    assertTrue(dataset.getFilenames().contains(filename1));
    assertTrue(dataset.getFilenames().contains(filename2));
    assertEquals(note, dataset.getNote());
    assertEquals(date, dataset.getDate());
    assertEquals(2, dataset.getSamples().size());
    Sample expectedSample = sampleRepository.findById(4L).orElseThrow();
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
    expectedSample = sampleRepository.findById(5L).orElseThrow();
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
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + SAVED, dataset.getName()),
        test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void cancel() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);

    clickButton(dialog.cancel);

    assertFalse($(Notification.class).exists());
    assertFalse(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void delete_Confirm() {
    dialog.addDeletedListener(deletedListener);
    Dataset dataset = repository.findById(1L).orElseThrow();
    dialog.setDatasetId(1L);

    clickButton(dialog.delete);

    assertTrue(dialog.confirm.isOpened());
    ConfirmDialog.ConfirmEvent event = new ConfirmDialog.ConfirmEvent(dialog.confirm, false);
    fireEvent(dialog.confirm, event);
    verify(service, never()).save(any());
    verify(service).delete(dataset);
    assertFalse(dialog.isOpened());
    Notification notification = $(Notification.class).first();
    assertEquals(dialog.getTranslation(MESSAGE_PREFIX + DELETED, dataset.getName()),
        test(notification).getText());
    verify(deletedListener).onComponentEvent(any());
  }

  @Test
  public void delete_Cancel() {
    Dataset dataset = repository.findById(1L).orElseThrow();
    dialog.setDatasetId(1L);

    clickButton(dialog.delete);

    assertTrue(dialog.confirm.isOpened());
    ConfirmDialog.CancelEvent event = new ConfirmDialog.CancelEvent(dialog.confirm, false);
    fireEvent(dialog.confirm, event);
    verify(service, never()).save(any());
    verify(service, never()).delete(dataset);
    assertTrue(dialog.isOpened());
    assertFalse($(Notification.class).exists());
    verify(deletedListener, never()).onComponentEvent(any());
  }
}
