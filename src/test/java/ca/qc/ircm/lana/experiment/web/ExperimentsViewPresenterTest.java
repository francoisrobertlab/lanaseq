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

import static ca.qc.ircm.lana.experiment.web.ExperimentsView.EXPERIMENTS_REQUIRED;
import static ca.qc.ircm.lana.experiment.web.ExperimentsView.PERMISSIONS_DENIED;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentRepository;
import ca.qc.ircm.lana.experiment.ExperimentService;
import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.user.UserRole;
import ca.qc.ircm.lana.web.SavedEvent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.DataProvider;
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
public class ExperimentsViewPresenterTest extends AbstractViewTestCase {
  private ExperimentsViewPresenter presenter;
  @Mock
  private ExperimentsView view;
  @Mock
  private ExperimentService experimentService;
  @Mock
  private AuthorizationService authorizationService;
  @Mock
  private DataProvider<Experiment, ?> dataProvider;
  @Captor
  private ArgumentCaptor<Experiment> experimentCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<ExperimentDialog>>> savedListenerCaptor;
  @Inject
  private ExperimentRepository experimentRepository;
  @Inject
  private UserRepository userRepository;
  private Locale locale = Locale.ENGLISH;
  private MessageResource resources = new MessageResource(ExperimentsView.class, locale);
  private List<Experiment> experiments;
  private User currentUser;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    presenter = new ExperimentsViewPresenter(experimentService, authorizationService);
    view.header = new H2();
    view.experiments = new Grid<>();
    view.experiments.setSelectionMode(SelectionMode.MULTI);
    view.nameFilter = new TextField();
    view.ownerFilter = new TextField();
    view.error = new Div();
    view.add = new Button();
    view.permissions = new Button();
    view.experimentDialog = mock(ExperimentDialog.class);
    view.experimentPermissionsDialog = mock(ExperimentPermissionsDialog.class);
    experiments = experimentRepository.findAll();
    when(experimentService.all()).thenReturn(experiments);
    currentUser = userRepository.findById(3L).orElse(null);
    when(authorizationService.currentUser()).thenReturn(currentUser);
  }

  @Test
  public void experiments() {
    presenter.init(view);
    presenter.localeChange(locale);
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
  public void ownerFilter_User() {
    presenter.init(view);
    presenter.localeChange(locale);

    assertEquals(currentUser.getEmail(), view.ownerFilter.getValue());
    verify(authorizationService).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void ownerFilter_ManagerOrAdmin() {
    when(authorizationService.hasAnyRole(any())).thenReturn(true);

    presenter.init(view);
    presenter.localeChange(locale);

    assertEquals("", view.ownerFilter.getValue());
    verify(authorizationService).hasAnyRole(UserRole.ADMIN, UserRole.MANAGER);
  }

  @Test
  public void filterName() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.experiments.setDataProvider(dataProvider);

    presenter.filterName("test");

    assertEquals("test", presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterName_Empty() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.experiments.setDataProvider(dataProvider);

    presenter.filterName("");

    assertEquals(null, presenter.filter().nameContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.experiments.setDataProvider(dataProvider);

    presenter.filterOwner("test");

    assertEquals("test", presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void filterOwner_Empty() {
    presenter.init(view);
    presenter.localeChange(locale);
    view.experiments.setDataProvider(dataProvider);

    presenter.filterOwner("");

    assertEquals(null, presenter.filter().ownerContains);
    verify(dataProvider).refreshAll();
  }

  @Test
  public void error() {
    presenter.init(view);
    presenter.localeChange(locale);
    assertFalse(view.error.isVisible());
  }

  @Test
  public void view() {
    presenter.init(view);
    presenter.localeChange(locale);
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
    presenter.localeChange(locale);
    presenter.add();
    verify(view.experimentDialog).setExperiment(experimentCaptor.capture());
    Experiment experiment = experimentCaptor.getValue();
    assertNull(experiment.getId());
    assertNull(experiment.getName());
    verify(view.experimentDialog).open();
  }

  @Test
  public void permissions() {
    final Experiment experiment = experiments.get(2);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(view);
    presenter.localeChange(locale);
    view.experiments.select(experiment);
    presenter.permissions();
    assertFalse(view.error.isVisible());
    verify(view.experimentPermissionsDialog).setExperiment(experiment);
    verify(view.experimentPermissionsDialog).open();
  }

  @Test
  public void permissions_NoExperiment() {
    presenter.init(view);
    presenter.localeChange(locale);
    presenter.permissions();
    assertEquals(resources.message(EXPERIMENTS_REQUIRED), view.error.getText());
    assertTrue(view.error.isVisible());
    verify(view.experimentPermissionsDialog, never()).setExperiment(any());
    verify(view.experimentPermissionsDialog, never()).open();
  }

  @Test
  public void permissions_Denied() {
    Experiment experiment = experiments.get(2);
    presenter.init(view);
    presenter.localeChange(locale);
    view.experiments.select(experiment);
    presenter.permissions();
    assertEquals(resources.message(PERMISSIONS_DENIED), view.error.getText());
    assertTrue(view.error.isVisible());
    verify(view.experimentPermissionsDialog, never()).setExperiment(any());
    verify(view.experimentPermissionsDialog, never()).open();
  }

  @Test
  public void permissions_ErrorThenView() {
    presenter.init(view);
    presenter.localeChange(locale);
    presenter.permissions();
    presenter.view(experiments.get(1));
    assertFalse(view.error.isVisible());
  }

  @Test
  public void permissions_ErrorThenAdd() {
    presenter.init(view);
    presenter.localeChange(locale);
    presenter.permissions();
    presenter.add();
    assertFalse(view.error.isVisible());
  }

  @Test
  public void permissions_ErrorThenFilterName() {
    presenter.init(view);
    presenter.localeChange(locale);
    presenter.permissions();
    presenter.filterName("");
    assertFalse(view.error.isVisible());
  }

  @Test
  public void permissions_ErrorThenFilterOwner() {
    presenter.init(view);
    presenter.localeChange(locale);
    presenter.permissions();
    presenter.filterOwner("");
    assertFalse(view.error.isVisible());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void refreshExperimentsOnSaved() {
    presenter.init(view);
    presenter.localeChange(locale);
    verify(view.experimentDialog).addSavedListener(savedListenerCaptor.capture());
    ComponentEventListener<SavedEvent<ExperimentDialog>> savedListener =
        savedListenerCaptor.getValue();
    savedListener.onComponentEvent(mock(SavedEvent.class));
    verify(experimentService, times(2)).all();
  }
}
