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
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.OWNER;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.HEADER;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.ID;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.SAMPLES;
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
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
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
public class SamplesViewTest extends AbstractViewTestCase {
  private SamplesView view;
  @Mock
  private SamplesViewPresenter presenter;
  @Mock
  private SampleDialog dialog;
  @Captor
  private ArgumentCaptor<ValueProvider<Sample, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<LocalDateTimeRenderer<Sample>> localDateTimeRendererCaptor;
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
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    view = new SamplesView(presenter, dialog);
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
    when(view.name.setComparator(any(Comparator.class))).thenReturn(view.name);
    when(view.name.setHeader(any(String.class))).thenReturn(view.name);
    view.protocol = mock(Column.class);
    when(view.samples.addColumn(any(ValueProvider.class), eq(PROTOCOL))).thenReturn(view.protocol);
    when(view.protocol.setKey(any())).thenReturn(view.protocol);
    when(view.protocol.setComparator(any(Comparator.class))).thenReturn(view.protocol);
    when(view.protocol.setHeader(any(String.class))).thenReturn(view.protocol);
    view.date = mock(Column.class);
    when(view.samples.addColumn(any(LocalDateTimeRenderer.class), eq(DATE))).thenReturn(view.date);
    when(view.date.setKey(any())).thenReturn(view.date);
    when(view.date.setHeader(any(String.class))).thenReturn(view.date);
    view.owner = mock(Column.class);
    when(view.samples.addColumn(any(ValueProvider.class), eq(OWNER))).thenReturn(view.owner);
    when(view.owner.setKey(any())).thenReturn(view.owner);
    when(view.owner.setComparator(any(Comparator.class))).thenReturn(view.owner);
    when(view.owner.setHeader(any(String.class))).thenReturn(view.owner);
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
    assertEquals(ADD, view.add.getId().orElse(""));
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
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.protocolFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.ownerFilter.getPlaceholder());
    assertEquals(webResources.message(ADD), view.add.getText());
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
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
    when(ui.getLocale()).thenReturn(locale);
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
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.protocolFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.ownerFilter.getPlaceholder());
    assertEquals(webResources.message(ADD), view.add.getText());
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, webResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void samples() {
    assertEquals(4, view.samples.getColumns().size());
    assertNotNull(view.samples.getColumnByKey(NAME));
    assertNotNull(view.samples.getColumnByKey(PROTOCOL));
    assertNotNull(view.samples.getColumnByKey(DATE));
    assertNotNull(view.samples.getColumnByKey(OWNER));
    assertTrue(view.samples.getSelectionModel() instanceof SelectionModel.Single);
  }

  @Test
  public void samples_ColumnsValueProvider() {
    view = new SamplesView(presenter, dialog);
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
    verify(view.samples).addColumn(localDateTimeRendererCaptor.capture(), eq(DATE));
    LocalDateTimeRenderer<Sample> localDateTimeRenderer = localDateTimeRendererCaptor.getValue();
    for (Sample sample : samples) {
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(sample.getDate()),
          getFormattedValue(localDateTimeRenderer, sample));
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
  }

  @Test
  public void view() {
    Sample sample = samples.get(0);
    doubleClickItem(view.samples, sample);

    verify(presenter).view(sample);
  }

  @Test
  public void view_Protocol() {
    Sample sample = samples.get(0);
    doubleClickItem(view.samples, sample, view.protocol);

    verify(presenter).view(sample.getProtocol());
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
}
