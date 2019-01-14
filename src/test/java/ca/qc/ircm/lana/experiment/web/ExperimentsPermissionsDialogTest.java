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

import static ca.qc.ircm.lana.experiment.ExperimentProperties.NAME;
import static ca.qc.ircm.lana.experiment.web.ExperimentsPermissionsDialog.CLASS_NAME;
import static ca.qc.ircm.lana.experiment.web.ExperimentsPermissionsDialog.EXPERIMENTS;
import static ca.qc.ircm.lana.experiment.web.ExperimentsPermissionsDialog.HEADER;
import static ca.qc.ircm.lana.experiment.web.ExperimentsPermissionsDialog.MANAGERS;
import static ca.qc.ircm.lana.experiment.web.ExperimentsPermissionsDialog.READ;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lana.text.Strings.property;
import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lana.user.UserProperties.MANAGER;
import static ca.qc.ircm.lana.web.WebConstants.ALL;
import static ca.qc.ircm.lana.web.WebConstants.CANCEL;
import static ca.qc.ircm.lana.web.WebConstants.PRIMARY;
import static ca.qc.ircm.lana.web.WebConstants.SAVE;
import static ca.qc.ircm.lana.web.WebConstants.THEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentRepository;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserRepository;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
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
public class ExperimentsPermissionsDialogTest extends AbstractViewTestCase {
  private ExperimentsPermissionsDialog dialog;
  @Mock
  private ExperimentsPermissionsDialogPresenter presenter;
  @Captor
  private ArgumentCaptor<ValueProvider<Experiment, String>> experimentValueProviderCaptor;
  @Captor
  private ArgumentCaptor<ValueProvider<User, String>> userValueProviderCaptor;
  @Captor
  private ArgumentCaptor<ValueProvider<User, Checkbox>> userCheckboxValueProviderCaptor;
  @Inject
  private ExperimentRepository experimentRepository;
  @Inject
  private UserRepository userRepository;
  private Locale locale = Locale.ENGLISH;
  private MessageResource resources =
      new MessageResource(ExperimentsPermissionsDialog.class, locale);
  private MessageResource userResources = new MessageResource(User.class, locale);
  private MessageResource webResources = new MessageResource(WebConstants.class, locale);
  private List<Experiment> experiments;
  private List<User> users;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    dialog = new ExperimentsPermissionsDialog(presenter);
    dialog.init();
    experiments = experimentRepository.findAll();
    users = userRepository.findAll();
  }

  @SuppressWarnings("unchecked")
  private void mockExperimentColumns() {
    Element experimentsElement = dialog.experiments.getElement();
    dialog.experiments = mock(Grid.class);
    when(dialog.experiments.getElement()).thenReturn(experimentsElement);
    dialog.name = mock(Column.class);
    when(dialog.experiments.addColumn(any(ValueProvider.class), eq(NAME))).thenReturn(dialog.name);
    when(dialog.name.setKey(any())).thenReturn(dialog.name);
    when(dialog.name.setComparator(any(Comparator.class))).thenReturn(dialog.name);
    when(dialog.name.setHeader(any(String.class))).thenReturn(dialog.name);
  }

  @SuppressWarnings("unchecked")
  private void mockManagersColumns() {
    Element managersElement = dialog.managers.getElement();
    dialog.managers = mock(Grid.class);
    when(dialog.managers.getElement()).thenReturn(managersElement);
    dialog.laboratory = mock(Column.class);
    when(dialog.managers.addColumn(any(ValueProvider.class), eq(LABORATORY)))
        .thenReturn(dialog.laboratory);
    when(dialog.laboratory.setKey(any())).thenReturn(dialog.laboratory);
    when(dialog.laboratory.setHeader(any(String.class))).thenReturn(dialog.laboratory);
    dialog.email = mock(Column.class);
    when(dialog.managers.addColumn(any(ValueProvider.class), eq(EMAIL))).thenReturn(dialog.email);
    when(dialog.email.setKey(any())).thenReturn(dialog.email);
    when(dialog.email.setHeader(any(String.class))).thenReturn(dialog.email);
    dialog.read = mock(Column.class);
    when(dialog.managers.addComponentColumn(any(ValueProvider.class))).thenReturn(dialog.read);
    when(dialog.read.setKey(any())).thenReturn(dialog.read);
    when(dialog.read.setHeader(any(String.class))).thenReturn(dialog.read);
    HeaderRow filtersRow = mock(HeaderRow.class);
    when(dialog.managers.appendHeaderRow()).thenReturn(filtersRow);
    HeaderCell laboratoryFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(dialog.laboratory)).thenReturn(laboratoryFilterCell);
    HeaderCell emailFilterCell = mock(HeaderCell.class);
    when(filtersRow.getCell(dialog.email)).thenReturn(emailFilterCell);
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(dialog);
  }

  @Test
  public void styles() {
    assertEquals(CLASS_NAME, dialog.getId().orElse(""));
    assertTrue(dialog.header.getClassNames().contains(HEADER));
    assertTrue(dialog.experiments.getClassNames().contains(EXPERIMENTS));
    assertTrue(dialog.managers.getClassNames().contains(MANAGERS));
    assertTrue(dialog.save.getClassNames().contains(SAVE));
    assertEquals(PRIMARY, dialog.save.getElement().getAttribute(THEME));
    assertTrue(dialog.cancel.getClassNames().contains(CANCEL));
  }

  @Test
  public void labels() {
    mockExperimentColumns();
    mockManagersColumns();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    verify(dialog.name).setHeader(resources.message(property(EXPERIMENTS, NAME)));
    verify(dialog.name).setFooter(resources.message(property(EXPERIMENTS, NAME)));
    verify(dialog.laboratory).setHeader(userResources.message(LABORATORY));
    verify(dialog.laboratory).setFooter(userResources.message(LABORATORY));
    verify(dialog.email).setHeader(userResources.message(MANAGER));
    verify(dialog.email).setFooter(userResources.message(MANAGER));
    verify(dialog.read).setHeader(resources.message(READ));
    verify(dialog.read).setFooter(resources.message(READ));
    assertEquals(webResources.message(ALL), dialog.emailFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), dialog.laboratoryFilter.getPlaceholder());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), dialog.save);
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel);
  }

  @Test
  public void localeChange() {
    mockExperimentColumns();
    mockManagersColumns();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final MessageResource resources =
        new MessageResource(ExperimentsPermissionsDialog.class, locale);
    final MessageResource userResources = new MessageResource(User.class, locale);
    final MessageResource webResources = new MessageResource(WebConstants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    verify(dialog.name).setHeader(resources.message(property(EXPERIMENTS, NAME)));
    verify(dialog.name).setFooter(resources.message(property(EXPERIMENTS, NAME)));
    verify(dialog.laboratory).setHeader(userResources.message(LABORATORY));
    verify(dialog.laboratory).setFooter(userResources.message(LABORATORY));
    verify(dialog.email).setHeader(userResources.message(MANAGER));
    verify(dialog.email).setFooter(userResources.message(MANAGER));
    verify(dialog.read).setHeader(resources.message(READ));
    verify(dialog.read).setFooter(resources.message(READ));
    assertEquals(webResources.message(ALL), dialog.emailFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), dialog.laboratoryFilter.getPlaceholder());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), dialog.save);
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel);
  }

  @Test
  public void experiments_Columns() {
    assertEquals(1, dialog.experiments.getColumns().size());
    assertNotNull(dialog.experiments.getColumnByKey(NAME));
  }

  @Test
  public void experiments_ColumnsValueProvider() {
    dialog = new ExperimentsPermissionsDialog(presenter);
    mockExperimentColumns();
    dialog.init();
    verify(dialog.experiments).addColumn(experimentValueProviderCaptor.capture(), eq(NAME));
    ValueProvider<Experiment, String> valueProvider = experimentValueProviderCaptor.getValue();
    for (Experiment experiment : experiments) {
      assertEquals(experiment.getName(), valueProvider.apply(experiment));
    }
  }

  @Test
  public void managers_Columns() {
    assertEquals(3, dialog.managers.getColumns().size());
    assertNotNull(dialog.managers.getColumnByKey(LABORATORY));
    assertNotNull(dialog.managers.getColumnByKey(EMAIL));
    assertNotNull(dialog.managers.getColumnByKey(READ));
  }

  @Test
  public void managers_ColumnsValueProvider() {
    dialog = new ExperimentsPermissionsDialog(presenter);
    mockManagersColumns();
    dialog.init();
    verify(dialog.managers).addColumn(userValueProviderCaptor.capture(), eq(LABORATORY));
    ValueProvider<User, String> valueProvider = userValueProviderCaptor.getValue();
    for (User user : users) {
      assertEquals(user.getLaboratory().getName(), valueProvider.apply(user));
    }
    verify(dialog.managers).addColumn(userValueProviderCaptor.capture(), eq(EMAIL));
    valueProvider = userValueProviderCaptor.getValue();
    for (User user : users) {
      assertEquals(user.getEmail(), valueProvider.apply(user));
    }
    verify(dialog.managers).addComponentColumn(userCheckboxValueProviderCaptor.capture());
    ValueProvider<User, Checkbox> userCheckboxProvider = userCheckboxValueProviderCaptor.getValue();
    for (User user : users) {
      Checkbox checkbox = userCheckboxProvider.apply(user);
      assertTrue(checkbox.getClassNames().contains(READ));
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  public void getExperiments() {
    experiments = mock(List.class);
    when(presenter.getExperiments()).thenReturn(experiments);
    assertEquals(experiments, dialog.getExperiments());
    verify(presenter).getExperiments();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void setExperiments() {
    experiments = mock(List.class);
    dialog.setExperiments(experiments);
    verify(presenter).setExperiments(experiments);
  }

  @Test
  public void save() {
    clickButton(dialog.save);

    verify(presenter).save();
  }

  @Test
  public void cancel() {
    clickButton(dialog.cancel);

    verify(presenter).cancel();
  }
}
