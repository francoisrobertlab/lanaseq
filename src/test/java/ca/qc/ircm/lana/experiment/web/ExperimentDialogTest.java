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
import static ca.qc.ircm.lana.experiment.web.ExperimentDialog.CLASS_NAME;
import static ca.qc.ircm.lana.experiment.web.ExperimentDialog.HEADER;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lana.web.WebConstants.CANCEL;
import static ca.qc.ircm.lana.web.WebConstants.PRIMARY;
import static ca.qc.ircm.lana.web.WebConstants.SAVE;
import static ca.qc.ircm.lana.web.WebConstants.THEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.experiment.ExperimentRepository;
import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Locale;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class ExperimentDialogTest extends AbstractViewTestCase {
  private ExperimentDialog dialog;
  @Mock
  private ExperimentDialogPresenter presenter;
  @Mock
  private Experiment experiment;
  @Inject
  private ExperimentRepository experimentRepository;
  private Locale locale = Locale.ENGLISH;
  private MessageResource resources = new MessageResource(ExperimentDialog.class, locale);
  private MessageResource experimentResources = new MessageResource(Experiment.class, locale);
  private MessageResource webResources = new MessageResource(WebConstants.class, locale);

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    dialog = new ExperimentDialog(presenter);
  }

  @Test
  public void styles() {
    assertEquals(CLASS_NAME, dialog.getId().orElse(""));
    assertTrue(dialog.header.getClassNames().contains(HEADER));
    assertTrue(dialog.name.getClassNames().contains(NAME));
    assertTrue(dialog.save.getClassNames().contains(SAVE));
    assertEquals(PRIMARY, dialog.save.getElement().getAttribute(THEME));
    assertTrue(dialog.cancel.getClassNames().contains(CANCEL));
  }

  @Test
  public void labels() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(experimentResources.message(NAME), dialog.name.getLabel());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), dialog.save);
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel);
  }

  @Test
  public void localeChange() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final MessageResource resources = new MessageResource(ExperimentDialog.class, locale);
    final MessageResource userResources = new MessageResource(Experiment.class, locale);
    final MessageResource webResources = new MessageResource(WebConstants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(userResources.message(NAME), dialog.name.getLabel());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
  }

  @Test
  public void isReadOnly_False() {
    assertFalse(dialog.isReadOnly());
    verify(presenter).isReadOnly();
  }

  @Test
  public void isReadOnly_True() {
    when(presenter.isReadOnly()).thenReturn(true);
    assertTrue(dialog.isReadOnly());
    verify(presenter).isReadOnly();
  }

  @Test
  public void setReadOnly_False() {
    dialog.setReadOnly(false);
    verify(presenter).setReadOnly(false);
  }

  @Test
  public void setReadOnly_True() {
    dialog.setReadOnly(true);
    verify(presenter).setReadOnly(true);
  }

  @Test
  public void getExperiment() {
    when(presenter.getExperiment()).thenReturn(experiment);
    assertEquals(experiment, dialog.getExperiment());
    verify(presenter).getExperiment();
  }

  @Test
  public void setExperiment_NewExperiment() {
    Experiment experiment = new Experiment();

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setExperiment(experiment);

    verify(presenter).setExperiment(experiment);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void setExperiment_Experiment() {
    Experiment experiment = experimentRepository.findById(2L).get();

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setExperiment(experiment);

    verify(presenter).setExperiment(experiment);
    assertEquals(resources.message(HEADER, 1, experiment.getName()), dialog.header.getText());
  }

  @Test
  public void setExperiment_BeforeLocaleChange() {
    Experiment experiment = experimentRepository.findById(2L).get();

    dialog.setExperiment(experiment);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(presenter).setExperiment(experiment);
    assertEquals(resources.message(HEADER, 1, experiment.getName()), dialog.header.getText());
  }

  @Test
  public void setExperiment_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setExperiment(null);

    verify(presenter).setExperiment(experiment);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
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
