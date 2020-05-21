package ca.qc.ircm.lanaseq.web;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * List of tags field.
 */
@HtmlImport("styles/tags-field-styles.html")
public class TagsField extends CustomField<Set<String>> {
  public static final String CLASS_NAME = "tags-field";
  private static final long serialVersionUID = -1880458092354113415L;
  protected HorizontalLayout tagsLayout = new HorizontalLayout();
  protected List<Button> tags = new ArrayList<>();
  protected TextField newTag = new TextField();

  public TagsField() {
    HorizontalLayout layout = new HorizontalLayout(tagsLayout, newTag);
    layout.addClassName(CLASS_NAME);
    layout.setSpacing(false);
    tagsLayout.setSpacing(false);
    this.getElement().appendChild(layout.getElement());
    newTag.addThemeVariants(TextFieldVariant.LUMO_SMALL);
    newTag.setValueChangeMode(ValueChangeMode.EAGER);
    newTag.addKeyPressListener(Key.ENTER, e -> addTag(newTag.getValue()));
    newTag.addKeyDownListener(Key.BACKSPACE, e -> {
      if (newTag.isEmpty() && !tags.isEmpty()) {
        removeTag(tags.get(tags.size() - 1));
      }
    });
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
    if (!tag.isEmpty() && !tags.stream().map(button -> button.getText())
        .filter(value -> tag.equals(value)).findAny().isPresent()) {
      Button button = new Button();
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
}
