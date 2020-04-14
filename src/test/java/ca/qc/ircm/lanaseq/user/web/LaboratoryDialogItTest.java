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

package ca.qc.ircm.lanaseq.user.web;

import static ca.qc.ircm.lanaseq.user.web.LaboratoryDialog.SAVED;
import static ca.qc.ircm.lanaseq.user.web.UsersView.ID;
import static ca.qc.ircm.lanaseq.user.web.UsersView.VIEW_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.LaboratoryRepository;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TestTransaction;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
@WithUserDetails("lana@ircm.qc.ca")
public class LaboratoryDialogItTest extends AbstractTestBenchTestCase {
  @Autowired
  private LaboratoryRepository repository;
  private String name = "test_name";

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    UsersViewElement view = $(UsersViewElement.class).id(ID);

    view.doubleClickLaboratory(1);

    assertTrue(optional(() -> $(LaboratoryDialogElement.class).first()).isPresent());
    LaboratoryDialogElement dialog = $(LaboratoryDialogElement.class).first();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.name()).isPresent());
    assertTrue(optional(() -> dialog.save()).isPresent());
    assertTrue(optional(() -> dialog.cancel()).isPresent());
  }

  @Test
  public void save() throws Throwable {
    open();
    UsersViewElement view = $(UsersViewElement.class).id(ID);
    view.doubleClickLaboratory(1);
    assertTrue(optional(() -> $(LaboratoryDialogElement.class).first()).isPresent());
    LaboratoryDialogElement dialog = $(LaboratoryDialogElement.class).first();
    dialog.name().setValue(name);

    TestTransaction.flagForCommit();
    dialog.save().click();
    TestTransaction.end();

    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(LaboratoryDialog.class);
    assertEquals(resources.message(SAVED, name), notification.getText());
    Laboratory laboratory = repository.findById(2L).get();
    assertEquals(name, laboratory.getName());
  }

  @Test
  public void cancel() throws Throwable {
    open();
    UsersViewElement view = $(UsersViewElement.class).id(ID);
    view.doubleClickLaboratory(1);
    assertTrue(optional(() -> $(LaboratoryDialogElement.class).first()).isPresent());
    LaboratoryDialogElement dialog = $(LaboratoryDialogElement.class).first();
    dialog.name().setValue(name);

    TestTransaction.flagForCommit();
    dialog.cancel().click();
    TestTransaction.end();

    assertFalse(optional(() -> $(NotificationElement.class).first()).isPresent());
    Laboratory laboratory = repository.findById(2L).get();
    assertEquals("Chromatin and Genomic Expression", laboratory.getName());
  }
}
