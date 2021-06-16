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

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.MERGED;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link DatasetsView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetsViewItTest extends AbstractTestBenchTestCase {
  @Autowired
  private DatasetRepository repository;

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  @WithAnonymousUser
  public void security_Anonymous() throws Throwable {
    open();
    Locale locale = currentLocale();
    assertEquals(
        new AppResources(SigninView.class, locale).message(TITLE,
            new AppResources(Constants.class, locale).message(APPLICATION_NAME)),
        getDriver().getTitle());
  }

  @Test
  public void title() throws Throwable {
    open();
    assertEquals(resources(DatasetsView.class).message(TITLE,
        resources(Constants.class).message(APPLICATION_NAME)), getDriver().getTitle());
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(ID);
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.datasets()).isPresent());
    assertTrue(optional(() -> view.add()).isPresent());
    assertTrue(optional(() -> view.merge()).isPresent());
    assertTrue(optional(() -> view.files()).isPresent());
  }

  @Test
  public void view() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(ID);
    view.datasets().doubleClick(0);
    assertTrue(view.dialog().isOpen());
  }

  @Test
  public void view_Protocol() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(ID);
    view.datasets().doubleClickProtocol(0);
    assertTrue(view.protocolDialog().isOpen());
  }

  @Test
  public void view_Files() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(ID);
    view.datasets().controlClick(0);
    assertTrue(view.filesDialog().isOpen());
  }

  @Test
  public void edit() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(ID);
    view.datasets().edit(0).click();
    assertTrue(view.dialog().isOpen());
  }

  @Test
  public void add() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(ID);
    view.add().click();
    assertTrue(view.dialog().isOpen());
  }

  @Test
  public void merge() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(ID);
    view.datasets().select(0);
    view.datasets().select(3);

    view.merge().click();

    String name = "ChIPseq_Spt16_yFR101_G24D_JS1-JS2-JS3_20181022";
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(DatasetsView.class);
    assertEquals(resources.message(MERGED, name), notification.getText());
    List<Dataset> datasets = repository.findByOwner(new User(3L));
    Dataset dataset =
        datasets.stream().filter(ex -> name.equals(ex.getName())).findFirst().orElse(null);
    assertNotNull(dataset);
    assertNotNull(dataset.getId());
    assertEquals(name, dataset.getName());
    assertEquals(4, dataset.getTags().size());
    assertTrue(dataset.getTags().contains("chipseq"));
    assertTrue(dataset.getTags().contains("ip"));
    assertTrue(dataset.getTags().contains("G24D"));
    assertTrue(dataset.getTags().contains("Spt16"));
    assertEquals(LocalDate.of(2018, 10, 22), dataset.getDate());
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(dataset.getCreationDate()));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(dataset.getCreationDate()));
    assertTrue(dataset.isEditable());
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(3, dataset.getSamples().size());
    assertTrue(find(dataset.getSamples(), 4L).isPresent());
    assertTrue(find(dataset.getSamples(), 5L).isPresent());
    assertTrue(find(dataset.getSamples(), 11L).isPresent());
  }

  @Test
  public void files() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(ID);
    view.datasets().select(0);
    view.files().click();
    assertTrue(view.filesDialog().isOpen());
  }
}
