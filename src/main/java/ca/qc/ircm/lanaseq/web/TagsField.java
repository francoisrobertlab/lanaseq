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

import static ca.qc.ircm.lanaseq.Constants.messagePrefix;

import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import java.util.List;

/**
 * List of tags field.
 */
@JsModule("./styles/tags-field-styles.js")
public class TagsField extends MultiSelectComboBox<String> implements LocaleChangeObserver {
  public static final String CLASS_NAME = "tags-field";
  public static final String NEW_TAG_REGEX = "[\\w-]*";
  public static final String NEW_TAG_REGEX_ERROR = "regex";
  private static final String MESSAGE_PREFIX = messagePrefix(TagsField.class);
  private static final long serialVersionUID = -1880458092354113415L;
  private Validator<String> validator;

  /**
   * Creates a tags field.
   */
  public TagsField() {
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
    validator =
        new RegexpValidator(getTranslation(MESSAGE_PREFIX + NEW_TAG_REGEX_ERROR), NEW_TAG_REGEX);
  }

  public List<String> getSuggestions() {
    return getListDataView().getItems().toList();
  }

  public void setSuggestions(List<String> suggestions) {
    setItems(suggestions);
  }
}
