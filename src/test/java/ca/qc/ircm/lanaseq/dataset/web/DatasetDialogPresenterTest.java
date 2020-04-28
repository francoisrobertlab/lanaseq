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

import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetDialog.SAVED;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Assay;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.dataset.DatasetType;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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
public class DatasetDialogPresenterTest extends AbstractViewTestCase {
  private DatasetDialogPresenter presenter;
  @Mock
  private DatasetDialog dialog;
  @Mock
  private DatasetService service;
  @Mock
  private ProtocolService protocolService;
  @Captor
  private ArgumentCaptor<Dataset> datasetCaptor;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private ProtocolRepository protocolRepository;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(DatasetDialog.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<Protocol> protocols;
  private String name = "Test Dataset";
  private String project = "Test Project";
  private Protocol protocol;
  private Assay assay = Assay.CHIP_SEQ;
  private DatasetType type = DatasetType.IMMUNO_PRECIPITATION;

  /**
   * Before test.
   */
  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    presenter = new DatasetDialogPresenter(service, protocolService);
    dialog.header = new H3();
    dialog.name = new TextField();
    dialog.project = new TextField();
    dialog.protocol = new ComboBox<>();
    dialog.assay = new ComboBox<>();
    dialog.assay.setItems(Assay.values());
    dialog.type = new ComboBox<>();
    dialog.type.setItems(DatasetType.values());
    dialog.buttonsLayout = new HorizontalLayout();
    dialog.save = new Button();
    dialog.cancel = new Button();
    protocols = protocolRepository.findAll();
    protocol = protocolRepository.findById(1L).get();
    when(protocolService.all()).thenReturn(protocols);
    presenter.init(dialog);
  }

  private void fillForm() {
    dialog.name.setValue(name);
    dialog.project.setValue(project);
    dialog.protocol.setValue(protocol);
    dialog.assay.setValue(assay);
    dialog.type.setValue(type);
  }

  @Test
  public void getDataset() {
    Dataset dataset = new Dataset();
    presenter.setDataset(dataset);
    assertEquals(dataset, presenter.getDataset());
  }

  @Test
  public void setDataset_NewDataset() {
    Dataset dataset = new Dataset();

    presenter.localeChange(locale);
    presenter.setDataset(dataset);

    assertEquals("", dialog.name.getValue());
  }

  @Test
  public void setDataset_Dataset() {
    Dataset dataset = repository.findById(2L).get();

    presenter.localeChange(locale);
    presenter.setDataset(dataset);

    assertEquals(dataset.getName(), dialog.name.getValue());
  }

  @Test
  public void setDataset_BeforeLocaleChange() {
    Dataset dataset = repository.findById(2L).get();

    presenter.setDataset(dataset);
    presenter.localeChange(locale);

    assertEquals(dataset.getName(), dialog.name.getValue());
  }

  @Test
  public void setDataset_Null() {
    presenter.localeChange(locale);
    presenter.setDataset(null);

    assertEquals("", dialog.name.getValue());
  }

  @Test
  public void save_NameEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.name.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.name);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_ProjectEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.project.setValue("");

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getProject());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_AssayEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.assay.setValue(Assay.NULL);

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getAssay());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_TypeEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.type.setValue(DatasetType.NULL);

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertTrue(status.isOk());
    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertNull(dataset.getType());
    verify(dialog).showNotification(any());
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_ProtocolEmpty() {
    presenter.localeChange(locale);
    fillForm();
    dialog.protocol.setItems();

    presenter.save(locale);

    BinderValidationStatus<Dataset> status = presenter.validateDataset();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.protocol);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog, never()).close();
    verify(dialog, never()).fireSavedEvent();
  }

  @Test
  public void save_NewDataset() {
    presenter.localeChange(locale);
    fillForm();

    presenter.save(locale);

    verify(service).save(datasetCaptor.capture());
    Dataset dataset = datasetCaptor.getValue();
    assertEquals(name, dataset.getName());
    assertEquals(project, dataset.getProject());
    assertEquals(protocol.getId(), dataset.getProtocol().getId());
    assertEquals(assay, dataset.getAssay());
    assertEquals(type, dataset.getType());
    verify(dialog).showNotification(resources.message(SAVED, name));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void save_UpdateDataset() {
    Dataset dataset = repository.findById(2L).get();
    presenter.setDataset(dataset);
    presenter.localeChange(locale);
    fillForm();

    presenter.save(locale);

    verify(service).save(datasetCaptor.capture());
    dataset = datasetCaptor.getValue();
    assertEquals(name, dataset.getName());
    assertEquals(project, dataset.getProject());
    assertEquals(protocol.getId(), dataset.getProtocol().getId());
    assertEquals(assay, dataset.getAssay());
    assertEquals(type, dataset.getType());
    verify(dialog).showNotification(resources.message(SAVED, name));
    verify(dialog).close();
    verify(dialog).fireSavedEvent();
  }

  @Test
  public void cancel_Close() {
    presenter.init(dialog);
    presenter.localeChange(locale);

    presenter.cancel();

    verify(service, never()).save(any());
    verify(dialog, never()).showNotification(any());
    verify(dialog).close();
    verify(dialog, never()).fireSavedEvent();
  }
}
