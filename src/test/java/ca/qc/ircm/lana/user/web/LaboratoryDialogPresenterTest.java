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

package ca.qc.ircm.lana.user.web;

import static ca.qc.ircm.lana.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lana.web.WebConstants.REQUIRED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.test.config.AbstractViewTestCase;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lana.user.Laboratory;
import ca.qc.ircm.lana.user.LaboratoryRepository;
import ca.qc.ircm.lana.user.LaboratoryService;
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
public class LaboratoryDialogPresenterTest extends AbstractViewTestCase {
  private LaboratoryDialogPresenter presenter;
  @Mock
  private LaboratoryDialog dialog;
  @Mock
  private LaboratoryService laboratoryService;
  @Captor
  private ArgumentCaptor<Laboratory> laboratoryCaptor;
  @Inject
  private LaboratoryRepository laboratoryRepository;
  private Locale locale = Locale.ENGLISH;
  private MessageResource webResources = new MessageResource(WebConstants.class, locale);
  private String name = "Test Laboratory";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    presenter = new LaboratoryDialogPresenter(laboratoryService);
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
  public void getLaboratory() {
    Laboratory laboratory = new Laboratory();
    presenter.setLaboratory(laboratory);
    assertEquals(laboratory, presenter.getLaboratory());
  }

  @Test
  public void setLaboratory_NewLaboratory() {
    Laboratory laboratory = new Laboratory();

    presenter.localeChange(locale);
    presenter.setLaboratory(laboratory);

    assertEquals("", dialog.name.getValue());
  }

  @Test
  public void setLaboratory_Laboratory() {
    Laboratory laboratory = laboratoryRepository.findById(2L).get();

    presenter.localeChange(locale);
    presenter.setLaboratory(laboratory);

    assertEquals(laboratory.getName(), dialog.name.getValue());
  }

  @Test
  public void setLaboratory_BeforeLocaleChange() {
    Laboratory laboratory = laboratoryRepository.findById(2L).get();

    presenter.setLaboratory(laboratory);
    presenter.localeChange(locale);

    assertEquals(laboratory.getName(), dialog.name.getValue());
  }

  @Test
  public void setLaboratory_Null() {
    presenter.localeChange(locale);
    presenter.setLaboratory(null);

    assertEquals("", dialog.name.getValue());
  }

  @Test
  public void save_NameEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.name.setValue("");

    presenter.save();

    BinderValidationStatus<Laboratory> status = presenter.validateLaboratory();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void save_NewLaboratory() {
    presenter.localeChange(locale);
    fillForm();

    presenter.save();

    verify(laboratoryService).save(laboratoryCaptor.capture());
    Laboratory laboratory = laboratoryCaptor.getValue();
    assertEquals(name, laboratory.getName());
    verify(dialog).close();
  }

  @Test
  public void save_UpdateLaboratory() {
    Laboratory laboratory = laboratoryRepository.findById(2L).get();
    presenter.setLaboratory(laboratory);
    presenter.localeChange(locale);
    fillForm();

    presenter.save();

    verify(laboratoryService).save(laboratoryCaptor.capture());
    laboratory = laboratoryCaptor.getValue();
    assertEquals(name, laboratory.getName());
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
