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

import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.OWNER;
import static ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog.ID;
import static ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog.SAMPLES;
import static ca.qc.ircm.lanaseq.sample.web.SelectSampleDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.getFormattedValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import com.google.common.collect.Range;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.data.renderer.BasicRenderer;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link SelectSampleDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SelectSampleDialogTest extends AbstractKaribuTestCase {
  private SelectSampleDialog dialog;
  @Mock
  private SelectSampleDialogPresenter presenter;
  @Captor
  private ArgumentCaptor<ValueProvider<Sample, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<LocalDateRenderer<Sample>> localDateRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<Sample>> comparatorCaptor;
  @Autowired
  private SampleRepository sampleRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources sampleResources = new AppResources(Sample.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Sample> samples;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    dialog = new SelectSampleDialog(presenter);
    dialog.init();
    samples = sampleRepository.findAll();
  }

  @SuppressWarnings("unchecked")
  private void mockColumns() {
    Element samplesElement = dialog.samples.getElement();
    dialog.samples = mock(Grid.class);
    when(dialog.samples.getElement()).thenReturn(samplesElement);
    dialog.name = mock(Column.class);
    when(dialog.samples.addColumn(any(ValueProvider.class), eq(NAME))).thenReturn(dialog.name);
    when(dialog.name.setKey(any())).thenReturn(dialog.name);
    when(dialog.name.setComparator(any(Comparator.class))).thenReturn(dialog.name);
    when(dialog.name.setHeader(any(String.class))).thenReturn(dialog.name);
    dialog.date = mock(Column.class);
    when(dialog.samples.addColumn(any(BasicRenderer.class))).thenReturn(dialog.date);
    when(dialog.date.setKey(any())).thenReturn(dialog.date);
    when(dialog.date.setSortProperty(any())).thenReturn(dialog.date);
    when(dialog.date.setComparator(any(Comparator.class))).thenReturn(dialog.date);
    when(dialog.date.setHeader(any(String.class))).thenReturn(dialog.date);
    dialog.owner = mock(Column.class);
    when(dialog.samples.addColumn(any(ValueProvider.class), eq(OWNER))).thenReturn(dialog.owner);
    when(dialog.owner.setKey(any())).thenReturn(dialog.owner);
    when(dialog.owner.setComparator(any(Comparator.class))).thenReturn(dialog.owner);
    when(dialog.owner.setHeader(any(String.class))).thenReturn(dialog.owner);
    HeaderRow filtersRow = mock(HeaderRow.class);
    when(dialog.samples.appendHeaderRow()).thenReturn(filtersRow);
    HeaderCell nameFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(dialog.name)).thenReturn(nameFilterCell);
    HeaderCell dateFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(dialog.date)).thenReturn(dateFilterCell);
    HeaderCell ownerFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(dialog.owner)).thenReturn(ownerFilterCell);
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(dialog);
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(SAMPLES), dialog.samples.getId().orElse(""));
  }

  @Test
  public void labels() {
    mockColumns();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    verify(dialog.name).setHeader(sampleResources.message(NAME));
    verify(dialog.name).setFooter(sampleResources.message(NAME));
    verify(dialog.date).setHeader(sampleResources.message(DATE));
    verify(dialog.date).setFooter(sampleResources.message(DATE));
    verify(dialog.owner).setHeader(sampleResources.message(OWNER));
    verify(dialog.owner).setFooter(sampleResources.message(OWNER));
    assertEquals(webResources.message(ALL), dialog.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), dialog.ownerFilter.getPlaceholder());
  }

  @Test
  public void localeChange() {
    mockColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources sampleResources = new AppResources(Sample.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    ui.setLocale(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    verify(dialog.name, atLeastOnce()).setHeader(sampleResources.message(NAME));
    verify(dialog.name, atLeastOnce()).setFooter(sampleResources.message(NAME));
    verify(dialog.date, atLeastOnce()).setHeader(sampleResources.message(DATE));
    verify(dialog.date, atLeastOnce()).setFooter(sampleResources.message(DATE));
    verify(dialog.owner).setHeader(sampleResources.message(OWNER));
    verify(dialog.owner).setFooter(sampleResources.message(OWNER));
    assertEquals(webResources.message(ALL), dialog.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), dialog.ownerFilter.getPlaceholder());
  }

  @Test
  public void samples() {
    assertEquals(3, dialog.samples.getColumns().size());
    assertNotNull(dialog.samples.getColumnByKey(NAME));
    assertNotNull(dialog.samples.getColumnByKey(DATE));
    assertNotNull(dialog.samples.getColumnByKey(OWNER));
    assertTrue(dialog.samples.getSelectionModel() instanceof SelectionModel.Single);
  }

  @Test
  public void samples_ColumnsValueProvider() {
    mockColumns();
    dialog.init();
    verify(dialog.samples).addColumn(valueProviderCaptor.capture(), eq(NAME));
    ValueProvider<Sample, String> valueProvider = valueProviderCaptor.getValue();
    for (Sample sample : samples) {
      assertEquals(sample.getName(), valueProvider.apply(sample));
    }
    verify(dialog.name).setComparator(comparatorCaptor.capture());
    Comparator<Sample> comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Sample sample : samples) {
      assertEquals(sample.getName(),
          ((NormalizedComparator<Sample>) comparator).getConverter().apply(sample));
    }
    verify(dialog.samples).addColumn(localDateRendererCaptor.capture());
    LocalDateRenderer<Sample> localDateTimeRenderer = localDateRendererCaptor.getValue();
    for (Sample sample : samples) {
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(sample.getDate()),
          getFormattedValue(localDateTimeRenderer, sample));
    }
    verify(dialog.date).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    LocalDate firstDate = samples.get(0).getDate();
    for (Sample sample : samples) {
      assertEquals(firstDate.compareTo(sample.getDate()),
          comparator.compare(samples.get(0), sample));
    }
    verify(dialog.samples).addColumn(valueProviderCaptor.capture(), eq(OWNER));
    valueProvider = valueProviderCaptor.getValue();
    for (Sample sample : samples) {
      assertEquals(sample.getOwner().getEmail(), valueProvider.apply(sample));
    }
    verify(dialog.owner).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Sample sample : samples) {
      assertEquals(sample.getOwner().getEmail(),
          ((NormalizedComparator<Sample>) comparator).getConverter().apply(sample));
    }
  }

  @Test
  public void select() {
    Sample sample = samples.get(0);
    doubleClickItem(dialog.samples, sample);

    verify(presenter).select(sample);
  }

  @Test
  public void filterName() {
    dialog.nameFilter.setValue("test");

    verify(presenter).filterName("test");
  }

  @Test
  public void filterDate() {
    Range<LocalDate> range = Range.closed(LocalDate.now().minusDays(10), LocalDate.now());
    dialog.dateFilter.setValue(range);

    verify(presenter).filterDate(range);
  }

  @Test
  public void filterOwner() {
    dialog.ownerFilter.setValue("test");

    verify(presenter).filterOwner("test");
  }
}
