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

package ca.qc.ircm.lana.text;

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.lana.text.Strings;
import org.junit.Test;

public class StringsTest {
  @Test
  public void property() {
    assertEquals("", Strings.property((Object) null));
    assertEquals("true", Strings.property(true));
    assertEquals("sample", Strings.property("sample"));
    assertEquals("sample", Strings.property("sample", null));
    assertEquals("sample.name", Strings.property("sample", null, "name"));
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
    assertEquals("", Strings.property((Object) null));
    assertEquals("true", Strings.styleName(true));
    assertEquals("sample", Strings.styleName("sample"));
    assertEquals("sample-true", Strings.styleName("sample", true));
    assertEquals("sample", Strings.styleName("sample", null));
    assertEquals("sample-name", Strings.styleName("sample", null, "name"));
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
    assertEquals("c", Strings.normalize("ć"));
    assertEquals("e", Strings.normalize("ę"));
    // Doesn't work because ł in Unicode is not l with a slash, but its own character.
    //assertEquals("l", Strings.normalize("ł"));
    assertEquals("n", Strings.normalize("ń"));
    assertEquals("o", Strings.normalize("ó"));
    assertEquals("s", Strings.normalize("ś"));
    assertEquals("z", Strings.normalize("ź"));
    assertEquals("z", Strings.normalize("ż"));
  }
}
