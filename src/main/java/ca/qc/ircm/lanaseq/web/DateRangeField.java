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

import static ca.qc.ircm.lanaseq.Constants.PLACEHOLDER;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.web.DatePickerInternationalization.datePickerI18n;

import ca.qc.ircm.lanaseq.AppResources;
import com.google.common.collect.Range;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Date range.
 */
public class DateRangeField extends CustomField<Range<LocalDate>> implements LocaleChangeObserver {
  public static final String CLASS_NAME = "date-range";
  public static final String FROM = "from";
  public static final String TO = "to";
  public static final String FROM_AFTER_TO = "fromAfterTo";
  private static final long serialVersionUID = -4145468405854590525L;
  protected FormLayout layout = new FormLayout();
  protected DatePicker from = new DatePicker();
  protected DatePicker to = new DatePicker();
  private Binder<Dates> binder = new BeanValidationBinder<>(Dates.class);

  /**
   * Creates a new date range field.
   */
  public DateRangeField() {
    layout.setResponsiveSteps(new ResponsiveStep("20em", 1), new ResponsiveStep("20em", 2));
    layout.add(from, to);
    layout.addClassName(CLASS_NAME);
    add(layout);
    from.addClassName(FROM);
    from.setClearButtonVisible(true);
    from.addValueChangeListener(e -> to.setMin(from.getValue()));
    to.addClassName(TO);
    to.setClearButtonVisible(true);
    to.addValueChangeListener(e -> from.setMax(to.getValue()));
    to.addValueChangeListener(e -> binder.validate());
    binder.setBean(new Dates());
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    AppResources resources = new AppResources(DateRangeField.class, getLocale());
    from.setI18n(datePickerI18n(getLocale()));
    from.setLocale(Locale.CANADA); // ISO format.
    from.setPlaceholder(resources.message(property(FROM, PLACEHOLDER)));
    to.setI18n(datePickerI18n(getLocale()));
    to.setPlaceholder(resources.message(property(TO, PLACEHOLDER)));
    to.setLocale(Locale.CANADA); // ISO format.
    binder.forField(from).withValidator(fromBeforeTo(getLocale())).bind(TO);
    binder.forField(to).bind(FROM);
  }

  private Validator<LocalDate> fromBeforeTo(Locale locale) {
    return (value, context) -> {
      if (value != null && to.getValue() != null && value.isAfter(to.getValue())) {
        final AppResources resources = new AppResources(DateRangeField.class, locale);
        return ValidationResult.error(resources.message(FROM_AFTER_TO));
      }
      return ValidationResult.ok();
    };
  }

  BinderValidationStatus<Dates> validateDates() {
    return binder.validate();
  }

  @Override
  protected Range<LocalDate> generateModelValue() {
    LocalDate from = this.from.getValue();
    LocalDate to = this.to.getValue();
    Range<LocalDate> range;
    if (from != null && to != null) {
      if (from.isAfter(to)) {
        range = Range.atMost(to);
      } else if (from.equals(to)) {
        range = Range.singleton(from);
      } else {
        range = Range.closed(from, to);
      }
    } else if (from != null) {
      range = Range.atLeast(from);
    } else if (to != null) {
      range = Range.atMost(to);
    } else {
      range = Range.all();
    }
    return range;
  }

  @Override
  protected void setPresentationValue(Range<LocalDate> range) {
    if (range == null) {
      range = Range.all();
    }
    from.setValue(range.hasLowerBound() ? range.lowerEndpoint() : null);
    to.setValue(range.hasUpperBound() ? range.upperEndpoint() : null);
  }

  /**
   * From and to dates to allow date range.
   */
  protected static class Dates {
    private LocalDate from;
    private LocalDate to;

    public LocalDate getFrom() {
      return from;
    }

    public void setFrom(LocalDate from) {
      this.from = from;
    }

    public LocalDate getTo() {
      return to;
    }

    public void setTo(LocalDate to) {
      this.to = to;
    }
  }
}
