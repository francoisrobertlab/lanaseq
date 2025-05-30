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
 * List of keywords field.
 */
public class KeywordsField extends MultiSelectComboBox<String> implements LocaleChangeObserver {

  public static final String CLASS_NAME = "keywords-field";
  public static final String NEW_KEYWORD_REGEX = "[\\w-]*";
  public static final String NEW_KEYWORD_REGEX_ERROR = "regex";
  private static final String MESSAGE_PREFIX = messagePrefix(KeywordsField.class);
  @Serial
  private static final long serialVersionUID = -1880458092354113415L;
  private Validator<String> validator;

  /**
   * Creates a keywords field.
   */
  public KeywordsField() {
    this.addClassName(CLASS_NAME);
    setItems(List.of());
    setAutoExpand(AutoExpandMode.BOTH);
    setSelectedItemsOnTop(true);
    setAllowCustomValue(true);
    addCustomValueSetListener(e -> {
      String value = e.getDetail();
      ValidationResult result = validator.apply(value,
          new ValueContext(null, this, this, getLocale()));
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
    validator = new RegexpValidator(getTranslation(MESSAGE_PREFIX + NEW_KEYWORD_REGEX_ERROR),
        NEW_KEYWORD_REGEX);
  }

  public List<String> getSuggestions() {
    return getListDataView().getItems().toList();
  }

  public void setSuggestions(List<String> suggestions) {
    setItems(suggestions);
  }
}
