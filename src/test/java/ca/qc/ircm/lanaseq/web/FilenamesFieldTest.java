package ca.qc.ircm.lanaseq.web;

import static ca.qc.ircm.lanaseq.Constants.ENGLISH;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static ca.qc.ircm.lanaseq.web.FilenamesField.CLASS_NAME;
import static ca.qc.ircm.lanaseq.web.FilenamesField.NEW_FILENAME_REGEX_ERROR;
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
 * Tests for {@link FilenamesField}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class FilenamesFieldTest extends SpringUIUnitTest {
  private static final String MESSAGE_PREFIX = messagePrefix(FilenamesField.class);
  private FilenamesField filenamesField;
  @Mock
  private LocaleChangeEvent localeChangeEvent;
  private Locale locale = ENGLISH;

  /**
   * Before test.
   */
  @BeforeEach
  public void beforeTest() {
    UI.getCurrent().setLocale(locale);
    filenamesField = new FilenamesField();
    when(localeChangeEvent.getLocale()).thenReturn(locale);
    filenamesField.localeChange(localeChangeEvent);
  }

  @Test
  public void styles() {
    assertTrue(filenamesField.hasClassName(CLASS_NAME));
  }

  @Test
  public void getSuggestions() {
    assertTrue(filenamesField.getSuggestions().isEmpty());
    filenamesField.setSuggestions(list("input", "chip"));
    List<String> suggestions = filenamesField.getSuggestions();
    assertEquals(2, suggestions.size());
    assertTrue(suggestions.contains("input"));
    assertTrue(suggestions.contains("chip"));
  }

  @Test
  public void setSuggestions() {
    List<String> items = filenamesField.getListDataView().getItems().toList();
    assertTrue(items.isEmpty());
    filenamesField.setSuggestions(list("input", "chip"));
    items = filenamesField.getListDataView().getItems().toList();
    assertEquals(2, items.size());
    assertTrue(items.contains("input"));
    assertTrue(items.contains("chip"));
  }

  @Test
  public void invalidCustom() {
    CustomValueSetEvent<MultiSelectComboBox<String>> event =
        new CustomValueSetEvent<>(filenamesField, false, "chip?");
    fireEvent(filenamesField, event);
    assertTrue(filenamesField.getValue().isEmpty());
    assertTrue(filenamesField.isInvalid());
    assertEquals(filenamesField.getTranslation(MESSAGE_PREFIX + NEW_FILENAME_REGEX_ERROR),
        filenamesField.getErrorMessage());
  }

  @Test
  public void noValues_AddCustom() {
    CustomValueSetEvent<MultiSelectComboBox<String>> event =
        new CustomValueSetEvent<>(filenamesField, false, "chip");
    fireEvent(filenamesField, event);
    Set<String> keywords = filenamesField.getValue();
    assertEquals(1, keywords.size());
    assertTrue(keywords.contains("chip"));
  }

  @Test
  public void noValues_Select() {
    filenamesField.setSuggestions(list("input", "chip"));
    filenamesField.select("chip");
    Set<String> keywords = filenamesField.getValue();
    assertEquals(1, keywords.size());
    assertTrue(keywords.contains("chip"));
  }

  @Test
  public void values_AddCustom() {
    filenamesField.setValue(set("input", "rappa"));
    CustomValueSetEvent<MultiSelectComboBox<String>> event =
        new CustomValueSetEvent<>(filenamesField, false, "chip");
    fireEvent(filenamesField, event);
    Set<String> keywords = filenamesField.getValue();
    assertEquals(3, keywords.size());
    assertTrue(keywords.contains("input"));
    assertTrue(keywords.contains("rappa"));
    assertTrue(keywords.contains("chip"));
  }

  @Test
  public void values_Select() {
    filenamesField.setSuggestions(list("input", "chip"));
    filenamesField.setValue(set("input", "rappa"));
    filenamesField.select("chip");
    Set<String> keywords = filenamesField.getValue();
    assertEquals(3, keywords.size());
    assertTrue(keywords.contains("input"));
    assertTrue(keywords.contains("rappa"));
    assertTrue(keywords.contains("chip"));
  }

  @Test
  public void deselect() {
    filenamesField.setValue(set("input", "rappa"));
    filenamesField.deselect("input");
    Set<String> keywords = filenamesField.getValue();
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
