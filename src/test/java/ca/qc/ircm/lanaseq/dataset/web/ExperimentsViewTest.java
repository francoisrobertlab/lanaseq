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
import static ca.qc.ircm.lanaseq.dataset.web.ExperimentsView.EXPERIMENTS;
import static ca.qc.ircm.lanaseq.dataset.web.ExperimentsView.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.ExperimentsView.ID;
import static ca.qc.ircm.lanaseq.dataset.web.ExperimentsView.PERMISSIONS;
import static ca.qc.ircm.lanaseq.dataset.ExperimentProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.ExperimentProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.ExperimentProperties.OWNER;
import static ca.qc.ircm.lanaseq.dataset.ExperimentProperties.PROJECT;
import static ca.qc.ircm.lanaseq.dataset.ExperimentProperties.PROTOCOL;
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
import ca.qc.ircm.lanaseq.dataset.Experiment;
import ca.qc.ircm.lanaseq.dataset.ExperimentRepository;
import ca.qc.ircm.lanaseq.dataset.web.ExperimentDialog;
import ca.qc.ircm.lanaseq.dataset.web.ExperimentPermissionsDialog;
import ca.qc.ircm.lanaseq.dataset.web.ExperimentsView;
import ca.qc.ircm.lanaseq.dataset.web.ExperimentsViewPresenter;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
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
public class ExperimentsViewTest extends AbstractViewTestCase {
  private ExperimentsView view;
  @Mock
  private ExperimentsViewPresenter presenter;
  @Mock
  private ExperimentDialog experimentDialog;
  @Mock
  private ProtocolDialog protocolDialog;
  @Mock
  private ExperimentPermissionsDialog experimentPermissionsDialog;
  @Captor
  private ArgumentCaptor<ValueProvider<Experiment, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<LocalDateTimeRenderer<Experiment>> localDateTimeRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<Experiment>> comparatorCaptor;
  @Autowired
  private ExperimentRepository experimentRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(ExperimentsView.class, locale);
  private AppResources experimentResources = new AppResources(Experiment.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Experiment> experiments;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    view = new ExperimentsView(presenter, experimentDialog, protocolDialog,
        experimentPermissionsDialog);
    view.init();
    experiments = experimentRepository.findAll();
  }

