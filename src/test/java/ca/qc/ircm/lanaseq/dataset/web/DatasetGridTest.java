package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.KEYWORDS;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.OWNER;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.web.EditEvent;
import com.google.common.collect.Range;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link DatasetGrid}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetGridTest extends SpringUIUnitTest {
  private static final String DATASET_PREFIX = messagePrefix(Dataset.class);
  private static final String SAMPLE_PREFIX = messagePrefix(Sample.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private DatasetGrid grid;
  @MockBean
  private DatasetService service;
  @Autowired
  private AuthenticatedUser authenticatedUser;
  @Mock
  private ComponentEventListener<EditEvent<DatasetGrid, Dataset>> editListener;
  @Captor
  private ArgumentCaptor<EditEvent<DatasetGrid, Dataset>> editEventCaptor;
  @Autowired
  private DatasetRepository repository;
  private Locale locale = Locale.ENGLISH;
  private List<Dataset> datasets;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(service.get(any())).then(
        i -> i.getArgument(0) != null ? repository.findById(i.getArgument(0)) : Optional.empty());
    datasets = repository.findAll();
    when(service.all(any())).thenReturn(datasets);
    UI.getCurrent().setLocale(locale);
    navigate(DatasetsView.class);
    grid = $(DatasetGrid.class).first();
  }

  private Optional<Protocol> protocol(Dataset dataset) {
    return dataset.getSamples() != null
        ? dataset.getSamples().stream().findFirst().map(s -> s.getProtocol())
        : Optional.empty();
  }

  private Dataset name(String name) {
    Dataset dataset = new Dataset();
    dataset.setName(name);
    return dataset;
  }

  private Dataset owner(String email) {
    Dataset dataset = new Dataset();
    dataset.setOwner(new User());
    dataset.getOwner().setEmail(email);
    return dataset;
  }

  @Test
  public void styles() {
    assertEquals(DatasetGrid.ID, grid.getId().orElse(""));
    assertTrue(
        grid.dateFilter.getThemeNames().contains(CustomFieldVariant.LUMO_SMALL.getVariantName()));
  }

  @Test
  public void labels() {
    HeaderRow headerRow = grid.getHeaderRows().get(0);
    FooterRow footerRow = grid.getFooterRows().get(0);
    assertEquals(grid.getTranslation(DATASET_PREFIX + NAME),
        headerRow.getCell(grid.name).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + NAME),
        footerRow.getCell(grid.name).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + KEYWORDS),
        headerRow.getCell(grid.keywords).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + KEYWORDS),
        footerRow.getCell(grid.keywords).getText());
    assertEquals(grid.getTranslation(SAMPLE_PREFIX + PROTOCOL),
        headerRow.getCell(grid.protocol).getText());
    assertEquals(grid.getTranslation(SAMPLE_PREFIX + PROTOCOL),
        footerRow.getCell(grid.protocol).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + DATE),
        headerRow.getCell(grid.date).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + DATE),
        footerRow.getCell(grid.date).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + OWNER),
        headerRow.getCell(grid.owner).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + OWNER),
        footerRow.getCell(grid.owner).getText());
    assertEquals(grid.getTranslation(CONSTANTS_PREFIX + ALL), grid.nameFilter.getPlaceholder());
    assertEquals(grid.getTranslation(CONSTANTS_PREFIX + ALL), grid.keywordsFilter.getPlaceholder());
    assertEquals(grid.getTranslation(CONSTANTS_PREFIX + ALL), grid.protocolFilter.getPlaceholder());
    assertEquals(grid.getTranslation(CONSTANTS_PREFIX + ALL), grid.ownerFilter.getPlaceholder());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    HeaderRow headerRow = grid.getHeaderRows().get(0);
    FooterRow footerRow = grid.getFooterRows().get(0);
    assertEquals(grid.getTranslation(DATASET_PREFIX + NAME),
        headerRow.getCell(grid.name).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + NAME),
        footerRow.getCell(grid.name).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + KEYWORDS),
        headerRow.getCell(grid.keywords).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + KEYWORDS),
        footerRow.getCell(grid.keywords).getText());
    assertEquals(grid.getTranslation(SAMPLE_PREFIX + PROTOCOL),
        headerRow.getCell(grid.protocol).getText());
    assertEquals(grid.getTranslation(SAMPLE_PREFIX + PROTOCOL),
        footerRow.getCell(grid.protocol).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + DATE),
        headerRow.getCell(grid.date).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + DATE),
        footerRow.getCell(grid.date).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + OWNER),
        headerRow.getCell(grid.owner).getText());
    assertEquals(grid.getTranslation(DATASET_PREFIX + OWNER),
        footerRow.getCell(grid.owner).getText());
    assertEquals(grid.getTranslation(CONSTANTS_PREFIX + ALL), grid.nameFilter.getPlaceholder());
    assertEquals(grid.getTranslation(CONSTANTS_PREFIX + ALL), grid.keywordsFilter.getPlaceholder());
    assertEquals(grid.getTranslation(CONSTANTS_PREFIX + ALL), grid.protocolFilter.getPlaceholder());
    assertEquals(grid.getTranslation(CONSTANTS_PREFIX + ALL), grid.ownerFilter.getPlaceholder());
  }

  @Test
  public void datasets() {
    assertEquals(5, grid.getColumns().size());
    assertNotNull(grid.getColumnByKey(NAME));
    assertTrue(grid.name.isSortable());
    assertEquals(NAME, grid.getColumnByKey(NAME).getSortOrder(SortDirection.ASCENDING).findFirst()
        .map(so -> so.getSorted()).orElse(null));
    assertNotNull(grid.getColumnByKey(KEYWORDS));
    assertFalse(grid.keywords.isSortable());
    assertNotNull(grid.getColumnByKey(PROTOCOL));
    assertFalse(grid.protocol.isSortable());
    assertNotNull(grid.getColumnByKey(DATE));
    assertTrue(grid.date.isSortable());
    assertEquals(DATE, grid.getColumnByKey(DATE).getSortOrder(SortDirection.ASCENDING).findFirst()
        .map(so -> so.getSorted()).orElse(null));
    assertNotNull(grid.getColumnByKey(OWNER));
    assertTrue(grid.owner.isSortable());
    assertEquals(OWNER + "." + EMAIL, grid.getColumnByKey(OWNER)
        .getSortOrder(SortDirection.ASCENDING).findFirst().map(so -> so.getSorted()).orElse(null));
    assertEquals(GridSortOrder.desc(grid.date).build(), grid.getSortOrder());
    List<Dataset> datasets = items(grid);
    verify(service, atLeastOnce()).all(grid.filter());
    assertEquals(this.datasets.size(), datasets.size());
    for (Dataset dataset : this.datasets) {
      assertTrue(datasets.contains(dataset), () -> dataset.toString());
    }
  }

  @Test
  public void datasets_ColumnsValueProvider() {
    grid.setItems(datasets);
    for (int i = 0; i < datasets.size(); i++) {
      Dataset dataset = datasets.get(i);
      assertEquals(dataset.getName(),
          test(grid).getCellText(i, grid.getColumns().indexOf(grid.name)));
      assertEquals(dataset.getKeywords().stream().collect(Collectors.joining(", ")),
          test(grid).getCellText(i, grid.getColumns().indexOf(grid.keywords)));
      assertEquals(protocol(dataset).map(Protocol::getName).orElse(""),
          test(grid).getCellText(i, grid.getColumns().indexOf(grid.protocol)));
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(dataset.getDate()),
          test(grid).getCellText(i, grid.getColumns().indexOf(grid.date)));
      assertEquals(dataset.getOwner().getEmail(),
          test(grid).getCellText(i, grid.getColumns().indexOf(grid.owner)));
    }
  }

  @Test
  public void datasets_NameColumnComparator() {
    Comparator<Dataset> comparator = grid.name.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(name("éê"), name("ee")));
    assertTrue(comparator.compare(name("a"), name("e")) < 0);
    assertTrue(comparator.compare(name("a"), name("é")) < 0);
    assertTrue(comparator.compare(name("e"), name("a")) > 0);
    assertTrue(comparator.compare(name("é"), name("a")) > 0);
  }

  @Test
  public void datasets_OwnerColumnComparator() {
    Comparator<Dataset> comparator = grid.owner.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(owner("éê"), owner("ee")));
    assertTrue(comparator.compare(owner("a"), owner("e")) < 0);
    assertTrue(comparator.compare(owner("a"), owner("é")) < 0);
    assertTrue(comparator.compare(owner("e"), owner("a")) > 0);
    assertTrue(comparator.compare(owner("é"), owner("a")) > 0);
  }

  @Test
  public void datasets_OwnerFilter_User() {
    assertEquals("jonh.smith@ircm.qc.ca", grid.ownerFilter.getValue());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void datasets_OwnerFilter_Manager() {
    assertEquals("", grid.ownerFilter.getValue());
  }

  @Test
  @WithUserDetails("lanaseq@ircm.qc.ca")
  public void datasets_OwnerFilter_Admin() {
    assertEquals("", grid.ownerFilter.getValue());
  }

  @Test
  public void filterName() {
    grid.setItems(mock(DataProvider.class));

    grid.nameFilter.setValue("test");

    verify(grid.getDataProvider()).refreshAll();
    assertEquals("test", grid.filter().nameContains);
  }

  @Test
  public void filterName_Empty() {
    grid.setItems(mock(DataProvider.class));
    grid.nameFilter.setValue("test");

    grid.nameFilter.setValue("");

    verify(grid.getDataProvider(), times(2)).refreshAll();
    assertNull(grid.filter().nameContains);
  }

  @Test
  public void filterKeywords() {
    grid.setItems(mock(DataProvider.class));

    grid.keywordsFilter.setValue("test");

    verify(grid.getDataProvider()).refreshAll();
    assertEquals("test", grid.filter().keywordsContains);
  }

  @Test
  public void filterKeywords_Empty() {
    grid.setItems(mock(DataProvider.class));
    grid.keywordsFilter.setValue("test");

    grid.keywordsFilter.setValue("");

    verify(grid.getDataProvider(), times(2)).refreshAll();
    assertNull(grid.filter().keywordsContains);
  }

  @Test
  public void filterProtocol() {
    grid.setItems(mock(DataProvider.class));

    grid.protocolFilter.setValue("test");

    verify(grid.getDataProvider()).refreshAll();
    assertEquals("test", grid.filter().protocolContains);
  }

  @Test
  public void filterProtocol_Empty() {
    grid.setItems(mock(DataProvider.class));
    grid.protocolFilter.setValue("test");

    grid.protocolFilter.setValue("");

    verify(grid.getDataProvider(), times(2)).refreshAll();
    assertNull(grid.filter().protocolContains);
  }

  @Test
  public void filterDate() {
    grid.setItems(mock(DataProvider.class));

    Range<LocalDate> range =
        Range.closed(LocalDate.now().minusDays(11), LocalDate.now().minusDays(3));
    grid.dateFilter.setValue(range);

    verify(grid.getDataProvider()).refreshAll();
    assertEquals(range, grid.filter().dateRange);
  }

  @Test
  public void filterDate_Null() {
    grid.setItems(mock(DataProvider.class));
    Range<LocalDate> range =
        Range.closed(LocalDate.now().minusDays(11), LocalDate.now().minusDays(3));
    grid.dateFilter.setValue(range);

    grid.dateFilter.setValue(null);

    verify(grid.getDataProvider(), times(2)).refreshAll();
    assertNull(grid.filter().dateRange);
  }

  @Test
  public void filterOwner() {
    grid.setItems(mock(DataProvider.class));

    grid.ownerFilter.setValue("test");

    verify(grid.getDataProvider()).refreshAll();
    assertEquals("test", grid.filter().ownerContains);
  }

  @Test
  public void filterOwner_Empty() {
    grid.setItems(mock(DataProvider.class));
    grid.ownerFilter.setValue("test");

    grid.ownerFilter.setValue("");

    verify(grid.getDataProvider(), times(2)).refreshAll();
    assertNull(grid.filter().ownerContains);
  }

  @Test
  public void refreshDataset() {
    grid.setItems(mock(DataProvider.class));
    grid.refreshDatasets();
    verify(grid.getDataProvider()).refreshAll();
  }
}
