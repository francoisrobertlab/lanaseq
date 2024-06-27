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

import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.web.TagsField.CLASS_NAME;
import static ca.qc.ircm.lanaseq.web.TagsField.NEW_TAG_REGEX_ERROR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.combobox.ComboBoxBase.CustomValueSetEvent;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link TagsField}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class TagsFieldTest extends SpringUIUnitTest {
  private static final String MESSAGE_PREFIX = messagePrefix(TagsField.class);
  private TagsField tagsField;
  @Mock
  private LocaleChangeEvent localeChangeEvent;
  private Locale locale = ENGLISH;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    tagsField = new TagsField();
    when(localeChangeEvent.getLocale()).thenReturn(locale);
    tagsField.localeChange(localeChangeEvent);
  }

  @Test
  public void styles() {
    assertTrue(tagsField.hasClassName(CLASS_NAME));
  }

  @Test
  public void getSuggestions() {
    assertTrue(tagsField.getSuggestions().isEmpty());
    tagsField.setSuggestions(list("input", "chip"));
    List<String> suggestions = tagsField.getSuggestions();
    assertEquals(2, suggestions.size());
    assertTrue(suggestions.contains("input"));
    assertTrue(suggestions.contains("chip"));
  }

  @Test
  public void setSuggestions() {
    List<String> items = tagsField.getListDataView().getItems().toList();
    assertTrue(items.isEmpty());
    tagsField.setSuggestions(list("input", "chip"));
    items = tagsField.getListDataView().getItems().toList();
    assertEquals(2, items.size());
    assertTrue(items.contains("input"));
    assertTrue(items.contains("chip"));
  }

  @Test
  public void invalidCustom() {
    CustomValueSetEvent<MultiSelectComboBox<String>> event =
        new CustomValueSetEvent<>(tagsField, false, "chip?");
    fireEvent(tagsField, event);
    assertTrue(tagsField.getValue().isEmpty());
    assertTrue(tagsField.isInvalid());
    assertEquals(tagsField.getTranslation(MESSAGE_PREFIX + NEW_TAG_REGEX_ERROR),
        tagsField.getErrorMessage());
  }

  @Test
  public void noValues_AddCustom() {
    CustomValueSetEvent<MultiSelectComboBox<String>> event =
        new CustomValueSetEvent<>(tagsField, false, "chip");
    fireEvent(tagsField, event);
    Set<String> tags = tagsField.getValue();
    assertEquals(1, tags.size());
    assertTrue(tags.contains("chip"));
  }

  @Test
  public void noValues_Select() {
    tagsField.setSuggestions(list("input", "chip"));
    tagsField.select("chip");
    Set<String> tags = tagsField.getValue();
    assertEquals(1, tags.size());
    assertTrue(tags.contains("chip"));
  }

  @Test
  public void values_AddCustom() {
    tagsField.setValue(set("input", "rappa"));
    CustomValueSetEvent<MultiSelectComboBox<String>> event =
        new CustomValueSetEvent<>(tagsField, false, "chip");
    fireEvent(tagsField, event);
    Set<String> tags = tagsField.getValue();
    assertEquals(3, tags.size());
    assertTrue(tags.contains("input"));
    assertTrue(tags.contains("rappa"));
    assertTrue(tags.contains("chip"));
  }

  @Test
  public void values_Select() {
    tagsField.setSuggestions(list("input", "chip"));
    tagsField.setValue(set("input", "rappa"));
    tagsField.select("chip");
    Set<String> tags = tagsField.getValue();
    assertEquals(3, tags.size());
    assertTrue(tags.contains("input"));
    assertTrue(tags.contains("rappa"));
    assertTrue(tags.contains("chip"));
  }

  @Test
  public void deselect() {
    tagsField.setValue(set("input", "rappa"));
    tagsField.deselect("input");
    Set<String> tags = tagsField.getValue();
    assertEquals(1, tags.size());
    assertTrue(tags.contains("rappa"));
  }

  private Set<String> set(String... values) {
    return Stream.of(values).collect(Collectors.toSet());
  }

  private List<String> list(String... values) {
    return Stream.of(values).collect(Collectors.toList());
  }
}
