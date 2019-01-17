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

import static ca.qc.ircm.lana.test.utils.SearchUtils.find;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentRepository;
import ca.qc.ircm.lana.experiment.ExperimentService;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.LaboratoryRepository;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.user.UserService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.data.provider.DataProvider;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ExperimentPermissionsDialogPresenterTest extends AbstractViewTestCase {
  @SuppressWarnings("unused")
  private static final Logger logger =
      LoggerFactory.getLogger(ExperimentPermissionsDialogPresenterTest.class);
  private ExperimentPermissionsDialogPresenter presenter;
  @Mock
  private ExperimentPermissionsDialog dialog;
  @Mock
  private ExperimentService experimentService;
  @Mock
  private UserService userService;
  @Mock
  private DataProvider<User, ?> managersDataProvider;
  @Captor
  private ArgumentCaptor<Collection<Laboratory>> laboratoriesCaptor;
  @Inject
  private ExperimentRepository experimentRepository;
  @Inject
  private UserRepository userRepository;
  @Inject
  private LaboratoryRepository laboratoryRepository;
  private Experiment experiment;
  private List<User> managers;
  private User secondManager = new User(800L, "second.manager@ircm.qc.ca");

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    presenter = new ExperimentPermissionsDialogPresenter(experimentService, userService);
    dialog.header = new H2();
    dialog.managers = new Grid<>();
    dialog.save = new Button();
    dialog.cancel = new Button();
    experiment = experimentRepository.findById(2L).orElse(null);
    managers = userRepository.findByManagerTrueAndActiveTrue();
    dialog.reads = new HashMap<>();
    when(dialog.read(any())).thenAnswer(i -> {
      User user = (User) i.getArgument(0);
      if (dialog.reads.containsKey(user)) {
        return dialog.reads.get(user);
      }
      Checkbox checkbox = new Checkbox();
      dialog.reads.put(user, checkbox);
      return checkbox;
    });
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
  public void init_Reads() {
    Set<Laboratory> permissions = new HashSet<>();
    permissions.add(laboratoryRepository.findById(3L).orElse(null));
    when(experimentService.permissions(any())).thenReturn(permissions);

    presenter.init(dialog);
    presenter.setExperiment(experiment);

    for (User manager : managers) {
      verify(dialog, atLeastOnce()).read(manager);
      if (manager.getLaboratory().getId().equals(experiment.getOwner().getLaboratory().getId())) {
        assertTrue(dialog.reads.get(manager).getValue());
        assertTrue(dialog.reads.get(manager).isReadOnly());
      } else if (manager.getLaboratory().getId().equals(3L)) {
        assertTrue(dialog.reads.get(manager).getValue());
        assertFalse(dialog.reads.get(manager).isReadOnly());
      } else {
        assertFalse(dialog.reads.get(manager).getValue());
        assertFalse(dialog.reads.get(manager).isReadOnly());
      }
    }
  }

  @Test
  public void init_ReadsSetExperimentBeforeInit() {
    Set<Laboratory> permissions = new HashSet<>();
    permissions.add(laboratoryRepository.findById(3L).orElse(null));
    when(experimentService.permissions(any())).thenReturn(permissions);

    presenter.setExperiment(experiment);
    presenter.init(dialog);

    for (User manager : managers) {
      if (manager.getLaboratory().getId().equals(experiment.getOwner().getLaboratory().getId())) {
        assertTrue(dialog.reads.get(manager).getValue());
        assertTrue(dialog.reads.get(manager).isReadOnly());
      } else if (manager.getLaboratory().getId().equals(3L)) {
        assertTrue(dialog.reads.get(manager).getValue());
        assertFalse(dialog.reads.get(manager).isReadOnly());
      } else {
        assertFalse(dialog.reads.get(manager).getValue());
        assertFalse(dialog.reads.get(manager).isReadOnly());
      }
    }
  }

  @Test
  public void init_ReadsNoAclPermissions() {
    when(experimentService.permissions(any())).thenReturn(new HashSet<>());

    presenter.init(dialog);
    presenter.setExperiment(experiment);

    for (User manager : managers) {
      if (manager.getLaboratory().getId().equals(experiment.getOwner().getLaboratory().getId())) {
        assertTrue(dialog.reads.get(manager).getValue());
        assertTrue(dialog.reads.get(manager).isReadOnly());
      } else {
        assertFalse(dialog.reads.get(manager).getValue());
        assertFalse(dialog.reads.get(manager).isReadOnly());
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
  public void save_NullExperiment() {
    presenter.init(dialog);
    dialog.reads.get(managers.get(2)).setValue(true);

    try {
      presenter.save();
      fail("Expected an exception");
    } catch (Exception e) {
      // Success.
    }

    verify(experimentService, never()).savePermissions(any(), any());
  }

  @Test
  public void save() {
    presenter.init(dialog);
    presenter.setExperiment(experiment);
    dialog.reads.get(managers.get(2)).setValue(true);

    presenter.save();

    verify(experimentService).savePermissions(eq(experiment), laboratoriesCaptor.capture());
    Collection<Laboratory> laboratories = laboratoriesCaptor.getValue();
    assertEquals(1, laboratories.size());
    assertTrue(find(laboratories, 3L).isPresent());
    verify(dialog).close();
  }

  @Test
  public void save_Many() {
    presenter.init(dialog);
    presenter.setExperiment(experiment);
    dialog.reads.get(managers.get(0)).setValue(true);
    dialog.reads.get(managers.get(2)).setValue(true);

    presenter.save();

    verify(experimentService).savePermissions(eq(experiment), laboratoriesCaptor.capture());
    Collection<Laboratory> laboratories = laboratoriesCaptor.getValue();
    assertEquals(2, laboratories.size());
    assertTrue(find(laboratories, 1L).isPresent());
    assertTrue(find(laboratories, 3L).isPresent());
    verify(dialog).close();
  }

  @Test
  public void cancel() {
    presenter.init(dialog);

    presenter.cancel();

    verify(dialog).close();
  }

  @Test
  public void getExperiment() {
    presenter.init(dialog);
    assertNull(presenter.getExperiment());
    presenter.setExperiment(experiment);
    assertEquals(experiment, presenter.getExperiment());
  }

  @Test
  public void setExperiment() {
    presenter.init(dialog);
    presenter.setExperiment(experiment);
    assertEquals(experiment, presenter.getExperiment());
  }
}
