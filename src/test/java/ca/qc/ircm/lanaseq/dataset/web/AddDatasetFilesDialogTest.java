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

import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.FILENAME;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.ID;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.NETWORK;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.OVERWRITE;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.SIZE;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.SIZE_VALUE;
import static ca.qc.ircm.lanaseq.dataset.web.AddDatasetFilesDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractKaribuTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.grid.HeaderRow.HeaderCell;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
@WithMockUser
public class AddDatasetFilesDialogTest extends AbstractKaribuTestCase {
  private AddDatasetFilesDialog dialog;
  @Mock
  private AddDatasetFilesDialogPresenter presenter;
  @Mock
  private Dataset dataset;
  @Captor
  private ArgumentCaptor<ValueProvider<File, String>> valueProviderCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Span, File>> spanRendererCaptor;
  @Captor
  private ArgumentCaptor<ComponentRenderer<Checkbox, File>> checkboxRendererCaptor;
  @Captor
  private ArgumentCaptor<Comparator<File>> comparatorCaptor;
  @Mock
  private ComponentEventListener<SavedEvent<AddDatasetFilesDialog>> savedListener;
  @Autowired
  private DatasetRepository repository;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(AddDatasetFilesDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<File> files = new ArrayList<>();
  private Random random = new Random();

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    ui.setLocale(locale);
    dialog = new AddDatasetFilesDialog(presenter);
    files.add(temporaryFolder.newFile("dataset_R1.fastq"));
    files.add(temporaryFolder.newFile("dataset_R2.fastq"));
    files.add(temporaryFolder.newFile("dataset.bw"));
    files.add(temporaryFolder.newFile("dataset.png"));
    for (File file : files) {
      writeFile(file.toPath(), random.nextInt(10) * 1024 ^ 2);
    }
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
  }

  private void writeFile(Path file, long size) throws IOException {
    try (OutputStream output =
        new BufferedOutputStream(Files.newOutputStream(file, StandardOpenOption.CREATE))) {
      long written = 0;
      byte[] buff = new byte[1024];
      while (written < size) {
        random.nextBytes(buff);
        output.write(buff);
        written += buff.length;
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void mockColumns() {
    Element filesElement = dialog.files.getElement();
    dialog.files = mock(Grid.class);
    when(dialog.files.getElement()).thenReturn(filesElement);
    dialog.filename = mock(Column.class);
    when(dialog.files.addColumn(any(ComponentRenderer.class), eq(FILENAME)))
        .thenReturn(dialog.filename);
    when(dialog.filename.setKey(any())).thenReturn(dialog.filename);
    when(dialog.filename.setComparator(any(Comparator.class))).thenReturn(dialog.filename);
    when(dialog.filename.setHeader(any(String.class))).thenReturn(dialog.filename);
    dialog.size = mock(Column.class);
    when(dialog.files.addColumn(any(ValueProvider.class), eq(SIZE))).thenReturn(dialog.size);
    when(dialog.size.setKey(any())).thenReturn(dialog.size);
    when(dialog.size.setComparator(any(Comparator.class))).thenReturn(dialog.size);
    when(dialog.size.setHeader(any(String.class))).thenReturn(dialog.size);
    dialog.overwrite = mock(Column.class);
    when(dialog.files.addColumn(any(ComponentRenderer.class), eq(OVERWRITE)))
        .thenReturn(dialog.overwrite);
    when(dialog.overwrite.setKey(any())).thenReturn(dialog.overwrite);
    when(dialog.overwrite.setSortable(anyBoolean())).thenReturn(dialog.overwrite);
    when(dialog.overwrite.setHeader(any(String.class))).thenReturn(dialog.overwrite);
    HeaderRow headerRow = mock(HeaderRow.class);
    when(dialog.files.appendHeaderRow()).thenReturn(headerRow);
    HeaderCell overwriteCell = mock(HeaderCell.class);
    when(headerRow.getCell(dialog.overwrite)).thenReturn(overwriteCell);
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
    assertEquals(id(NETWORK), dialog.network.getId().orElse(""));
    assertEquals(id(FILES), dialog.files.getId().orElse(""));
    assertEquals(id(SAVE), dialog.save.getId().orElse(""));
    assertTrue(dialog.save.hasThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName()));
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
  }

  @Test
  public void labels() {
    mockColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), dialog.header.getText());
    assertEquals("", dialog.message.getText());
    assertEquals("", dialog.network.getText());
    verify(dialog.filename).setHeader(resources.message(FILENAME));
    verify(dialog.size).setHeader(resources.message(SIZE));
    verify(dialog.overwrite).setHeader(resources.message(OVERWRITE));
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    verify(presenter, atLeastOnce()).localeChange(locale);
  }

  @Test
  public void localeChange() {
    mockColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(AddDatasetFilesDialog.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    ui.setLocale(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), dialog.header.getText());
    assertEquals("", dialog.message.getText());
    assertEquals("", dialog.network.getText());
    verify(dialog.filename).setHeader(resources.message(FILENAME));
    verify(dialog.size).setHeader(resources.message(SIZE));
    verify(dialog.overwrite).setHeader(resources.message(OVERWRITE));
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    verify(presenter, atLeastOnce()).localeChange(locale);
  }

