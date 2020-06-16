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
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.DELETED;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.ID;
import static ca.qc.ircm.lanaseq.sample.web.SamplesView.VIEW_NAME;
import static ca.qc.ircm.lanaseq.test.utils.SearchUtils.find;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialogElement;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SamplesViewItTest extends AbstractTestBenchTestCase {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private AppConfiguration configuration;
  private Path home;

  private Path getHome() throws NoSuchMethodException, SecurityException, IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
    Method getHome = AppConfiguration.class.getDeclaredMethod("getHome");
    getHome.setAccessible(true);
    return (Path) getHome.invoke(configuration);
  }

  private void setHome(Path path) throws NoSuchMethodException, SecurityException,
      IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Method setHome = AppConfiguration.class.getDeclaredMethod("setHome", Path.class);
    setHome.setAccessible(true);
    setHome.invoke(configuration, path);
  }

  @Before
  public void beforeTest() throws Throwable {
    home = getHome();
    setHome(temporaryFolder.newFolder("home").toPath());
  }

  @After
  public void afterTest() throws Throwable {
    setHome(home);
  }

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
    assertEquals(resources(SamplesView.class).message(TITLE,
        resources(Constants.class).message(APPLICATION_NAME)), getDriver().getTitle());
  }

  @Test
  public void fieldsExistence() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(ID);
    assertTrue(optional(() -> view.header()).isPresent());
    assertTrue(optional(() -> view.samples()).isPresent());
    assertTrue(optional(() -> view.add()).isPresent());
    assertTrue(optional(() -> view.merge()).isPresent());
    assertTrue(optional(() -> view.addFiles()).isPresent());
  }

  @Test
  public void view() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(ID);
    view.doubleClick(0);
    assertTrue(optional(() -> $(SampleDialogElement.class).id(SampleDialog.ID)).isPresent());
  }

  @Test
  public void viewFiles() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(ID);
    view.shiftClick(0);
    assertTrue(
        optional(() -> $(SampleFilesDialogElement.class).id(SampleFilesDialog.ID)).isPresent());
  }

  @Test
  public void addFiles_Grid() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(ID);
    view.controlClick(0);
    assertTrue(optional(() -> $(AddSampleFilesDialogElement.class).id(AddSampleFilesDialog.ID))
        .isPresent());
  }

  @Test
  public void view_Protocol() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(ID);
    view.doubleClickProtocol(0);
    assertTrue(optional(() -> $(ProtocolDialogElement.class).id(ProtocolDialog.ID)).isPresent());
  }

  @Test
  public void add() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(ID);
    view.add().click();
    assertTrue(optional(() -> $(SampleDialogElement.class).id(SampleDialog.ID)).isPresent());
  }

  @Test
  @Ignore("Cannot select samples")
  public void merge() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(ID);
    view.samples().select(0);
    view.samples().select(1);

    view.merge().click();

    String name = "ChIPSeq_Spt16_yFR101_G24D_JS1-JS2_"
        + DateTimeFormatter.BASIC_ISO_DATE.format(LocalDateTime.now());
    NotificationElement notification = $(NotificationElement.class).waitForFirst();
    AppResources resources = this.resources(SampleDialog.class);
    assertEquals(resources.message(DELETED, name), notification.getText());
    List<Dataset> datasets = datasetRepository.findByOwner(new User(3L));
    Dataset dataset =
        datasets.stream().filter(ex -> name.equals(ex.getName())).findFirst().orElse(null);
    assertNotNull(dataset);
    assertNotNull(dataset.getId());
    assertEquals(name, dataset.getName());
    assertTrue(dataset.getTags().isEmpty());
    assertTrue(LocalDateTime.now().minusMinutes(2).isBefore(dataset.getDate()));
    assertTrue(LocalDateTime.now().plusMinutes(2).isAfter(dataset.getDate()));
    assertTrue(dataset.getTags().isEmpty());
    assertEquals((Long) 3L, dataset.getOwner().getId());
    assertEquals(2, dataset.getSamples().size());
    assertTrue(find(dataset.getSamples(), 4L).isPresent());
    assertTrue(find(dataset.getSamples(), 5L).isPresent());
  }

  @Test
  @Ignore("Cannot select samples")
  public void addFiles() throws Throwable {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).id(ID);
    view.samples().select(0);
    view.addFiles().click();
    assertTrue(optional(() -> $(AddSampleFilesDialogElement.class).id(AddSampleFilesDialog.ID))
        .isPresent());
  }
}
