package ca.qc.ircm.lanaseq.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link NormalizedComparator}.
 */
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
  public void compare_DatasetName() {
    NormalizedComparator<TestName> comparator = new NormalizedComparator<>(TestName::getName);
    assertTrue(comparator.compare(name("bateau"), name("bàteau")) == 0);
    assertTrue(comparator.compare(name("bateau"), name("bàteau")) == 0);
    assertTrue(comparator.compare(name("bateau"), name("BÀTEAU")) == 0);
    assertTrue(comparator.compare(name("BATEAU"), name("BÀTEAU")) == 0);
    assertTrue(comparator.compare(name("bateau"), name("bâteau")) == 0);
    assertTrue(comparator.compare(name("BATEAU"), name("BÂTEAU")) == 0);
    assertTrue(comparator.compare(name("bateau"), name("bäteau")) == 0);
    assertTrue(comparator.compare(name("BATEAU"), name("BÄTEAU")) == 0);
    assertTrue(comparator.compare(name("pepin"), name("pépin")) == 0);
    assertTrue(comparator.compare(name("pepin"), name("pèpin")) == 0);
    assertTrue(comparator.compare(name("pepin"), name("pêpin")) == 0);
    assertTrue(comparator.compare(name("pepin"), name("pëpin")) == 0);
    assertTrue(comparator.compare(name("pepin"), name("pepîn")) == 0);
    assertTrue(comparator.compare(name("pepin"), name("pepïn")) == 0);
    assertTrue(comparator.compare(name("pepin"), name("PÉPIN")) == 0);
    assertTrue(comparator.compare(name("pepon"), name("pepôn")) == 0);
    assertTrue(comparator.compare(name("pepon"), name("pepön")) == 0);
    assertTrue(comparator.compare(name("pepun"), name("pepùn")) == 0);
    assertTrue(comparator.compare(name("pepun"), name("pepûn")) == 0);
    assertTrue(comparator.compare(name("pepun"), name("pepün")) == 0);
    assertTrue(comparator.compare(name("pepin"), name("pépîn")) == 0);
    assertTrue(comparator.compare(name("pepin"), name("peqin")) < 0);
    assertTrue(comparator.compare(name("peqin"), name("pepin")) > 0);
    assertTrue(comparator.compare(name("pepin"), name("pepin1")) < 0);
    assertTrue(comparator.compare(name("pepin1"), name("pepin")) > 0);
    assertTrue(comparator.compare(name("pepin"), name("péqîn")) < 0);
    assertTrue(comparator.compare(name("peqin"), name("pépîn")) > 0);
    assertTrue(comparator.compare(name(""), name("pepin")) < 0);
    assertTrue(comparator.compare(name("pepin"), name("")) > 0);
    // Test Polish, out of curiosity.
    assertTrue(comparator.compare(name("a"), name("ą")) == 0);
    assertTrue(comparator.compare(name("c"), name("ć")) == 0);
    assertTrue(comparator.compare(name("e"), name("ę")) == 0);
    // Doesn't work because ł in Unicode is not l with a slash, but its own character.
    //assertTrue(comparator.compare(name("l"), name("ł")) == 0);
    assertTrue(comparator.compare(name("n"), name("ń")) == 0);
    assertTrue(comparator.compare(name("o"), name("ó")) == 0);
    assertTrue(comparator.compare(name("s"), name("ś")) == 0);
    assertTrue(comparator.compare(name("z"), name("ź")) == 0);
    assertTrue(comparator.compare(name("z"), name("ż")) == 0);
  }

  @Test
  public void getConverter_Identity() {
    NormalizedComparator<String> comparator = new NormalizedComparator<>(s -> s);
    assertEquals("test", comparator.getConverter().apply("test"));
    assertEquals("abc", comparator.getConverter().apply("abc"));
    assertEquals("pépîn", comparator.getConverter().apply("pépîn"));
  }

  @Test
  public void getConverter_DatasetName() {
    NormalizedComparator<TestName> comparator = new NormalizedComparator<>(TestName::getName);
    assertEquals("test", comparator.getConverter().apply(name("test")));
    assertEquals("abc", comparator.getConverter().apply(name("abc")));
    assertEquals("pépîn", comparator.getConverter().apply(name("pépîn")));
  }

  private TestName name(String name) {
    TestName testName = new TestName();
    testName.name = name;
    return testName;
  }

  private static class TestName {
    private String name;

    public String getName() {
      return name;
    }
  }
}
