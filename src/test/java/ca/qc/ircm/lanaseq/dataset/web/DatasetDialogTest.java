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

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.PRIMARY;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.ASSAY;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.PROJECT;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TARGET;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TYPE;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.HEADER;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Assay;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetType;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.List;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class DatasetDialogTest extends AbstractViewTestCase {
  private DatasetDialog dialog;
  @Mock
  private DatasetDialogPresenter presenter;
  @Mock
  private Dataset dataset;
  @Mock
  private ComponentEventListener<SavedEvent<DatasetDialog>> savedListener;
  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private ProtocolRepository protocolRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetDialog.class, locale);
  private AppResources datasetResources = new AppResources(Dataset.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    dialog = new DatasetDialog(presenter);
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
    assertEquals(id(NAME), dialog.name.getId().orElse(""));
    assertEquals(id(PROJECT), dialog.project.getId().orElse(""));
    assertEquals(id(PROTOCOL), dialog.protocol.getId().orElse(""));
    assertEquals(id(ASSAY), dialog.assay.getId().orElse(""));
    assertEquals(id(TYPE), dialog.type.getId().orElse(""));
    assertEquals(id(TARGET), dialog.target.getId().orElse(""));
    assertEquals(id(SAVE), dialog.save.getId().orElse(""));
    assertTrue(dialog.save.getThemeName().contains(PRIMARY));
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
    assertEquals(id(CANCEL), dialog.cancel.getId().orElse(""));
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel.getIcon());
  }

  @Test
  public void labels() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(datasetResources.message(NAME), dialog.name.getLabel());
    assertEquals(datasetResources.message(PROJECT), dialog.project.getLabel());
    assertEquals(datasetResources.message(PROTOCOL), dialog.protocol.getLabel());
    assertEquals(datasetResources.message(ASSAY), dialog.assay.getLabel());
    assertEquals(datasetResources.message(TYPE), dialog.type.getLabel());
    assertEquals(datasetResources.message(TARGET), dialog.target.getLabel());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    verify(presenter).localeChange(locale);
  }

  @Test
  public void localeChange() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(DatasetDialog.class, locale);
    final AppResources datasetResources = new AppResources(Dataset.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    when(ui.getLocale()).thenReturn(locale);
    dialog.localeChange(mock(LocaleChangeEvent.class));
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
    assertEquals(datasetResources.message(NAME), dialog.name.getLabel());
    assertEquals(datasetResources.message(PROJECT), dialog.project.getLabel());
    assertEquals(datasetResources.message(PROTOCOL), dialog.protocol.getLabel());
    assertEquals(datasetResources.message(ASSAY), dialog.assay.getLabel());
    assertEquals(datasetResources.message(TYPE), dialog.type.getLabel());
    assertEquals(datasetResources.message(TARGET), dialog.target.getLabel());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
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
    List<DatasetType> types = items(dialog.type);
    assertArrayEquals(DatasetType.values(), types.toArray(new DatasetType[0]));
    for (DatasetType type : types) {
      assertEquals(type.getLabel(locale), dialog.type.getItemLabelGenerator().apply(type));
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
  public void getDataset() {
    when(presenter.getDataset()).thenReturn(dataset);
    assertEquals(dataset, dialog.getDataset());
    verify(presenter).getDataset();
  }

  @Test
  public void setDataset_NewDataset() {
    Dataset dataset = new Dataset();
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(dataset);

    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER, 0), dialog.header.getText());
  }

  @Test
  public void setDataset_Dataset() {
    Dataset dataset = datasetRepository.findById(2L).get();
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(dataset);

    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER, 1, dataset.getName()), dialog.header.getText());
  }

  @Test
  public void setDataset_BeforeLocaleChange() {
    Dataset dataset = datasetRepository.findById(2L).get();
    when(presenter.getDataset()).thenReturn(dataset);

    dialog.setDataset(dataset);
    dialog.localeChange(mock(LocaleChangeEvent.class));

    verify(presenter).setDataset(dataset);
    assertEquals(resources.message(HEADER, 1, dataset.getName()), dialog.header.getText());
  }

  @Test
  public void setDataset_Null() {
    dialog.localeChange(mock(LocaleChangeEvent.class));
    dialog.setDataset(null);

    verify(presenter).setDataset(null);
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
