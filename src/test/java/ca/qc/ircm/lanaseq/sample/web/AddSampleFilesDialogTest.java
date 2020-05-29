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

import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.FILENAME;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.FILES;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.HEADER;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.ID;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.NETWORK;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.SIZE;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.SIZE_VALUE;
import static ca.qc.ircm.lanaseq.sample.web.AddSampleFilesDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.io.BufferedOutputStream;
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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class AddSampleFilesDialogTest extends AbstractViewTestCase {
  private AddSampleFilesDialog dialog;
  @Mock
  private AddSampleFilesDialogPresenter presenter;
  @Mock
  private Sample sample;
  @Captor
  private ArgumentCaptor<ValueProvider<Path, String>> valueProviderCaptor;
  @Mock
  private ComponentEventListener<SavedEvent<AddSampleFilesDialog>> savedListener;
  @Autowired
  private SampleRepository sampleRepository;
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(AddSampleFilesDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Path> files = new ArrayList<>();
  private Random random = new Random();

  /**
   * Before test.
   */
  @Before
  public void beforeTest() throws Throwable {
    when(ui.getLocale()).thenReturn(locale);
    dialog = new AddSampleFilesDialog(presenter);
    files.add(temporaryFolder.getRoot().toPath().resolve("sample_R1.fastq"));
    files.add(temporaryFolder.getRoot().toPath().resolve("sample_R2.fastq"));
    files.add(temporaryFolder.getRoot().toPath().resolve("sample.bw"));
    files.add(temporaryFolder.getRoot().toPath().resolve("sample.png"));
    for (Path file : files) {
      writeFile(temporaryFolder.getRoot().toPath().resolve(file), random.nextInt(10) * 1024 ^ 2);
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
    when(dialog.files.addColumn(any(ValueProvider.class), eq(FILENAME)))
        .thenReturn(dialog.filename);
    when(dialog.filename.setKey(any())).thenReturn(dialog.filename);
    when(dialog.filename.setComparator(any(Comparator.class))).thenReturn(dialog.filename);
    when(dialog.filename.setHeader(any(String.class))).thenReturn(dialog.filename);
    dialog.size = mock(Column.class);
    when(dialog.files.addColumn(any(ValueProvider.class), eq(SIZE))).thenReturn(dialog.size);
    when(dialog.size.setKey(any())).thenReturn(dialog.size);
    when(dialog.size.setComparator(any(Comparator.class))).thenReturn(dialog.size);
    when(dialog.size.setHeader(any(String.class))).thenReturn(dialog.size);
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
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    verify(presenter, atLeastOnce()).localeChange(locale);
  }

  @Test
  public void localeChange() {
    mockColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(AddSampleFilesDialog.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER), dialog.header.getText());
    assertEquals("", dialog.message.getText());
    assertEquals("", dialog.network.getText());
    verify(dialog.filename).setHeader(resources.message(FILENAME));
    verify(dialog.size).setHeader(resources.message(SIZE));
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    verify(presenter, atLeastOnce()).localeChange(locale);
  }

  @Test
  public void files() {
    assertEquals(2, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
    assertNotNull(dialog.files.getColumnByKey(SIZE));
  }

  @Test
  public void files_ColumnsValueProvider() throws Throwable {
    mockColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    verify(dialog.files).addColumn(valueProviderCaptor.capture(), eq(FILENAME));
    ValueProvider<Path, String> valueProvider = valueProviderCaptor.getValue();
    for (Path file : files) {
      assertEquals(file.getFileName().toString(), valueProvider.apply(file));
    }
    verify(dialog.files).addColumn(valueProviderCaptor.capture(), eq(SIZE));
    valueProvider = valueProviderCaptor.getValue();
    for (Path file : files) {
      assertEquals(
          resources.message(SIZE_VALUE,
              Files.size(temporaryFolder.getRoot().toPath().resolve(file)) / 1048576),
          valueProvider.apply(file));
    }
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
  public void getSample() {
    when(presenter.getSample()).thenReturn(sample);
    assertEquals(sample, dialog.getSample());
    verify(presenter, atLeastOnce()).getSample();
  }

  @Test
  public void setSample_NewSample() {
    Sample sample = new Sample();
    when(presenter.getSample()).thenReturn(sample);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setSample(sample);

    verify(presenter).setSample(sample, locale);
    assertEquals(resources.message(HEADER), dialog.header.getText());
  }

  @Test
  public void setSample_NewSampleWithName() {
    Sample sample = new Sample();
    sample.setName("my sample");
    when(presenter.getSample()).thenReturn(sample);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setSample(sample);

    verify(presenter).setSample(sample, locale);
    assertEquals(resources.message(HEADER, sample.getName()), dialog.header.getText());
  }

  @Test
  public void setSample_Sample() {
    Sample sample = sampleRepository.findById(2L).get();
    when(presenter.getSample()).thenReturn(sample);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setSample(sample);

    verify(presenter).setSample(sample, locale);
    assertEquals(resources.message(HEADER, sample.getName()), dialog.header.getText());
  }

  @Test
  public void setSample_BeforeLocaleChange() {
    Sample sample = sampleRepository.findById(2L).get();
    when(presenter.getSample()).thenReturn(sample);

    dialog.setSample(sample);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(presenter).setSample(sample, locale);
    assertEquals(resources.message(HEADER, sample.getName()), dialog.header.getText());
  }

  @Test
  public void setSample_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setSample(null);

    verify(presenter).setSample(null, locale);
    assertEquals(resources.message(HEADER), dialog.header.getText());
  }

  @Test
  public void save() {
    clickButton(dialog.save);

    verify(presenter).save(locale);
  }
}
