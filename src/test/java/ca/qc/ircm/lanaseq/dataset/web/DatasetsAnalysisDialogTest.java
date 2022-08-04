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

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.CREATE_FOLDER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.ERRORS;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link DatasetsAnalysisDialog}.
 */
@ServiceTestAnnotations
@WithMockUser
public class DatasetsAnalysisDialogTest extends AbstractKaribuTestCase {
  private DatasetsAnalysisDialog dialog;
  @Mock
  private DatasetsAnalysisDialogPresenter presenter;
  @Mock
  private Dataset dataset;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetsAnalysisDialog.class, locale);

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    ui.setLocale(locale);
    dialog = new DatasetsAnalysisDialog(presenter);
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
    assertEquals(id(ERRORS), dialog.errors.getId().orElse(""));
    assertEquals(dialog.errorsLayout.getElement(), dialog.errors.getElement().getChild(0));
  }

  @Test
  public void labels() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
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
    final AppResources resources = new AppResources(DatasetsAnalysisDialog.class, locale);
    ui.setLocale(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
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
  public void closeOnConfirm() throws Throwable {
    fireEvent(dialog.confirm, new ConfirmDialog.ConfirmEvent(dialog.confirm, false));
    assertFalse(dialog.isOpened());
  }

  @Test
  public void closeOnErrorsConfirm() throws Throwable {
    fireEvent(dialog.errors, new ConfirmDialog.ConfirmEvent(dialog.errors, false));
    assertFalse(dialog.isOpened());
  }

  @Test
  public void setDataset() {
    when(dataset.getName()).thenReturn("Test Dataset Name");
    when(presenter.getDatasets()).thenReturn(Collections.nCopies(1, dataset));
    dialog.setDataset(dataset);
    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER, 1, dataset.getName()), dialog.header.getText());
  }

  @Test
  public void setDatasets() {
    when(presenter.getDatasets())
        .thenReturn(Arrays.asList(mock(Dataset.class), mock(Dataset.class), mock(Dataset.class)));
    dialog.setDataset(dataset);
    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER, 3), dialog.header.getText());
  }
}
