package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.text.Strings.property;

import ca.qc.ircm.lanaseq.AppResources;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.RegexpValidator;
import com.vaadin.flow.data.value.ValueChangeMode;
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
  protected TextField newTag = new TextField();
  private Binder<Tag> binder = new Binder<>(Tag.class);

  public TagsField() {
    HorizontalLayout layout = new HorizontalLayout(tagsLayout, newTag);
    layout.addClassName(CLASS_NAME);
    layout.setSpacing(false);
    tagsLayout.setSpacing(false);
    this.getElement().appendChild(layout.getElement());
    newTag.addClassName(NEW_TAG);
    newTag.addThemeVariants(TextFieldVariant.LUMO_SMALL);
    newTag.setValueChangeMode(ValueChangeMode.EAGER);
    newTag.addKeyPressListener(Key.ENTER, e -> addTag());
    newTag.addKeyDownListener(Key.BACKSPACE, e -> {
      if (newTag.isEmpty() && !tags.isEmpty()) {
        removeTag(tags.get(tags.size() - 1));
      }
    });
    binder.setBean(new Tag());
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    AppResources resources = new AppResources(TagsField.class, getLocale());
    binder.forField(newTag)
        .withValidator(new RegexpValidator(resources.message(NEW_TAG_REGEX_ERROR), NEW_TAG_REGEX))
        .bind("tag");
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

  private void addTag() {
    if (binder.validate().isOk()) {
      addTag(newTag.getValue());
    }
  }

  private void addTag(String tag) {
    if (!tag.isEmpty() && !tags.stream().map(button -> button.getText())
        .filter(value -> tag.equals(value)).findAny().isPresent()) {
      Button button = new Button();
      button.addClassName(TAG);
      button.addThemeVariants(ButtonVariant.LUMO_SMALL);
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
