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

  public DateRangeField() {
    layout.setResponsiveSteps(new ResponsiveStep("20em", 1), new ResponsiveStep("20em", 2));
    layout.add(from, to);
    layout.addClassName(CLASS_NAME);
    add(layout);
    from.addClassName(FROM);
    from.setClearButtonVisible(true);
    to.addClassName(TO);
    to.setClearButtonVisible(true);
    to.addValueChangeListener(e -> binder.validate());
    binder.setBean(new Dates());
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    AppResources resources = new AppResources(DateRangeField.class, getLocale());
    from.setI18n(datePickerI18n(getLocale()));
    from.setPlaceholder(resources.message(property(FROM, PLACEHOLDER)));
    to.setI18n(datePickerI18n(getLocale()));
    to.setPlaceholder(resources.message(property(TO, PLACEHOLDER)));
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
    from.setValue(range.hasLowerBound() ? range.lowerEndpoint() : null);
    to.setValue(range.hasUpperBound() ? range.upperEndpoint() : null);
  }

  public static class Dates {
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
