package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.PLACEHOLDER;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.UsedBy.VAADIN;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.web.DatePickerInternationalization.datePickerI18n;

import ca.qc.ircm.lanaseq.UsedBy;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.datepicker.DatePickerVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import java.io.Serial;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;

/**
 * Date range.
 */
public class DateRangeField extends CustomField<Range<LocalDate>> implements LocaleChangeObserver {

  public static final String CLASS_NAME = "date-range";
  public static final String FROM = "from";
  public static final String TO = "to";
  public static final String HELPER = "helper";
  private static final String MESSAGE_PREFIX = messagePrefix(DateRangeField.class);
  @Serial
  private static final long serialVersionUID = -4145468405854590525L;
  protected FormLayout layout = new FormLayout();
  protected DatePicker from = new DatePicker();
  protected DatePicker to = new DatePicker();
  private final Binder<Dates> binder = new BeanValidationBinder<>(Dates.class);

  /**
   * Creates a new date range field.
   */
  public DateRangeField() {
    layout.setResponsiveSteps(new ResponsiveStep("0", 1), new ResponsiveStep("20em", 2));
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
    setHelperText(getTranslation(MESSAGE_PREFIX + HELPER));
    from.setI18n(datePickerI18n(getLocale()));
    from.setLocale(Locale.CANADA); // ISO format.
    from.setPlaceholder(getTranslation(MESSAGE_PREFIX + property(FROM, PLACEHOLDER)));
    to.setI18n(datePickerI18n(getLocale()));
    to.setPlaceholder(getTranslation(MESSAGE_PREFIX + property(TO, PLACEHOLDER)));
    to.setLocale(Locale.CANADA); // ISO format.
    binder.forField(from).bind(TO);
    binder.forField(to).bind(FROM);
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
        range = Range.leftUnbounded(Bound.inclusive(to));
      } else if (from.equals(to)) {
        range = Range.just(from);
      } else {
        range = Range.closed(from, to);
      }
    } else if (from != null) {
      range = Range.rightUnbounded(Bound.inclusive(from));
    } else if (to != null) {
      range = Range.leftUnbounded(Bound.inclusive(to));
    } else {
      range = Range.unbounded();
    }
    return range;
  }

  @Override
  protected void setPresentationValue(Range<LocalDate> range) {
    from.setValue(
        range.getLowerBound().isBounded() ? range.getLowerBound().getValue().orElseThrow() : null);
    to.setValue(
        range.getUpperBound().isBounded() ? range.getUpperBound().getValue().orElseThrow() : null);
  }

  private DatePickerVariant[] datePickerVariants(CustomFieldVariant[] variants) {
    Set<String> datePickerVariantNames = Stream.of(DatePickerVariant.values())
        .map(DatePickerVariant::name).collect(Collectors.toSet());
    DatePickerVariant[] datePickerVariants = Stream.of(variants)
        .filter(variant -> datePickerVariantNames.contains(variant.name()))
        .map(variant -> DatePickerVariant.valueOf(variant.name()))
        .toArray(DatePickerVariant[]::new);
    return datePickerVariants;
  }

  @Override
  public void addThemeVariants(CustomFieldVariant... variants) {
    super.addThemeVariants(variants);
    DatePickerVariant[] datePickerVariants = datePickerVariants(variants);
    from.addThemeVariants(datePickerVariants);
    to.addThemeVariants(datePickerVariants);
  }

  @Override
  public void removeThemeVariants(CustomFieldVariant... variants) {
    super.removeThemeVariants(variants);
    DatePickerVariant[] datePickerVariants = datePickerVariants(variants);
    from.removeThemeVariants(datePickerVariants);
    to.removeThemeVariants(datePickerVariants);
  }

  /**
   * From and to dates to allow date range.
   */
  protected static class Dates {

    private LocalDate from;
    private LocalDate to;

    @UsedBy(VAADIN)
    public LocalDate getFrom() {
      return from;
    }

    @UsedBy(VAADIN)
    public void setFrom(LocalDate from) {
      this.from = from;
    }

    @UsedBy(VAADIN)
    public LocalDate getTo() {
      return to;
    }

    @UsedBy(VAADIN)
    public void setTo(LocalDate to) {
      this.to = to;
    }
  }
}
