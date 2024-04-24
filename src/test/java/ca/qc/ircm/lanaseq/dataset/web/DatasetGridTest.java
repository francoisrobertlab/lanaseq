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

import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.OWNER;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TAGS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetGrid.EDIT_BUTTON;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.functions;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.rendererTemplate;
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

import ca.qc.ircm.lanaseq.AppResources;
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
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.shared.Registration;
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
  private AppResources datasetResources = new AppResources(Dataset.class, locale);
  private AppResources sampleResources = new AppResources(Sample.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Dataset> datasets;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
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
    assertEquals(datasetResources.message(NAME), headerRow.getCell(grid.name).getText());
    assertEquals(datasetResources.message(NAME), footerRow.getCell(grid.name).getText());
    assertEquals(datasetResources.message(TAGS), headerRow.getCell(grid.tags).getText());
    assertEquals(datasetResources.message(TAGS), footerRow.getCell(grid.tags).getText());
    assertEquals(sampleResources.message(PROTOCOL), headerRow.getCell(grid.protocol).getText());
    assertEquals(sampleResources.message(PROTOCOL), footerRow.getCell(grid.protocol).getText());
    assertEquals(datasetResources.message(DATE), headerRow.getCell(grid.date).getText());
    assertEquals(datasetResources.message(DATE), footerRow.getCell(grid.date).getText());
    assertEquals(datasetResources.message(OWNER), headerRow.getCell(grid.owner).getText());
    assertEquals(datasetResources.message(OWNER), footerRow.getCell(grid.owner).getText());
    assertEquals(webResources.message(EDIT), headerRow.getCell(grid.edit).getText());
    assertEquals(webResources.message(EDIT), footerRow.getCell(grid.edit).getText());
    assertEquals(webResources.message(ALL), grid.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), grid.tagsFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), grid.protocolFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), grid.ownerFilter.getPlaceholder());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    final AppResources datasetResources = new AppResources(Dataset.class, locale);
    final AppResources sampleResources = new AppResources(Sample.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    UI.getCurrent().setLocale(locale);
    HeaderRow headerRow = grid.getHeaderRows().get(0);
    FooterRow footerRow = grid.getFooterRows().get(0);
    assertEquals(datasetResources.message(NAME), headerRow.getCell(grid.name).getText());
    assertEquals(datasetResources.message(NAME), footerRow.getCell(grid.name).getText());
    assertEquals(datasetResources.message(TAGS), headerRow.getCell(grid.tags).getText());
    assertEquals(datasetResources.message(TAGS), footerRow.getCell(grid.tags).getText());
    assertEquals(sampleResources.message(PROTOCOL), headerRow.getCell(grid.protocol).getText());
    assertEquals(sampleResources.message(PROTOCOL), footerRow.getCell(grid.protocol).getText());
    assertEquals(datasetResources.message(DATE), headerRow.getCell(grid.date).getText());
    assertEquals(datasetResources.message(DATE), footerRow.getCell(grid.date).getText());
    assertEquals(datasetResources.message(OWNER), headerRow.getCell(grid.owner).getText());
    assertEquals(datasetResources.message(OWNER), footerRow.getCell(grid.owner).getText());
    assertEquals(webResources.message(EDIT), headerRow.getCell(grid.edit).getText());
    assertEquals(webResources.message(EDIT), footerRow.getCell(grid.edit).getText());
    assertEquals(webResources.message(ALL), grid.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), grid.tagsFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), grid.protocolFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), grid.ownerFilter.getPlaceholder());
  }

  @Test
  public void datasets() {
    assertEquals(6, grid.getColumns().size());
    assertNotNull(grid.getColumnByKey(NAME));
    assertTrue(grid.name.isSortable());
    assertEquals(NAME, grid.getColumnByKey(NAME).getSortOrder(SortDirection.ASCENDING).findFirst()
        .map(so -> so.getSorted()).orElse(null));
    assertNotNull(grid.getColumnByKey(TAGS));
    assertFalse(grid.tags.isSortable());
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
    assertNotNull(grid.getColumnByKey(EDIT));
    assertFalse(grid.edit.isSortable());
    assertTrue(grid.edit.isVisible());
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
      assertEquals(dataset.getTags().stream().collect(Collectors.joining(", ")),
          test(grid).getCellText(i, grid.getColumns().indexOf(grid.tags)));
      assertEquals(protocol(dataset).map(Protocol::getName).orElse(""),
          test(grid).getCellText(i, grid.getColumns().indexOf(grid.protocol)));
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(dataset.getDate()),
          test(grid).getCellText(i, grid.getColumns().indexOf(grid.date)));
      assertEquals(dataset.getOwner().getEmail(),
          test(grid).getCellText(i, grid.getColumns().indexOf(grid.owner)));
      LitRenderer<Dataset> editRenderer = (LitRenderer<Dataset>) grid.edit.getRenderer();
      assertEquals(EDIT_BUTTON, rendererTemplate(editRenderer));
      assertTrue(functions(editRenderer).containsKey("edit"));
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
  public void datasets_ClickEdit() {
    grid.addEditListener(editListener);
    Dataset dataset = datasets.get(1);
    LitRenderer<Dataset> editRenderer = (LitRenderer<Dataset>) grid.edit.getRenderer();
    functions(editRenderer).get("edit").accept(dataset, null);
    verify(editListener, atLeastOnce()).onComponentEvent(editEventCaptor.capture());
    assertEquals(dataset, editEventCaptor.getValue().getItem());
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
  public void filterTags() {
    grid.setItems(mock(DataProvider.class));

    grid.tagsFilter.setValue("test");

    verify(grid.getDataProvider()).refreshAll();
    assertEquals("test", grid.filter().tagsContains);
  }

  @Test
  public void filterTags_Empty() {
    grid.setItems(mock(DataProvider.class));
    grid.tagsFilter.setValue("test");

    grid.tagsFilter.setValue("");

    verify(grid.getDataProvider(), times(2)).refreshAll();
    assertNull(grid.filter().tagsContains);
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
  public void addEditListener() {
    DatasetGrid grid = new DatasetGrid(service, authenticatedUser);
    grid.init();
    Registration registration = grid.addEditListener(editListener);
    assertTrue(grid.edit.isVisible());
    registration.remove();
    assertFalse(grid.edit.isVisible());
  }

  @Test
  public void refreshDataset() {
    grid.setItems(mock(DataProvider.class));
    grid.refreshDatasets();
    verify(grid.getDataProvider()).refreshAll();
  }
}
