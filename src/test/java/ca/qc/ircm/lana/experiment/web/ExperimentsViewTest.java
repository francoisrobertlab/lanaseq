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
import static ca.qc.ircm.lana.experiment.web.ExperimentsView.ADD;
import static ca.qc.ircm.lana.experiment.web.ExperimentsView.EXPERIMENTS;
import static ca.qc.ircm.lana.experiment.web.ExperimentsView.HEADER;
import static ca.qc.ircm.lana.experiment.web.ExperimentsView.VIEW_NAME;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lana.web.WebConstants.APPLICATION_NAME;
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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.dom.Element;
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
  @Captor
  private ArgumentCaptor<ComponentRenderer<Button, Experiment>> buttonRendererCaptor;
  @Captor
  private ArgumentCaptor<LocalDateTimeRenderer<Experiment>> localDateTimeRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<Experiment>> comparatorCaptor;
  @Inject
  private ExperimentRepository experimentRepository;
  private Locale locale = Locale.ENGLISH;
  private MessageResource resources = new MessageResource(ExperimentsView.class, locale);
  private MessageResource experimentResources = new MessageResource(Experiment.class, locale);
  private MessageResource generalResources = new MessageResource(WebConstants.class, locale);
  private List<Experiment> experiments;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    view = new ExperimentsView(presenter);
    view.init();
    experiments = experimentRepository.findAll();
  }

  @SuppressWarnings("unchecked")
  private void mockColumns() {
    Element experimentsElement = view.experiments.getElement();
    view.experiments = mock(Grid.class);
    when(view.experiments.getElement()).thenReturn(experimentsElement);
    view.name = mock(Column.class);
    when(view.experiments.addColumn(any(ComponentRenderer.class), eq(NAME))).thenReturn(view.name);
    when(view.name.setKey(any())).thenReturn(view.name);
    when(view.name.setComparator(any(Comparator.class))).thenReturn(view.name);
    when(view.name.setHeader(any(String.class))).thenReturn(view.name);
    view.date = mock(Column.class);
    when(view.experiments.addColumn(any(LocalDateTimeRenderer.class), eq(DATE)))
        .thenReturn(view.date);
    when(view.date.setKey(any())).thenReturn(view.date);
    when(view.date.setHeader(any(String.class))).thenReturn(view.date);
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
    assertTrue(view.add.getClassNames().contains(ADD));
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
    assertEquals(resources.message(ADD), view.add.getText());
    validateIcon(VaadinIcon.PLUS.create(), view.add);
  }

  @Test
  public void localeChange() {
    view = new ExperimentsView(presenter);
    mockColumns();
    view.init();
    view.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final MessageResource resources = new MessageResource(ExperimentsView.class, locale);
    final MessageResource experimentResources = new MessageResource(Experiment.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    view.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), view.header.getText());
    verify(view.name, atLeastOnce()).setHeader(experimentResources.message(NAME));
    verify(view.name, atLeastOnce()).setFooter(experimentResources.message(NAME));
    verify(view.date, atLeastOnce()).setHeader(experimentResources.message(DATE));
    verify(view.date, atLeastOnce()).setFooter(experimentResources.message(DATE));
  }

  @Test
  public void getPageTitle() {
    assertEquals(resources.message(TITLE, generalResources.message(APPLICATION_NAME)),
        view.getPageTitle());
  }

  @Test
  public void experiments_Columns() {
    assertEquals(2, view.experiments.getColumns().size());
    assertNotNull(view.experiments.getColumnByKey(NAME));
    assertNotNull(view.experiments.getColumnByKey(DATE));
  }

  @Test
  public void experiments_ColumnsValueProvider() {
    view = new ExperimentsView(presenter);
    mockColumns();
    view.init();
    verify(view.experiments).addColumn(buttonRendererCaptor.capture(), eq(NAME));
    ComponentRenderer<Button, Experiment> buttonRenderer = buttonRendererCaptor.getValue();
    for (Experiment experiment : experiments) {
      Button button = buttonRenderer.createComponent(experiment);
      assertTrue(button.getClassNames().contains(NAME));
      assertEquals(experiment.getName(), button.getText());
      clickButton(button);
      verify(presenter).view(experiment);
    }
    verify(view.name).setComparator(comparatorCaptor.capture());
    Comparator<Experiment> comparator = comparatorCaptor.getValue();
    assertTrue(comparator.compare(name("abc"), name("test")) < 0);
    assertTrue(comparator.compare(name("Abc"), name("test")) < 0);
    assertTrue(comparator.compare(name("test"), name("test")) == 0);
    assertTrue(comparator.compare(name("Test"), name("test")) == 0);
    assertTrue(comparator.compare(name("Expérienceà"), name("experiencea")) == 0);
    assertTrue(comparator.compare(name("test"), name("abc")) > 0);
    assertTrue(comparator.compare(name("Test"), name("abc")) > 0);
    verify(view.experiments).addColumn(localDateTimeRendererCaptor.capture(), eq(DATE));
    LocalDateTimeRenderer<Experiment> localDateTimeRenderer =
        localDateTimeRendererCaptor.getValue();
    for (Experiment experiment : experiments) {
      Component component = localDateTimeRenderer.createComponent(experiment);
      assertEquals(DateTimeFormatter.ISO_LOCAL_DATE.format(experiment.getDate()),
          component.getElement().getText());
    }
  }

  private Experiment name(String name) {
    Experiment experiment = new Experiment();
    experiment.setName(name);
    return experiment;
  }
}
