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
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.getFormattedValue;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.rendererTemplate;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.EditEvent;
import com.google.common.collect.Range;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.shared.Registration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
public class DatasetGridTest extends AbstractKaribuTestCase {
  private DatasetGrid grid;
  @MockBean
  private DatasetGridPresenter presenter;
  @Mock
  private ComponentEventListener<EditEvent<DatasetGrid, Dataset>> editListener;
  @Captor
  private ArgumentCaptor<ValueProvider<Dataset, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<LocalDateRenderer<Dataset>> localDateRendererCaptor;
  @Captor
  private ArgumentCaptor<LitRenderer<Dataset>> litRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<Dataset>> comparatorCaptor;
  @Captor
  private ArgumentCaptor<EditEvent<DatasetGrid, Dataset>> editEventCaptor;
  @Autowired
  private DatasetRepository datasetRepository;
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
    ui.setLocale(locale);
    grid = new DatasetGrid(presenter);
    grid.init();
    datasets = datasetRepository.findAll();
  }

  private Protocol protocol(Dataset dataset) {
    return dataset.getSamples() != null
        ? dataset.getSamples().stream().findFirst().map(s -> s.getProtocol()).orElse(new Protocol())
        : new Protocol();
  }

  @SuppressWarnings("unchecked")
  private DatasetGrid mockColumns() {
    DatasetGrid grid = spy(this.grid);
    doNothing().when(grid).sort(any());
    grid.name = mock(Column.class);
    doReturn(grid.name).when(grid).addColumn(any(ValueProvider.class), eq(NAME));
    when(grid.name.setKey(any())).thenReturn(grid.name);
    when(grid.name.setSortProperty(any())).thenReturn(grid.name);
    when(grid.name.setSortable(anyBoolean())).thenReturn(grid.name);
    when(grid.name.setComparator(any(Comparator.class))).thenReturn(grid.name);
    when(grid.name.setHeader(any(String.class))).thenReturn(grid.name);
    when(grid.name.setFlexGrow(anyInt())).thenReturn(grid.name);
    grid.tags = mock(Column.class);
    doReturn(grid.tags).when(grid).addColumn(any(ValueProvider.class), eq(TAGS));
    when(grid.tags.setKey(any())).thenReturn(grid.tags);
    when(grid.tags.setSortProperty(any())).thenReturn(grid.tags);
    when(grid.tags.setSortable(anyBoolean())).thenReturn(grid.tags);
    when(grid.tags.setComparator(any(Comparator.class))).thenReturn(grid.tags);
    when(grid.tags.setHeader(any(String.class))).thenReturn(grid.tags);
    when(grid.tags.setFlexGrow(anyInt())).thenReturn(grid.tags);
    grid.protocol = mock(Column.class);
    doReturn(grid.protocol).when(grid).addColumn(any(ValueProvider.class), eq(PROTOCOL));
    when(grid.protocol.setKey(any())).thenReturn(grid.protocol);
    when(grid.protocol.setSortProperty(any())).thenReturn(grid.protocol);
    when(grid.protocol.setSortable(anyBoolean())).thenReturn(grid.protocol);
    when(grid.protocol.setComparator(any(Comparator.class))).thenReturn(grid.protocol);
    when(grid.protocol.setHeader(any(String.class))).thenReturn(grid.protocol);
    when(grid.protocol.setFlexGrow(anyInt())).thenReturn(grid.protocol);
    grid.date = mock(Column.class);
    doReturn(grid.date).when(grid).addColumn(any(LocalDateRenderer.class), eq(DATE));
    when(grid.date.setKey(any())).thenReturn(grid.date);
    when(grid.date.setSortProperty(any())).thenReturn(grid.date);
    when(grid.date.setSortable(anyBoolean())).thenReturn(grid.date);
    when(grid.date.setHeader(any(String.class))).thenReturn(grid.date);
    when(grid.date.setFlexGrow(anyInt())).thenReturn(grid.date);
    grid.owner = mock(Column.class);
    doReturn(grid.owner).when(grid).addColumn(any(ValueProvider.class), eq(OWNER));
    when(grid.owner.setKey(any())).thenReturn(grid.owner);
    when(grid.owner.setSortProperty(any())).thenReturn(grid.owner);
    when(grid.owner.setSortable(anyBoolean())).thenReturn(grid.owner);
    when(grid.owner.setComparator(any(Comparator.class))).thenReturn(grid.owner);
    when(grid.owner.setHeader(any(String.class))).thenReturn(grid.owner);
    when(grid.owner.setFlexGrow(anyInt())).thenReturn(grid.owner);
    grid.edit = mock(Column.class);
    doReturn(grid.edit).when(grid).addColumn(any(LitRenderer.class), eq(EDIT));
    when(grid.edit.setKey(any())).thenReturn(grid.edit);
    when(grid.edit.setSortProperty(any())).thenReturn(grid.edit);
    when(grid.edit.setSortable(anyBoolean())).thenReturn(grid.edit);
    when(grid.edit.setComparator(any(Comparator.class))).thenReturn(grid.edit);
    when(grid.edit.setHeader(any(String.class))).thenReturn(grid.edit);
    when(grid.edit.setFlexGrow(anyInt())).thenReturn(grid.edit);
    HeaderRow filtersRow = mock(HeaderRow.class);
    doReturn(filtersRow).when(grid).appendHeaderRow();
    HeaderCell nameFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(grid.name)).thenReturn(nameFilterCell);
    HeaderCell tagsFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(grid.tags)).thenReturn(tagsFilterCell);
    HeaderCell protocolFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(grid.protocol)).thenReturn(protocolFilterCell);
    HeaderCell dateFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(grid.date)).thenReturn(dateFilterCell);
    HeaderCell ownerFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(grid.owner)).thenReturn(ownerFilterCell);
    return grid;
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(grid);
  }

  @Test
  public void labels() {
    grid = mockColumns();
    grid.localeChange(mock(LocaleChangeEvent.class));
    verify(grid.name).setHeader(datasetResources.message(NAME));
    verify(grid.name).setFooter(datasetResources.message(NAME));
    verify(grid.tags).setHeader(datasetResources.message(TAGS));
    verify(grid.tags).setFooter(datasetResources.message(TAGS));
    verify(grid.protocol).setHeader(sampleResources.message(PROTOCOL));
    verify(grid.protocol).setFooter(sampleResources.message(PROTOCOL));
    verify(grid.date).setHeader(datasetResources.message(DATE));
    verify(grid.date).setFooter(datasetResources.message(DATE));
    verify(grid.owner).setHeader(datasetResources.message(OWNER));
    verify(grid.owner).setFooter(datasetResources.message(OWNER));
    verify(grid.edit).setHeader(webResources.message(EDIT));
    verify(grid.edit).setFooter(webResources.message(EDIT));
    assertEquals(webResources.message(ALL), grid.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), grid.tagsFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), grid.protocolFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), grid.ownerFilter.getPlaceholder());
  }

  @Test
  public void localeChange() {
    grid = mockColumns();
    grid.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources datasetResources = new AppResources(Dataset.class, locale);
    final AppResources sampleResources = new AppResources(Sample.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    ui.setLocale(locale);
    grid.localeChange(mock(LocaleChangeEvent.class));
    verify(grid.name).setHeader(datasetResources.message(NAME));
    verify(grid.name).setFooter(datasetResources.message(NAME));
    verify(grid.tags).setHeader(datasetResources.message(TAGS));
    verify(grid.tags).setFooter(datasetResources.message(TAGS));
    verify(grid.protocol).setHeader(sampleResources.message(PROTOCOL));
    verify(grid.protocol).setFooter(sampleResources.message(PROTOCOL));
    verify(grid.date, atLeastOnce()).setHeader(datasetResources.message(DATE));
    verify(grid.date, atLeastOnce()).setFooter(datasetResources.message(DATE));
    verify(grid.owner, atLeastOnce()).setHeader(datasetResources.message(OWNER));
    verify(grid.owner, atLeastOnce()).setFooter(datasetResources.message(OWNER));
    verify(grid.edit).setHeader(webResources.message(EDIT));
    verify(grid.edit).setFooter(webResources.message(EDIT));
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
    assertFalse(grid.edit.isVisible());
  }

  @Test
  public void datasets_ColumnsValueProvider() {
    grid = mockColumns();
    grid.init();
    grid.addEditListener(editListener);
    verify(grid).addColumn(valueProviderCaptor.capture(), eq(NAME));
    ValueProvider<Dataset, String> valueProvider = valueProviderCaptor.getValue();
    for (Dataset dataset : datasets) {
      assertEquals(dataset.getName(), valueProvider.apply(dataset));
    }
    verify(grid.name).setComparator(comparatorCaptor.capture());
    Comparator<Dataset> comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Dataset dataset : datasets) {
      assertEquals(dataset.getName(),
          ((NormalizedComparator<Dataset>) comparator).getConverter().apply(dataset));
    }
    verify(grid).addColumn(valueProviderCaptor.capture(), eq(TAGS));
    valueProvider = valueProviderCaptor.getValue();
    for (Dataset dataset : datasets) {
      assertEquals(dataset.getTags().stream().collect(Collectors.joining(", ")),
          valueProvider.apply(dataset));
    }
    verify(grid).addColumn(valueProviderCaptor.capture(), eq(PROTOCOL));
    valueProvider = valueProviderCaptor.getValue();
    for (Dataset dataset : datasets) {
      assertEquals(protocol(dataset).getName(), valueProvider.apply(dataset));
    }
    verify(grid).addColumn(localDateRendererCaptor.capture(), eq(DATE));
    LocalDateRenderer<Dataset> localDateRenderer = localDateRendererCaptor.getValue();
    for (Dataset dataset : datasets) {
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(dataset.getDate()),
          getFormattedValue(localDateRenderer, dataset));
    }
    verify(grid).addColumn(valueProviderCaptor.capture(), eq(OWNER));
    valueProvider = valueProviderCaptor.getValue();
    for (Dataset dataset : datasets) {
      assertEquals(dataset.getOwner().getEmail(), valueProvider.apply(dataset));
    }
    verify(grid.owner).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Dataset dataset : datasets) {
      assertEquals(dataset.getOwner().getEmail(),
          ((NormalizedComparator<Dataset>) comparator).getConverter().apply(dataset));
    }
    verify(grid).addColumn(litRendererCaptor.capture(), eq(EDIT));
    LitRenderer<Dataset> litRenderer = litRendererCaptor.getValue();
    for (Dataset dataset : datasets) {
      assertEquals(EDIT_BUTTON, rendererTemplate(litRenderer));
      assertTrue(functions(litRenderer).containsKey("edit"));
      functions(litRenderer).get("edit").accept(dataset, null);
      verify(editListener, atLeastOnce()).onComponentEvent(editEventCaptor.capture());
      assertEquals(dataset, editEventCaptor.getValue().getItem());
    }
  }

  @Test
  public void filterName() {
    grid.nameFilter.setValue("test");

    verify(presenter).filterName("test");
  }

  @Test
  public void filterTags() {
    grid.tagsFilter.setValue("test");

    verify(presenter).filterTags("test");
  }

  @Test
  public void filterProtocol() {
    grid.protocolFilter.setValue("test");

    verify(presenter).filterProtocol("test");
  }

  @Test
  public void filterDate() {
    Range<LocalDate> range =
        Range.closed(LocalDate.now().minusDays(11), LocalDate.now().minusDays(3));
    grid.dateFilter.setValue(range);

    verify(presenter).filterDate(range);
  }

  @Test
  public void filterOwner() {
    grid.ownerFilter.setValue("test");

    verify(presenter).filterOwner("test");
  }

  @Test
  public void addEditListener() {
    Registration registration = grid.addEditListener(editListener);
    assertTrue(grid.edit.isVisible());
    registration.remove();
    assertFalse(grid.edit.isVisible());
  }

  @Test
  public void refreshDataset() {
    grid.refreshDatasets();
    verify(presenter).refreshDatasets();
  }
}
