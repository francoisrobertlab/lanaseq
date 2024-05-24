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
import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.PLACEHOLDER;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.sample.QSample.sample;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.ASSAY;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NOTE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.REPLICATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.SAMPLE_ID;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.STRAIN_DESCRIPTION;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TARGET;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TREATMENT;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.TYPE;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.DELETED;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.DELETE_HEADER;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.DELETE_MESSAGE;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.HEADER;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.ID;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.SAVED;
import static ca.qc.ircm.lanaseq.sample.web.SampleDialog.id;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateEquals;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.web.DatePickerInternationalization.englishDatePickerI18n;
import static ca.qc.ircm.lanaseq.web.DatePickerInternationalization.frenchDatePickerI18n;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.ProtocolRepository;
import ca.qc.ircm.lanaseq.protocol.ProtocolService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.DeletedEvent;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.validation.ValidationLogger;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBoxBase;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog.CancelEvent;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog.ConfirmEvent;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link SampleDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SampleDialogTest extends SpringUIUnitTest {
  private SampleDialog dialog;
  @MockBean
  private SampleService service;
  @MockBean
  private ProtocolService protocolService;
  @Autowired
  private AuthenticatedUser authenticatedUser;
  @Captor
  private ArgumentCaptor<Sample> sampleCaptor;
  @Mock
  private ComponentEventListener<SavedEvent<SampleDialog>> savedListener;
  @Mock
  private ComponentEventListener<DeletedEvent<SampleDialog>> deletedListener;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private ProtocolRepository protocolRepository;
  @Autowired
  private JPAQueryFactory jpaQueryFactory;
  private Locale locale = Locale.ENGLISH;
  private AppResources resources = new AppResources(SampleDialog.class, locale);
  private AppResources sampleResources = new AppResources(Sample.class, locale);
  private AppResources webResources = new AppResources(Constants.class, locale);
  private List<String> topAssays;
  private String sampleId = "Test Sample";
  private String replicate = "Test Replicate";
  private Protocol protocol;
  private String assay = "ChIP-seq";
  private SampleType type = SampleType.IMMUNO_PRECIPITATION;
  private String target = "polr3a";
  private String strain = "yFR20";
  private String strainDescription = "WT";
  private String treatment = "37C";
  private LocalDate date = LocalDate.of(2020, 7, 20);
  private String note = "test note\nsecond line";

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(service.get(anyLong())).then(
        i -> i.getArgument(0) != null ? repository.findById(i.getArgument(0)) : Optional.empty());
    when(service.all(any())).thenReturn(repository.findAll());
    topAssays = jpaQueryFactory.select(sample.assay).from(sample).fetch();
    when(service.topAssays(anyInt())).thenReturn(topAssays);
    when(protocolService.all()).thenReturn(protocolRepository.findAll());
    protocol = protocolRepository.findById(1L).get();
    UI.getCurrent().setLocale(locale);
    SamplesView view = navigate(SamplesView.class);
    view.samples.setItems(repository.findAll());
    test(view.samples).doubleClickRow(1);
    dialog = $(SampleDialog.class).first();
  }

  private void fillForm() {
    dialog.date.setValue(date);
    dialog.sampleId.setValue(sampleId);
    dialog.replicate.setValue(replicate);
    dialog.protocol.setValue(protocol);
    dialog.assay.setValue(assay);
    dialog.type.setValue(type);
    dialog.target.setValue(target);
    dialog.strain.setValue(strain);
    dialog.strainDescription.setValue(strainDescription);
    dialog.treatment.setValue(treatment);
    dialog.note.setValue(note);
  }

  @Test
  public void styles() {
    assertEquals(ID, dialog.getId().orElse(""));
    assertEquals(id(DATE), dialog.date.getId().orElse(""));
    assertEquals(id(SAMPLE_ID), dialog.sampleId.getId().orElse(""));
    assertEquals(id(REPLICATE), dialog.replicate.getId().orElse(""));
    assertEquals(id(PROTOCOL), dialog.protocol.getId().orElse(""));
    assertEquals(id(ASSAY), dialog.assay.getId().orElse(""));
    assertEquals(id(TYPE), dialog.type.getId().orElse(""));
    assertEquals(id(TARGET), dialog.target.getId().orElse(""));
    assertEquals(id(STRAIN), dialog.strain.getId().orElse(""));
    assertEquals(id(STRAIN_DESCRIPTION), dialog.strainDescription.getId().orElse(""));
    assertEquals(id(TREATMENT), dialog.treatment.getId().orElse(""));
    assertEquals(id(NOTE), dialog.note.getId().orElse(""));
    assertEquals(id(ERROR_TEXT), dialog.error.getId().orElse(""));
    assertFalse(dialog.error.isVisible());
    assertEquals(id(SAVE), dialog.save.getId().orElse(""));
    assertTrue(dialog.save.hasThemeName(ButtonVariant.LUMO_PRIMARY.getVariantName()));
    validateIcon(VaadinIcon.CHECK.create(), dialog.save.getIcon());
    assertEquals(id(CANCEL), dialog.cancel.getId().orElse(""));
    validateIcon(VaadinIcon.CLOSE.create(), dialog.cancel.getIcon());
    assertEquals(id(DELETE), dialog.delete.getId().orElse(""));
    assertEquals("auto", dialog.delete.getStyle().get("margin-inline-end"));
    assertTrue(dialog.delete.hasThemeName(ButtonVariant.LUMO_ERROR.getVariantName()));
    validateIcon(VaadinIcon.TRASH.create(), dialog.delete.getIcon());
    assertEquals(id(CONFIRM), dialog.confirm.getId().orElse(""));
    assertEquals("true", dialog.confirm.getElement().getProperty("cancelButtonVisible"));
    assertTrue(dialog.confirm.getElement().getProperty("confirmTheme")
        .contains(ButtonVariant.LUMO_ERROR.getVariantName()));
    assertTrue(dialog.confirm.getElement().getProperty("confirmTheme")
        .contains(ButtonVariant.LUMO_PRIMARY.getVariantName()));
  }

  @Test
  public void labels() {
    Sample sample = repository.findById(dialog.getSampleId()).get();
    assertEquals(resources.message(HEADER, 1, sample.getName()), dialog.getHeaderTitle());
    assertEquals(sampleResources.message(DATE), dialog.date.getLabel());
    validateEquals(englishDatePickerI18n(), dialog.date.getI18n());
    assertEquals(Locale.CANADA, dialog.date.getLocale());
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
    assertEquals(sampleResources.message(NOTE), dialog.note.getLabel());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    assertEquals(webResources.message(DELETE), dialog.delete.getText());
    assertEquals(resources.message(DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(webResources.message(DELETE),
        dialog.confirm.getElement().getProperty("confirmText"));
    assertEquals(webResources.message(CANCEL),
        dialog.confirm.getElement().getProperty("cancelText"));
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    final AppResources resources = new AppResources(SampleDialog.class, locale);
    final AppResources sampleResources = new AppResources(Sample.class, locale);
    final AppResources webResources = new AppResources(Constants.class, locale);
    UI.getCurrent().setLocale(locale);
    Sample sample = repository.findById(dialog.getSampleId()).get();
    assertEquals(resources.message(HEADER, 1, sample.getName()), dialog.getHeaderTitle());
    assertEquals(sampleResources.message(DATE), dialog.date.getLabel());
    validateEquals(frenchDatePickerI18n(), dialog.date.getI18n());
    assertEquals(Locale.CANADA, dialog.date.getLocale());
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
    assertEquals(sampleResources.message(NOTE), dialog.note.getLabel());
    assertEquals(webResources.message(SAVE), dialog.save.getText());
    assertEquals(webResources.message(CANCEL), dialog.cancel.getText());
    assertEquals(webResources.message(DELETE), dialog.delete.getText());
    assertEquals(resources.message(DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(webResources.message(DELETE),
        dialog.confirm.getElement().getProperty("confirmText"));
    assertEquals(webResources.message(CANCEL),
        dialog.confirm.getElement().getProperty("cancelText"));
  }

  @Test
  public void requiredIndicator() {
    assertTrue(dialog.date.isRequiredIndicatorVisible());
    assertTrue(dialog.sampleId.isRequiredIndicatorVisible());
    assertTrue(dialog.replicate.isRequiredIndicatorVisible());
    assertTrue(dialog.protocol.isRequiredIndicatorVisible());
    assertTrue(dialog.assay.isRequiredIndicatorVisible());
    assertFalse(dialog.type.isRequiredIndicatorVisible());
    assertFalse(dialog.target.isRequiredIndicatorVisible());
    assertTrue(dialog.strain.isRequiredIndicatorVisible());
    assertFalse(dialog.strainDescription.isRequiredIndicatorVisible());
    assertFalse(dialog.treatment.isRequiredIndicatorVisible());
    assertFalse(dialog.note.isRequiredIndicatorVisible());
  }

  @Test
  public void protocol() {
    verify(protocolService).all();
    List<Protocol> expectedProtocols = protocolRepository.findAll();
    List<Protocol> protocols =
        dialog.protocol.getListDataView().getItems().collect(Collectors.toList());
    assertEquals(expectedProtocols.size(), protocols.size());
    for (int i = 0; i < protocols.size(); i++) {
      assertEquals(expectedProtocols.get(i), protocols.get(i));
    }
    for (Protocol protocol : protocolRepository.findAll()) {
      assertEquals(protocol.getName(), dialog.protocol.getItemLabelGenerator().apply(protocol));
    }
  }

  @Test
  public void assay() {
    verify(service).topAssays(50);
    List<String> assays = dialog.assay.getListDataView().getItems().collect(Collectors.toList());
    assertEquals(topAssays.size(), assays.size());
    for (String assay : topAssays) {
      assertTrue(assays.contains(assay));
    }
    assertTrue(dialog.assay.isAllowCustomValue());
    dialog.assay.setItems("Test", "Test2");
    fireEvent(dialog.assay,
        new ComboBoxBase.CustomValueSetEvent(dialog.assay, false, "new_assay_type"));
    assertEquals("new_assay_type", dialog.assay.getValue());
    assertEquals("ChIP-chip", dialog.assay.getItemLabelGenerator().apply("ChIP-chip"));
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
  public void getSampleId() {
    assertEquals(10L, dialog.getSampleId());
  }

  @Test
  public void setSampleId() {
    Sample sample = repository.findById(4L).get();

    dialog.setSampleId(4L);

    assertEquals(resources.message(HEADER, 1, sample.getName()), dialog.getHeaderTitle());
    assertEquals(resources.message(DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(resources.message(DELETE_MESSAGE, sample.getName()),
        dialog.confirm.getElement().getProperty("message"));
    assertEquals(LocalDate.of(2018, 10, 22), dialog.date.getValue());
    assertFalse(dialog.date.isReadOnly());
    assertEquals("JS1", dialog.sampleId.getValue());
    assertFalse(dialog.sampleId.isReadOnly());
    assertEquals("R1", dialog.replicate.getValue());
    assertFalse(dialog.replicate.isReadOnly());
    assertEquals(protocolRepository.findById(3L).get(), dialog.protocol.getValue());
    assertFalse(dialog.protocol.isReadOnly());
    assertEquals("ChIP-seq", dialog.assay.getValue());
    assertFalse(dialog.assay.isReadOnly());
    assertEquals(SampleType.NULL, dialog.type.getValue());
    assertFalse(dialog.type.isReadOnly());
    assertEquals("Spt16", dialog.target.getValue());
    assertFalse(dialog.target.isReadOnly());
    assertEquals("yFR101", dialog.strain.getValue());
    assertFalse(dialog.strain.isReadOnly());
    assertEquals("G24D", dialog.strainDescription.getValue());
    assertFalse(dialog.strainDescription.isReadOnly());
    assertEquals("", dialog.treatment.getValue());
    assertFalse(dialog.treatment.isReadOnly());
    assertEquals("", dialog.note.getValue());
    assertFalse(dialog.note.isReadOnly());
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void setSampleId_ReadOnly() {
    Sample sample = repository.findById(2L).get();

    dialog.setSampleId(2L);

    assertEquals(resources.message(HEADER, 1, sample.getName()), dialog.getHeaderTitle());
    assertEquals(resources.message(DELETE_HEADER),
        dialog.confirm.getElement().getProperty("header"));
    assertEquals(resources.message(DELETE_MESSAGE, sample.getName()),
        dialog.confirm.getElement().getProperty("message"));
    assertEquals(LocalDate.of(2018, 10, 20), dialog.date.getValue());
    assertTrue(dialog.date.isReadOnly());
    assertEquals("FR2", dialog.sampleId.getValue());
    assertTrue(dialog.sampleId.isReadOnly());
    assertEquals("R2", dialog.replicate.getValue());
    assertTrue(dialog.replicate.isReadOnly());
    assertEquals(protocolRepository.findById(1L).get(), dialog.protocol.getValue());
    assertTrue(dialog.protocol.isReadOnly());
    assertEquals("MNase-seq", dialog.assay.getValue());
    assertTrue(dialog.assay.isReadOnly());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertTrue(dialog.type.isReadOnly());
    assertEquals("polr2a", dialog.target.getValue());
    assertTrue(dialog.target.isReadOnly());
    assertEquals("yFR100", dialog.strain.getValue());
    assertTrue(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertEquals("Rappa", dialog.treatment.getValue());
    assertTrue(dialog.treatment.isReadOnly());
    assertEquals("", dialog.note.getValue());
    assertTrue(dialog.note.isReadOnly());
    assertFalse(dialog.save.isVisible());
    assertFalse(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  @WithUserDetails("benoit.coulombe@ircm.qc.ca")
  public void setSampleId_NotEditable() {
    Sample sample = repository.findById(8L).get();

    dialog.setSampleId(8L);

    assertEquals(LocalDate.of(2018, 12, 5), dialog.date.getValue());
    assertTrue(dialog.date.isReadOnly());
    assertEquals("BC1", dialog.sampleId.getValue());
    assertTrue(dialog.sampleId.isReadOnly());
    assertEquals("R1", dialog.replicate.getValue());
    assertTrue(dialog.replicate.isReadOnly());
    assertEquals((Long) 2L, dialog.protocol.getValue().getId());
    assertTrue(dialog.protocol.isReadOnly());
    assertEquals("ChIP-seq", dialog.assay.getValue());
    assertTrue(dialog.assay.isReadOnly());
    assertEquals(SampleType.IMMUNO_PRECIPITATION, dialog.type.getValue());
    assertTrue(dialog.type.isReadOnly());
    assertEquals("polr2b", dialog.target.getValue());
    assertTrue(dialog.target.isReadOnly());
    assertEquals("yBC103", dialog.strain.getValue());
    assertTrue(dialog.strain.isReadOnly());
    assertEquals("WT", dialog.strainDescription.getValue());
    assertTrue(dialog.strainDescription.isReadOnly());
    assertEquals("", dialog.treatment.getValue());
    assertTrue(dialog.treatment.isReadOnly());
    assertEquals("", dialog.note.getValue());
    assertTrue(dialog.note.isReadOnly());
    assertFalse(dialog.save.isVisible());
    assertFalse(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void setSampleId_DeletableSample() {
    when(service.isDeletable(any())).thenReturn(true);
    Sample sample = repository.findById(4L).get();

    dialog.setSampleId(4L);

    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertTrue(dialog.delete.isVisible());
  }

  @Test
  public void setSampleId_Empty() {
    assertThrows(NoSuchElementException.class, () -> dialog.setSampleId(200L));
  }

  @Test
  public void setSampleId_Null() {
    dialog.setSampleId(null);

    assertEquals(resources.message(HEADER, 0), dialog.getHeaderTitle());
    assertTrue(LocalDate.now().minusDays(2).isBefore(dialog.date.getValue())
        && LocalDate.now().plusDays(1).isAfter(dialog.date.getValue()));
    assertFalse(dialog.date.isReadOnly());
    assertEquals("", dialog.sampleId.getValue());
    assertFalse(dialog.sampleId.isReadOnly());
    assertEquals("", dialog.replicate.getValue());
    assertFalse(dialog.replicate.isReadOnly());
    assertNull(dialog.protocol.getValue());
    assertFalse(dialog.protocol.isReadOnly());
    assertNull(dialog.assay.getValue());
    assertFalse(dialog.assay.isReadOnly());
    assertEquals(SampleType.NULL, dialog.type.getValue());
    assertFalse(dialog.type.isReadOnly());
    assertEquals("", dialog.target.getValue());
    assertFalse(dialog.target.isReadOnly());
    assertEquals("", dialog.strain.getValue());
    assertFalse(dialog.strain.isReadOnly());
    assertEquals("", dialog.strainDescription.getValue());
    assertFalse(dialog.strainDescription.isReadOnly());
    assertEquals("", dialog.treatment.getValue());
    assertFalse(dialog.treatment.isReadOnly());
    assertEquals("", dialog.note.getValue());
    assertFalse(dialog.note.isReadOnly());
    assertTrue(dialog.save.isVisible());
    assertTrue(dialog.cancel.isVisible());
    assertFalse(dialog.delete.isVisible());
  }

  @Test
  public void save_DateEmpty() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();
    dialog.date.setValue(null);

    clickButton(dialog.save);

    BinderValidationStatus<Sample> status = dialog.validateSample();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.date);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(service, never()).delete(any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_SampleIdEmpty() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();
    dialog.sampleId.setValue("");

    clickButton(dialog.save);

    BinderValidationStatus<Sample> status = dialog.validateSample();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.sampleId);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(service, never()).delete(any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_ReplicateEmpty() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();
    dialog.replicate.setValue("");

    clickButton(dialog.save);

    BinderValidationStatus<Sample> status = dialog.validateSample();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.replicate);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(service, never()).delete(any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_ProtocolEmpty() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();
    dialog.protocol.setItems();

    clickButton(dialog.save);

    BinderValidationStatus<Sample> status = dialog.validateSample();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.protocol);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(service, never()).delete(any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_AssayEmpty() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();
    dialog.assay.clear();

    clickButton(dialog.save);

    BinderValidationStatus<Sample> status = dialog.validateSample();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.assay);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(service, never()).delete(any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_AssayNew() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();
    dialog.assay.setValue("new_assay_type");

    clickButton(dialog.save);

    BinderValidationStatus<Sample> status = dialog.validateSample();
    assertTrue(status.isOk());
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertEquals("new_assay_type", sample.getAssay());
    verify(service, never()).delete(any());
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(SAVED, sample.getName()), test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_TypeEmpty() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();
    dialog.type.setValue(SampleType.NULL);

    clickButton(dialog.save);

    BinderValidationStatus<Sample> status = dialog.validateSample();
    assertTrue(status.isOk());
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertNull(sample.getType());
    verify(service, never()).delete(any());
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(SAVED, sample.getName()), test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_TargetEmpty() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();
    dialog.target.setValue("");

    clickButton(dialog.save);

    BinderValidationStatus<Sample> status = dialog.validateSample();
    assertTrue(status.isOk());
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertNull(sample.getTarget());
    verify(service, never()).delete(any());
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(SAVED, sample.getName()), test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_StrainEmpty() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();
    dialog.strain.setValue("");

    clickButton(dialog.save);

    BinderValidationStatus<Sample> status = dialog.validateSample();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dialog.strain);
    assertTrue(optionalError.isPresent());
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(webResources.message(REQUIRED)), error.getMessage());
    verify(service, never()).save(any());
    verify(service, never()).delete(any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_StrainDescriptionEmpty() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();
    dialog.strainDescription.setValue("");

    clickButton(dialog.save);

    BinderValidationStatus<Sample> status = dialog.validateSample();
    ValidationLogger.logValidation(status);
    assertTrue(status.isOk());
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertNull(sample.getStrainDescription());
    verify(service, never()).delete(any());
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(SAVED, sample.getName()), test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_TreatmentEmpty() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();
    dialog.treatment.setValue("");

    clickButton(dialog.save);

    BinderValidationStatus<Sample> status = dialog.validateSample();
    assertTrue(status.isOk());
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertNull(sample.getTreatment());
    verify(service, never()).delete(any());
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(SAVED, sample.getName()), test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_NoteEmpty() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();
    dialog.note.setValue("");

    clickButton(dialog.save);

    BinderValidationStatus<Sample> status = dialog.validateSample();
    assertTrue(status.isOk());
    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    verify(service, never()).delete(any());
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(SAVED, sample.getName()), test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_NameExists() {
    when(service.get(anyLong())).thenReturn(Optional.empty());
    when(service.exists(any())).thenReturn(true);
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();

    clickButton(dialog.save);

    verify(service).exists(
        (sampleId + "_" + "ChIPseq_IP_" + target + "_" + strain + "_" + strainDescription + "_"
            + treatment + "_" + replicate + "_" + DateTimeFormatter.BASIC_ISO_DATE.format(date))
                .replaceAll("[^\\w-]", ""));
    verify(service, never()).save(any());
    assertFalse($(Notification.class).exists());
    assertTrue(dialog.isOpened());
    verify(savedListener, never()).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_NameExistsSameSample() {
    Sample sample = repository.findById(2L).get();
    when(service.exists(any())).thenReturn(true);
    when(service.get(any())).thenReturn(Optional.of(sample));
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    dialog.setSampleId(sample.getId());

    clickButton(dialog.save);

    verify(service).exists("FR2_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R2_20181020");
    verify(service, atLeastOnce()).get(2L);
    verify(service).save(any());
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(SAVED, sample.getName()), test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_NewSample() {
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();

    clickButton(dialog.save);

    verify(service).save(sampleCaptor.capture());
    Sample sample = sampleCaptor.getValue();
    assertEquals(date, sample.getDate());
    assertEquals(sampleId, sample.getSampleId());
    assertEquals(replicate, sample.getReplicate());
    assertEquals(protocol, sample.getProtocol());
    assertEquals(assay, sample.getAssay());
    assertEquals(type, sample.getType());
    assertEquals(target, sample.getTarget());
    assertEquals(strain, sample.getStrain());
    assertEquals(strainDescription, sample.getStrainDescription());
    assertEquals(treatment, sample.getTreatment());
    assertEquals(note, sample.getNote());
    verify(service, never()).delete(any());
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(SAVED, sample.getName()), test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void save_UpdateSample() {
    Sample sample = repository.findById(4L).get();
    dialog.setSampleId(sample.getId());
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();

    clickButton(dialog.save);

    verify(service).save(sampleCaptor.capture());
    sample = sampleCaptor.getValue();
    assertEquals(date, sample.getDate());
    assertEquals(sampleId, sample.getSampleId());
    assertEquals(replicate, sample.getReplicate());
    assertEquals(protocol, sample.getProtocol());
    assertEquals(assay, sample.getAssay());
    assertEquals(type, sample.getType());
    assertEquals(target, sample.getTarget());
    assertEquals(strain, sample.getStrain());
    assertEquals(strainDescription, sample.getStrainDescription());
    assertEquals(treatment, sample.getTreatment());
    assertEquals(note, sample.getNote());
    verify(service, never()).delete(any());
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(SAVED, sample.getName()), test(notification).getText());
    assertFalse(dialog.isOpened());
    verify(savedListener).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void cancel() {
    Sample sample = repository.findById(4L).get();
    dialog.setSampleId(sample.getId());
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);
    fillForm();

    clickButton(dialog.cancel);

    verify(service, never()).save(any());
    verify(service, never()).delete(any());
    assertFalse(dialog.isOpened());
    assertFalse($(Notification.class).exists());
    verify(savedListener, never()).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }

  @Test
  public void delete_Confirm() {
    when(service.isDeletable(any())).thenReturn(true);
    Sample sample = repository.findById(4L).get();
    dialog.setSampleId(sample.getId());
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);

    clickButton(dialog.delete);

    assertTrue(dialog.confirm.isOpened());
    ConfirmEvent event = new ConfirmEvent(dialog.confirm, false);
    fireEvent(dialog.confirm, event);
    verify(service, never()).save(any());
    verify(service).delete(sample);
    assertFalse(dialog.isOpened());
    Notification notification = $(Notification.class).first();
    assertEquals(resources.message(DELETED, sample.getName()), test(notification).getText());
    verify(savedListener, never()).onComponentEvent(any());
    verify(deletedListener).onComponentEvent(any());
  }

  @Test
  public void delete_Cancel() {
    when(service.isDeletable(any())).thenReturn(true);
    Sample sample = repository.findById(4L).get();
    dialog.setSampleId(sample.getId());
    dialog.addSavedListener(savedListener);
    dialog.addDeletedListener(deletedListener);

    clickButton(dialog.delete);

    assertTrue(dialog.confirm.isOpened());
    CancelEvent event = new CancelEvent(dialog.confirm, false);
    fireEvent(dialog.confirm, event);
    verify(service, never()).save(any());
    verify(service, never()).delete(any());
    assertTrue(dialog.isOpened());
    assertFalse($(Notification.class).exists());
    verify(savedListener, never()).onComponentEvent(any());
    verify(deletedListener, never()).onComponentEvent(any());
  }
}
