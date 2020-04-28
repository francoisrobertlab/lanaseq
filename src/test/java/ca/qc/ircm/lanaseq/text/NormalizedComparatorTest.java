/*
 * Copyright (c) 2006 Institut de recherches cliniques de Montreal (IRCM)
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

package ca.qc.ircm.lanaseq.text;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.experiment.Experiment;
import org.junit.Test;

public class NormalizedComparatorTest {
  @Test
  public void compare_Identity() {
    NormalizedComparator<String> comparator = new NormalizedComparator<>(s -> s);
    assertTrue(comparator.compare("bateau", "bàteau") == 0);
    assertTrue(comparator.compare("bateau", "bàteau") == 0);
    assertTrue(comparator.compare("bateau", "BÀTEAU") == 0);
    assertTrue(comparator.compare("BATEAU", "BÀTEAU") == 0);
    assertTrue(comparator.compare("bateau", "bâteau") == 0);
    assertTrue(comparator.compare("BATEAU", "BÂTEAU") == 0);
    assertTrue(comparator.compare("bateau", "bäteau") == 0);
    assertTrue(comparator.compare("BATEAU", "BÄTEAU") == 0);
    assertTrue(comparator.compare("pepin", "pépin") == 0);
    assertTrue(comparator.compare("pepin", "pèpin") == 0);
    assertTrue(comparator.compare("pepin", "pêpin") == 0);
    assertTrue(comparator.compare("pepin", "pëpin") == 0);
    assertTrue(comparator.compare("pepin", "pepîn") == 0);
    assertTrue(comparator.compare("pepin", "pepïn") == 0);
    assertTrue(comparator.compare("pepin", "PÉPIN") == 0);
    assertTrue(comparator.compare("pepon", "pepôn") == 0);
    assertTrue(comparator.compare("pepon", "pepön") == 0);
    assertTrue(comparator.compare("pepun", "pepùn") == 0);
    assertTrue(comparator.compare("pepun", "pepûn") == 0);
    assertTrue(comparator.compare("pepun", "pepün") == 0);
    assertTrue(comparator.compare("pepin", "pépîn") == 0);
    assertTrue(comparator.compare("pepin", "peqin") < 0);
    assertTrue(comparator.compare("peqin", "pepin") > 0);
    assertTrue(comparator.compare("pepin", "pepin1") < 0);
    assertTrue(comparator.compare("pepin1", "pepin") > 0);
    assertTrue(comparator.compare("pepin", "péqîn") < 0);
    assertTrue(comparator.compare("peqin", "pépîn") > 0);
    assertTrue(comparator.compare(null, "pepin") < 0);
    assertTrue(comparator.compare("pepin", null) > 0);
    assertTrue(comparator.compare("", "pepin") < 0);
    assertTrue(comparator.compare("pepin", "") > 0);
    // Test Polish, out of curiosity.
    assertTrue(comparator.compare("a", "ą") == 0);
    assertTrue(comparator.compare("c", "ć") == 0);
    assertTrue(comparator.compare("e", "ę") == 0);
    // Doesn't work because ł in Unicode is not l with a slash, but its own character.
    //assertTrue(comparator.compare("l", "ł") == 0);
    assertTrue(comparator.compare("n", "ń") == 0);
    assertTrue(comparator.compare("o", "ó") == 0);
    assertTrue(comparator.compare("s", "ś") == 0);
    assertTrue(comparator.compare("z", "ź") == 0);
    assertTrue(comparator.compare("z", "ż") == 0);
  }

  @Test
  public void compare_ExperimentName() {
    NormalizedComparator<Experiment> comparator = new NormalizedComparator<>(Experiment::getName);
    assertTrue(comparator.compare(expName("bateau"), expName("bàteau")) == 0);
    assertTrue(comparator.compare(expName("bateau"), expName("bàteau")) == 0);
    assertTrue(comparator.compare(expName("bateau"), expName("BÀTEAU")) == 0);
    assertTrue(comparator.compare(expName("BATEAU"), expName("BÀTEAU")) == 0);
    assertTrue(comparator.compare(expName("bateau"), expName("bâteau")) == 0);
    assertTrue(comparator.compare(expName("BATEAU"), expName("BÂTEAU")) == 0);
    assertTrue(comparator.compare(expName("bateau"), expName("bäteau")) == 0);
    assertTrue(comparator.compare(expName("BATEAU"), expName("BÄTEAU")) == 0);
    assertTrue(comparator.compare(expName("pepin"), expName("pépin")) == 0);
    assertTrue(comparator.compare(expName("pepin"), expName("pèpin")) == 0);
    assertTrue(comparator.compare(expName("pepin"), expName("pêpin")) == 0);
    assertTrue(comparator.compare(expName("pepin"), expName("pëpin")) == 0);
    assertTrue(comparator.compare(expName("pepin"), expName("pepîn")) == 0);
    assertTrue(comparator.compare(expName("pepin"), expName("pepïn")) == 0);
    assertTrue(comparator.compare(expName("pepin"), expName("PÉPIN")) == 0);
    assertTrue(comparator.compare(expName("pepon"), expName("pepôn")) == 0);
    assertTrue(comparator.compare(expName("pepon"), expName("pepön")) == 0);
    assertTrue(comparator.compare(expName("pepun"), expName("pepùn")) == 0);
    assertTrue(comparator.compare(expName("pepun"), expName("pepûn")) == 0);
    assertTrue(comparator.compare(expName("pepun"), expName("pepün")) == 0);
    assertTrue(comparator.compare(expName("pepin"), expName("pépîn")) == 0);
    assertTrue(comparator.compare(expName("pepin"), expName("peqin")) < 0);
    assertTrue(comparator.compare(expName("peqin"), expName("pepin")) > 0);
    assertTrue(comparator.compare(expName("pepin"), expName("pepin1")) < 0);
    assertTrue(comparator.compare(expName("pepin1"), expName("pepin")) > 0);
    assertTrue(comparator.compare(expName("pepin"), expName("péqîn")) < 0);
    assertTrue(comparator.compare(expName("peqin"), expName("pépîn")) > 0);
    assertTrue(comparator.compare(null, expName("pepin")) < 0);
    assertTrue(comparator.compare(expName(null), expName("pepin")) < 0);
    assertTrue(comparator.compare(expName("pepin"), null) > 0);
    assertTrue(comparator.compare(expName("pepin"), expName(null)) > 0);
    assertTrue(comparator.compare(expName(""), expName("pepin")) < 0);
    assertTrue(comparator.compare(expName("pepin"), expName("")) > 0);
    // Test Polish, out of curiosity.
    assertTrue(comparator.compare(expName("a"), expName("ą")) == 0);
    assertTrue(comparator.compare(expName("c"), expName("ć")) == 0);
    assertTrue(comparator.compare(expName("e"), expName("ę")) == 0);
    // Doesn't work because ł in Unicode is not l with a slash, but its own character.
    //assertTrue(comparator.compare(expName("l"), expName("ł")) == 0);
    assertTrue(comparator.compare(expName("n"), expName("ń")) == 0);
    assertTrue(comparator.compare(expName("o"), expName("ó")) == 0);
    assertTrue(comparator.compare(expName("s"), expName("ś")) == 0);
    assertTrue(comparator.compare(expName("z"), expName("ź")) == 0);
    assertTrue(comparator.compare(expName("z"), expName("ż")) == 0);
  }

  @Test
  public void getConverter_Identity() {
    NormalizedComparator<String> comparator = new NormalizedComparator<>(s -> s);
    assertEquals("test", comparator.getConverter().apply("test"));
    assertEquals("abc", comparator.getConverter().apply("abc"));
    assertEquals("pépîn", comparator.getConverter().apply("pépîn"));
    assertEquals(null, comparator.getConverter().apply(null));
  }

  @Test
  public void getConverter_ExperimentName() {
    NormalizedComparator<Experiment> comparator = new NormalizedComparator<>(Experiment::getName);
    assertEquals("test", comparator.getConverter().apply(expName("test")));
    assertEquals("abc", comparator.getConverter().apply(expName("abc")));
    assertEquals("pépîn", comparator.getConverter().apply(expName("pépîn")));
    assertEquals(null, comparator.getConverter().apply(expName(null)));
  }

  private Experiment expName(String name) {
    Experiment experiment = new Experiment();
    experiment.setName(name);
    return experiment;
  }
}
