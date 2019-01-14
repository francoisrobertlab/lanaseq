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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentRepository;
import ca.qc.ircm.lana.experiment.ExperimentService;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.user.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.List;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ExperimentsPermissionsDialogPresenterTest extends AbstractViewTestCase {
  private ExperimentsPermissionsDialogPresenter presenter;
  @Mock
  private ExperimentsPermissionsDialog dialog;
  @Mock
  private ExperimentService experimentService;
  @Mock
  private UserService userService;
  @Mock
  private DataProvider<User, ?> managersDataProvider;
  @Inject
  private ExperimentRepository experimentRepository;
  @Inject
  private UserRepository userRepository;
  private List<Experiment> experiments;
  private List<User> managers;
  private User secondManager = new User(800L, "second.manager@ircm.qc.ca");

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    presenter = new ExperimentsPermissionsDialogPresenter(experimentService, userService);
    dialog.header = new H2();
    dialog.experiments = new Grid<>();
    dialog.managers = new Grid<>();
    dialog.save = new Button();
    dialog.cancel = new Button();
    experiments = experimentRepository.findAll();
    managers = userRepository.findByManagerTrue();
    User manager = userRepository.findById(2L).orElse(null);
    secondManager.setLaboratory(manager.getLaboratory());
    when(userService.managers()).thenReturn(managers);
  }

  @Test
  public void init() {
    managers.add(secondManager);

    presenter.init(dialog);

    List<User> users = items(dialog.managers);
    assertEquals(managers.size() - 1, users.size());
    for (User user : managers) {
      if (user != secondManager) {
        assertTrue(users.contains(user));
      }
    }
  }

  @Test
  public void filterLaboratory() {
    presenter.init(dialog);
    dialog.managers.setDataProvider(managersDataProvider);

    presenter.filterLaboratory("test");

    assertEquals("test", presenter.userFilter().laboratoryNameContains);
    verify(managersDataProvider).refreshAll();
  }

  @Test
  public void filterLaboratory_Empty() {
    presenter.init(dialog);
    dialog.managers.setDataProvider(managersDataProvider);

    presenter.filterLaboratory("");

    assertEquals(null, presenter.userFilter().laboratoryNameContains);
    verify(managersDataProvider).refreshAll();
  }

  @Test
  public void filterEmail() {
    presenter.init(dialog);
    dialog.managers.setDataProvider(managersDataProvider);

    presenter.filterEmail("test");

    assertEquals("test", presenter.userFilter().emailContains);
    verify(managersDataProvider).refreshAll();
  }

  @Test
  public void filterEmail_Empty() {
    presenter.init(dialog);
    dialog.managers.setDataProvider(managersDataProvider);

    presenter.filterEmail("");

    assertEquals(null, presenter.userFilter().emailContains);
    verify(managersDataProvider).refreshAll();
  }

  @Test
  @Ignore("program test")
  public void save() {
  }

  @Test
  public void cancel() {
    presenter.init(dialog);

    presenter.cancel();

    verify(dialog).close();
  }

  @Test
  public void getExperiments() {
    presenter.init(dialog);
    assertTrue(presenter.getExperiments() == null || presenter.getExperiments().isEmpty());
    presenter.setExperiments(experiments);
    assertEquals(experiments, presenter.getExperiments());
  }

  @Test
  public void setExperiments() {
    presenter.init(dialog);
    presenter.setExperiments(experiments);
    assertEquals(experiments, items(dialog.experiments));
  }
}
