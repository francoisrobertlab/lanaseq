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
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsAnalysisDialog.CREATE_FOLDER_EXCEPTION;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.analysis.AnalysisService;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.test.config.UserAgent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog.ConfirmEvent;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.GeneratedVaadinDialog.OpenedChangeEvent;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;

/**
 * Tests for {@link DatasetsAnalysisDialogPresenter}.
 */
@ServiceTestAnnotations
@WithMockUser
public class DatasetsAnalysisDialogPresenterTest extends AbstractKaribuTestCase {
  @Autowired
  private DatasetsAnalysisDialogPresenter presenter;
  @Mock
  private DatasetsAnalysisDialog dialog;
  @MockBean
  private AnalysisService service;
  @MockBean
  private AppConfiguration configuration;
  @Captor
  private ArgumentCaptor<Component> componentCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<OpenedChangeEvent<Dialog>>> openListenerCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<ConfirmEvent>> confirmListenerCaptor;
  @Autowired
  private DatasetRepository datasetRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetsAnalysisDialog.class, locale);
  private List<Dataset> datasets = new ArrayList<>();

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(dialog.getUI()).thenReturn(Optional.of(ui));
    datasets.add(datasetRepository.findById(2L).get());
    datasets.add(datasetRepository.findById(7L).get());
    dialog.header = new H3();
    dialog.message = new Div();
    dialog.createFolder = new Button();
    dialog.confirm = mock(ConfirmDialog.class);
    dialog.errors = mock(ConfirmDialog.class);
    dialog.errorsLayout = new VerticalLayout();
    presenter.init(dialog);
    presenter.localChange(locale);
    presenter.setDatasets(datasets);
  }

  @Test
  public void validate_Success() {
    presenter.validate();
    verify(service).validateDatasets(eq(datasets), eq(locale), any());
    assertEquals(0, dialog.errorsLayout.getComponentCount());
    verify(dialog.errors, never()).open();
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
    presenter.validate();
    verify(service).validateDatasets(eq(datasets), eq(locale), any());
    assertEquals(2, dialog.errorsLayout.getComponentCount());
    assertTrue(dialog.errorsLayout.getComponentAt(0) instanceof Span);
    assertEquals("error1", ((Span) dialog.errorsLayout.getComponentAt(0)).getText());
    assertTrue(dialog.errorsLayout.getComponentAt(1) instanceof Span);
    assertEquals("error2", ((Span) dialog.errorsLayout.getComponentAt(1)).getText());
    verify(dialog.errors).open();
    assertFalse(dialog.createFolder.isEnabled());
  }

  @Test
  public void validate_MultipleCallsWithErrors() {
    doAnswer(i -> {
      Consumer<String> errorHandler = i.getArgument(2);
      errorHandler.accept("error1");
      errorHandler.accept("error2");
      return null;
    }).when(service).validateDatasets(any(Collection.class), any(), any());
    presenter.validate();
    presenter.validate();
    verify(service, times(2)).validateDatasets(eq(datasets), eq(locale), any());
    assertEquals(2, dialog.errorsLayout.getComponentCount());
    assertTrue(dialog.errorsLayout.getComponentAt(0) instanceof Span);
    assertEquals("error1", ((Span) dialog.errorsLayout.getComponentAt(0)).getText());
    assertTrue(dialog.errorsLayout.getComponentAt(1) instanceof Span);
    assertEquals("error2", ((Span) dialog.errorsLayout.getComponentAt(1)).getText());
    verify(dialog.errors, times(2)).open();
    assertFalse(dialog.createFolder.isEnabled());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void validate_OnOpen() {
    OpenedChangeEvent<Dialog> event = mock(OpenedChangeEvent.class);
    when(event.isOpened()).thenReturn(true);
    verify(dialog).addOpenedChangeListener(openListenerCaptor.capture());
    openListenerCaptor.getValue().onComponentEvent(event);
    verify(service).validateDatasets(eq(datasets), eq(locale), any());
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void createFolder_Windows() throws Throwable {
    String folder = "smb://test/dataset";
    when(configuration.datasetAnalysisLabel(any(Collection.class), anyBoolean()))
        .thenReturn(folder);
    presenter.createFolder();
    verify(service).validateDatasets(eq(datasets), eq(locale), any());
    verify(service).copyDatasetsResources(datasets);
    verify(configuration).datasetAnalysisLabel(datasets, false);
    verify(dialog.confirm).setText(resources.message(property(CONFIRM, "message"), folder));
    verify(dialog.confirm).open();
    verify(dialog.errors, never()).open();
    verify(dialog, never()).close();
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_LINUX_USER_AGENT)
  public void createFolder_Unix() throws Throwable {
    String folder = "smb://test/dataset";
    when(configuration.datasetAnalysisLabel(any(Collection.class), anyBoolean()))
        .thenReturn(folder);
    presenter.createFolder();
    verify(service).validateDatasets(eq(datasets), eq(locale), any());
    verify(service).copyDatasetsResources(datasets);
    verify(configuration).datasetAnalysisLabel(datasets, true);
    verify(dialog.confirm).setText(resources.message(property(CONFIRM, "message"), folder));
    verify(dialog.confirm).open();
    verify(dialog.errors, never()).open();
    verify(dialog, never()).close();
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_MACOSX_USER_AGENT)
  public void createFolder_Mac() throws Throwable {
    String folder = "smb://test/dataset";
    when(configuration.datasetAnalysisLabel(any(Collection.class), anyBoolean()))
        .thenReturn(folder);
    presenter.createFolder();
    verify(service).validateDatasets(eq(datasets), eq(locale), any());
    verify(service).copyDatasetsResources(datasets);
    verify(configuration).datasetAnalysisLabel(datasets, true);
    verify(dialog.confirm).setText(resources.message(property(CONFIRM, "message"), folder));
    verify(dialog.confirm).open();
    verify(dialog.errors, never()).open();
    verify(dialog, never()).close();
  }

  @Test
  @UserAgent(UserAgent.FIREFOX_WINDOWS_USER_AGENT)
  public void createFolder_MultipleCalls() throws Throwable {
    String folder = "smb://test/dataset";
    when(configuration.datasetAnalysisLabel(any(Collection.class), anyBoolean()))
        .thenReturn(folder);
    presenter.createFolder();
    presenter.createFolder();
    verify(service, times(2)).validateDatasets(eq(datasets), eq(locale), any());
    verify(service, times(2)).copyDatasetsResources(datasets);
    verify(configuration, times(2)).datasetAnalysisLabel(datasets, false);
    verify(dialog.confirm, times(2))
        .setText(resources.message(property(CONFIRM, "message"), folder));
    verify(dialog.confirm, times(2)).open();
    verify(dialog.errors, never()).open();
    verify(dialog, never()).close();
  }

  @Test
  public void createFolder_IoException() throws Throwable {
    doThrow(new IOException("test")).when(service).copyDatasetsResources(any(Collection.class));
    presenter.createFolder();
    verify(service).validateDatasets(eq(datasets), eq(locale), any());
    verify(service).copyDatasetsResources(datasets);
    verify(dialog.confirm, never()).open();
    assertEquals(1, dialog.errorsLayout.getComponentCount());
    assertTrue(dialog.errorsLayout.getComponentAt(0) instanceof Span);
    assertEquals(resources.message(CREATE_FOLDER_EXCEPTION),
        ((Span) dialog.errorsLayout.getComponentAt(0)).getText());
    verify(dialog.errors).open();
    verify(dialog, never()).close();
  }

  @Test
  public void createFolder_IllegalArgumentException() throws Throwable {
    doThrow(new IllegalArgumentException("test")).when(service)
        .copyDatasetsResources(any(Collection.class));
    doAnswer(new Answer<Void>() {
      private int calls = 0;

      @Override
      public Void answer(InvocationOnMock invocation) throws Throwable {
        if (calls > 0) {
          Consumer<String> errorHandler = invocation.getArgument(2);
          errorHandler.accept("error1");
        }
        calls++;
        return null;
      }
    }).when(service).validateDatasets(any(Collection.class), any(), any());
    presenter.createFolder();
    verify(service, times(2)).validateDatasets(eq(datasets), eq(locale), any());
    verify(service).copyDatasetsResources(datasets);
    verify(dialog.confirm, never()).open();
    assertEquals(1, dialog.errorsLayout.getComponentCount());
    assertTrue(dialog.errorsLayout.getComponentAt(0) instanceof Span);
    assertEquals("error1", ((Span) dialog.errorsLayout.getComponentAt(0)).getText());
    verify(dialog.errors).open();
    verify(dialog, never()).close();
  }

  @Test
  public void getDatasets() {
    assertEquals(datasets, presenter.getDatasets());
  }

  @Test
  public void getDatasets_OneDataset() {
    Dataset dataset = datasets.get(0);
    presenter.setDataset(dataset);
    assertEquals(Arrays.asList(dataset), presenter.getDatasets());
  }
}
