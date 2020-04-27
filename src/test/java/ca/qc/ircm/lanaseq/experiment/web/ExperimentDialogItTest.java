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

package ca.qc.ircm.lanaseq.experiment.web;

import static ca.qc.ircm.lanaseq.experiment.web.ExperimentDialog.ID;
import static ca.qc.ircm.lanaseq.experiment.web.ExperimentDialog.SAVED;
import static ca.qc.ircm.lanaseq.experiment.web.ExperimentsView.VIEW_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.experiment.Experiment;
import ca.qc.ircm.lanaseq.experiment.ExperimentRepository;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TestTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class ExperimentDialogItTest extends AbstractTestBenchTestCase {
  @Autowired
  private ExperimentRepository repository;
  @Autowired
  private ProtocolRepository protocolRepository;
  private String name = "test experiment";
  private Protocol protocol;

  @Before
  public void beforeTest() {
    protocol = protocolRepository.findById(1L).get();
  }

  private void open() {
    openView(VIEW_NAME);
  }

  private void fill(ExperimentDialogElement dialog) {
    dialog.name().setValue(name);
    dialog.protocol().selectByText(protocol.getName());
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    ExperimentsViewElement view = $(ExperimentsViewElement.class).id(ExperimentsView.ID);
    view.doubleClickExperiment(0);
    ExperimentDialogElement dialog = $(ExperimentDialogElement.class).id(ID);
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.name()).isPresent());
    assertTrue(optional(() -> dialog.protocol()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
    assertTrue(optional(() -> dialog.cancel()).isPresent());
  }

  @Test
  public void save_New() throws Throwable {
    open();
    ExperimentsViewElement view = $(ExperimentsViewElement.class).id(ExperimentsView.ID);
    view.add().click();
    ExperimentDialogElement dialog = $(ExperimentDialogElement.class).id(ID);
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(ExperimentDialog.class);
    assertEquals(resources.message(SAVED, name), notification.getText());
    List<Experiment> experiments = repository.findByOwner(new User(3L));
    Experiment experiment =
        experiments.stream().filter(ex -> name.equals(ex.getName())).findFirst().orElse(null);
    assertNotNull(experiment);
    assertNotNull(experiment.getId());
    assertEquals(name, experiment.getName());
    assertEquals(protocol.getId(), experiment.getProtocol().getId());
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(experiment.getDate()));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(experiment.getDate()));
    assertEquals((Long) 3L, experiment.getOwner().getId());
  }

  @Test
  public void save_Update() throws Throwable {
    open();
    ExperimentsViewElement view = $(ExperimentsViewElement.class).id(ExperimentsView.ID);
    view.doubleClickExperiment(0);
    ExperimentDialogElement dialog = $(ExperimentDialogElement.class).id(ID);
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(ExperimentDialog.class);
    assertEquals(resources.message(SAVED, name), notification.getText());
    Experiment experiment = repository.findById(2L).get();
    assertEquals(name, experiment.getName());
    assertEquals(protocol.getId(), experiment.getProtocol().getId());
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), experiment.getDate());
    assertEquals((Long) 3L, experiment.getOwner().getId());
  }

  @Test
  public void cancel() throws Throwable {
    open();
    ExperimentsViewElement view = $(ExperimentsViewElement.class).id(ExperimentsView.ID);
    view.doubleClickExperiment(1);
    ExperimentDialogElement dialog = $(ExperimentDialogElement.class).id(ID);
    fill(dialog);

    TestTransaction.flagForCommit();
    dialog.cancel().click();
    TestTransaction.end();

    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
    Experiment experiment = repository.findById(2L).get();
    assertEquals("Histone location", experiment.getName());
    assertEquals((Long) 3L, experiment.getProtocol().getId());
    assertEquals(LocalDateTime.of(2018, 10, 22, 9, 48, 20), experiment.getDate());
    assertEquals((Long) 3L, experiment.getOwner().getId());
  }
}
