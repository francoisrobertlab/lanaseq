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

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.PLACEHOLDER;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ASSAY;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.REPLICATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.SAMPLE_ID;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN_DESCRIPTION;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TARGET;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TREATMENT;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TYPE;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.FILENAME;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.FILES;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.HEADER;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.ID;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.sample.Assay;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
public class SampleDialogTest extends AbstractViewTestCase {
  private SampleDialog dialog;
  @Mock
  private SampleDialogPresenter presenter;
  @Mock
  private Sample sample;
  @Captor
  private ArgumentCaptor<ValueProvider<Path, String>> valueProviderCaptor;
  @Mock
  private ComponentEventListener<SavedEvent<SampleDialog>> savedListener;
  @Mock
  private ComponentEventListener<DeletedEvent<SampleDialog>> deletedListener;
  @Autowired
  private SampleRepository sampleRepository;
  @Autowired
  private ProtocolRepository protocolRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SampleDialog.class, locale);
  private AppResources sampleResources = new AppResources(Sample.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Path> files = new ArrayList<>();

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    dialog = new SampleDialog(presenter);
    dialog.init();
    files.add(Paths.get("sample_R1.fastq"));
    files.add(Paths.get("sample_R2.fastq"));
    files.add(Paths.get("sample.bw"));
    files.add(Paths.get("sample.png"));
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
  }

  @Test
  public void presenter_Init() {
    verify(presenter).init(dialog);
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(HEADER), dialog.header.getId().orElse(""));
    assertEquals(id(SAMPLE_ID), dialog.sampleId.getId().orElse(""));
    assertEquals(id(REPLICATE), dialog.replicate.getId().orElse(""));
    assertEquals(id(PROTOCOL), dialog.protocol.getId().orElse(""));
    assertEquals(id(ASSAY), dialog.assay.getId().orElse(""));
    assertEquals(id(TYPE), dialog.type.getId().orElse(""));
    assertEquals(id(TARGET), dialog.target.getId().orElse(""));
    assertEquals(id(STRAIN), dialog.strain.getId().orElse(""));
    assertEquals(id(STRAIN_DESCRIPTION), dialog.strainDescription.getId().orElse(""));
    assertEquals(id(TREATMENT), dialog.treatment.getId().orElse(""));
    assertEquals(id(FILES), dialog.files.getId().orElse(""));
    assertEquals(id(SAVE), dialog.save.getId().orElse(""));
    assertTrue(dialog.save.hasThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName()));
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
    assertEquals(id(CANCEL), dialog.cancel.getId().orElse(""));
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel.getIcon());
    assertEquals(id(DELETE), dialog.delete.getId().orElse(""));
    assertTrue(dialog.delete.hasThemeName(ButtonVariant.LUMO_ERROR.getVariantName()));
    validateIcon(VaadinIcon.TRASH.create(), dialog.delete.getIcon());
  }

  @Test
  public void labels() {
    mockColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(sampleResources.message(SAMPLE_ID), dialog.sampleId.getLabel());
    assertEquals(sampleResources.message(REPLICATE), dialog.replicate.getLabel());
    assertEquals(sampleResources.message(PROTOCOL), dialog.protocol.getLabel());
    assertEquals(sampleResources.message(ASSAY), dialog.assay.getLabel());
    assertEquals(sampleResources.message(TYPE), dialog.type.getLabel());
    assertEquals(sampleResources.message(TARGET), dialog.target.getLabel());
    assertEquals(sampleResources.message(property(TARGET, PLACEHOLDER)),
        dialog.target.getPlaceholder());
    assertEquals(sampleResources.message(STRAIN), dialog.strain.getLabel());
    assertEquals(sampleResources.message(property(STRAIN, PLACEHOLDER)),
        dialog.strain.getPlaceholder());
    assertEquals(sampleResources.message(STRAIN_DESCRIPTION), dialog.strainDescription.getLabel());
    assertEquals(sampleResources.message(property(STRAIN_DESCRIPTION, PLACEHOLDER)),
        dialog.strainDescription.getPlaceholder());
    assertEquals(sampleResources.message(TREATMENT), dialog.treatment.getLabel());
    assertEquals(sampleResources.message(property(TREATMENT, PLACEHOLDER)),
        dialog.treatment.getPlaceholder());
    verify(dialog.filename).setHeader(resources.message(FILENAME));
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    assertEquals(webResources.message(DELETE), dialog.delete.getText());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    mockColumns();
    dialog.init();
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(SampleDialog.class, locale);
    final AppResources sampleResources = new AppResources(Sample.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(sampleResources.message(SAMPLE_ID), dialog.sampleId.getLabel());
    assertEquals(sampleResources.message(REPLICATE), dialog.replicate.getLabel());
    assertEquals(sampleResources.message(PROTOCOL), dialog.protocol.getLabel());
    assertEquals(sampleResources.message(ASSAY), dialog.assay.getLabel());
    assertEquals(sampleResources.message(TYPE), dialog.type.getLabel());
    assertEquals(sampleResources.message(TARGET), dialog.target.getLabel());
    assertEquals(sampleResources.message(property(TARGET, PLACEHOLDER)),
        dialog.target.getPlaceholder());
    assertEquals(sampleResources.message(STRAIN), dialog.strain.getLabel());
    assertEquals(sampleResources.message(property(STRAIN, PLACEHOLDER)),
        dialog.strain.getPlaceholder());
    assertEquals(sampleResources.message(STRAIN_DESCRIPTION), dialog.strainDescription.getLabel());
    assertEquals(sampleResources.message(property(STRAIN_DESCRIPTION, PLACEHOLDER)),
        dialog.strainDescription.getPlaceholder());
    assertEquals(sampleResources.message(TREATMENT), dialog.treatment.getLabel());
    assertEquals(sampleResources.message(property(TREATMENT, PLACEHOLDER)),
        dialog.treatment.getPlaceholder());
    verify(dialog.filename).setHeader(resources.message(FILENAME));
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    assertEquals(webResources.message(DELETE), dialog.delete.getText());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void protocol() {
    for (Protocol protocol : protocolRepository.findAll()) {
      assertEquals(protocol.getName(), dialog.protocol.getItemLabelGenerator().apply(protocol));
    }
  }

  @Test
  public void assay() {
    List<Assay> assays = items(dialog.assay);
    assertArrayEquals(Assay.values(), assays.toArray(new Assay[0]));
    for (Assay assay : assays) {
      assertEquals(assay.getLabel(locale), dialog.assay.getItemLabelGenerator().apply(assay));
    }
  }

  @Test
  public void type() {
    List<SampleType> types = items(dialog.type);
    assertArrayEquals(SampleType.values(), types.toArray(new SampleType[0]));
    for (SampleType type : types) {
      assertEquals(type.getLabel(locale), dialog.type.getItemLabelGenerator().apply(type));
    }
  }

  @Test
  public void files() {
    assertEquals(1, dialog.files.getColumns().size());
    assertNotNull(dialog.files.getColumnByKey(FILENAME));
  }

  @Test
  public void files_ColumnsValueProvider() {
    mockColumns();
    dialog.init();
    verify(dialog.files).addColumn(valueProviderCaptor.capture(), eq(FILENAME));
    ValueProvider<Path, String> valueProvider = valueProviderCaptor.getValue();
    for (Path file : files) {
      assertEquals(file.getFileName().toString(), valueProvider.apply(file));
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
  public void deletedListener() {
    dialog.addDeletedListener(deletedListener);
    dialog.fireDeletedEvent();
    verify(deletedListener).onComponentEvent(any());
  }

  @Test
  public void deletedListener_Remove() {
    dialog.addDeletedListener(deletedListener).remove();
    dialog.fireDeletedEvent();
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void getSample() {
    when(presenter.getSample()).thenReturn(sample);
    assertEquals(sample, dialog.getSample());
    verify(presenter).getSample();
  }

  @Test
  public void setSample_NewSample() {
    Sample sample = new Sample();
    when(presenter.getSample()).thenReturn(sample);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setSample(sample);

    verify(presenter).setSample(sample);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void setSample_NewSampleWithName() {
    Sample sample = new Sample();
    sample.setSampleId("my sample");
    when(presenter.getSample()).thenReturn(sample);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setSample(sample);

    verify(presenter).setSample(sample);
    assertEquals(resources.message(HEADER, 1, sample.getSampleId()), dialog.header.getText());
  }

  @Test
  public void setSample_Sample() {
    Sample sample = sampleRepository.findById(2L).get();
    when(presenter.getSample()).thenReturn(sample);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setSample(sample);

    verify(presenter).setSample(sample);
    assertEquals(resources.message(HEADER, 1, sample.getSampleId()), dialog.header.getText());
  }

  @Test
  public void setSample_BeforeLocaleChange() {
    Sample sample = sampleRepository.findById(2L).get();
    when(presenter.getSample()).thenReturn(sample);

    dialog.setSample(sample);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(presenter).setSample(sample);
    assertEquals(resources.message(HEADER, 1, sample.getSampleId()), dialog.header.getText());
  }

  @Test
  public void setSample_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setSample(null);

    verify(presenter).setSample(null);
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

  @Test
  public void delete() {
    clickButton(dialog.delete);

    verify(presenter).delete(locale);
  }
}
