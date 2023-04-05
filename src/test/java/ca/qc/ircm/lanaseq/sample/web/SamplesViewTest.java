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

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.OWNER;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.ANALYZE;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.EDIT_BUTTON;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.FILES;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.HEADER;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.ID;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.MERGE;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.functions;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.getFormattedValue;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.rendererTemplate;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import com.google.common.collect.Range;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.selection.SelectionModel;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
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
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link SamplesView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SamplesViewTest extends AbstractKaribuTestCase {
  private SamplesView view;
  @Mock
  private SamplesViewPresenter presenter;
  @Autowired
  private ObjectFactory<SampleDialog> dialogFactory;
  @Autowired
  private ObjectFactory<SampleFilesDialog> filesDialogFactory;
  @Autowired
  private ObjectFactory<SamplesAnalysisDialog> analysisDialogFactory;
  @Captor
  private ArgumentCaptor<ValueProvider<Sample, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<LocalDateRenderer<Sample>> localDateRendererCaptor;
  @Captor
  private ArgumentCaptor<LitRenderer<Sample>> litRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<Sample>> comparatorCaptor;
  @Autowired
  private SampleRepository sampleRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SamplesView.class, locale);
  private AppResources sampleResources = new AppResources(Sample.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Sample> samples;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    view = new SamplesView(presenter, dialogFactory, filesDialogFactory, analysisDialogFactory);
    view.init();
    samples = sampleRepository.findAll();
  }

  @SuppressWarnings("unchecked")
  private void mockColumns() {
    Element samplesElement = view.samples.getElement();
    view.samples = mock(Grid.class);
    when(view.samples.getElement()).thenReturn(samplesElement);
    view.name = mock(Column.class);
    when(view.samples.addColumn(any(ValueProvider.class), eq(NAME))).thenReturn(view.name);
    when(view.name.setKey(any())).thenReturn(view.name);
    when(view.name.setSortable(anyBoolean())).thenReturn(view.name);
    when(view.name.setSortProperty(any())).thenReturn(view.name);
    when(view.name.setComparator(any(Comparator.class))).thenReturn(view.name);
    when(view.name.setHeader(any(String.class))).thenReturn(view.name);
    when(view.name.setFlexGrow(anyInt())).thenReturn(view.name);
    view.protocol = mock(Column.class);
    when(view.samples.addColumn(any(ValueProvider.class), eq(PROTOCOL))).thenReturn(view.protocol);
    when(view.protocol.setKey(any())).thenReturn(view.protocol);
    when(view.protocol.setSortable(anyBoolean())).thenReturn(view.protocol);
    when(view.protocol.setSortProperty(any())).thenReturn(view.protocol);
    when(view.protocol.setComparator(any(Comparator.class))).thenReturn(view.protocol);
    when(view.protocol.setHeader(any(String.class))).thenReturn(view.protocol);
    when(view.protocol.setFlexGrow(anyInt())).thenReturn(view.protocol);
    view.date = mock(Column.class);
    when(view.samples.addColumn(any(LocalDateRenderer.class))).thenReturn(view.date);
    when(view.date.setKey(any())).thenReturn(view.date);
    when(view.date.setSortable(anyBoolean())).thenReturn(view.date);
    when(view.date.setSortProperty(any())).thenReturn(view.date);
    when(view.date.setComparator(any(Comparator.class))).thenReturn(view.date);
    when(view.date.setHeader(any(String.class))).thenReturn(view.date);
    when(view.date.setFlexGrow(anyInt())).thenReturn(view.date);
    view.owner = mock(Column.class);
    when(view.samples.addColumn(any(ValueProvider.class), eq(OWNER))).thenReturn(view.owner);
    when(view.owner.setKey(any())).thenReturn(view.owner);
    when(view.owner.setSortable(anyBoolean())).thenReturn(view.owner);
    when(view.owner.setSortProperty(any())).thenReturn(view.owner);
    when(view.owner.setComparator(any(Comparator.class))).thenReturn(view.owner);
    when(view.owner.setHeader(any(String.class))).thenReturn(view.owner);
    when(view.owner.setFlexGrow(anyInt())).thenReturn(view.owner);
    view.edit = mock(Column.class);
    when(view.samples.addColumn(any(LitRenderer.class))).thenReturn(view.edit);
    when(view.edit.setKey(any())).thenReturn(view.edit);
    when(view.edit.setSortable(anyBoolean())).thenReturn(view.edit);
    when(view.edit.setSortProperty(any())).thenReturn(view.edit);
    when(view.edit.setComparator(any(Comparator.class))).thenReturn(view.edit);
    when(view.edit.setHeader(any(String.class))).thenReturn(view.edit);
    when(view.edit.setFlexGrow(anyInt())).thenReturn(view.edit);
    HeaderRow filtersRow = mock(HeaderRow.class);
    when(view.samples.appendHeaderRow()).thenReturn(filtersRow);
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
    assertEquals(SAMPLES, view.samples.getId().orElse(""));
    assertEquals(ERROR_TEXT, view.error.getId().orElse(""));
    assertTrue(view.error.getClassNames().contains(ERROR_TEXT));
    assertEquals(ADD, view.add.getId().orElse(""));
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
    assertEquals(MERGE, view.merge.getId().orElse(""));
    validateIcon(VaadinIcon.CONNECT.create(), view.merge.getIcon());
    assertEquals(FILES, view.files.getId().orElse(""));
    validateIcon(VaadinIcon.FILE_O.create(), view.files.getIcon());
    assertEquals(ANALYZE, view.analyze.getId().orElse(""));
  }

  @Test
  public void labels() {
    mockColumns();
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.name).setHeader(sampleResources.message(NAME));
    verify(view.name).setFooter(sampleResources.message(NAME));
    verify(view.protocol).setHeader(sampleResources.message(PROTOCOL));
    verify(view.protocol).setFooter(sampleResources.message(PROTOCOL));
    verify(view.date).setHeader(sampleResources.message(DATE));
    verify(view.date).setFooter(sampleResources.message(DATE));
    verify(view.owner).setHeader(sampleResources.message(OWNER));
    verify(view.owner).setFooter(sampleResources.message(OWNER));
    verify(view.edit).setHeader(webResources.message(EDIT));
    verify(view.edit).setFooter(webResources.message(EDIT));
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.protocolFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.ownerFilter.getPlaceholder());
    assertEquals(webResources.message(ADD), view.add.getText());
    assertEquals(resources.message(MERGE), view.merge.getText());
    assertEquals(resources.message(FILES), view.files.getText());
    assertEquals(resources.message(ANALYZE), view.analyze.getText());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    mockColumns();
    view.init();
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(SamplesView.class, locale);
    final AppResources sampleResources = new AppResources(Sample.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    ui.setLocale(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.name, atLeastOnce()).setHeader(sampleResources.message(NAME));
    verify(view.name, atLeastOnce()).setFooter(sampleResources.message(NAME));
    verify(view.protocol).setHeader(sampleResources.message(PROTOCOL));
    verify(view.protocol).setFooter(sampleResources.message(PROTOCOL));
    verify(view.date, atLeastOnce()).setHeader(sampleResources.message(DATE));
    verify(view.date, atLeastOnce()).setFooter(sampleResources.message(DATE));
    verify(view.owner, atLeastOnce()).setHeader(sampleResources.message(OWNER));
    verify(view.owner, atLeastOnce()).setFooter(sampleResources.message(OWNER));
    verify(view.edit).setHeader(webResources.message(EDIT));
    verify(view.edit).setFooter(webResources.message(EDIT));
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.protocolFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.ownerFilter.getPlaceholder());
    assertEquals(webResources.message(ADD), view.add.getText());
    assertEquals(resources.message(MERGE), view.merge.getText());
    assertEquals(resources.message(FILES), view.files.getText());
    assertEquals(resources.message(ANALYZE), view.analyze.getText());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, webResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void samples() {
    assertEquals(5, view.samples.getColumns().size());
    assertNotNull(view.samples.getColumnByKey(NAME));
    assertEquals(NAME, view.samples.getColumnByKey(NAME).getSortOrder(SortDirection.ASCENDING)
        .findFirst().map(so -> so.getSorted()).orElse(null));
    assertTrue(view.name.isSortable());
    assertNotNull(view.samples.getColumnByKey(PROTOCOL));
    assertEquals(PROTOCOL + "." + NAME, view.samples.getColumnByKey(PROTOCOL)
        .getSortOrder(SortDirection.ASCENDING).findFirst().map(so -> so.getSorted()).orElse(null));
    assertTrue(view.protocol.isSortable());
    assertNotNull(view.samples.getColumnByKey(DATE));
    assertEquals(DATE, view.samples.getColumnByKey(DATE).getSortOrder(SortDirection.ASCENDING)
        .findFirst().map(so -> so.getSorted()).orElse(null));
    assertNotNull(view.samples.getColumnByKey(OWNER));
    assertTrue(view.date.isSortable());
    assertEquals(OWNER + "." + EMAIL, view.samples.getColumnByKey(OWNER)
        .getSortOrder(SortDirection.ASCENDING).findFirst().map(so -> so.getSorted()).orElse(null));
    assertTrue(view.samples.getSelectionModel() instanceof SelectionModel.Multi);
    assertEquals(GridSortOrder.desc(view.date).build(), view.samples.getSortOrder());
    assertTrue(view.owner.isSortable());
  }

  @Test
  public void samples_ColumnsValueProvider() {
    mockColumns();
    view.init();
    verify(view.samples).addColumn(valueProviderCaptor.capture(), eq(NAME));
    ValueProvider<Sample, String> valueProvider = valueProviderCaptor.getValue();
    for (Sample sample : samples) {
      assertEquals(sample.getName(), valueProvider.apply(sample));
    }
    verify(view.name).setComparator(comparatorCaptor.capture());
    Comparator<Sample> comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Sample sample : samples) {
      assertEquals(sample.getName(),
          ((NormalizedComparator<Sample>) comparator).getConverter().apply(sample));
    }
    verify(view.samples).addColumn(valueProviderCaptor.capture(), eq(PROTOCOL));
    valueProvider = valueProviderCaptor.getValue();
    for (Sample sample : samples) {
      assertEquals(sample.getProtocol().getName(), valueProvider.apply(sample));
    }
    verify(view.protocol).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Sample sample : samples) {
      assertEquals(sample.getProtocol().getName(),
          ((NormalizedComparator<Sample>) comparator).getConverter().apply(sample));
    }
    verify(view.samples, times(2)).addColumn(localDateRendererCaptor.capture());
    LocalDateRenderer<Sample> localDateRenderer = localDateRendererCaptor.getAllValues().get(0);
    for (Sample sample : samples) {
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(sample.getDate()),
          getFormattedValue(localDateRenderer, sample));
    }
    verify(view.date).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    LocalDate firstDate = samples.get(0).getDate();
    for (Sample sample : samples) {
      assertEquals(firstDate.compareTo(sample.getDate()),
          comparator.compare(samples.get(0), sample));
    }
    verify(view.samples).addColumn(valueProviderCaptor.capture(), eq(OWNER));
    valueProvider = valueProviderCaptor.getValue();
    for (Sample sample : samples) {
      assertEquals(sample.getOwner().getEmail(), valueProvider.apply(sample));
    }
    verify(view.owner).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Sample sample : samples) {
      assertEquals(sample.getOwner().getEmail(),
          ((NormalizedComparator<Sample>) comparator).getConverter().apply(sample));
    }
    verify(view.samples, times(2)).addColumn(litRendererCaptor.capture());
    LitRenderer<Sample> litRenderer = litRendererCaptor.getAllValues().get(1);
    for (Sample sample : samples) {
      assertEquals(EDIT_BUTTON, rendererTemplate(litRenderer));
      assertTrue(functions(litRenderer).containsKey("edit"));
      functions(litRenderer).get("edit").accept(sample, null);
      verify(presenter).view(sample);
    }
  }

  @Test
  public void view() {
    Sample sample = samples.get(0);
    doubleClickItem(view.samples, sample);

    verify(presenter).view(sample);
  }

  @Test
  public void viewFiles_Control() {
    Sample sample = samples.get(0);
    clickItem(view.samples, sample, view.name, true, false, false, false);

    verify(presenter).viewFiles(sample);
  }

  @Test
  public void addFiles_Meta() {
    Sample sample = samples.get(0);
    clickItem(view.samples, sample, view.name, false, false, false, true);

    verify(presenter).viewFiles(sample);
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
    Range<LocalDate> range = Range.closed(LocalDate.now().minusDays(10), LocalDate.now());
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
    verify(presenter).merge();
  }

  @Test
  public void files() {
    clickButton(view.files);
    verify(presenter).viewFiles();
  }

  @Test
  public void analyze() {
    clickButton(view.analyze);
    verify(presenter).analyze();
  }
}
