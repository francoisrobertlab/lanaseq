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

package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static ca.qc.ircm.lanaseq.Constants.FRENCH;
import static ca.qc.ircm.lanaseq.Constants.PLACEHOLDER;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.findValidationStatusByField;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateEquals;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.web.DatePickerInternationalization.englishDatePickerI18n;
import static ca.qc.ircm.lanaseq.web.DatePickerInternationalization.frenchDatePickerI18n;
import static ca.qc.ircm.lanaseq.web.DateRangeField.CLASS_NAME;
import static ca.qc.ircm.lanaseq.web.DateRangeField.FROM;
import static ca.qc.ircm.lanaseq.web.DateRangeField.FROM_AFTER_TO;
import static ca.qc.ircm.lanaseq.web.DateRangeField.TO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import ca.qc.ircm.lanaseq.web.DateRangeField.Dates;
import com.google.common.collect.Range;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.BindingValidationStatus;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

/**
 * Tests for {@link DateRangeField}.
 */
@NonTransactionalTestAnnotations
public class DateRangeFieldTest extends AbstractViewTestCase {
  private DateRangeField dateRange;
  @Mock
  private LocaleChangeEvent localeChangeEvent;
  private Locale locale = ENGLISH;
  private AppResources resources = new AppResources(DateRangeField.class, locale);

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    dateRange = new DateRangeField();
    when(localeChangeEvent.getLocale()).thenReturn(locale);
    dateRange.localeChange(localeChangeEvent);
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
    assertEquals(resources.message(property(FROM, PLACEHOLDER)), dateRange.from.getPlaceholder());
    validateEquals(englishDatePickerI18n(), dateRange.from.getI18n());
    assertEquals(Locale.CANADA, dateRange.from.getLocale());
    assertEquals(resources.message(property(TO, PLACEHOLDER)), dateRange.to.getPlaceholder());
    validateEquals(englishDatePickerI18n(), dateRange.to.getI18n());
    assertEquals(Locale.CANADA, dateRange.to.getLocale());
  }

  @Test
  public void localeChange() {
    locale = FRENCH;
    when(ui.getLocale()).thenReturn(locale);
    when(localeChangeEvent.getLocale()).thenReturn(locale);
    dateRange.localeChange(localeChangeEvent);
    AppResources resources = new AppResources(DateRangeField.class, locale);
    assertEquals(resources.message(property(FROM, PLACEHOLDER)), dateRange.from.getPlaceholder());
    validateEquals(frenchDatePickerI18n(), dateRange.from.getI18n());
    assertEquals(Locale.CANADA, dateRange.from.getLocale());
    assertEquals(resources.message(property(TO, PLACEHOLDER)), dateRange.to.getPlaceholder());
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
    BindingValidationStatus<?> error = optionalError.get();
    assertEquals(Optional.of(resources.message(FROM_AFTER_TO)), error.getMessage());
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
  public void setPresentationValue_FromEqualsTo() {
    LocalDate date = LocalDate.now().minusDays(2);
    dateRange.setPresentationValue(Range.singleton(date));
    assertEquals(date, dateRange.from.getValue());
    assertEquals(date, dateRange.to.getValue());
  }
}
