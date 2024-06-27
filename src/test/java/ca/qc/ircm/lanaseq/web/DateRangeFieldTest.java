package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static ca.qc.ircm.lanaseq.Constants.FRENCH;
import static ca.qc.ircm.lanaseq.Constants.PLACEHOLDER;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateEquals;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.web.DatePickerInternationalization.englishDatePickerI18n;
import static ca.qc.ircm.lanaseq.web.DatePickerInternationalization.frenchDatePickerI18n;
import static ca.qc.ircm.lanaseq.web.DateRangeField.CLASS_NAME;
import static ca.qc.ircm.lanaseq.web.DateRangeField.FROM;
import static ca.qc.ircm.lanaseq.web.DateRangeField.HELPER;
import static ca.qc.ircm.lanaseq.web.DateRangeField.TO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.dataset.DatasetProperties;
import ca.qc.ircm.lanaseq.dataset.web.DatasetGrid;
import ca.qc.ircm.lanaseq.dataset.web.DatasetsView;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.DateRangeField.Dates;
import com.google.common.collect.Range;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.datepicker.DatePickerVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link DateRangeField}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DateRangeFieldTest extends SpringUIUnitTest {
  private static final String MESSAGE_PREFIX = messagePrefix(DateRangeField.class);
  private DateRangeField dateRange;
  private Locale locale = ENGLISH;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    navigate(DatasetsView.class);
    DatasetGrid datasetGrid = $(DatasetGrid.class).first();
    HeaderRow filtersRow = datasetGrid.getHeaderRows().get(1);
    dateRange =
        test(filtersRow.getCell(datasetGrid.getColumnByKey(DatasetProperties.DATE)).getComponent())
            .find(DateRangeField.class).first();
  }

  @Test
  public void styles() {
    assertTrue(dateRange.layout.hasClassName(CLASS_NAME));
    assertTrue(dateRange.from.hasClassName(FROM));
    assertTrue(dateRange.from.isClearButtonVisible());
    assertTrue(dateRange.to.hasClassName(TO));
    assertTrue(dateRange.to.isClearButtonVisible());
  }

  @Test
  public void labels() {
    assertEquals(dateRange.getTranslation(MESSAGE_PREFIX + HELPER), dateRange.getHelperText());
    assertEquals(dateRange.getTranslation(MESSAGE_PREFIX + property(FROM, PLACEHOLDER)),
        dateRange.from.getPlaceholder());
    validateEquals(englishDatePickerI18n(), dateRange.from.getI18n());
    assertEquals(Locale.CANADA, dateRange.from.getLocale());
    assertEquals(dateRange.getTranslation(MESSAGE_PREFIX + property(TO, PLACEHOLDER)),
        dateRange.to.getPlaceholder());
    validateEquals(englishDatePickerI18n(), dateRange.to.getI18n());
    assertEquals(Locale.CANADA, dateRange.to.getLocale());
  }

  @Test
  public void localeChange() {
    locale = FRENCH;
    UI.getCurrent().setLocale(locale);
    assertEquals(dateRange.getTranslation(MESSAGE_PREFIX + HELPER), dateRange.getHelperText());
    assertEquals(dateRange.getTranslation(MESSAGE_PREFIX + property(FROM, PLACEHOLDER)),
        dateRange.from.getPlaceholder());
    validateEquals(frenchDatePickerI18n(), dateRange.from.getI18n());
    assertEquals(Locale.CANADA, dateRange.from.getLocale());
    assertEquals(dateRange.getTranslation(MESSAGE_PREFIX + property(TO, PLACEHOLDER)),
        dateRange.to.getPlaceholder());
    validateEquals(frenchDatePickerI18n(), dateRange.to.getI18n());
    assertEquals(Locale.CANADA, dateRange.to.getLocale());
  }

  @Test
  public void minimumToAfterFromIsSet() {
    LocalDate from = LocalDate.now().minusDays(2);
    dateRange.from.setValue(from);
    assertEquals(from, dateRange.to.getMin());
  }

  @Test
  public void minimumToAfterFromIsCleared() {
    LocalDate from = LocalDate.now().minusDays(2);
    dateRange.from.setValue(from);
    dateRange.from.clear();
    assertNull(dateRange.to.getMin());
  }

  @Test
  public void maximumFromAfterToIsSet() {
    LocalDate to = LocalDate.now().minusDays(2);
    dateRange.to.setValue(to);
    assertEquals(to, dateRange.from.getMax());
  }

  @Test
  public void maximumFromAfterToIsCleared() {
    LocalDate to = LocalDate.now().minusDays(2);
    dateRange.to.setValue(to);
    dateRange.to.clear();
    assertNull(dateRange.from.getMax());
  }

  @Test
  public void validate_FromGreaterThanTo() {
    LocalDate from = LocalDate.now().minusDays(2);
    dateRange.from.setValue(from);
    LocalDate to = LocalDate.now().minusDays(10);
    dateRange.to.setValue(to);
    BinderValidationStatus<Dates> status = dateRange.validateDates();
    assertFalse(status.isOk());
    Optional<BindingValidationStatus<?>> optionalError =
        findValidationStatusByField(status, dateRange.from);
    assertTrue(optionalError.isPresent());
  }

  @Test
  public void generateModelValue_Empty() {
    Range<LocalDate> range = dateRange.generateModelValue();
    assertEquals(Range.all(), range);
  }

  @Test
  public void generateModelValue_From() {
    LocalDate from = LocalDate.now().minusDays(10);
    dateRange.from.setValue(from);
    Range<LocalDate> range = dateRange.generateModelValue();
    assertEquals(Range.atLeast(from), range);
  }

  @Test
  public void generateModelValue_To() {
    LocalDate to = LocalDate.now().minusDays(1);
    dateRange.to.setValue(to);
    Range<LocalDate> range = dateRange.generateModelValue();
    assertEquals(Range.atMost(to), range);
  }

  @Test
  public void generateModelValue_FromTo() {
    LocalDate from = LocalDate.now().minusDays(10);
    dateRange.from.setValue(from);
    LocalDate to = LocalDate.now().minusDays(1);
    dateRange.to.setValue(to);
    Range<LocalDate> range = dateRange.generateModelValue();
    assertEquals(Range.closed(from, to), range);
  }

  @Test
  public void generateModelValue_FromGreaterThanTo() {
    LocalDate from = LocalDate.now().minusDays(2);
    dateRange.from.setValue(from);
    LocalDate to = LocalDate.now().minusDays(10);
    dateRange.to.setValue(to);
    Range<LocalDate> range = dateRange.generateModelValue();
    assertEquals(Range.atMost(to), range);
  }

  @Test
  public void generateModelValue_FromEqualsThan() {
    LocalDate from = LocalDate.now().minusDays(2);
    dateRange.from.setValue(from);
    LocalDate to = LocalDate.now().minusDays(2);
    dateRange.to.setValue(to);
    Range<LocalDate> range = dateRange.generateModelValue();
    assertEquals(Range.singleton(to), range);
  }

  @Test
  public void generateModelValue_Null() {
    dateRange.from.setValue(null);
    dateRange.to.setValue(null);
    Range<LocalDate> range = dateRange.generateModelValue();
    assertEquals(Range.all(), range);
  }

  @Test
  public void setPresentationValue_Empty() {
    dateRange.setPresentationValue(Range.all());
    assertNull(dateRange.from.getValue());
    assertNull(dateRange.to.getValue());
  }

  @Test
  public void setPresentationValue_EmptyAfterOtherValues() {
    LocalDate from = LocalDate.now().minusDays(10);
    dateRange.from.setValue(from);
    LocalDate to = LocalDate.now().minusDays(1);
    dateRange.to.setValue(to);
    dateRange.setPresentationValue(Range.all());
    assertNull(dateRange.from.getValue());
    assertNull(dateRange.to.getValue());
  }

  @Test
  public void setPresentationValue_From() {
    LocalDate from = LocalDate.now().minusDays(10);
    dateRange.setPresentationValue(Range.atLeast(from));
    assertEquals(from, dateRange.from.getValue());
    assertNull(dateRange.to.getValue());
  }

  @Test
  public void setPresentationValue_To() {
    LocalDate to = LocalDate.now().minusDays(1);
    dateRange.setPresentationValue(Range.atMost(to));
    assertNull(dateRange.from.getValue());
    assertEquals(to, dateRange.to.getValue());
  }

  @Test
  public void setPresentationValue_FromTo() {
    LocalDate from = LocalDate.now().minusDays(10);
    LocalDate to = LocalDate.now().minusDays(1);
    dateRange.setPresentationValue(Range.closed(from, to));
    assertEquals(from, dateRange.from.getValue());
    assertEquals(to, dateRange.to.getValue());
  }

  @Test
  public void setPresentationValue_Null() {
    dateRange.setPresentationValue(null);
    assertNull(dateRange.from.getValue());
    assertNull(dateRange.to.getValue());
  }

  @Test
  public void addThemeVariants() {
    dateRange.from.getThemeNames().forEach(theme -> dateRange.from.removeThemeName(theme));
    dateRange.to.getThemeNames().forEach(theme -> dateRange.to.removeThemeName(theme));
    assertTrue(dateRange.from.getThemeNames().isEmpty());
    assertTrue(dateRange.to.getThemeNames().isEmpty());
    assertFalse(dateRange.from.hasThemeName(DatePickerVariant.LUMO_SMALL.getVariantName()));
    assertFalse(dateRange.to.hasThemeName(DatePickerVariant.LUMO_SMALL.getVariantName()));
    assertFalse(
        dateRange.from.hasThemeName(DatePickerVariant.LUMO_HELPER_ABOVE_FIELD.getVariantName()));
    assertFalse(
        dateRange.to.hasThemeName(DatePickerVariant.LUMO_HELPER_ABOVE_FIELD.getVariantName()));
    dateRange.addThemeVariants(CustomFieldVariant.LUMO_SMALL,
        CustomFieldVariant.LUMO_HELPER_ABOVE_FIELD, CustomFieldVariant.LUMO_WHITESPACE);
    assertEquals(2, dateRange.from.getThemeNames().size());
    assertEquals(2, dateRange.to.getThemeNames().size());
    assertTrue(dateRange.from.hasThemeName(DatePickerVariant.LUMO_SMALL.getVariantName()));
    assertTrue(dateRange.to.hasThemeName(DatePickerVariant.LUMO_SMALL.getVariantName()));
    assertTrue(
        dateRange.from.hasThemeName(DatePickerVariant.LUMO_HELPER_ABOVE_FIELD.getVariantName()));
    assertTrue(
        dateRange.to.hasThemeName(DatePickerVariant.LUMO_HELPER_ABOVE_FIELD.getVariantName()));
  }

  @Test
  public void removeThemeVariants() {
    dateRange.addThemeVariants(CustomFieldVariant.LUMO_SMALL,
        CustomFieldVariant.LUMO_HELPER_ABOVE_FIELD, CustomFieldVariant.LUMO_WHITESPACE);
    dateRange.removeThemeVariants(CustomFieldVariant.LUMO_SMALL,
        CustomFieldVariant.LUMO_HELPER_ABOVE_FIELD, CustomFieldVariant.LUMO_WHITESPACE);
    assertTrue(dateRange.from.getThemeNames().isEmpty());
    assertTrue(dateRange.to.getThemeNames().isEmpty());
  }
}
