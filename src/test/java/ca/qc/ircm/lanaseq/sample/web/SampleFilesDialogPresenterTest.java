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

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.FILENAME_REGEX_ERROR;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.sample.web.SampleFilesDialog.SampleFile;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.editor.Editor;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class SampleFilesDialogPresenterTest extends AbstractViewTestCase {
  @Autowired
  private SampleFilesDialogPresenter presenter;
  @Mock
  private SampleFilesDialog dialog;
  @MockBean
  private SampleService service;
  @MockBean
  private ProtocolService protocolService;
  @MockBean
  private AuthorizationService authorizationService;
  @Captor
  private ArgumentCaptor<Sample> sampleCaptor;
  @Captor
  private ArgumentCaptor<ComponentEventListener<SavedEvent<AddSampleFilesDialog>>> savedListenerCaptor;
  @Autowired
  private SampleRepository repository;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SampleFilesDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Path> files = new ArrayList<>();
  private Random random = new Random();

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    dialog.header = new H3();
    dialog.files = new Grid<>();
    dialog.delete = dialog.files.addColumn(file -> file);
    dialog.filenameEdit = new TextField();
    dialog.addFilesDialog = mock(AddSampleFilesDialog.class);
    files.add(Paths.get("sample_R1.fastq"));
    files.add(Paths.get("sample_R2.fastq"));
    files.add(Paths.get("sample.bw"));
    files.add(Paths.get("sample.png"));
    when(service.files(any())).thenReturn(files);
    when(authorizationService.hasPermission(any(), any())).thenReturn(true);
    presenter.init(dialog);
    presenter.localeChange(locale);
  }

  @Test
  public void getSample() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    assertEquals(sample, presenter.getSample());
  }

  @Test(expected = NullPointerException.class)
  public void setSample_NewSample() {
    Sample sample = new Sample();

    presenter.setSample(sample);
  }

  @Test
  public void setSample_Sample() {
    Sample sample = repository.findById(1L).get();

    presenter.setSample(sample);

    verify(service).files(sample);
    List<SampleFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (SampleFile file : files) {
      assertTrue(this.files.contains(file.getPath()));
    }
    assertTrue(dialog.delete.isVisible());
    assertFalse(dialog.filenameEdit.isReadOnly());
    verify(dialog.addFilesDialog).setSample(sample);
  }

  @Test
  public void setSample_CannotWrite() {
    when(authorizationService.hasPermission(any(), any())).thenReturn(false);
    Sample sample = repository.findById(1L).get();

    presenter.setSample(sample);

    verify(service).files(sample);
    List<SampleFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (SampleFile file : files) {
      assertTrue(this.files.contains(file.getPath()));
    }
    assertFalse(dialog.delete.isVisible());
    assertTrue(dialog.filenameEdit.isReadOnly());
  }

  @Test
  public void setSample_NotEditable() {
    Sample sample = repository.findById(8L).get();

    presenter.setSample(sample);

    verify(service).files(sample);
    List<SampleFile> files = items(dialog.files);
    assertEquals(this.files.size(), files.size());
    for (SampleFile file : files) {
      assertTrue(this.files.contains(file.getPath()));
    }
    assertFalse(dialog.delete.isVisible());
    assertTrue(dialog.filenameEdit.isReadOnly());
  }

  @Test(expected = NullPointerException.class)
  public void setSample_Null() {
    presenter.setSample(null);
  }

  @Test
  public void filenameEdit_Empty() throws Throwable {
    dialog.filenameEdit.setValue("");

    BinderValidationStatus<SampleFile> status = presenter.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void filenameEdit_Invalid() throws Throwable {
    dialog.filenameEdit.setValue("abc?.txt");

    BinderValidationStatus<SampleFile> status = presenter.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(resources.message(FILENAME_REGEX_ERROR)), error.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void filenameEdit_Exists() throws Throwable {
    Path path = temporaryFolder.newFile("source.txt").toPath();
    temporaryFolder.newFile("abc.txt");
    SampleFile file = new SampleFile(path);
    dialog.files = mock(Grid.class);
    when(dialog.files.getEditor()).thenReturn(mock(Editor.class));
    when(dialog.files.getEditor().getItem()).thenReturn(file);

    dialog.filenameEdit.setValue("abc.txt");

    BinderValidationStatus<SampleFile> status = presenter.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(ALREADY_EXISTS)), error.getMessage());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void filenameEdit_ExistsSameFile() throws Throwable {
    temporaryFolder.newFile("abc.txt");
    dialog.files = mock(Grid.class);
    when(dialog.files.getEditor()).thenReturn(mock(Editor.class));
    when(dialog.files.getEditor().getItem()).thenReturn(null);

    dialog.filenameEdit.setValue("abc.txt");

    BinderValidationStatus<SampleFile> status = presenter.validateSampleFile();
    assertTrue(status.isOk());
  }

  @Test
  public void filenameEdit_ItemNull() throws Throwable {
    dialog.filenameEdit.setValue("");

    BinderValidationStatus<SampleFile> status = presenter.validateSampleFile();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.filenameEdit);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
  }

  @Test
  public void add() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);

    presenter.add();

    verify(dialog.addFilesDialog).open();
  }

  @Test
  public void add_CannotWrite() {
    when(authorizationService.hasPermission(any(), any())).thenReturn(false);
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);

    presenter.add();

    verify(dialog.addFilesDialog, never()).open();
  }

  @Test
  public void add_NotEditable() {
    Sample sample = repository.findById(8L).get();
    presenter.setSample(sample);

    presenter.add();

    verify(dialog.addFilesDialog, never()).open();
  }

  @Test
  public void add_NoSample() {
    presenter.add();

    verify(dialog.addFilesDialog, never()).open();
  }

  @Test
  public void add_Saved() {
    Sample sample = repository.findById(1L).get();
    presenter.setSample(sample);
    verify(dialog.addFilesDialog).addSavedListener(savedListenerCaptor.capture());

    savedListenerCaptor.getValue().onComponentEvent(new SavedEvent<>(dialog.addFilesDialog, false));

    verify(service, atLeast(2)).files(sample);
  }

  @Test
  public void rename() throws Throwable {
    Path path = temporaryFolder.newFile("source.txt").toPath();
    SampleFile file = new SampleFile(path);
    file.setFilename("target.txt");
    byte[] bytes = new byte[1024];
    random.nextBytes(bytes);
    Files.write(path, bytes);

    presenter.rename(file, locale);

    assertTrue(Files.exists(path.resolveSibling("target.txt")));
    assertArrayEquals(bytes, Files.readAllBytes(path.resolveSibling("target.txt")));
    assertFalse(Files.exists(path));
  }

  @Test
  public void deleteFile() throws Throwable {
    Path path = temporaryFolder.newFile("source.txt").toPath();
    SampleFile file = new SampleFile(path);
    byte[] bytes = new byte[1024];
    random.nextBytes(bytes);
    Files.write(path, bytes);

    presenter.deleteFile(file, locale);

    assertFalse(Files.exists(path));
  }
}
