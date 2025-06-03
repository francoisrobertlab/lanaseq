package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.web.KeywordsField.CLASS_NAME;
import static ca.qc.ircm.lanaseq.web.KeywordsField.NEW_KEYWORD_REGEX_ERROR;
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
 * Tests for {@link KeywordsField}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class KeywordsFieldTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(KeywordsField.class);
  private KeywordsField keywordsField;
  @Mock
  private LocaleChangeEvent localeChangeEvent;
  private final Locale locale = ENGLISH;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    keywordsField = new KeywordsField();
    when(localeChangeEvent.getLocale()).thenReturn(locale);
    keywordsField.localeChange(localeChangeEvent);
  }

  @Test
  public void styles() {
    assertTrue(keywordsField.hasClassName(CLASS_NAME));
  }

  @Test
  public void getSuggestions() {
    assertTrue(keywordsField.getSuggestions().isEmpty());
    keywordsField.setSuggestions(list("input", "chip"));
    List<String> suggestions = keywordsField.getSuggestions();
    assertEquals(2, suggestions.size());
    assertTrue(suggestions.contains("input"));
    assertTrue(suggestions.contains("chip"));
  }

  @Test
  public void setSuggestions() {
    List<String> items = keywordsField.getListDataView().getItems().toList();
    assertTrue(items.isEmpty());
    keywordsField.setSuggestions(list("input", "chip"));
    items = keywordsField.getListDataView().getItems().toList();
    assertEquals(2, items.size());
    assertTrue(items.contains("input"));
    assertTrue(items.contains("chip"));
  }

  @Test
  public void invalidCustom() {
    CustomValueSetEvent<MultiSelectComboBox<String>> event = new CustomValueSetEvent<>(
        keywordsField, false, "chip?");
    fireEvent(keywordsField, event);
    assertTrue(keywordsField.getValue().isEmpty());
    assertTrue(keywordsField.isInvalid());
    assertEquals(keywordsField.getTranslation(MESSAGE_PREFIX + NEW_KEYWORD_REGEX_ERROR),
        keywordsField.getErrorMessage());
  }

  @Test
  public void noValues_AddCustom() {
    CustomValueSetEvent<MultiSelectComboBox<String>> event = new CustomValueSetEvent<>(
        keywordsField, false, "chip");
    fireEvent(keywordsField, event);
    Set<String> keywords = keywordsField.getValue();
    assertEquals(1, keywords.size());
    assertTrue(keywords.contains("chip"));
  }

  @Test
  public void noValues_Select() {
    keywordsField.setSuggestions(list("input", "chip"));
    keywordsField.select("chip");
    Set<String> keywords = keywordsField.getValue();
    assertEquals(1, keywords.size());
    assertTrue(keywords.contains("chip"));
  }

  @Test
  public void values_AddCustom() {
    keywordsField.setValue(set("input", "rappa"));
    CustomValueSetEvent<MultiSelectComboBox<String>> event = new CustomValueSetEvent<>(
        keywordsField, false, "chip");
    fireEvent(keywordsField, event);
    Set<String> keywords = keywordsField.getValue();
    assertEquals(3, keywords.size());
    assertTrue(keywords.contains("input"));
    assertTrue(keywords.contains("rappa"));
    assertTrue(keywords.contains("chip"));
  }

  @Test
  public void values_Select() {
    keywordsField.setSuggestions(list("input", "chip"));
    keywordsField.setValue(set("input", "rappa"));
    keywordsField.select("chip");
    Set<String> keywords = keywordsField.getValue();
    assertEquals(3, keywords.size());
    assertTrue(keywords.contains("input"));
    assertTrue(keywords.contains("rappa"));
    assertTrue(keywords.contains("chip"));
  }

  @Test
  public void deselect() {
    keywordsField.setValue(set("input", "rappa"));
    keywordsField.deselect("input");
    Set<String> keywords = keywordsField.getValue();
    assertEquals(1, keywords.size());
    assertTrue(keywords.contains("rappa"));
  }

  private Set<String> set(String... values) {
    return Stream.of(values).collect(Collectors.toSet());
  }

  private List<String> list(String... values) {
    return Stream.of(values).collect(Collectors.toList());
  }
}
