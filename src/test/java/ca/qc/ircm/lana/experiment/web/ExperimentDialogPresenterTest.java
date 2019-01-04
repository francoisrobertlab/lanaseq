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

import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentRepository;
import ca.qc.ircm.lana.experiment.ExperimentService;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import java.util.Locale;
import java.util.Optional;
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
public class ExperimentDialogPresenterTest extends AbstractViewTestCase {
  private ExperimentDialogPresenter presenter;
  @Mock
  private ExperimentDialog dialog;
  @Mock
  private ExperimentService experimentService;
  @Captor
  private ArgumentCaptor<Experiment> experimentCaptor;
  @Inject
  private ExperimentRepository experimentRepository;
  private Locale locale = Locale.ENGLISH;
  private MessageResource webResources = new MessageResource(WebConstants.class, locale);
  private String name = "Test Experiment";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    presenter = new ExperimentDialogPresenter(experimentService);
    dialog.header = new H2();
    dialog.name = new TextField();
    dialog.buttonsLayout = new HorizontalLayout();
    dialog.save = new Button();
    dialog.cancel = new Button();
    presenter.init(dialog);
  }

  private void fillForm() {
    dialog.name.setValue(name);
  }

  @Test
  public void isReadOnly_Default() {
    presenter.localeChange(locale);
    assertFalse(presenter.isReadOnly());
  }

  @Test
  public void isReadOnly_False() {
    presenter.localeChange(locale);
    presenter.setReadOnly(false);
    assertFalse(presenter.isReadOnly());
  }

  @Test
  public void isReadOnly_True() {
    presenter.localeChange(locale);
    presenter.setReadOnly(true);
    assertTrue(presenter.isReadOnly());
  }

  @Test
  public void setReadOnly_False() {
    presenter.localeChange(locale);
    presenter.setReadOnly(false);
    assertFalse(dialog.name.isReadOnly());
    assertTrue(dialog.buttonsLayout.isVisible());
  }

  @Test
  public void setReadOnly_True() {
    presenter.localeChange(locale);
    presenter.setReadOnly(true);
    assertTrue(dialog.name.isReadOnly());
    assertFalse(dialog.buttonsLayout.isVisible());
  }

  @Test
  public void getExperiment() {
    Experiment experiment = new Experiment();
    presenter.setExperiment(experiment);
    assertEquals(experiment, presenter.getExperiment());
  }

  @Test
  public void setExperiment_NewExperiment() {
    Experiment experiment = new Experiment();

    presenter.localeChange(locale);
    presenter.setExperiment(experiment);

    assertEquals("", dialog.name.getValue());
  }

  @Test
  public void setExperiment_Experiment() {
    Experiment experiment = experimentRepository.findById(2L).get();

    presenter.localeChange(locale);
    presenter.setExperiment(experiment);

    assertEquals(experiment.getName(), dialog.name.getValue());
  }

  @Test
  public void setExperiment_BeforeLocaleChange() {
    Experiment experiment = experimentRepository.findById(2L).get();

    presenter.setExperiment(experiment);
    presenter.localeChange(locale);

    assertEquals(experiment.getName(), dialog.name.getValue());
  }

  @Test
  public void setExperiment_Null() {
    presenter.localeChange(locale);
    presenter.setExperiment(null);

    assertEquals("", dialog.name.getValue());
  }

  @Test
  public void save_NameEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.name.setValue("");

    presenter.save();

    BinderValidationStatus<Experiment> status = presenter.validateExperiment();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void save_NewExperiment() {
    presenter.localeChange(locale);
    fillForm();

    presenter.save();

    verify(experimentService).save(experimentCaptor.capture());
    Experiment experiment = experimentCaptor.getValue();
    assertEquals(name, experiment.getName());
    verify(dialog).close();
  }

  @Test
  public void save_UpdateExperiment() {
    Experiment experiment = experimentRepository.findById(2L).get();
    presenter.setExperiment(experiment);
    presenter.localeChange(locale);
    fillForm();

    presenter.save();

    verify(experimentService).save(experimentCaptor.capture());
    experiment = experimentCaptor.getValue();
    assertEquals(name, experiment.getName());
    verify(dialog).close();
  }

  @Test
  public void cancel_Close() {
    presenter.init(dialog);
    presenter.localeChange(locale);

    presenter.cancel();

    verify(dialog).close();
  }
}
