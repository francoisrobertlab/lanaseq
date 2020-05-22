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
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.OWNER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.DATASETS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.MERGE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.getFormattedValue;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import com.google.common.collect.Range;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class DatasetsViewTest extends AbstractViewTestCase {
  private DatasetsView view;
  @Mock
  private DatasetsViewPresenter presenter;
  @Mock
  private DatasetDialog datasetDialog;
  @Mock
  private ProtocolDialog protocolDialog;
  @Captor
  private ArgumentCaptor<ValueProvider<Dataset, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<LocalDateTimeRenderer<Dataset>> localDateTimeRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<Dataset>> comparatorCaptor;
  @Autowired
  private DatasetRepository datasetRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetsView.class, locale);
  private AppResources datasetResources = new AppResources(Dataset.class, locale);
  private AppResources sampleResources = new AppResources(Sample.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Dataset> datasets;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    view = new DatasetsView(presenter, datasetDialog, protocolDialog);
    view.init();
    datasets = datasetRepository.findAll();
  }

  private Protocol protocol(Dataset dataset) {
    return dataset.getSamples() != null
        ? dataset.getSamples().stream().findFirst().map(s -> s.getProtocol()).orElse(new Protocol())
        : new Protocol();
  }

  @SuppressWarnings("unchecked")
  private void mockColumns() {
    Element datasetsElement = view.datasets.getElement();
    view.datasets = mock(Grid.class);
    when(view.datasets.getElement()).thenReturn(datasetsElement);
    view.name = mock(Column.class);
    when(view.datasets.addColumn(any(ValueProvider.class), eq(NAME))).thenReturn(view.name);
    when(view.name.setKey(any())).thenReturn(view.name);
    when(view.name.setComparator(any(Comparator.class))).thenReturn(view.name);
    when(view.name.setHeader(any(String.class))).thenReturn(view.name);
    view.protocol = mock(Column.class);
    when(view.datasets.addColumn(any(ValueProvider.class), eq(PROTOCOL))).thenReturn(view.protocol);
    when(view.protocol.setKey(any())).thenReturn(view.protocol);
    when(view.protocol.setComparator(any(Comparator.class))).thenReturn(view.protocol);
    when(view.protocol.setHeader(any(String.class))).thenReturn(view.protocol);
    view.date = mock(Column.class);
    when(view.datasets.addColumn(any(LocalDateTimeRenderer.class), eq(DATE))).thenReturn(view.date);
    when(view.date.setKey(any())).thenReturn(view.date);
    when(view.date.setHeader(any(String.class))).thenReturn(view.date);
    view.owner = mock(Column.class);
    when(view.datasets.addColumn(any(ValueProvider.class), eq(OWNER))).thenReturn(view.owner);
    when(view.owner.setKey(any())).thenReturn(view.owner);
    when(view.owner.setComparator(any(Comparator.class))).thenReturn(view.owner);
    when(view.owner.setHeader(any(String.class))).thenReturn(view.owner);
    HeaderRow filtersRow = mock(HeaderRow.class);
    when(view.datasets.appendHeaderRow()).thenReturn(filtersRow);
    HeaderCell nameFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.name)).thenReturn(nameFilterCell);
    HeaderCell protocolFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.protocol)).thenReturn(protocolFilterCell);
    HeaderCell dateFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.date)).thenReturn(dateFilterCell);
    HeaderCell ownerFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.owner)).thenReturn(ownerFilterCell);
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(view);
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(HEADER, view.header.getId().orElse(""));
    assertEquals(DATASETS, view.datasets.getId().orElse(""));
    assertEquals(ERROR_TEXT, view.error.getId().orElse(""));
    assertEquals(ADD, view.add.getId().orElse(""));
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
    assertEquals(MERGE, view.merge.getId().orElse(""));
    validateIcon(VaadinIcon.CONNECT.create(), view.merge.getIcon());
  }

  @Test
  public void labels() {
    mockColumns();
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.name).setHeader(datasetResources.message(NAME));
    verify(view.name).setFooter(datasetResources.message(NAME));
    verify(view.protocol).setHeader(sampleResources.message(PROTOCOL));
    verify(view.protocol).setFooter(sampleResources.message(PROTOCOL));
    verify(view.date).setHeader(datasetResources.message(DATE));
    verify(view.date).setFooter(datasetResources.message(DATE));
    verify(view.owner).setHeader(datasetResources.message(OWNER));
    verify(view.owner).setFooter(datasetResources.message(OWNER));
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.protocolFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.ownerFilter.getPlaceholder());
    assertEquals(webResources.message(ADD), view.add.getText());
    assertEquals(resources.message(MERGE), view.merge.getText());
  }

  @Test
  public void localeChange() {
    mockColumns();
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(DatasetsView.class, locale);
    final AppResources datasetResources = new AppResources(Dataset.class, locale);
    final AppResources sampleResources = new AppResources(Sample.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.name).setHeader(datasetResources.message(NAME));
    verify(view.name).setFooter(datasetResources.message(NAME));
    verify(view.protocol).setHeader(sampleResources.message(PROTOCOL));
    verify(view.protocol).setFooter(sampleResources.message(PROTOCOL));
    verify(view.date, atLeastOnce()).setHeader(datasetResources.message(DATE));
    verify(view.date, atLeastOnce()).setFooter(datasetResources.message(DATE));
    verify(view.owner, atLeastOnce()).setHeader(datasetResources.message(OWNER));
    verify(view.owner, atLeastOnce()).setFooter(datasetResources.message(OWNER));
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.protocolFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.ownerFilter.getPlaceholder());
    assertEquals(webResources.message(ADD), view.add.getText());
    assertEquals(resources.message(MERGE), view.merge.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, webResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void datasets() {
    assertEquals(4, view.datasets.getColumns().size());
    assertNotNull(view.datasets.getColumnByKey(NAME));
    assertNotNull(view.datasets.getColumnByKey(PROTOCOL));
    assertNotNull(view.datasets.getColumnByKey(DATE));
    assertNotNull(view.datasets.getColumnByKey(OWNER));
    assertTrue(view.datasets.getSelectionModel() instanceof SelectionModel.Single);
  }

  @Test
  public void datasets_ColumnsValueProvider() {
    view = new DatasetsView(presenter, datasetDialog, protocolDialog);
    mockColumns();
    view.init();
    verify(view.datasets).addColumn(valueProviderCaptor.capture(), eq(NAME));
    ValueProvider<Dataset, String> valueProvider = valueProviderCaptor.getValue();
    for (Dataset dataset : datasets) {
      assertEquals(dataset.getName(), valueProvider.apply(dataset));
    }
    verify(view.name).setComparator(comparatorCaptor.capture());
    Comparator<Dataset> comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Dataset dataset : datasets) {
      assertEquals(dataset.getName(),
          ((NormalizedComparator<Dataset>) comparator).getConverter().apply(dataset));
    }
    verify(view.datasets).addColumn(valueProviderCaptor.capture(), eq(PROTOCOL));
    valueProvider = valueProviderCaptor.getValue();
    for (Dataset dataset : datasets) {
      assertEquals(protocol(dataset).getName(), valueProvider.apply(dataset));
    }
    verify(view.protocol).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Dataset dataset : datasets) {
      assertEquals(protocol(dataset).getName(),
          ((NormalizedComparator<Dataset>) comparator).getConverter().apply(dataset));
    }
    verify(view.datasets).addColumn(localDateTimeRendererCaptor.capture(), eq(DATE));
    LocalDateTimeRenderer<Dataset> localDateTimeRenderer = localDateTimeRendererCaptor.getValue();
    for (Dataset dataset : datasets) {
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(dataset.getDate()),
          getFormattedValue(localDateTimeRenderer, dataset));
    }
    verify(view.datasets).addColumn(valueProviderCaptor.capture(), eq(OWNER));
    valueProvider = valueProviderCaptor.getValue();
    for (Dataset dataset : datasets) {
      assertEquals(dataset.getOwner().getEmail(), valueProvider.apply(dataset));
    }
    verify(view.owner).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Dataset dataset : datasets) {
      assertEquals(dataset.getOwner().getEmail(),
          ((NormalizedComparator<Dataset>) comparator).getConverter().apply(dataset));
    }
  }

  @Test
  public void view() {
    Dataset dataset = datasets.get(0);
    doubleClickItem(view.datasets, dataset);

    verify(presenter).view(dataset);
  }

  @Test
  public void viewProtocol() {
    Dataset dataset = datasets.get(0);
    doubleClickItem(view.datasets, dataset, view.protocol);

    verify(presenter).view(protocol(dataset));
  }

  @Test
  public void viewProtocol_NoProtocol() {
    Dataset dataset = find(datasets, 3L).get();
    doubleClickItem(view.datasets, dataset, view.protocol);

    verify(presenter).view(dataset);
  }

  @Test
  public void filterName() {
    view.nameFilter.setValue("test");

    verify(presenter).filterName("test");
  }

  @Test
  public void filterProtocol() {
    view.protocolFilter.setValue("test");

    verify(presenter).filterProtocol("test");
  }

  @Test
  public void filterDate() {
    Range<LocalDate> range =
        Range.closed(LocalDate.now().minusDays(11), LocalDate.now().minusDays(3));
    view.dateFilter.setValue(range);

    verify(presenter).filterDate(range);
  }

  @Test
  public void filterOwner() {
    view.ownerFilter.setValue("test");

    verify(presenter).filterOwner("test");
  }

  @Test
  public void add() {
    clickButton(view.add);
    verify(presenter).add();
  }

  @Test
  public void merge() {
    clickButton(view.merge);
    verify(presenter).merge(locale);
  }
}
