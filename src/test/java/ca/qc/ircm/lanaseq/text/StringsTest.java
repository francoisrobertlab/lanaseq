package ca.qc.ircm.lanaseq.text;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Strings}.
 */
public class StringsTest {

  @Test
  public void property() {
    assertEquals("true", Strings.property(true));
    assertEquals("sample", Strings.property("sample"));
    assertEquals("sample.true", Strings.property("sample", true));
    assertEquals("sample.name", Strings.property("sample.name"));
    assertEquals("sample.name", Strings.property("sample", "name"));
    assertEquals("sample.standards.name", Strings.property("sample.standards.name"));
    assertEquals("sample.standards.name", Strings.property("sample.standards", "name"));
    assertEquals("sample.standards.name", Strings.property("sample", "standards.name"));
    assertEquals("sample.standards.name", Strings.property("sample", "standards", "name"));
  }

  @Test
  public void styleName() {
    assertEquals("true", Strings.styleName(true));
    assertEquals("sample", Strings.styleName("sample"));
    assertEquals("sample-true", Strings.styleName("sample", true));
    assertEquals("sample-name", Strings.styleName("sample-name"));
    assertEquals("sample-name", Strings.styleName("sample.name"));
    assertEquals("sample-name", Strings.styleName("sample", "name"));
    assertEquals("sample-standards-name", Strings.styleName("sample-standards-name"));
    assertEquals("sample-standards-name", Strings.styleName("sample.standards.name"));
    assertEquals("sample-standards-name", Strings.styleName("sample.standards-name"));
    assertEquals("sample-standards-name", Strings.styleName("sample-standards.name"));
    assertEquals("sample-standards-name", Strings.styleName("sample-standards", "name"));
    assertEquals("sample-standards-name", Strings.styleName("sample.standards", "name"));
    assertEquals("sample-standards-name", Strings.styleName("sample", "standards-name"));
    assertEquals("sample-standards-name", Strings.styleName("sample", "standards.name"));
    assertEquals("sample-standards-name", Strings.styleName("sample", "standards", "name"));
  }

  @Test
  public void normalize() {
    assertEquals("bateau", Strings.normalize("bàteau"));
    assertEquals("BATEAU", Strings.normalize("BÀTEAU"));
    assertEquals("bateau", Strings.normalize("bâteau"));
    assertEquals("BATEAU", Strings.normalize("BÂTEAU"));
    assertEquals("bateau", Strings.normalize("bäteau"));
    assertEquals("BATEAU", Strings.normalize("BÄTEAU"));
    assertEquals("pepin", Strings.normalize("pépin"));
    assertEquals("pepin", Strings.normalize("pèpin"));
    assertEquals("pepin", Strings.normalize("pêpin"));
    assertEquals("pepin", Strings.normalize("pëpin"));
    assertEquals("pepin", Strings.normalize("pepîn"));
    assertEquals("pepin", Strings.normalize("pepïn"));
    assertEquals("pepon", Strings.normalize("pepôn"));
    assertEquals("pepon", Strings.normalize("pepön"));
    assertEquals("pepun", Strings.normalize("pepùn"));
    assertEquals("pepun", Strings.normalize("pepûn"));
    assertEquals("pepun", Strings.normalize("pepün"));
    assertEquals("pepin", Strings.normalize("pépîn"));
    // Test Polish, out of curiosity.
    assertEquals("a", Strings.normalize("ą"));
    assertEquals("A", Strings.normalize("Ą"));
    assertEquals("c", Strings.normalize("ć"));
    assertEquals("C", Strings.normalize("Ć"));
    assertEquals("e", Strings.normalize("ę"));
    assertEquals("E", Strings.normalize("Ę"));
    // Doesn't work because ł in Unicode is not l with a slash, but its own character.
    //assertEquals("l", Strings.normalize("ł"));
    assertEquals("n", Strings.normalize("ń"));
    assertEquals("N", Strings.normalize("Ń"));
    assertEquals("o", Strings.normalize("ó"));
    assertEquals("O", Strings.normalize("Ó"));
    assertEquals("s", Strings.normalize("ś"));
    assertEquals("S", Strings.normalize("Ś"));
    assertEquals("z", Strings.normalize("ź"));
    assertEquals("Z", Strings.normalize("Ź"));
    assertEquals("z", Strings.normalize("ż"));
    assertEquals("Z", Strings.normalize("Ż"));
  }

  @Test
  public void comparable() {
    assertEquals("bateau", Strings.comparable("bàteau"));
    assertEquals("bateau", Strings.comparable("BÀTEAU"));
    assertEquals("bateau", Strings.comparable("bâteau"));
    assertEquals("bateau", Strings.comparable("BÂTEAU"));
    assertEquals("bateau", Strings.comparable("bäteau"));
    assertEquals("bateau", Strings.comparable("BÄTEAU"));
    assertEquals("pepin", Strings.comparable("pépin"));
    assertEquals("pepin", Strings.comparable("pèpin"));
    assertEquals("pepin", Strings.comparable("pêpin"));
    assertEquals("pepin", Strings.comparable("pëpin"));
    assertEquals("pepin", Strings.comparable("pepîn"));
    assertEquals("pepin", Strings.comparable("pepïn"));
    assertEquals("pepon", Strings.comparable("pepôn"));
    assertEquals("pepon", Strings.comparable("pepön"));
    assertEquals("pepun", Strings.comparable("pepùn"));
    assertEquals("pepun", Strings.comparable("pepûn"));
    assertEquals("pepun", Strings.comparable("pepün"));
    assertEquals("pepin", Strings.comparable("pépîn"));
    // Test Polish, out of curiosity.
    assertEquals("a", Strings.comparable("ą"));
    assertEquals("a", Strings.comparable("Ą"));
    assertEquals("c", Strings.comparable("ć"));
    assertEquals("c", Strings.comparable("Ć"));
    assertEquals("e", Strings.comparable("ę"));
    assertEquals("e", Strings.comparable("Ę"));
    // Doesn't work because ł in Unicode is not l with a slash, but its own character.
    //assertEquals("l", Strings.comparable("ł"));
    assertEquals("n", Strings.comparable("ń"));
    assertEquals("n", Strings.comparable("Ń"));
    assertEquals("o", Strings.comparable("ó"));
    assertEquals("o", Strings.comparable("Ó"));
    assertEquals("s", Strings.comparable("ś"));
    assertEquals("s", Strings.comparable("Ś"));
    assertEquals("z", Strings.comparable("ź"));
    assertEquals("z", Strings.comparable("Ź"));
    assertEquals("z", Strings.comparable("ż"));
    assertEquals("z", Strings.comparable("Ż"));
  }
}
