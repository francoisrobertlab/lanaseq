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

import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.PRIMARY;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.THEME;
import static ca.qc.ircm.lanaseq.dataset.web.ExperimentPermissionsDialog.CLASS_NAME;
import static ca.qc.ircm.lanaseq.dataset.web.ExperimentPermissionsDialog.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.ExperimentPermissionsDialog.MANAGERS;
import static ca.qc.ircm.lanaseq.dataset.web.ExperimentPermissionsDialog.READ;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lanaseq.user.UserProperties.MANAGER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Experiment;
import ca.qc.ircm.lanaseq.dataset.ExperimentRepository;
import ca.qc.ircm.lanaseq.dataset.web.ExperimentPermissionsDialog;
import ca.qc.ircm.lanaseq.dataset.web.ExperimentPermissionsDialogPresenter;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserRepository;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ExperimentPermissionsDialogTest extends AbstractViewTestCase {
  private ExperimentPermissionsDialog dialog;
  @Mock
  private ExperimentPermissionsDialogPresenter presenter;
  @Captor
  private ArgumentCaptor<ValueProvider<Experiment, String>> experimentValueProviderCaptor;
  @Captor
  private ArgumentCaptor<ValueProvider<User, String>> userValueProviderCaptor;
  @Captor
  private ArgumentCaptor<ValueProvider<User, Checkbox>> userCheckboxValueProviderCaptor;
  @Autowired
  private ExperimentRepository experimentRepository;
  @Autowired
  private UserRepository userRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(ExperimentPermissionsDialog.class, locale);
  private AppResources userResources = new AppResources(User.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private Experiment experiment;
  private List<User> users;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    dialog = new ExperimentPermissionsDialog(presenter);
    dialog.init();
    experiment = experimentRepository.findById(2L).orElse(null);
    users = userRepository.findAll();
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
    assertTrue(dialog.managers.getClassNames().contains(MANAGERS));
    assertTrue(dialog.save.getClassNames().contains(SAVE));
    assertTrue(dialog.save.getElement().getAttribute(THEME).contains(PRIMARY));
    assertTrue(dialog.cancel.getClassNames().contains(CANCEL));
  }

  @Test
  public void labels() {
    mockManagersColumns();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, ""), dialog.header.getText());
    verify(dialog.laboratory).setHeader(userResources.message(LABORATORY));
    verify(dialog.laboratory).setFooter(userResources.message(LABORATORY));
    verify(dialog.email).setHeader(userResources.message(MANAGER));
    verify(dialog.email).setFooter(userResources.message(MANAGER));
    verify(dialog.read).setHeader(resources.message(READ));
    verify(dialog.read).setFooter(resources.message(READ));
    assertEquals(webResources.message(ALL), dialog.emailFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), dialog.laboratoryFilter.getPlaceholder());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel.getIcon());
  }

  @Test
  public void localeChange() {
    mockManagersColumns();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(ExperimentPermissionsDialog.class, locale);
    final AppResources userResources = new AppResources(User.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, ""), dialog.header.getText());
    verify(dialog.laboratory).setHeader(userResources.message(LABORATORY));
    verify(dialog.laboratory).setFooter(userResources.message(LABORATORY));
    verify(dialog.email).setHeader(userResources.message(MANAGER));
    verify(dialog.email).setFooter(userResources.message(MANAGER));
    verify(dialog.read).setHeader(resources.message(READ));
    verify(dialog.read).setFooter(resources.message(READ));
    assertEquals(webResources.message(ALL), dialog.emailFilter.getPlaceholder());
    assertEquals(webResources.message(ALL), dialog.laboratoryFilter.getPlaceholder());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel.getIcon());
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
    dialog = new ExperimentPermissionsDialog(presenter);
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
  public void getExperiment() {
    experiment = mock(Experiment.class);
    when(presenter.getExperiment()).thenReturn(experiment);
    assertEquals(experiment, dialog.getExperiment());
    verify(presenter).getExperiment();
  }

  @Test
  public void setExperiments() {
    when(presenter.getExperiment()).thenReturn(experiment);
    dialog.setExperiment(experiment);
    verify(presenter).setExperiment(experiment);
    assertEquals(resources.message(HEADER, experiment.getName()), dialog.header.getText());
  }

  @Test
  public void setExperiments_NullExperiment() {
    when(presenter.getExperiment()).thenReturn(null);
    dialog.setExperiment(null);
    verify(presenter).setExperiment(null);
    assertEquals(resources.message(HEADER, ""), dialog.header.getText());
  }

  @Test
  public void setExperiments_NullName() {
    experiment = new Experiment();
    when(presenter.getExperiment()).thenReturn(experiment);
    dialog.setExperiment(experiment);
    verify(presenter).setExperiment(experiment);
    assertEquals(resources.message(HEADER, ""), dialog.header.getText());
  }

  @Test
  public void setExperiment_BeforeLocaleChange() {
    Experiment experiment = experimentRepository.findById(2L).get();
    when(presenter.getExperiment()).thenReturn(experiment);

    dialog.setExperiment(experiment);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(presenter).setExperiment(experiment);
    assertEquals(resources.message(HEADER, experiment.getName()), dialog.header.getText());
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
