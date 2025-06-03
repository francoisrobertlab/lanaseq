package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.FRENCH;

import com.vaadin.flow.component.datepicker.DatePicker.DatePickerI18n;
import java.util.Arrays;
import java.util.Locale;

/**
 * Date picker internationalization.
 */
public class DatePickerInternationalization {

  /**
   * Returns {@link DatePickerI18n} for specified locale.
   *
   * <p>Falls back to English if no instance exists for specified locale.</p>
   *
   * @param locale locale
   * @return {@link DatePickerI18n} for specified locale, never null
   */
  public static DatePickerI18n datePickerI18n(Locale locale) {
    if (FRENCH.getLanguage().equals(locale.getLanguage())) {
      return frenchDatePickerI18n();
    }
    return englishDatePickerI18n();
  }

  /**
   * Returns {@link DatePickerI18n} for English.
   *
   * @return {@link DatePickerI18n} for English
   */
  public static DatePickerI18n englishDatePickerI18n() {
    return new DatePickerI18n().setToday("Today").setCancel("Cancel").setFirstDayOfWeek(0)
        .setMonthNames(
            Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August",
                "September", "October", "November", "December")).setWeekdays(
            Arrays.asList("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday",
                "Saturday"))
        .setWeekdaysShort(Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"));
  }

  /**
   * Returns {@link DatePickerI18n} for French.
   *
   * @return {@link DatePickerI18n} for French
   */
  public static DatePickerI18n frenchDatePickerI18n() {
    return new DatePickerI18n().setToday("Aujourd'hui").setCancel("Annuler").setFirstDayOfWeek(0)
        .setMonthNames(
            Arrays.asList("Janvier", "Février", "Mars", "Avril", "Mai", "Juin", "Juillet", "Août",
                "Septembre", "Octobre", "Novembre", "Décembre")).setWeekdays(
            Arrays.asList("Dimanche", "Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi"))
        .setWeekdaysShort(Arrays.asList("Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"));
  }
}
