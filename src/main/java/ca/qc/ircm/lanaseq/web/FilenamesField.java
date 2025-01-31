package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import java.io.Serial;
import java.util.List;

/**
 * Field containing list of filenames.
 */
public class FilenamesField extends MultiSelectComboBox<String> implements LocaleChangeObserver {

  public static final String CLASS_NAME = "filenames-field";
  public static final String NEW_FILENAME_REGEX = "[\\w-\\.]*";
  public static final String NEW_FILENAME_REGEX_ERROR = "regex";
  private static final String MESSAGE_PREFIX = messagePrefix(FilenamesField.class);
  @Serial
  private static final long serialVersionUID = 4861185655330127522L;
  private Validator<String> validator;

  /**
   * Creates a filenames field.
   */
  public FilenamesField() {
    this.addClassName(CLASS_NAME);
    setItems(List.of());
    setAutoExpand(AutoExpandMode.BOTH);
    setSelectedItemsOnTop(true);
    setAllowCustomValue(true);
    addCustomValueSetListener(e -> {
      String value = e.getDetail();
      ValidationResult result = validator.apply(value, new ValueContext(this, this, getLocale()));
      if (result.isError()) {
        setInvalid(true);
        setErrorMessage(result.getErrorMessage());
      } else {
        select(value);
      }
    });
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    validator = new RegexpValidator(getTranslation(MESSAGE_PREFIX + NEW_FILENAME_REGEX_ERROR),
        NEW_FILENAME_REGEX);
  }

  public List<String> getSuggestions() {
    return getListDataView().getItems().toList();
  }

  public void setSuggestions(List<String> suggestions) {
    setItems(suggestions);
  }
}
