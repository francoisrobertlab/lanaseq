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

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.CREATE_FOLDER;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.CREATE_FOLDER_EXCEPTION;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.ERRORS;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.HEADER;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.ID;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.SamplesAnalysisDialog.id;
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
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
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
 * Tests for {@link SamplesAnalysisDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SamplesAnalysisDialogTest extends AbstractKaribuTestCase {
  private SamplesAnalysisDialog dialog;
  @MockBean
  private AnalysisService service;
  @MockBean
  private AppConfiguration configuration;
  @Autowired
  private SampleRepository repository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SamplesAnalysisDialog.class, locale);
  private List<Sample> samples = new ArrayList<>();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(configuration.getAnalysis()).thenReturn(mock(AppConfiguration.NetworkDrive.class));
    samples.add(repository.findById(10L).get());
    samples.add(repository.findById(11L).get());
    ui.setLocale(locale);
    SamplesView view = ui.navigate(SamplesView.class).get();
    view.samples.setItems(repository.findAll());
    samples.forEach(sample -> view.samples.select(sample));
    view.analyze.click();
    dialog = LocatorJ._find(SamplesAnalysisDialog.class).get(0);
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
    assertEquals(resources.message(HEADER, samples.size()), dialog.getHeaderTitle());
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
    final AppResources resources = new AppResources(SamplesAnalysisDialog.class, locale);
    ui.setLocale(locale);
    assertEquals(resources.message(HEADER, samples.size()), dialog.getHeaderTitle());
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
    verify(service, atLeastOnce()).validateSamples(eq(samples), eq(locale), any());
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
    }).when(service).validateSamples(any(Collection.class), any(), any());
    dialog.validate();
    verify(service, atLeastOnce()).validateSamples(eq(samples), eq(locale), any());
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
    verify(service, times(1)).validateSamples(eq(samples), eq(locale), any());
    dialog.close();
    dialog.open();
    verify(service, times(2)).validateSamples(eq(samples), eq(locale), any());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void createFolder_Windows() throws Throwable {
    String folder = "test/sample";
    when(configuration.getAnalysis().label(any(Collection.class), anyBoolean())).thenReturn(folder);
    dialog.createFolder.click();
    verify(service, atLeast(2)).validateSamples(eq(samples), eq(locale), any());
    verify(service).copySamplesResources(samples);
    verify(configuration.getAnalysis()).label(samples, false);
    assertEquals(resources.message(property(CONFIRM, "message"), folder),
        dialog.confirm.getElement().getProperty("message"));
    assertTrue(dialog.confirm.isOpened());
    assertFalse(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void createFolder_Unix() throws Throwable {
    String folder = "test/sample";
    when(configuration.getAnalysis().label(any(Collection.class), anyBoolean())).thenReturn(folder);
    dialog.createFolder.click();
    verify(service, atLeast(2)).validateSamples(eq(samples), eq(locale), any());
    verify(service).copySamplesResources(samples);
    verify(configuration.getAnalysis()).label(samples, true);
    assertEquals(resources.message(property(CONFIRM, "message"), folder),
        dialog.confirm.getElement().getProperty("message"));
    assertTrue(dialog.confirm.isOpened());
    assertFalse(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void createFolder_Mac() throws Throwable {
    String folder = "test/sample";
    when(configuration.getAnalysis().label(any(Collection.class), anyBoolean())).thenReturn(folder);
    dialog.createFolder.click();
    verify(service, atLeast(2)).validateSamples(eq(samples), eq(locale), any());
    verify(service).copySamplesResources(samples);
    verify(configuration.getAnalysis()).label(samples, true);
    assertEquals(resources.message(property(CONFIRM, "message"), folder),
        dialog.confirm.getElement().getProperty("message"));
    assertTrue(dialog.confirm.isOpened());
    assertFalse(dialog.errors.isOpened());
    assertTrue(dialog.isOpened());
  }

  @Test
  public void createFolder_IoException() throws Throwable {
    doThrow(new IOException("test")).when(service).copySamplesResources(any(Collection.class));
    dialog.createFolder.click();
    verify(service, atLeast(2)).validateSamples(eq(samples), eq(locale), any());
    verify(service).copySamplesResources(samples);
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
        .copySamplesResources(any(Collection.class));
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
    }).when(service).validateSamples(any(Collection.class), any(), any());
    dialog.createFolder.click();
    verify(service, atLeast(3)).validateSamples(eq(samples), eq(locale), any());
    verify(service).copySamplesResources(samples);
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
  public void setSample() {
    Sample sample = repository.findById(10L).get();
    dialog.setSample(sample);
    assertEquals(resources.message(HEADER, 1, sample.getName()), dialog.getHeaderTitle());
  }

  @Test
  public void setDatasets() {
    List<Sample> samples = new ArrayList<>();
    samples.add(repository.findById(4L).get());
    samples.add(repository.findById(5L).get());
    samples.add(repository.findById(10L).get());
    dialog.setSamples(samples);
    assertEquals(resources.message(HEADER, samples.size()), dialog.getHeaderTitle());
  }
}
