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
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.clickButton;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.items;
import static ca.qc.ircm.lanaseq.web.TagsField.CLASS_NAME;
import static ca.qc.ircm.lanaseq.web.TagsField.NEW_TAG;
import static ca.qc.ircm.lanaseq.web.TagsField.NEW_TAG_REGEX_ERROR;
import static ca.qc.ircm.lanaseq.web.TagsField.TAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.test.config.AbstractViewTestCase;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.combobox.GeneratedVaadinComboBox.CustomValueSetEvent;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
public class TagsFieldTest extends AbstractViewTestCase {
  private TagsField tagsField;
  @Mock
  private LocaleChangeEvent localeChangeEvent;
  private Locale locale = ENGLISH;
  private AppResources resources = new AppResources(TagsField.class, locale);

  @Before
  public void beforeTest() {
    when(ui.getLocale()).thenReturn(locale);
    tagsField = new TagsField();
    when(localeChangeEvent.getLocale()).thenReturn(locale);
    tagsField.localeChange(localeChangeEvent);
  }

  @Test
  public void styles() {
    assertTrue(
        tagsField.getChildren().findFirst().get().getElement().getClassList().contains(CLASS_NAME));
    tagsField.setValue(set("test", "test2"));
    for (int i = 0; i < 2; i++) {
      assertTrue(tagsField.tags.get(i).hasClassName(TAG));
    }
    assertTrue(tagsField.newTag.hasClassName(NEW_TAG));
  }

  @Test
  public void generateModelValue_Empty() {
    Set<String> tags = tagsField.generateModelValue();
    assertNotNull(tags);
    assertTrue(tags.isEmpty());
  }

  @Test
  public void generateModelValue_One() {
    tagsField.setValue(set("chip"));
    Set<String> tags = tagsField.generateModelValue();
    assertNotNull(tags);
    assertEquals(1, tags.size());
    assertTrue(tags.contains("chip"));
  }

  @Test
  public void generateModelValue_Two() {
    tagsField.setValue(set("chip", "input"));
    Set<String> tags = tagsField.generateModelValue();
    assertNotNull(tags);
    assertEquals(2, tags.size());
    assertTrue(tags.contains("chip"));
    assertTrue(tags.contains("input"));
  }

  @Test
  public void setPresentationValue_Empty() {
    tagsField.setPresentationValue(set());
    assertTrue(tagsField.tags.isEmpty());
  }

  @Test
  public void setPresentationValue_One() {
    tagsField.setPresentationValue(set("chip"));
    assertEquals(1, tagsField.tags.size());
    assertEquals("chip", tagsField.tags.get(0).getText());
  }

  @Test
  public void setPresentationValue_Two() {
    tagsField.setPresentationValue(set("chip", "input"));
    assertEquals(2, tagsField.tags.size());
    List<String> tags =
        tagsField.tags.stream().map(button -> button.getText()).collect(Collectors.toList());
    assertTrue(tags.contains("chip"));
    assertTrue(tags.contains("input"));
  }

  @Test
  public void setPresentationValue_Null() {
    tagsField.setPresentationValue(null);
    assertTrue(tagsField.tags.isEmpty());
  }

  @Test
  public void setTagSuggestions() {
    List<String> items = items(tagsField.newTag);
    assertTrue(items.isEmpty());
    tagsField.setTagSuggestions(list("input", "chip"));
    items = items(tagsField.newTag);
    assertEquals(2, items.size());
    assertTrue(items.contains("input"));
    assertTrue(items.contains("chip"));
  }

  @Test
  public void newTag_EmptyNew() {
    CustomValueSetEvent<ComboBox<String>> event =
        new CustomValueSetEvent<>(tagsField.newTag, false, "");
    fireEvent(tagsField.newTag, event);
    assertTrue(tagsField.tags.isEmpty());
    assertFalse(tagsField.newTag.isInvalid());
  }

  @Test
  public void newTag_InvalidNew() {
    CustomValueSetEvent<ComboBox<String>> event =
        new CustomValueSetEvent<>(tagsField.newTag, false, "chip?");
    fireEvent(tagsField.newTag, event);
    assertTrue(tagsField.tags.isEmpty());
    assertTrue(tagsField.newTag.isInvalid());
    assertEquals(resources.message(NEW_TAG_REGEX_ERROR), tagsField.newTag.getErrorMessage());
  }

  @Test
  public void newTag_NoTagsNew() {
    CustomValueSetEvent<ComboBox<String>> event =
        new CustomValueSetEvent<>(tagsField.newTag, false, "chip");
    fireEvent(tagsField.newTag, event);
    assertEquals(1, tagsField.tags.size());
    assertEquals("chip", tagsField.tags.get(0).getText());
    Set<String> tags = tagsField.generateModelValue();
    assertEquals(1, tags.size());
    assertTrue(tags.contains("chip"));
  }

  @Test
  public void newTag_NoTagsExisting() {
    tagsField.setTagSuggestions(list("input", "chip"));
    tagsField.newTag.setValue("chip");
    assertEquals(1, tagsField.tags.size());
    assertEquals("chip", tagsField.tags.get(0).getText());
    Set<String> tags = tagsField.generateModelValue();
    assertEquals(1, tags.size());
    assertTrue(tags.contains("chip"));
  }

  @Test
  public void newTag_WithOtherTagsNew() {
    tagsField.setValue(set("input", "rappa"));
    CustomValueSetEvent<ComboBox<String>> event =
        new CustomValueSetEvent<>(tagsField.newTag, false, "chip");
    fireEvent(tagsField.newTag, event);
    assertEquals(3, tagsField.tags.size());
    assertEquals("input", tagsField.tags.get(0).getText());
    assertEquals("rappa", tagsField.tags.get(1).getText());
    assertEquals("chip", tagsField.tags.get(2).getText());
    Set<String> tags = tagsField.generateModelValue();
    assertEquals(3, tags.size());
    assertTrue(tags.contains("input"));
    assertTrue(tags.contains("rappa"));
    assertTrue(tags.contains("chip"));
  }

  @Test
  public void newTag_WithOtherTagsExisting() {
    tagsField.setTagSuggestions(list("input", "chip"));
    tagsField.setValue(set("input", "rappa"));
    tagsField.newTag.setValue("chip");
    assertEquals(3, tagsField.tags.size());
    assertEquals("input", tagsField.tags.get(0).getText());
    assertEquals("rappa", tagsField.tags.get(1).getText());
    assertEquals("chip", tagsField.tags.get(2).getText());
    Set<String> tags = tagsField.generateModelValue();
    assertEquals(3, tags.size());
    assertTrue(tags.contains("input"));
    assertTrue(tags.contains("rappa"));
    assertTrue(tags.contains("chip"));
  }

  @Test
  public void removeTag_ButtonClick() {
    tagsField.setValue(set("input", "rappa"));
    clickButton(tagsField.tags.get(0));
    assertEquals(1, tagsField.tags.size());
    assertEquals("rappa", tagsField.tags.get(0).getText());
    Set<String> tags = tagsField.generateModelValue();
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
