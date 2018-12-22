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

import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentRepository;
import ca.qc.ircm.lana.experiment.ExperimentService;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lana.web.SaveEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.H2;
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
@NonTransactionalTestAnnotations
public class ExperimentsViewPresenterTest extends AbstractViewTestCase {
  private ExperimentsViewPresenter presenter;
  @Mock
  private ExperimentsView view;
  @Mock
  private ExperimentService experimentService;
  @Captor
  private ArgumentCaptor<Experiment> experimentCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SaveEvent<Experiment>>> saveListenerCaptor;
  @Inject
  private ExperimentRepository experimentRepository;
  private Locale locale = Locale.ENGLISH;
  private List<Experiment> experiments;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    presenter = new ExperimentsViewPresenter(experimentService);
    view.header = new H2();
    view.experiments = new Grid<>();
    view.experiments.setSelectionMode(SelectionMode.MULTI);
    view.experimentDialog = mock(ExperimentDialog.class);
    experiments = experimentRepository.findAll();
    when(view.getLocale()).thenReturn(locale);
    when(experimentService.all()).thenReturn(experiments);
  }

  @Test
  public void experiments() {
    presenter.init(view);
    List<Experiment> experiments = items(view.experiments);
    assertEquals(this.experiments.size(), experiments.size());
    for (Experiment experiment : this.experiments) {
      assertTrue(experiment.toString(), experiments.contains(experiment));
    }
    assertEquals(0, view.experiments.getSelectedItems().size());
    experiments.forEach(experiment -> view.experiments.select(experiment));
    assertEquals(experiments.size(), view.experiments.getSelectedItems().size());
  }

  @Test
  public void view() {
    presenter.init(view);
    Experiment experiment = new Experiment();
    experiment.setId(2L);
    Experiment databaseExperiment = new Experiment();
    when(experimentService.get(any())).thenReturn(databaseExperiment);
    presenter.view(experiment);
    verify(experimentService).get(2L);
    verify(view.experimentDialog).setExperiment(databaseExperiment);
    verify(view.experimentDialog).open();
  }

  @Test
  public void add() {
    presenter.init(view);
    presenter.add();
    verify(view.experimentDialog).setExperiment(experimentCaptor.capture());
    Experiment experiment = experimentCaptor.getValue();
    assertNull(experiment.getId());
    assertNull(experiment.getName());
    verify(view.experimentDialog).open();
  }

  @Test
  public void save() {
    Experiment experiment = new Experiment();
    presenter.init(view);
    verify(view.experimentDialog).addSaveListener(saveListenerCaptor.capture());
    ComponentEventListener<SaveEvent<Experiment>> listener = saveListenerCaptor.getValue();
    SaveEvent<Experiment> saveEvent = new SaveEvent<>(view.experimentDialog, false, experiment);
    listener.onComponentEvent(saveEvent);
    verify(experimentService).save(experiment);
  }

  @Test
  public void save_NullPassword() {
    Experiment experiment = new Experiment();
    presenter.init(view);
    verify(view.experimentDialog).addSaveListener(saveListenerCaptor.capture());
    ComponentEventListener<SaveEvent<Experiment>> listener = saveListenerCaptor.getValue();
    SaveEvent<Experiment> saveEvent = new SaveEvent<>(view.experimentDialog, false, experiment);
    listener.onComponentEvent(saveEvent);
    verify(experimentService).save(experiment);
  }
}