  @SuppressWarnings("unchecked")
  private void mockColumns() {
    Element experimentsElement = view.experiments.getElement();
    view.experiments = mock(Grid.class);
    when(view.experiments.getElement()).thenReturn(experimentsElement);
    view.name = mock(Column.class);
    when(view.experiments.addColumn(any(ValueProvider.class), eq(NAME))).thenReturn(view.name);
    when(view.name.setKey(any())).thenReturn(view.name);
    when(view.name.setComparator(any(Comparator.class))).thenReturn(view.name);
    when(view.name.setHeader(any(String.class))).thenReturn(view.name);
    view.project = mock(Column.class);
    when(view.experiments.addColumn(any(ValueProvider.class), eq(PROJECT)))
        .thenReturn(view.project);
    when(view.project.setKey(any())).thenReturn(view.project);
    when(view.project.setComparator(any(Comparator.class))).thenReturn(view.project);
    when(view.project.setHeader(any(String.class))).thenReturn(view.project);
    view.protocol = mock(Column.class);
    when(view.experiments.addColumn(any(ValueProvider.class), eq(PROTOCOL)))
        .thenReturn(view.protocol);
    when(view.protocol.setKey(any())).thenReturn(view.protocol);
    when(view.protocol.setComparator(any(Comparator.class))).thenReturn(view.protocol);
    when(view.protocol.setHeader(any(String.class))).thenReturn(view.protocol);
    view.date = mock(Column.class);
    when(view.experiments.addColumn(any(LocalDateTimeRenderer.class), eq(DATE)))
        .thenReturn(view.date);
    when(view.date.setKey(any())).thenReturn(view.date);
    when(view.date.setHeader(any(String.class))).thenReturn(view.date);
    view.owner = mock(Column.class);
    when(view.experiments.addColumn(any(ValueProvider.class), eq(OWNER))).thenReturn(view.owner);
    when(view.owner.setKey(any())).thenReturn(view.owner);
    when(view.owner.setComparator(any(Comparator.class))).thenReturn(view.owner);
    when(view.owner.setHeader(any(String.class))).thenReturn(view.owner);
    HeaderRow filtersRow = mock(HeaderRow.class);
    when(view.experiments.appendHeaderRow()).thenReturn(filtersRow);
    HeaderCell nameFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.name)).thenReturn(nameFilterCell);
    HeaderCell projectFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.project)).thenReturn(projectFilterCell);
    HeaderCell protocolFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.protocol)).thenReturn(protocolFilterCell);
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
    assertEquals(EXPERIMENTS, view.experiments.getId().orElse(""));
    assertEquals(ERROR_TEXT, view.error.getId().orElse(""));
    assertEquals(ADD, view.add.getId().orElse(""));
    assertEquals(PERMISSIONS, view.permissions.getId().orElse(""));
  }

  @Test
  public void labels() {
    mockColumns();
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.name).setHeader(experimentResources.message(NAME));
    verify(view.name).setFooter(experimentResources.message(NAME));
    verify(view.project).setHeader(experimentResources.message(PROJECT));
    verify(view.project).setFooter(experimentResources.message(PROJECT));
    verify(view.protocol).setHeader(experimentResources.message(PROTOCOL));
    verify(view.protocol).setFooter(experimentResources.message(PROTOCOL));
    verify(view.date).setHeader(experimentResources.message(DATE));
    verify(view.date).setFooter(experimentResources.message(DATE));
    verify(view.owner).setHeader(experimentResources.message(OWNER));
    verify(view.owner).setFooter(experimentResources.message(OWNER));
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.projectFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.protocolFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.ownerFilter.getPlaceholder());
    assertEquals(webResources.message(ADD), view.add.getText());
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
    assertEquals(resources.message(PERMISSIONS), view.permissions.getText());
    validateIcon(VaadinIcon.LOCK.create(), view.permissions.getIcon());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    view = new ExperimentsView(presenter, experimentDialog, protocolDialog,
        experimentPermissionsDialog);
    mockColumns();
    view.init();
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(ExperimentsView.class, locale);
    final AppResources experimentResources = new AppResources(Experiment.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.name, atLeastOnce()).setHeader(experimentResources.message(NAME));
    verify(view.name, atLeastOnce()).setFooter(experimentResources.message(NAME));
    verify(view.project).setHeader(experimentResources.message(PROJECT));
    verify(view.project).setFooter(experimentResources.message(PROJECT));
    verify(view.protocol).setHeader(experimentResources.message(PROTOCOL));
    verify(view.protocol).setFooter(experimentResources.message(PROTOCOL));
    verify(view.date, atLeastOnce()).setHeader(experimentResources.message(DATE));
    verify(view.date, atLeastOnce()).setFooter(experimentResources.message(DATE));
    verify(view.owner, atLeastOnce()).setHeader(experimentResources.message(OWNER));
    verify(view.owner, atLeastOnce()).setFooter(experimentResources.message(OWNER));
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.projectFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.protocolFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.ownerFilter.getPlaceholder());
    assertEquals(webResources.message(ADD), view.add.getText());
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
    assertEquals(resources.message(PERMISSIONS), view.permissions.getText());
    validateIcon(VaadinIcon.LOCK.create(), view.permissions.getIcon());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, webResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void experiments() {
    assertEquals(5, view.experiments.getColumns().size());
    assertNotNull(view.experiments.getColumnByKey(NAME));
    assertNotNull(view.experiments.getColumnByKey(PROJECT));
    assertNotNull(view.experiments.getColumnByKey(PROTOCOL));
    assertNotNull(view.experiments.getColumnByKey(DATE));
    assertNotNull(view.experiments.getColumnByKey(OWNER));
    assertTrue(view.experiments.getSelectionModel() instanceof SelectionModel.Single);
  }

  @Test
  public void experiments_ColumnsValueProvider() {
    view = new ExperimentsView(presenter, experimentDialog, protocolDialog,
        experimentPermissionsDialog);
    mockColumns();
    view.init();
    verify(view.experiments).addColumn(valueProviderCaptor.capture(), eq(NAME));
    ValueProvider<Experiment, String> valueProvider = valueProviderCaptor.getValue();
    for (Experiment experiment : experiments) {
      assertEquals(experiment.getName(), valueProvider.apply(experiment));
    }
    verify(view.name).setComparator(comparatorCaptor.capture());
    Comparator<Experiment> comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Experiment experiment : experiments) {
      assertEquals(experiment.getName(),
          ((NormalizedComparator<Experiment>) comparator).getConverter().apply(experiment));
    }
    verify(view.experiments).addColumn(valueProviderCaptor.capture(), eq(PROJECT));
    valueProvider = valueProviderCaptor.getValue();
    for (Experiment experiment : experiments) {
      assertEquals(experiment.getProject(), valueProvider.apply(experiment));
    }
    verify(view.project).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Experiment experiment : experiments) {
      assertEquals(experiment.getProject(),
          ((NormalizedComparator<Experiment>) comparator).getConverter().apply(experiment));
    }
    verify(view.experiments).addColumn(valueProviderCaptor.capture(), eq(PROTOCOL));
    valueProvider = valueProviderCaptor.getValue();
    for (Experiment experiment : experiments) {
      assertEquals(experiment.getProtocol().getName(), valueProvider.apply(experiment));
    }
    verify(view.protocol).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Experiment experiment : experiments) {
      assertEquals(experiment.getProtocol().getName(),
          ((NormalizedComparator<Experiment>) comparator).getConverter().apply(experiment));
    }
    verify(view.experiments).addColumn(localDateTimeRendererCaptor.capture(), eq(DATE));
    LocalDateTimeRenderer<Experiment> localDateTimeRenderer =
        localDateTimeRendererCaptor.getValue();
    for (Experiment experiment : experiments) {
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(experiment.getDate()),
          getFormattedValue(localDateTimeRenderer, experiment));
    }
    verify(view.experiments).addColumn(valueProviderCaptor.capture(), eq(OWNER));
    valueProvider = valueProviderCaptor.getValue();
    for (Experiment experiment : experiments) {
      assertEquals(experiment.getOwner().getEmail(), valueProvider.apply(experiment));
    }
    verify(view.owner).setComparator(comparatorCaptor.capture());
    comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (Experiment experiment : experiments) {
      assertEquals(experiment.getOwner().getEmail(),
          ((NormalizedComparator<Experiment>) comparator).getConverter().apply(experiment));
    }
  }

  @Test
  public void view() {
    Experiment experiment = experiments.get(0);
    doubleClickItem(view.experiments, experiment);

    verify(presenter).view(experiment);
  }

  @Test
  public void viewProtocol() {
    Experiment experiment = experiments.get(0);
    doubleClickItem(view.experiments, experiment, view.protocol);

    verify(presenter).view(experiment.getProtocol());
  }

  @Test
  public void filterName() {
    view.nameFilter.setValue("test");

    verify(presenter).filterName("test");
  }

  @Test
  public void filterProject() {
    view.projectFilter.setValue("test");

    verify(presenter).filterProject("test");
  }

  @Test
  public void filterProtocol() {
    view.protocolFilter.setValue("test");

    verify(presenter).filterProtocol("test");
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
  public void permissions() {
    clickButton(view.permissions);
    verify(presenter).permissions();
  }
}
