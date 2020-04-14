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

import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.user.web.LaboratoryDialog.SAVED;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.LaboratoryRepository;
import ca.qc.ircm.lanaseq.user.LaboratoryService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import java.util.Locale;
import java.util.Optional;
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
public class LaboratoryDialogPresenterTest extends AbstractViewTestCase {
  private LaboratoryDialogPresenter presenter;
  @Mock
  private LaboratoryDialog dialog;
  @Mock
  private LaboratoryService laboratoryService;
  @Mock
  private AuthorizationService authorizationService;
  @Captor
  private ArgumentCaptor<Laboratory> laboratoryCaptor;
  @Autowired
  private LaboratoryRepository laboratoryRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(LaboratoryDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private String name = "Test Laboratory";

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    presenter = new LaboratoryDialogPresenter(laboratoryService, authorizationService);
    dialog.header = new H2();
    dialog.name = new TextField();
    dialog.buttonsLayout = new HorizontalLayout();
    dialog.save = new Button();
    dialog.cancel = new Button();
    presenter.init(dialog);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
  }

  private void fillForm() {
    dialog.name.setValue(name);
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
    assertFalse(dialog.name.isReadOnly());
    assertTrue(dialog.buttonsLayout.isVisible());
  }

  @Test
  public void setLaboratory_Laboratory() {
    Laboratory laboratory = laboratoryRepository.findById(2L).get();

    presenter.localeChange(locale);
    presenter.setLaboratory(laboratory);

    assertEquals(laboratory.getName(), dialog.name.getValue());
    assertFalse(dialog.name.isReadOnly());
    assertTrue(dialog.buttonsLayout.isVisible());
  }

  @Test
  public void setLaboratory_BeforeLocaleChange() {
    Laboratory laboratory = laboratoryRepository.findById(2L).get();

    presenter.setLaboratory(laboratory);
    presenter.localeChange(locale);

    assertEquals(laboratory.getName(), dialog.name.getValue());
    assertFalse(dialog.name.isReadOnly());
    assertTrue(dialog.buttonsLayout.isVisible());
  }

  @Test
  public void setLaboratory_CannotWrite() {
    Laboratory laboratory = new Laboratory();
    when(authorizationService.hasPermission(any(), any())).thenReturn(false);

    presenter.localeChange(locale);
    presenter.setLaboratory(laboratory);

    assertTrue(dialog.name.isReadOnly());
    assertFalse(dialog.buttonsLayout.isVisible());
  }

  @Test
  public void setLaboratory_CannotWriteBeforeLocaleChange() {
    Laboratory laboratory = new Laboratory();
    when(authorizationService.hasPermission(any(), any())).thenReturn(false);

    presenter.setLaboratory(laboratory);
    presenter.localeChange(locale);

    assertTrue(dialog.name.isReadOnly());
    assertFalse(dialog.buttonsLayout.isVisible());
  }

  @Test
  public void setLaboratory_Null() {
    presenter.localeChange(locale);
    presenter.setLaboratory(null);

    assertEquals("", dialog.name.getValue());
    assertFalse(dialog.name.isReadOnly());
    assertTrue(dialog.buttonsLayout.isVisible());
  }

  @Test
  public void save_NameEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.name.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Laboratory> status = presenter.validateLaboratory();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_NewLaboratory() {
    presenter.localeChange(locale);
    fillForm();

    presenter.save(locale);

    verify(laboratoryService).save(laboratoryCaptor.capture());
    Laboratory laboratory = laboratoryCaptor.getValue();
    assertEquals(name, laboratory.getName());
    verify(dialog).showNotification(resources.message(SAVED, name));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateLaboratory() {
    Laboratory laboratory = laboratoryRepository.findById(2L).get();
    presenter.setLaboratory(laboratory);
    presenter.localeChange(locale);
    fillForm();

    presenter.save(locale);

    verify(laboratoryService).save(laboratoryCaptor.capture());
    laboratory = laboratoryCaptor.getValue();
    assertEquals(name, laboratory.getName());
    verify(dialog).showNotification(resources.message(SAVED, name));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void cancel_Close() {
    presenter.init(dialog);
    presenter.localeChange(locale);

    presenter.cancel();

    verify(dialog).close();
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).fireSavedEvent();
  }
}
