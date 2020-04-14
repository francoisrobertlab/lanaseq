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

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.PRIMARY;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.THEME;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.user.LaboratoryProperties.NAME;
import static ca.qc.ircm.lanaseq.user.web.LaboratoryDialog.CLASS_NAME;
import static ca.qc.ircm.lanaseq.user.web.LaboratoryDialog.HEADER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.LaboratoryRepository;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class LaboratoryDialogTest extends AbstractViewTestCase {
  private LaboratoryDialog dialog;
  @Mock
  private LaboratoryDialogPresenter presenter;
  @Mock
  private Laboratory laboratory;
  @Mock
  private ComponentEventListener<SavedEvent<LaboratoryDialog>> savedListener;
  @Autowired
  private LaboratoryRepository laboratoryRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(LaboratoryDialog.class, locale);
  private AppResources laboratoryResources = new AppResources(Laboratory.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    dialog = new LaboratoryDialog(presenter);
    dialog.init();
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(dialog);
  }

  @Test
  public void styles() {
    assertEquals(CLASS_NAME, dialog.getId().orElse(""));
    assertTrue(dialog.header.getClassNames().contains(HEADER));
    assertTrue(dialog.name.getClassNames().contains(NAME));
    assertTrue(dialog.save.getClassNames().contains(SAVE));
    assertTrue(dialog.save.getElement().getAttribute(THEME).contains(PRIMARY));
    assertTrue(dialog.cancel.getClassNames().contains(CANCEL));
  }

  @Test
  public void labels() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(laboratoryResources.message(NAME), dialog.name.getLabel());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel.getIcon());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(LaboratoryDialog.class, locale);
    final AppResources userResources = new AppResources(Laboratory.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(userResources.message(NAME), dialog.name.getLabel());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void savedListener() {
    dialog.addSavedListener(savedListener);
    dialog.fireSavedEvent();
    verify(savedListener).onComponentEvent(any());
  }

  @Test
  public void savedListener_Remove() {
    dialog.addSavedListener(savedListener).remove();
    dialog.fireSavedEvent();
    verify(savedListener, never()).onComponentEvent(any());
  }

  @Test
  public void getLaboratory() {
    when(presenter.getLaboratory()).thenReturn(laboratory);
    assertEquals(laboratory, dialog.getLaboratory());
    verify(presenter).getLaboratory();
  }

  @Test
  public void setLaboratory_NewLaboratory() {
    Laboratory laboratory = new Laboratory();
    when(presenter.getLaboratory()).thenReturn(laboratory);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setLaboratory(laboratory);

    verify(presenter).setLaboratory(laboratory);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void setLaboratory_Laboratory() {
    Laboratory laboratory = laboratoryRepository.findById(2L).get();
    when(presenter.getLaboratory()).thenReturn(laboratory);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setLaboratory(laboratory);

    verify(presenter).setLaboratory(laboratory);
    assertEquals(resources.message(HEADER, 1, laboratory.getName()), dialog.header.getText());
  }

  @Test
  public void setLaboratory_BeforeLocaleChange() {
    Laboratory laboratory = laboratoryRepository.findById(2L).get();
    when(presenter.getLaboratory()).thenReturn(laboratory);

    dialog.setLaboratory(laboratory);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(presenter).setLaboratory(laboratory);
    assertEquals(resources.message(HEADER, 1, laboratory.getName()), dialog.header.getText());
  }

  @Test
  public void setLaboratory_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setLaboratory(null);

    verify(presenter).setLaboratory(null);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void save() {
    clickButton(dialog.save);

    verify(presenter).save(locale);
  }

  @Test
  public void cancel() {
    clickButton(dialog.cancel);

    verify(presenter).cancel();
  }
}
