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

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.SpringConfiguration.messagePrefix;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.MERGED;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.VIEW_NAME;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.web.SigninViewElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SamplesView}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SamplesViewItTest extends AbstractTestBenchTestCase {
  private static final String MESSAGE_PREFIX = messagePrefix(SamplesView.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private MessageSource messageSource;

  private void open() {
    openView(VIEW_NAME);
  }

  @Test
  @WithAnonymousUser
  public void security_Anonymous() throws Throwable {
    open();
    $(SigninViewElement.class).waitForFirst();
  }

  @Test
  public void title() throws Throwable {
    open();
    String applicationName =
        messageSource.getMessage(CONSTANTS_PREFIX + APPLICATION_NAME, null, currentLocale());
    assertEquals(messageSource.getMessage(MESSAGE_PREFIX + TITLE, new Object[] { applicationName },
        currentLocale()), getDriver().getTitle());
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.samples()).isPresent());
    assertTrue(optional(() -> view.add()).isPresent());
    assertTrue(optional(() -> view.merge()).isPresent());
    assertTrue(optional(() -> view.files()).isPresent());
    assertTrue(optional(() -> view.analyze()).isPresent());
  }

  @Test
  public void viewFiles() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().controlClick(0);
    assertTrue(view.filesDialog().isOpen());
  }

  @Test
  public void edit() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().edit(0).click();
    assertTrue(view.dialog().isOpen());
  }

  @Test
  public void add() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.add().click();
    assertTrue(view.dialog().isOpen());
  }

  @Test
  public void merge() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(1);
    view.samples().select(view.samples().name(2).startsWith("JS2") ? 2 : 3);

    view.merge().click();

    String name = "ChIPseq_Spt16_yFR101_G24D_JS2-JS1_20181022";
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    assertEquals(
        messageSource.getMessage(MESSAGE_PREFIX + MERGED, new Object[] { name }, currentLocale()),
        notification.getText());
    List<Dataset> datasets = datasetRepository.findByOwner(new User(3L));
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
    assertEquals(2, dataset.getSamples().size());
    assertTrue(find(dataset.getSamples(), 5L).isPresent());
    assertTrue(find(dataset.getSamples(), 10L).isPresent());
  }

  @Test
  public void files() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(0);
    view.files().click();
    assertTrue(view.filesDialog().isOpen());
  }

  @Test
  public void analyze() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(0);
    view.analyze().click();
    assertTrue(view.analyzeDialog().isOpen());
  }
}
