package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.Dataset.NAME_ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.KEYWORDS;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.OWNER;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.ANALYZE;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.FILES;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.ID;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.MERGE;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.MERGED;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.MERGE_ERROR;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES_MORE_THAN_ONE;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES_REQUIRED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.web.ErrorNotification;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.provider.SortOrder;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.testbench.unit.MetaKeys;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link SamplesView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SamplesViewTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(SamplesView.class);
  private static final String SAMPLE_PREFIX = messagePrefix(Sample.class);
  private static final String DATASET_PREFIX = messagePrefix(Dataset.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private SamplesView view;
  @MockitoBean
  private SampleService service;
  @MockitoBean
  private DatasetService datasetService;
  @Captor
  private ArgumentCaptor<Collection<Sample>> samplesCaptor;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Autowired
  private SampleRepository repository;
  @Mock
  private ListDataProvider<Sample> sampleDataProvider;
  private final Locale locale = Locale.ENGLISH;
  private List<Sample> samples;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(service.get(anyLong())).then(
        i -> i.getArgument(0) != null ? repository.findById(i.getArgument(0)) : Optional.empty());
    samples = repository.findAll();
    when(service.all(any(), any())).then(i -> samples.stream());
    UI.getCurrent().setLocale(locale);
    view = navigate(SamplesView.class);
  }

  private Sample name(String name) {
    Sample sample = new Sample();
    sample.setName(name);
    return sample;
  }

  private Sample protocol(String name) {
    Sample sample = new Sample();
    sample.setProtocol(new Protocol());
    sample.getProtocol().setName(name);
    return sample;
  }

  private Sample owner(String email) {
    Sample sample = new Sample();
    sample.setOwner(new User());
    sample.getOwner().setEmail(email);
    return sample;
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(SAMPLES, view.samples.getId().orElse(""));
    assertTrue(
        view.dateFilter.getThemeNames().contains(CustomFieldVariant.LUMO_SMALL.getVariantName()));
    assertEquals(ADD, view.add.getId().orElse(""));
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
    assertEquals(EDIT, view.edit.getId().orElse(""));
    validateIcon(VaadinIcon.EDIT.create(), view.edit.getIcon());
    assertEquals(MERGE, view.merge.getId().orElse(""));
    validateIcon(VaadinIcon.CONNECT.create(), view.merge.getIcon());
    assertEquals(FILES, view.files.getId().orElse(""));
    validateIcon(VaadinIcon.FILE_O.create(), view.files.getIcon());
    assertEquals(ANALYZE, view.analyze.getId().orElse(""));
  }

  @Test
  public void labels() {
    HeaderRow headerRow = view.samples.getHeaderRows().get(0);
    FooterRow footerRow = view.samples.getFooterRows().get(0);
    assertEquals(view.getTranslation(SAMPLE_PREFIX + NAME), headerRow.getCell(view.name).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + NAME), footerRow.getCell(view.name).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + KEYWORDS),
        headerRow.getCell(view.keywords).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + KEYWORDS),
        footerRow.getCell(view.keywords).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + PROTOCOL),
        headerRow.getCell(view.protocol).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + PROTOCOL),
        footerRow.getCell(view.protocol).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + DATE), headerRow.getCell(view.date).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + DATE), footerRow.getCell(view.date).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + OWNER),
        headerRow.getCell(view.owner).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + OWNER),
        footerRow.getCell(view.owner).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.nameFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.keywordsFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.protocolFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.ownerFilter.getPlaceholder());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ADD), view.add.getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + EDIT), view.edit.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MERGE), view.merge.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FILES), view.files.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ANALYZE), view.analyze.getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    HeaderRow headerRow = view.samples.getHeaderRows().get(0);
    FooterRow footerRow = view.samples.getFooterRows().get(0);
    assertEquals(view.getTranslation(SAMPLE_PREFIX + NAME), headerRow.getCell(view.name).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + NAME), footerRow.getCell(view.name).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + KEYWORDS),
        headerRow.getCell(view.keywords).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + KEYWORDS),
        footerRow.getCell(view.keywords).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + PROTOCOL),
        headerRow.getCell(view.protocol).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + PROTOCOL),
        footerRow.getCell(view.protocol).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + DATE), headerRow.getCell(view.date).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + DATE), footerRow.getCell(view.date).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + OWNER),
        headerRow.getCell(view.owner).getText());
    assertEquals(view.getTranslation(SAMPLE_PREFIX + OWNER),
        footerRow.getCell(view.owner).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.nameFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.keywordsFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.protocolFilter.getPlaceholder());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + ALL), view.ownerFilter.getPlaceholder());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ADD), view.add.getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + EDIT), view.edit.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MERGE), view.merge.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + FILES), view.files.getText());
    assertEquals(view.getTranslation(MESSAGE_PREFIX + ANALYZE), view.analyze.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + TITLE,
        view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME)), view.getPageTitle());
  }

  @Test
  public void samples() {
    assertEquals(5, view.samples.getColumns().size());
    assertNotNull(view.samples.getColumnByKey(NAME));
    assertEquals(NAME,
        view.samples.getColumnByKey(NAME).getSortOrder(SortDirection.ASCENDING).findFirst()
            .map(SortOrder::getSorted).orElseThrow());
    assertTrue(view.name.isSortable());
    assertNotNull(view.samples.getColumnByKey(KEYWORDS));
    assertFalse(view.keywords.isSortable());
    assertNotNull(view.samples.getColumnByKey(PROTOCOL));
    assertEquals(PROTOCOL + "." + NAME,
        view.samples.getColumnByKey(PROTOCOL).getSortOrder(SortDirection.ASCENDING).findFirst()
            .map(SortOrder::getSorted).orElseThrow());
    assertTrue(view.protocol.isSortable());
    assertNotNull(view.samples.getColumnByKey(DATE));
    assertEquals(DATE,
        view.samples.getColumnByKey(DATE).getSortOrder(SortDirection.ASCENDING).findFirst()
            .map(SortOrder::getSorted).orElseThrow());
    assertNotNull(view.samples.getColumnByKey(OWNER));
    assertTrue(view.date.isSortable());
    assertEquals(OWNER + "." + EMAIL,
        view.samples.getColumnByKey(OWNER).getSortOrder(SortDirection.ASCENDING).findFirst()
            .map(SortOrder::getSorted).orElseThrow());
    assertInstanceOf(SelectionModel.Multi.class, view.samples.getSelectionModel());
    assertEquals(GridSortOrder.desc(view.date).build(), view.samples.getSortOrder());
    assertTrue(view.owner.isSortable());
    List<Sample> samples = items(view.samples);
    verify(service).all(eq(view.filter()), any());
    assertEquals(this.samples.size(), samples.size());
    for (Sample sample : this.samples) {
      assertTrue(samples.contains(sample), sample::toString);
    }
    assertEquals(0, view.samples.getSelectedItems().size());
    samples.forEach(dataset -> view.samples.select(dataset));
    assertEquals(samples.size(), view.samples.getSelectedItems().size());
  }

  @Test
  public void samples_ColumnsValueProvider() {
    view.samples.setItems(samples);
    samples = view.samples.getListDataView().getItems().toList();
    for (int i = 0; i < samples.size(); i++) {
      Sample sample = samples.get(i);
      assertEquals(sample.getName(),
          test(view.samples).getCellText(i, view.samples.getColumns().indexOf(view.name)));
      assertEquals(String.join(", ", sample.getKeywords()),
          test(view.samples).getCellText(i, view.samples.getColumns().indexOf(view.keywords)));
      assertEquals(sample.getProtocol().getName(),
          test(view.samples).getCellText(i, view.samples.getColumns().indexOf(view.protocol)));
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(sample.getDate()),
          test(view.samples).getCellText(i, view.samples.getColumns().indexOf(view.date)));
      assertEquals(sample.getOwner().getEmail(),
          test(view.samples).getCellText(i, view.samples.getColumns().indexOf(view.owner)));
    }
  }

  @Test
  public void samples_NameColumnComparator() {
    Comparator<Sample> comparator = view.name.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(name("éê"), name("ee")));
    assertTrue(comparator.compare(name("a"), name("e")) < 0);
    assertTrue(comparator.compare(name("a"), name("é")) < 0);
    assertTrue(comparator.compare(name("e"), name("a")) > 0);
    assertTrue(comparator.compare(name("é"), name("a")) > 0);
  }

  @Test
  public void samples_ProtocolColumnComparator() {
    Comparator<Sample> comparator = view.protocol.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(protocol("éê"), protocol("ee")));
    assertTrue(comparator.compare(protocol("a"), protocol("e")) < 0);
    assertTrue(comparator.compare(protocol("a"), protocol("é")) < 0);
    assertTrue(comparator.compare(protocol("e"), protocol("a")) > 0);
    assertTrue(comparator.compare(protocol("é"), protocol("a")) > 0);
  }

  @Test
  public void samples_OwnerColumnComparator() {
    Comparator<Sample> comparator = view.owner.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(owner("éê"), owner("ee")));
    assertTrue(comparator.compare(owner("a"), owner("e")) < 0);
    assertTrue(comparator.compare(owner("a"), owner("é")) < 0);
    assertTrue(comparator.compare(owner("e"), owner("a")) > 0);
    assertTrue(comparator.compare(owner("é"), owner("a")) > 0);
  }

  @Test
  public void ownerFilter_User() {
    assertEquals("jonh.smith@ircm.qc.ca", view.filter().ownerContains);
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void ownerFilter_Manager() {
    assertNull(view.filter().ownerContains);
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void ownerFilter_Admin() {
    assertNull(view.filter().ownerContains);
  }

  @Test
  public void view() {
    Sample sample = samples.get(0);
    when(service.get(anyLong())).thenReturn(Optional.of(sample));

    doubleClickItem(view.samples, sample);

    verify(service).get(sample.getId());
    SampleDialog dialog = $(SampleDialog.class).first();
    assertTrue(dialog.isOpened());
    assertEquals(sample.getId(), dialog.getSampleId());
  }

  @Test
  public void view_RefreshOnSave() {
    Sample sample = samples.get(0);
    when(service.get(anyLong())).thenReturn(Optional.of(sample));
    view.samples.setItems(sampleDataProvider);

    doubleClickItem(view.samples, sample);

    SampleDialog dialog = $(SampleDialog.class).first();
    dialog.fireSavedEvent();
    verify(view.samples.getDataProvider()).refreshAll();
  }

  @Test
  public void view_RefreshOnDelete() {
    Sample sample = samples.get(0);
    when(service.get(anyLong())).thenReturn(Optional.of(sample));
    view.samples.setItems(sampleDataProvider);

    doubleClickItem(view.samples, sample);

    SampleDialog dialog = $(SampleDialog.class).first();
    dialog.fireDeletedEvent();
    verify(view.samples.getDataProvider()).refreshAll();
  }

  @Test
  public void viewFiles_Control() {
    view.samples.setItems(samples);
    Sample sample = view.samples.getListDataView().getItems().findFirst().orElseThrow();
    test(view.samples).clickRow(0, new MetaKeys().ctrl());

    SampleFilesDialog dialog = $(SampleFilesDialog.class).first();
    assertTrue(dialog.isOpened());
    assertEquals(sample.getId(), dialog.getSampleId());
  }

  @Test
  public void addFiles_Meta() {
    view.samples.setItems(samples);
    Sample sample = view.samples.getListDataView().getItems().findFirst().orElseThrow();
    test(view.samples).clickRow(0, new MetaKeys().meta());

    SampleFilesDialog dialog = $(SampleFilesDialog.class).first();
    assertTrue(dialog.isOpened());
    assertEquals(sample.getId(), dialog.getSampleId());
  }

  @Test
  public void filterName() {
    view.samples.setItems(sampleDataProvider);

    view.nameFilter.setValue("test");

    assertEquals("test", view.filter().nameContains);
    verify(view.samples.getDataProvider()).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    view.samples.setItems(sampleDataProvider);
    view.nameFilter.setValue("test");

    view.nameFilter.setValue("");

    assertNull(view.filter().nameContains);
    verify(view.samples.getDataProvider(), times(2)).refreshAll();
  }

  @Test
  public void filterKeywords() {
    view.samples.setItems(sampleDataProvider);

    view.keywordsFilter.setValue("test");

    verify(view.samples.getDataProvider()).refreshAll();
    assertEquals("test", view.filter().keywordsContains);
  }

  @Test
  public void filterKeywords_Empty() {
    view.samples.setItems(sampleDataProvider);
    view.keywordsFilter.setValue("test");

    view.keywordsFilter.setValue("");

    verify(view.samples.getDataProvider(), times(2)).refreshAll();
    assertNull(view.filter().keywordsContains);
  }

  @Test
  public void filterProtocol() {
    view.samples.setItems(sampleDataProvider);

    view.protocolFilter.setValue("test");

    assertEquals("test", view.filter().protocolContains);
    verify(view.samples.getDataProvider()).refreshAll();
  }

  @Test
  public void filterProtocol_Empty() {
    view.samples.setItems(sampleDataProvider);
    view.protocolFilter.setValue("test");

    view.protocolFilter.setValue("");

    assertNull(view.filter().protocolContains);
    verify(view.samples.getDataProvider(), times(2)).refreshAll();
  }

  @Test
  public void filterDate() {
    view.samples.setItems(sampleDataProvider);

    Range<LocalDate> range = Range.closed(LocalDate.now().minusDays(10), LocalDate.now());
    view.dateFilter.setValue(range);

    assertEquals(range, view.filter().dateRange);
    verify(view.samples.getDataProvider()).refreshAll();
  }

  @Test
  public void filterDate_All() {
    view.samples.setItems(sampleDataProvider);
    Range<LocalDate> range = Range.closed(LocalDate.now().minusDays(10), LocalDate.now());
    view.dateFilter.setValue(range);

    view.dateFilter.setValue(Range.unbounded());

    assertEquals(Range.unbounded(), view.filter().dateRange);
    verify(view.samples.getDataProvider(), times(2)).refreshAll();
  }

  @Test
  public void filterOwner() {
    view.samples.setItems(sampleDataProvider);

    view.ownerFilter.setValue("test");

    assertEquals("test", view.filter().ownerContains);
    verify(view.samples.getDataProvider()).refreshAll();
  }

  @Test
  public void filterOwner_Empty() {
    view.samples.setItems(sampleDataProvider);
    view.ownerFilter.setValue("test");

    view.ownerFilter.setValue("");

    assertNull(view.filter().ownerContains);
    verify(view.samples.getDataProvider(), times(2)).refreshAll();
  }

  @Test
  public void add() {
    test(view.add).click();

    SampleDialog dialog = $(SampleDialog.class).first();
    assertTrue(dialog.isOpened());
    assertEquals(0, dialog.getSampleId());
  }

  @Test
  public void add_RefreshOnSave() {
    view.samples.setItems(sampleDataProvider);

    test(view.add).click();

    SampleDialog dialog = $(SampleDialog.class).first();
    dialog.fireSavedEvent();
    verify(view.samples.getDataProvider()).refreshAll();
  }

  @Test
  public void add_RefreshOnDelete() {
    view.samples.setItems(sampleDataProvider);

    test(view.add).click();

    SampleDialog dialog = $(SampleDialog.class).first();
    dialog.fireDeletedEvent();
    verify(view.samples.getDataProvider()).refreshAll();
  }

  @Test
  public void edit_Enabled() {
    assertFalse(view.edit.isEnabled());
    view.samples.select(samples.get(0));
    assertTrue(view.edit.isEnabled());
    view.samples.select(samples.get(1));
    assertFalse(view.edit.isEnabled());
    view.samples.deselectAll();
    assertFalse(view.edit.isEnabled());
  }

  @Test
  public void edit() {
    Sample sample = samples.get(0);
    view.samples.select(sample);

    test(view.edit).click();

    SampleDialog dialog = $(SampleDialog.class).first();
    assertTrue(dialog.isOpened());
    assertEquals(sample.getId(), dialog.getSampleId());
  }

  @Test
  public void edit_NoSelection() {
    view.edit();

    Notification error = $(Notification.class).first();
    assertInstanceOf(ErrorNotification.class, error);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SAMPLES_REQUIRED),
        ((ErrorNotification) error).getText());
    assertFalse($(SampleFilesDialog.class).exists());
  }

  @Test
  public void edit_MultipleSamplesSelected() {
    view.samples.select(samples.get(0));
    view.samples.select(samples.get(1));

    view.edit();

    Notification error = $(Notification.class).first();
    assertInstanceOf(ErrorNotification.class, error);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SAMPLES_MORE_THAN_ONE),
        ((ErrorNotification) error).getText());
    assertFalse($(SampleFilesDialog.class).exists());
  }

  @Test
  public void merge_Enabled() {
    assertFalse(view.merge.isEnabled());
    view.samples.select(samples.get(0));
    assertTrue(view.merge.isEnabled());
    view.samples.select(samples.get(1));
    assertTrue(view.merge.isEnabled());
    view.samples.deselectAll();
    assertFalse(view.merge.isEnabled());
  }

  @Test
  public void merge() {
    when(service.isMergable(any())).thenReturn(true);
    view.samples.select(samples.get(0));
    view.samples.select(samples.get(1));

    test(view.merge).click();

    verify(service).isMergable(samplesCaptor.capture());
    assertEquals(2, samplesCaptor.getValue().size());
    assertTrue(samplesCaptor.getValue().contains(samples.get(0)));
    assertTrue(samplesCaptor.getValue().contains(samples.get(1)));
    verify(datasetService).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertEquals(0, dataset.getId());
    assertEquals(2, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("mnase"));
    assertTrue(dataset.getKeywords().contains("ip"));
    assertEquals(2, dataset.getSamples().size());
    assertEquals(samples.get(0), dataset.getSamples().get(0));
    assertEquals(samples.get(1), dataset.getSamples().get(1));
    assertEquals(samples.get(0).getDate(), dataset.getDate());
    Notification notification = $(Notification.class).first();
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MERGED, dataset.getName()),
        test(notification).getText());
  }

  @Test
  public void merge_SortById() {
    when(service.isMergable(any())).thenReturn(true);
    view.samples.select(samples.get(1));
    view.samples.select(samples.get(0));

    test(view.merge).click();

    verify(service).isMergable(samplesCaptor.capture());
    assertEquals(2, samplesCaptor.getValue().size());
    assertTrue(samplesCaptor.getValue().contains(samples.get(0)));
    assertTrue(samplesCaptor.getValue().contains(samples.get(1)));
    verify(datasetService).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertEquals(0, dataset.getId());
    assertEquals(2, dataset.getKeywords().size());
    assertTrue(dataset.getKeywords().contains("mnase"));
    assertTrue(dataset.getKeywords().contains("ip"));
    assertEquals(2, dataset.getSamples().size());
    assertEquals(samples.get(0), dataset.getSamples().get(0));
    assertEquals(samples.get(1), dataset.getSamples().get(1));
    assertEquals(samples.get(0).getDate(), dataset.getDate());
    Notification notification = $(Notification.class).first();
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MERGED, dataset.getName()),
        test(notification).getText());
  }

  @Test
  public void merge_NoSelection() {
    view.merge();

    Notification error = $(Notification.class).first();
    assertInstanceOf(ErrorNotification.class, error);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SAMPLES_REQUIRED),
        ((ErrorNotification) error).getText());
    verify(service, never()).isMergable(any());
    verify(datasetService, never()).save(any());
  }

  @Test
  public void merge_NotMergeable() {
    when(service.isMergable(any())).thenReturn(false);
    view.samples.select(samples.get(0));
    view.samples.select(samples.get(1));

    test(view.merge).click();

    Notification error = $(Notification.class).first();
    assertInstanceOf(ErrorNotification.class, error);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + MERGE_ERROR),
        ((ErrorNotification) error).getText());
    verify(service).isMergable(samplesCaptor.capture());
    assertEquals(2, samplesCaptor.getValue().size());
    assertTrue(samplesCaptor.getValue().contains(samples.get(0)));
    assertTrue(samplesCaptor.getValue().contains(samples.get(1)));
    verify(datasetService, never()).save(any());
  }

  @Test
  public void merge_NameExists() {
    when(service.isMergable(any())).thenReturn(true);
    when(datasetService.exists(any())).thenReturn(true);
    view.samples.select(samples.get(0));
    view.samples.select(samples.get(1));

    test(view.merge).click();

    Notification error = $(Notification.class).first();
    assertInstanceOf(ErrorNotification.class, error);
    assertEquals(view.getTranslation(DATASET_PREFIX + NAME_ALREADY_EXISTS,
            "MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2_20181020"),
        ((ErrorNotification) error).getText());
    verify(datasetService).exists("MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2_20181020");
    verify(datasetService, never()).save(any());
  }

  @Test
  public void files_Enabled() {
    assertFalse(view.files.isEnabled());
    view.samples.select(samples.get(0));
    assertTrue(view.files.isEnabled());
    view.samples.select(samples.get(1));
    assertFalse(view.files.isEnabled());
    view.samples.deselectAll();
    assertFalse(view.files.isEnabled());
  }

  @Test
  public void files() {
    Sample sample = samples.get(0);
    view.samples.select(sample);

    test(view.files).click();

    SampleFilesDialog dialog = $(SampleFilesDialog.class).first();
    assertTrue(dialog.isOpened());
    assertEquals(sample.getId(), dialog.getSampleId());
  }

  @Test
  public void files_NoSelection() {
    view.viewFiles();

    Notification error = $(Notification.class).first();
    assertInstanceOf(ErrorNotification.class, error);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SAMPLES_REQUIRED),
        ((ErrorNotification) error).getText());
    assertFalse($(SampleFilesDialog.class).exists());
  }

  @Test
  public void files_MultipleSamplesSelected() {
    view.samples.select(samples.get(0));
    view.samples.select(samples.get(1));

    view.viewFiles();

    Notification error = $(Notification.class).first();
    assertInstanceOf(ErrorNotification.class, error);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SAMPLES_MORE_THAN_ONE),
        ((ErrorNotification) error).getText());
    assertFalse($(SampleFilesDialog.class).exists());
  }

  @Test
  public void analyze_Enabled() {
    assertFalse(view.analyze.isEnabled());
    view.samples.select(samples.get(0));
    assertTrue(view.analyze.isEnabled());
    view.samples.select(samples.get(1));
    assertTrue(view.analyze.isEnabled());
    view.samples.deselectAll();
    assertFalse(view.analyze.isEnabled());
  }

  @Test
  public void analyze() {
    Sample sample = samples.get(0);
    view.samples.select(sample);

    test(view.analyze).click();

    SamplesAnalysisDialog dialog = $(SamplesAnalysisDialog.class).first();
    assertTrue(dialog.isOpened());
    assertEquals(1, dialog.getSampleIds().size());
    assertTrue(dialog.getSampleIds().contains(sample.getId()));
  }

  @Test
  public void analyze_NoSelection() {
    view.analyze();

    Notification error = $(Notification.class).first();
    assertInstanceOf(ErrorNotification.class, error);
    assertEquals(view.getTranslation(MESSAGE_PREFIX + SAMPLES_REQUIRED),
        ((ErrorNotification) error).getText());
    assertFalse($(SamplesAnalysisDialog.class).exists());
  }

  @Test
  public void analyze_MultipleSamples() {
    List<Sample> samples = this.samples.subList(0, 2);
    samples.forEach(sample -> view.samples.select(sample));

    test(view.analyze).click();

    SamplesAnalysisDialog dialog = $(SamplesAnalysisDialog.class).first();
    assertTrue(dialog.isOpened());
    assertEquals(2, dialog.getSampleIds().size());
    assertTrue(dialog.getSampleIds().contains(samples.get(0).getId()));
    assertTrue(dialog.getSampleIds().contains(samples.get(1).getId()));
  }
}
