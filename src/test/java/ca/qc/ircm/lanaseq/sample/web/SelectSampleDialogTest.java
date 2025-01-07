package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.ADD_SAMPLE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.OWNER;
import static ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog.ID;
import static ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog.SAMPLES;
import static ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.web.DatasetDialog;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.web.SelectedEvent;
import com.google.common.collect.Range;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link SelectSampleDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SelectSampleDialogTest extends SpringUIUnitTest {
  private static final String SAMPLE_PREFIX = messagePrefix(Sample.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private SelectSampleDialog dialog;
  @MockitoBean
  private SampleService service;
  @Mock
  private ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>> listener;
  @Captor
  private ArgumentCaptor<SelectedEvent<SelectSampleDialog, Sample>> selectedEventCaptor;
  @Captor
  private ArgumentCaptor<ValueProvider<Sample, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<LocalDateRenderer<Sample>> localDateRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<Sample>> comparatorCaptor;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private DatasetRepository datasetRepository;
  private Locale locale = Locale.ENGLISH;
  private List<Sample> samples;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() throws Exception {
    samples = repository.findAll();
    when(service.all()).thenReturn(samples);
    UI.getCurrent().setLocale(locale);
    navigate(DatasetsView.class);
    Grid<Dataset> datasetGrid = $(Grid.class).first();
    datasetGrid.setItems(datasetRepository.findAll());
    test(datasetGrid).doubleClickRow(1);
    if ($(Button.class).withId(DatasetDialog.id(ADD_SAMPLE)).exists()) {
      Button addSample = $(Button.class).id(DatasetDialog.id(ADD_SAMPLE));
      addSample.click();
    } else {
      // Invoke method DatasetDialog.addSample() directly since UI testing is bugged and does not show button.
      DatasetDialog datasetDialog = $(DatasetDialog.class).first();
      Method addSampleMethod = DatasetDialog.class.getDeclaredMethod("addSample");
      addSampleMethod.setAccessible(true);
      addSampleMethod.invoke(datasetDialog);
    }
    dialog = $(SelectSampleDialog.class).first();
  }

  private Sample name(String name) {
    Sample sample = new Sample();
    sample.setName(name);
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
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(SAMPLES), dialog.samples.getId().orElse(""));
    assertTrue(
        dialog.dateFilter.getThemeNames().contains(CustomFieldVariant.LUMO_SMALL.getVariantName()));
  }

  @Test
  public void labels() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    HeaderRow headerRow = dialog.samples.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + NAME),
        headerRow.getCell(dialog.name).getText());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + DATE),
        headerRow.getCell(dialog.date).getText());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + OWNER),
        headerRow.getCell(dialog.owner).getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + ALL), dialog.nameFilter.getPlaceholder());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + ALL),
        dialog.ownerFilter.getPlaceholder());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    HeaderRow headerRow = dialog.samples.getHeaderRows().get(0);
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + NAME),
        headerRow.getCell(dialog.name).getText());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + DATE),
        headerRow.getCell(dialog.date).getText());
    assertEquals(dialog.getTranslation(SAMPLE_PREFIX + OWNER),
        headerRow.getCell(dialog.owner).getText());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + ALL), dialog.nameFilter.getPlaceholder());
    assertEquals(dialog.getTranslation(CONSTANTS_PREFIX + ALL),
        dialog.ownerFilter.getPlaceholder());
  }

  @Test
  public void samples() {
    assertEquals(3, dialog.samples.getColumns().size());
    assertNotNull(dialog.samples.getColumnByKey(NAME));
    assertNotNull(dialog.samples.getColumnByKey(DATE));
    assertNotNull(dialog.samples.getColumnByKey(OWNER));
    assertTrue(dialog.samples.getSelectionModel() instanceof SelectionModel.Single);
    List<Sample> samples = items(dialog.samples);
    verify(service).all();
    assertEquals(this.samples.size(), samples.size());
    for (Sample sample : this.samples) {
      assertTrue(samples.contains(sample), () -> sample.toString());
    }
    assertEquals(4, dialog.samples.getListDataView().getItemCount());
    samples = dialog.samples.getListDataView().getItems().toList();
    assertFalse(find(samples, 1L).isPresent());
    assertTrue(find(samples, 4L).isPresent());
    assertTrue(find(samples, 5L).isPresent());
    assertTrue(find(samples, 10L).isPresent());
    assertTrue(find(samples, 11L).isPresent());
  }

  @Test
  public void samples_ColumnsValueProvider() {
    dialog.samples.setItems(samples);
    for (int i = 0; i < samples.size(); i++) {
      Sample sample = samples.get(i);
      assertEquals(sample.getName(),
          test(dialog.samples).getCellText(i, dialog.samples.getColumns().indexOf(dialog.name)));
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(sample.getDate()),
          test(dialog.samples).getCellText(i, dialog.samples.getColumns().indexOf(dialog.date)));
      assertEquals(sample.getOwner().getEmail(),
          test(dialog.samples).getCellText(i, dialog.samples.getColumns().indexOf(dialog.owner)));
    }
  }

  @Test
  public void samples_NameColumnComparator() {
    Comparator<Sample> comparator = dialog.name.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(name("éê"), name("ee")));
    assertTrue(comparator.compare(name("a"), name("e")) < 0);
    assertTrue(comparator.compare(name("a"), name("é")) < 0);
    assertTrue(comparator.compare(name("e"), name("a")) > 0);
    assertTrue(comparator.compare(name("é"), name("a")) > 0);
  }

  @Test
  public void samples_OwnerColumnComparator() {
    Comparator<Sample> comparator = dialog.owner.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(owner("éê"), owner("ee")));
    assertTrue(comparator.compare(owner("a"), owner("e")) < 0);
    assertTrue(comparator.compare(owner("a"), owner("é")) < 0);
    assertTrue(comparator.compare(owner("e"), owner("a")) > 0);
    assertTrue(comparator.compare(owner("é"), owner("a")) > 0);
  }

  @Test
  public void ownerFilter_User() {
    assertEquals("jonh.smith@ircm.qc.ca", dialog.ownerFilter.getValue());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void ownerFilter_Manager() {
    assertEquals("", dialog.ownerFilter.getValue());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void ownerFilter_Admin() {
    assertEquals("", dialog.ownerFilter.getValue());
  }

  @Test
  public void select() {
    dialog.addSelectedListener(listener);

    Sample sample = samples.get(0);
    doubleClickItem(dialog.samples, sample);

    verify(listener).onComponentEvent(selectedEventCaptor.capture());
    assertEquals(sample, selectedEventCaptor.getValue().getSelection());
    assertFalse(dialog.isOpened());
  }

  @Test
  public void filterName() {
    dialog.samples.setItems(mock(DataProvider.class));

    dialog.nameFilter.setValue("test");

    assertEquals("test", dialog.filter().nameContains);
    verify(dialog.samples.getDataProvider()).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    dialog.samples.setItems(mock(DataProvider.class));
    dialog.nameFilter.setValue("test");

    dialog.nameFilter.setValue("");

    assertNull(dialog.filter().nameContains);
    verify(dialog.samples.getDataProvider(), times(2)).refreshAll();
  }

  @Test
  public void filterDate() {
    dialog.samples.setItems(mock(DataProvider.class));
    Range<LocalDate> range = Range.closed(LocalDate.now().minusDays(10), LocalDate.now());

    dialog.dateFilter.setValue(range);

    assertEquals(range, dialog.filter().dateRange);
    verify(dialog.samples.getDataProvider()).refreshAll();
  }

  @Test
  public void filterDate_All() {
    dialog.samples.setItems(mock(DataProvider.class));
    Range<LocalDate> range = Range.closed(LocalDate.now().minusDays(10), LocalDate.now());
    dialog.dateFilter.setValue(range);

    dialog.dateFilter.setValue(Range.all());

    assertEquals(Range.all(), dialog.filter().dateRange);
    verify(dialog.samples.getDataProvider(), times(2)).refreshAll();
  }

  @Test
  public void filterOwner() {
    dialog.samples.setItems(mock(DataProvider.class));

    dialog.ownerFilter.setValue("test");

    assertEquals("test", dialog.filter().ownerContains);
    verify(dialog.samples.getDataProvider()).refreshAll();
  }

  @Test
  public void filterOwner_Empty() {
    dialog.samples.setItems(mock(DataProvider.class));
    dialog.ownerFilter.setValue("test");

    dialog.ownerFilter.setValue("");

    assertEquals(null, dialog.filter().ownerContains);
    verify(dialog.samples.getDataProvider(), times(2)).refreshAll();
  }
}