  @Test
  public void files() {
    assertEquals(3, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
    assertNotNull(dialog.files.getColumnByKey(SIZE));
    assertNotNull(dialog.files.getColumnByKey(OVERWRITE));
  }

  @Test
  public void files_ColumnsValueProvider() throws Throwable {
    when(presenter.exists(any())).then(i -> {
      File file = i.getArgument(0);
      return files.get(0).equals(file);
    });
    mockColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    verify(dialog.files).addColumn(spanRendererCaptor.capture(), eq(FILENAME));
    ComponentRenderer<Span, File> spanRenderer = spanRendererCaptor.getValue();
    for (File file : files) {
      Span span = spanRenderer.createComponent(file);
      assertEquals(file.getName(), span.getText());
      if (presenter.exists(file)) {
        assertTrue(span.hasClassName(ERROR_TEXT));
      }
    }
    verify(dialog.filename).setComparator(comparatorCaptor.capture());
    Comparator<File> comparator = comparatorCaptor.getValue();
    assertTrue(comparator instanceof NormalizedComparator);
    for (File file : files) {
      assertEquals(file.getName(),
          ((NormalizedComparator<File>) comparator).getConverter().apply(file));
    }
    verify(dialog.files).addColumn(valueProviderCaptor.capture(), eq(SIZE));
    ValueProvider<File, String> valueProvider = valueProviderCaptor.getValue();
    for (File file : files) {
      assertEquals(resources.message(SIZE_VALUE, file.length() / 1048576),
          valueProvider.apply(file));
    }
    verify(dialog.files).addColumn(checkboxRendererCaptor.capture(), eq(OVERWRITE));
    ComponentRenderer<Checkbox, File> checkboxRenderer = checkboxRendererCaptor.getValue();
    for (File file : files) {
      Checkbox checkbox = checkboxRenderer.createComponent(file);
      assertNotNull(checkbox);
    }
  }

  @Test
  public void overwriteAll_True() {
    for (File file : files) {
      dialog.overwrite(file);
    }
    dialog.overwriteAll.setValue(true);
    ComponentValueChangeEvent<Checkbox, Boolean> event =
        new ComponentValueChangeEvent<>(dialog.overwriteAll, dialog.overwriteAll, false, true);
    fireEvent(dialog.overwriteAll, event);
    for (File file : files) {
      Checkbox checkbox = dialog.overwrite(file);
      assertTrue(checkbox.getValue());
    }
  }

  @Test
  public void overwriteAll_False() {
    for (File file : files) {
      Checkbox checkbox = dialog.overwrite(file);
      checkbox.setValue(true);
    }
    dialog.overwriteAll.setValue(true);
    ComponentValueChangeEvent<Checkbox, Boolean> event =
        new ComponentValueChangeEvent<>(dialog.overwriteAll, dialog.overwriteAll, false, true);
    fireEvent(dialog.overwriteAll, event);
    dialog.overwriteAll.setValue(false);
    event = new ComponentValueChangeEvent<>(dialog.overwriteAll, dialog.overwriteAll, true, true);
    fireEvent(dialog.overwriteAll, event);
    for (File file : files) {
      Checkbox checkbox = dialog.overwrite(file);
      assertFalse(checkbox.getValue());
    }
  }

  @Test
  public void overwriteAll_UncheckIfOneOverwiteUnchecked() {
    for (File file : files) {
      dialog.overwrite(file);
    }
    dialog.overwriteAll.setValue(true);
    ComponentValueChangeEvent<Checkbox, Boolean> event =
        new ComponentValueChangeEvent<>(dialog.overwriteAll, dialog.overwriteAll, false, true);
    fireEvent(dialog.overwriteAll, event);
    dialog.overwrite(files.get(0)).setValue(false);
    assertFalse(dialog.overwriteAll.getValue());
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
  public void getDataset() {
    when(presenter.getDataset()).thenReturn(dataset);
    assertEquals(dataset, dialog.getDataset());
    verify(presenter, atLeastOnce()).getDataset();
  }

  @Test
  public void setDataset_NewDataset() {
    Dataset dataset = new Dataset();
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(dataset);

    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER), dialog.header.getText());
  }

  @Test
  public void setDataset_NewDatasetWithName() {
    Dataset dataset = new Dataset();
    dataset.setName("my dataset");
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(dataset);

    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER, dataset.getName()), dialog.header.getText());
  }

  @Test
  public void setDataset_Dataset() {
    Dataset dataset = repository.findById(2L).get();
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(dataset);

    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER, dataset.getName()), dialog.header.getText());
  }

  @Test
  public void setDataset_BeforeLocaleChange() {
    Dataset dataset = repository.findById(2L).get();
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.setDataset(dataset);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER, dataset.getName()), dialog.header.getText());
  }

  @Test
  public void setDataset_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(null);

    verify(presenter).setDataset(null);
    assertEquals(resources.message(HEADER), dialog.header.getText());
  }

  @Test
  public void save() {
    clickButton(dialog.save);

    verify(presenter).save();
  }
}
