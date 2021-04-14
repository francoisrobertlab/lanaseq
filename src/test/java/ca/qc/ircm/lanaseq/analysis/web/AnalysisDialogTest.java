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

package ca.qc.ircm.lanaseq.analysis.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisDialog.CREATE_FOLDER;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisDialog.ERRORS;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisDialog.HEADER;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisDialog.ID;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisDialog.id;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;

@ServiceTestAnnotations
@WithMockUser
public class AnalysisDialogTest extends AbstractKaribuTestCase {
  private AnalysisDialog dialog;
  @Mock
  private AnalysisDialogPresenter presenter;
  @Mock
  private Dataset dataset;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(AnalysisDialog.class, locale);

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    dialog = new AnalysisDialog(presenter);
    dialog.init();
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(dialog);
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(HEADER), dialog.header.getId().orElse(""));
    assertEquals(id(MESSAGE), dialog.message.getId().orElse(""));
    assertEquals(id(CREATE_FOLDER), dialog.createFolder.getId().orElse(""));
    assertEquals(id(CONFIRM), dialog.confirm.getId().orElse(""));
    assertEquals(dialog.confirmLayout.getElement(), dialog.confirm.getElement().getChild(0));
    assertEquals(id(ERRORS), dialog.errors.getId().orElse(""));
    assertEquals(dialog.errorsLayout.getElement(), dialog.errors.getElement().getChild(0));
  }

  @Test
  public void labels() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, ""), dialog.header.getText());
    assertEquals(resources.message(MESSAGE), dialog.message.getText());
    assertEquals(resources.message(CREATE_FOLDER), dialog.createFolder.getText());
    assertEquals(resources.message(CONFIRM), dialog.confirm.getElement().getProperty("header"));
    assertEquals(resources.message(property(CONFIRM, CONFIRM)),
        dialog.confirm.getElement().getProperty("confirmText"));
    assertEquals(resources.message(ERRORS), dialog.errors.getElement().getProperty("header"));
    assertEquals(resources.message(property(ERRORS, CONFIRM)),
        dialog.errors.getElement().getProperty("confirmText"));
    verify(presenter).localChange(locale);
  }

  @Test
  public void localeChange() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(AnalysisDialog.class, locale);
    ui.setLocale(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, ""), dialog.header.getText());
    assertEquals(resources.message(MESSAGE), dialog.message.getText());
    assertEquals(resources.message(CREATE_FOLDER), dialog.createFolder.getText());
    assertEquals(resources.message(CONFIRM), dialog.confirm.getElement().getProperty("header"));
    assertEquals(resources.message(property(CONFIRM, CONFIRM)),
        dialog.confirm.getElement().getProperty("confirmText"));
    assertEquals(resources.message(ERRORS), dialog.errors.getElement().getProperty("header"));
    assertEquals(resources.message(property(ERRORS, CONFIRM)),
        dialog.errors.getElement().getProperty("confirmText"));
    verify(presenter).localChange(locale);
  }

  @Test
  public void createFolder() {
    dialog.createFolder.click();
    verify(presenter).createFolder();
  }

  @Test
  public void setDataset() {
    when(dataset.getName()).thenReturn("Test Dataset Name");
    when(presenter.getDataset()).thenReturn(dataset);
    dialog.setDataset(dataset);
    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER, dataset.getName()), dialog.header.getText());
  }
}
