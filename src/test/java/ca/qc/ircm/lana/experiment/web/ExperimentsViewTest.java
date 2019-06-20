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

package ca.qc.ircm.lana.experiment.web;

import static ca.qc.ircm.lana.experiment.ExperimentProperties.DATE;
import static ca.qc.ircm.lana.experiment.ExperimentProperties.NAME;
import static ca.qc.ircm.lana.experiment.ExperimentProperties.OWNER;
import static ca.qc.ircm.lana.experiment.web.ExperimentsView.ADD;
import static ca.qc.ircm.lana.experiment.web.ExperimentsView.EXPERIMENTS;
import static ca.qc.ircm.lana.experiment.web.ExperimentsView.HEADER;
import static ca.qc.ircm.lana.experiment.web.ExperimentsView.PERMISSIONS;
import static ca.qc.ircm.lana.experiment.web.ExperimentsView.VIEW_NAME;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.doubleClickItem;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.getFormattedValue;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lana.web.WebConstants.ALL;
import static ca.qc.ircm.lana.web.WebConstants.APPLICATION_NAME;
import static ca.qc.ircm.lana.web.WebConstants.ERROR_TEXT;
import static ca.qc.ircm.lana.web.WebConstants.TITLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentRepository;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
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
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
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
  private ExperimentPermissionsDialog experimentPermissionsDialog;
  @Captor
  private ArgumentCaptor<ValueProvider<Experiment, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<LocalDateTimeRenderer<Experiment>> localDateTimeRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<Experiment>> comparatorCaptor;
  @Inject
  private ExperimentRepository experimentRepository;
  private Locale locale = Locale.ENGLISH;
  private MessageResource resources = new MessageResource(ExperimentsView.class, locale);
  private MessageResource experimentResources = new MessageResource(Experiment.class, locale);
  private MessageResource webResources = new MessageResource(WebConstants.class, locale);
  private List<Experiment> experiments;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    view = new ExperimentsView(presenter, experimentDialog, experimentPermissionsDialog);
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
    view.date = mock(Column.class);
    when(view.experiments.addColumn(any(LocalDateTimeRenderer.class), eq(DATE)))
        .thenReturn(view.date);
    when(view.date.setKey(any())).thenReturn(view.date);
    when(view.date.setHeader(any(String.class))).thenReturn(view.date);
    view.owner = mock(Column.class);
    when(view.experiments.addColumn(any(ValueProvider.class), eq(OWNER))).thenReturn(view.owner);
    when(view.owner.setKey(any())).thenReturn(view.owner);
    when(view.owner.setHeader(any(String.class))).thenReturn(view.owner);
    HeaderRow filtersRow = mock(HeaderRow.class);
    when(view.experiments.appendHeaderRow()).thenReturn(filtersRow);
    HeaderCell nameFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.name)).thenReturn(nameFilterCell);
    HeaderCell ownerFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(view.owner)).thenReturn(ownerFilterCell);
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(view);
  }

  @Test
  public void styles() {
    assertTrue(view.getContent().getId().orElse("").equals(VIEW_NAME));
    assertTrue(view.header.getClassNames().contains(HEADER));
    assertTrue(view.experiments.getClassNames().contains(EXPERIMENTS));
    assertTrue(view.error.getClassNames().contains(ERROR_TEXT));
    assertTrue(view.add.getClassNames().contains(ADD));
    assertTrue(view.permissions.getClassNames().contains(PERMISSIONS));
  }

  @Test
  public void labels() {
    mockColumns();
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.name).setHeader(experimentResources.message(NAME));
    verify(view.name).setFooter(experimentResources.message(NAME));
    verify(view.date).setHeader(experimentResources.message(DATE));
    verify(view.date).setFooter(experimentResources.message(DATE));
    verify(view.owner).setHeader(experimentResources.message(OWNER));
    verify(view.owner).setFooter(experimentResources.message(OWNER));
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.ownerFilter.getPlaceholder());
    assertEquals(resources.message(ADD), view.add.getText());
    validateIcon(VaadinIcon.PLUS.create(), view.add.getIcon());
    assertEquals(resources.message(PERMISSIONS), view.permissions.getText());
    validateIcon(VaadinIcon.LOCK.create(), view.permissions.getIcon());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    view = new ExperimentsView(presenter, experimentDialog, experimentPermissionsDialog);
    mockColumns();
    view.init();
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final MessageResource resources = new MessageResource(ExperimentsView.class, locale);
    final MessageResource experimentResources = new MessageResource(Experiment.class, locale);
    final MessageResource webResources = new MessageResource(WebConstants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.name, atLeastOnce()).setHeader(experimentResources.message(NAME));
    verify(view.name, atLeastOnce()).setFooter(experimentResources.message(NAME));
    verify(view.date, atLeastOnce()).setHeader(experimentResources.message(DATE));
    verify(view.date, atLeastOnce()).setFooter(experimentResources.message(DATE));
    verify(view.owner, atLeastOnce()).setHeader(experimentResources.message(OWNER));
    verify(view.owner, atLeastOnce()).setFooter(experimentResources.message(OWNER));
    assertEquals(webResources.message(ALL), view.nameFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), view.ownerFilter.getPlaceholder());
    assertEquals(resources.message(ADD), view.add.getText());
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
    assertEquals(3, view.experiments.getColumns().size());
    assertNotNull(view.experiments.getColumnByKey(NAME));
    assertNotNull(view.experiments.getColumnByKey(DATE));
    assertNotNull(view.experiments.getColumnByKey(OWNER));
    assertTrue(view.experiments.getSelectionModel() instanceof SelectionModel.Single);
  }

  @Test
  public void experiments_ColumnsValueProvider() {
    view = new ExperimentsView(presenter, experimentDialog, experimentPermissionsDialog);
    mockColumns();
    view.init();
    verify(view.experiments).addColumn(valueProviderCaptor.capture(), eq(NAME));
    ValueProvider<Experiment, String> valueProvider = valueProviderCaptor.getValue();
    for (Experiment experiment : experiments) {
      assertEquals(experiment.getName(), valueProvider.apply(experiment));
    }
    verify(view.name).setComparator(comparatorCaptor.capture());
    Comparator<Experiment> comparator = comparatorCaptor.getValue();
    assertTrue(comparator.compare(name("abc"), name("test")) < 0);
    assertTrue(comparator.compare(name("Abc"), name("test")) < 0);
    assertTrue(comparator.compare(name("élement"), name("facteur")) < 0);
    assertTrue(comparator.compare(name("test"), name("test")) == 0);
    assertTrue(comparator.compare(name("Test"), name("test")) == 0);
    assertTrue(comparator.compare(name("Expérienceà"), name("experiencea")) == 0);
    assertTrue(comparator.compare(name("experiencea"), name("Expérienceà")) == 0);
    assertTrue(comparator.compare(name("test"), name("abc")) > 0);
    assertTrue(comparator.compare(name("Test"), name("abc")) > 0);
    assertTrue(comparator.compare(name("facteur"), name("élement")) > 0);
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
  }

  @Test
  public void view() {
    Experiment experiment = experiments.get(0);
    doubleClickItem(view.experiments, experiment);

    verify(presenter).view(experiment);
  }

  private Experiment name(String name) {
    Experiment experiment = new Experiment();
    experiment.setName(name);
    return experiment;
  }

  @Test
  public void filterName() {
    view.nameFilter.setValue("test");

    verify(presenter).filterName("test");
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
