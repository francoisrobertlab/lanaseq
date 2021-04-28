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

import static ca.qc.ircm.lanaseq.text.Strings.property;

import ca.qc.ircm.lanaseq.AppResources;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * List of tags field.
 */
@HtmlImport("styles/tags-field-styles.html")
public class TagsField extends CustomField<Set<String>> implements LocaleChangeObserver {
  public static final String CLASS_NAME = "tags-field";
  public static final String TAG = "tag";
  public static final String NEW_TAG = "newTag";
  public static final String NEW_TAG_REGEX = "[\\w-]*";
  public static final String NEW_TAG_REGEX_ERROR = property(NEW_TAG, "regex");
  private static final long serialVersionUID = -1880458092354113415L;
  protected HorizontalLayout tagsLayout = new HorizontalLayout();
  protected List<Button> tags = new ArrayList<>();
  protected ComboBox<String> newTag = new ComboBox<>();
  private Validator<String> validator;

  /**
   * Creates a tags field.
   */
  public TagsField() {
    HorizontalLayout layout = new HorizontalLayout(tagsLayout, newTag);
    layout.addClassName(CLASS_NAME);
    layout.setSpacing(false);
    tagsLayout.setSpacing(false);
    this.getElement().appendChild(layout.getElement());
    newTag.addClassName(NEW_TAG);
    newTag.setAllowCustomValue(true);
    newTag.addCustomValueSetListener(e -> {
      newTag.setInvalid(false);
      ValidationResult result =
          validator.apply(e.getDetail(), new ValueContext(newTag, newTag, getLocale()));
      if (result.isError()) {
        newTag.setInvalid(true);
        newTag.setErrorMessage(result.getErrorMessage());
      } else {
        addTag(e.getDetail());
      }
    });
    newTag.addValueChangeListener(e -> addTag(e.getValue()));
    newTag.setItems();
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    AppResources resources = new AppResources(TagsField.class, getLocale());
    validator = new RegexpValidator(resources.message(NEW_TAG_REGEX_ERROR), NEW_TAG_REGEX);
  }

  public void setTagSuggestions(List<String> suggestions) {
    newTag.setItems(suggestions);
  }

  @Override
  protected Set<String> generateModelValue() {
    return tags.stream().map(button -> button.getText()).collect(Collectors.toSet());
  }

  @Override
  protected void setPresentationValue(Set<String> newPresentationValue) {
    tagsLayout.removeAll();
    tags.clear();
    if (newPresentationValue == null) {
      return;
    }
    Set<String> values = new HashSet<>();
    for (String tag : newPresentationValue) {
      if (values.add(tag)) {
        addTag(tag);
      }
    }
  }

  private void addTag(String tag) {
    if (tag != null && !tag.isEmpty() && !tags.stream().map(button -> button.getText())
        .filter(value -> tag.equals(value)).findAny().isPresent()) {
      Button button = new Button();
      button.addClassName(TAG);
      button.setText(tag);
      button.setIcon(VaadinIcon.CLOSE_CIRCLE_O.create());
      button.setIconAfterText(true);
      button.addClickListener(e -> removeTag(button));
      tagsLayout.add(button);
      tags.add(button);
      newTag.clear();
      updateValue();
    }
  }

  private void removeTag(Button button) {
    tagsLayout.remove(button);
    tags.remove(button);
    updateValue();
  }

  /**
   * A tag.
   */
  protected static class Tag {
    private String tag;

    public String getTag() {
      return tag;
    }

    public void setTag(String tag) {
      this.tag = tag;
    }
  }
}
