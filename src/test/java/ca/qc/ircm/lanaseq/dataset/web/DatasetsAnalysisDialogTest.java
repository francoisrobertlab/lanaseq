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
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.CREATE_FOLDER_EXCEPTION;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.analysis.AnalysisService;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.test.config.UserAgent;
import com.github.mvysny.kaributesting.v10.LocatorJ;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.html.Span;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link DatasetsAnalysisDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetsAnalysisDialogTest extends AbstractKaribuTestCase {
  private DatasetsAnalysisDialog dialog;
  @MockBean
  private AnalysisService service;
  @MockBean
  private AppConfiguration configuration;
  @Autowired
  private DatasetRepository repository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetsAnalysisDialog.class, locale);
  private List<Dataset> datasets = new ArrayList<>();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(configuration.getAnalysis()).thenReturn(mock(AppConfiguration.NetworkDrive.class));
    datasets.add(repository.findById(6L).get());
    datasets.add(repository.findById(7L).get());
    ui.setLocale(locale);
    DatasetsView view = ui.navigate(DatasetsView.class).get();
    view.datasets.setItems(repository.findAll());
    datasets.forEach(sample -> view.datasets.select(sample));
    view.analyze.click();
    dialog = LocatorJ._find(DatasetsAnalysisDialog.class).get(0);
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(MESSAGE), dialog.message.getId().orElse(""));
    assertEquals(id(CREATE_FOLDER), dialog.createFolder.getId().orElse(""));
    assertTrue(
        dialog.createFolder.getThemeNames().contains(ButtonVariant.LUMO_PRIMARY.getVariantName()));
    assertEquals(id(CONFIRM), dialog.confirm.getId().orElse(""));
    assertEquals(id(ERRORS), dialog.errors.getId().orElse(""));
    assertEquals(dialog.errorsLayout.getElement(), dialog.errors.getElement().getChild(0));
  }

  @Test
  public void labels() {
    assertEquals(resources.message(HEADER, datasets.size()), dialog.getHeaderTitle());
    assertEquals(resources.message(MESSAGE), dialog.message.getText());
    assertEquals(resources.message(CREATE_FOLDER), dialog.createFolder.getText());
    assertEquals(resources.message(CONFIRM), dialog.confirm.getElement().getProperty("header"));
    assertEquals(resources.message(property(CONFIRM, CONFIRM)),
        dialog.confirm.getElement().getProperty("confirmText"));
    assertEquals(resources.message(ERRORS), dialog.errors.getElement().getProperty("header"));
    assertEquals(resources.message(property(ERRORS, CONFIRM)),
        dialog.errors.getElement().getProperty("confirmText"));
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(DatasetsAnalysisDialog.class, locale);
    ui.setLocale(locale);
    assertEquals(resources.message(HEADER, datasets.size()), dialog.getHeaderTitle());
    assertEquals(resources.message(MESSAGE), dialog.message.getText());
    assertEquals(resources.message(CREATE_FOLDER), dialog.createFolder.getText());
    assertEquals(resources.message(CONFIRM), dialog.confirm.getElement().getProperty("header"));
    assertEquals(resources.message(property(CONFIRM, CONFIRM)),
        dialog.confirm.getElement().getProperty("confirmText"));
    assertEquals(resources.message(ERRORS), dialog.errors.getElement().getProperty("header"));
    assertEquals(resources.message(property(ERRORS, CONFIRM)),
        dialog.errors.getElement().getProperty("confirmText"));
  }

  @Test
  public void validate_Success() {
    dialog.validate();
    verify(service, atLeastOnce()).validateDatasets(eq(datasets), eq(locale), any());
    assertEquals(0, dialog.errorsLayout.getComponentCount());
    assertFalse(dialog.errors.isOpened());
    assertTrue(LocatorJ._find(ConfirmDialog.class).isEmpty());
    assertTrue(dialog.createFolder.isEnabled());
  }

  @Test
  public void validate_Errors() {
    doAnswer(i -> {
      Consumer<String> errorHandler = i.getArgument(2);
      errorHandler.accept("error1");
      errorHandler.accept("error2");
      return null;
    }).when(service).validateDatasets(any(Collection.class), any(), any());
    dialog.validate();
    verify(service, atLeastOnce()).validateDatasets(eq(datasets), eq(locale), any());
    assertEquals(2, dialog.errorsLayout.getComponentCount());
    assertTrue(dialog.errorsLayout.getComponentAt(0) instanceof Span);
    assertEquals("error1", ((Span) dialog.errorsLayout.getComponentAt(0)).getText());
    assertTrue(dialog.errorsLayout.getComponentAt(1) instanceof Span);
    assertEquals("error2", ((Span) dialog.errorsLayout.getComponentAt(1)).getText());
    assertTrue(dialog.errors.isOpened());
    assertEquals(1, LocatorJ._find(ConfirmDialog.class).size());
    ConfirmDialog confirmDialog = LocatorJ._find(ConfirmDialog.class).get(0);
    assertEquals(dialog.errors, confirmDialog);
    assertFalse(dialog.createFolder.isEnabled());
  }

  @Test
  public void validate_OnOpen() {
    verify(service, times(1)).validateDatasets(eq(datasets), eq(locale), any());
    dialog.close();
    dialog.open();
    verify(service, times(2)).validateDatasets(eq(datasets), eq(locale), any());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void createFolder_Windows() throws Throwable {
    String folder = "test/dataset";
    when(configuration.getAnalysis().label(any(Collection.class), anyBoolean())).thenReturn(folder);
    dialog.createFolder.click();
    verify(service, atLeast(2)).validateDatasets(eq(datasets), eq(locale), any());
    verify(service).copyDatasetsResources(datasets);
    verify(configuration.getAnalysis()).label(datasets, false);
    assertEquals(resources.message(property(CONFIRM, "message"), folder),
        dialog.confirm.getElement().getProperty("message"));
    assertTrue(dialog.confirm.isOpened());
    assertFalse(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void createFolder_Unix() throws Throwable {
    String folder = "test/dataset";
    when(configuration.getAnalysis().label(any(Collection.class), anyBoolean())).thenReturn(folder);
    dialog.createFolder.click();
    verify(service, atLeast(2)).validateDatasets(eq(datasets), eq(locale), any());
    verify(service).copyDatasetsResources(datasets);
    verify(configuration.getAnalysis()).label(datasets, true);
    assertEquals(resources.message(property(CONFIRM, "message"), folder),
        dialog.confirm.getElement().getProperty("message"));
    assertTrue(dialog.confirm.isOpened());
    assertFalse(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void createFolder_Mac() throws Throwable {
    String folder = "test/dataset";
    when(configuration.getAnalysis().label(any(Collection.class), anyBoolean())).thenReturn(folder);
    dialog.createFolder.click();
    verify(service, atLeast(2)).validateDatasets(eq(datasets), eq(locale), any());
    verify(service).copyDatasetsResources(datasets);
    verify(configuration.getAnalysis()).label(datasets, true);
    assertEquals(resources.message(property(CONFIRM, "message"), folder),
        dialog.confirm.getElement().getProperty("message"));
    assertTrue(dialog.confirm.isOpened());
    assertFalse(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  public void createFolder_IoException() throws Throwable {
    doThrow(new IOException("test")).when(service).copyDatasetsResources(any(Collection.class));
    dialog.createFolder.click();
    verify(service, atLeast(2)).validateDatasets(eq(datasets), eq(locale), any());
    verify(service).copyDatasetsResources(datasets);
    assertFalse(dialog.confirm.isOpened());
    assertEquals(1, dialog.errorsLayout.getComponentCount());
    assertTrue(dialog.errorsLayout.getComponentAt(0) instanceof Span);
    assertEquals(resources.message(CREATE_FOLDER_EXCEPTION),
        ((Span) dialog.errorsLayout.getComponentAt(0)).getText());
    assertTrue(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  public void createFolder_IllegalArgumentException() throws Throwable {
    doThrow(new IllegalArgumentException("test")).when(service)
        .copyDatasetsResources(any(Collection.class));
    doAnswer(new Answer<Void>() {
      private int calls = 0;

      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        calls++;
        if (calls > 1) {
          Consumer<String> errorHandler = invocation.getArgument(2);
          errorHandler.accept("error1");
        }
        return null;
      }
    }).when(service).validateDatasets(any(Collection.class), any(), any());
    dialog.createFolder.click();
    verify(service, atLeast(3)).validateDatasets(eq(datasets), eq(locale), any());
    verify(service).copyDatasetsResources(datasets);
    assertFalse(dialog.confirm.isOpened());
    assertEquals(1, dialog.errorsLayout.getComponentCount());
    assertTrue(dialog.errorsLayout.getComponentAt(0) instanceof Span);
    assertEquals("error1", ((Span) dialog.errorsLayout.getComponentAt(0)).getText());
    assertTrue(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  public void closeOnConfirm() {
    fireEvent(dialog.confirm, new ConfirmDialog.ConfirmEvent(dialog.confirm, false));
    assertFalse(dialog.isOpened());
  }

  @Test
  public void closeOnErrorsConfirm() {
    fireEvent(dialog.errors, new ConfirmDialog.ConfirmEvent(dialog.errors, false));
    assertFalse(dialog.isOpened());
  }

  @Test
  public void getDatasets() {
    List<Dataset> datasets = dialog.getDatasets();
    assertEquals(this.datasets.size(), datasets.size());
    for (Dataset dataset : this.datasets) {
      assertTrue(datasets.contains(dataset));
    }
  }

  @Test
  public void setDataset() {
    Dataset dataset = repository.findById(6L).get();
    dialog.setDataset(dataset);
    assertEquals(resources.message(SamplesAnalysisDialog.HEADER, 1, dataset.getName()),
        dialog.getHeaderTitle());
  }

  @Test
  public void setDatasets() {
    List<Dataset> datasets = new ArrayList<>();
    datasets.add(repository.findById(2L).get());
    datasets.add(repository.findById(6L).get());
    datasets.add(repository.findById(7L).get());
    dialog.setDatasets(datasets);
    assertEquals(resources.message(SamplesAnalysisDialog.HEADER, datasets.size()),
        dialog.getHeaderTitle());
  }
}
