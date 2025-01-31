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
    assertEquals(0, comparator.compare("bateau", "bàteau"));
    assertEquals(0, comparator.compare("bateau", "bàteau"));
    assertEquals(0, comparator.compare("bateau", "BÀTEAU"));
    assertEquals(0, comparator.compare("BATEAU", "BÀTEAU"));
    assertEquals(0, comparator.compare("bateau", "bâteau"));
    assertEquals(0, comparator.compare("BATEAU", "BÂTEAU"));
    assertEquals(0, comparator.compare("bateau", "bäteau"));
    assertEquals(0, comparator.compare("BATEAU", "BÄTEAU"));
    assertEquals(0, comparator.compare("pepin", "pépin"));
    assertEquals(0, comparator.compare("pepin", "pèpin"));
    assertEquals(0, comparator.compare("pepin", "pêpin"));
    assertEquals(0, comparator.compare("pepin", "pëpin"));
    assertEquals(0, comparator.compare("pepin", "pepîn"));
    assertEquals(0, comparator.compare("pepin", "pepïn"));
    assertEquals(0, comparator.compare("pepin", "PÉPIN"));
    assertEquals(0, comparator.compare("pepon", "pepôn"));
    assertEquals(0, comparator.compare("pepon", "pepön"));
    assertEquals(0, comparator.compare("pepun", "pepùn"));
    assertEquals(0, comparator.compare("pepun", "pepûn"));
    assertEquals(0, comparator.compare("pepun", "pepün"));
    assertEquals(0, comparator.compare("pepin", "pépîn"));
    assertTrue(comparator.compare("pepin", "peqin") < 0);
    assertTrue(comparator.compare("peqin", "pepin") > 0);
    assertTrue(comparator.compare("pepin", "pepin1") < 0);
    assertTrue(comparator.compare("pepin1", "pepin") > 0);
    assertTrue(comparator.compare("pepin", "péqîn") < 0);
    assertTrue(comparator.compare("peqin", "pépîn") > 0);
    assertTrue(comparator.compare("", "pepin") < 0);
    assertTrue(comparator.compare("pepin", "") > 0);
    // Test Polish, out of curiosity.
    assertEquals(0, comparator.compare("a", "ą"));
    assertEquals(0, comparator.compare("c", "ć"));
    assertEquals(0, comparator.compare("e", "ę"));
    // Doesn't work because ł in Unicode is not l with a slash, but its own character.
    //assertTrue(comparator.compare("l", "ł") == 0);
    assertEquals(0, comparator.compare("n", "ń"));
    assertEquals(0, comparator.compare("o", "ó"));
    assertEquals(0, comparator.compare("s", "ś"));
    assertEquals(0, comparator.compare("z", "ź"));
    assertEquals(0, comparator.compare("z", "ż"));
  }

  @Test
  public void compare_DatasetName() {
    NormalizedComparator<TestName> comparator = new NormalizedComparator<>(TestName::getName);
    assertEquals(0, comparator.compare(name("bateau"), name("bàteau")));
    assertEquals(0, comparator.compare(name("bateau"), name("bàteau")));
    assertEquals(0, comparator.compare(name("bateau"), name("BÀTEAU")));
    assertEquals(0, comparator.compare(name("BATEAU"), name("BÀTEAU")));
    assertEquals(0, comparator.compare(name("bateau"), name("bâteau")));
    assertEquals(0, comparator.compare(name("BATEAU"), name("BÂTEAU")));
    assertEquals(0, comparator.compare(name("bateau"), name("bäteau")));
    assertEquals(0, comparator.compare(name("BATEAU"), name("BÄTEAU")));
    assertEquals(0, comparator.compare(name("pepin"), name("pépin")));
    assertEquals(0, comparator.compare(name("pepin"), name("pèpin")));
    assertEquals(0, comparator.compare(name("pepin"), name("pêpin")));
    assertEquals(0, comparator.compare(name("pepin"), name("pëpin")));
    assertEquals(0, comparator.compare(name("pepin"), name("pepîn")));
    assertEquals(0, comparator.compare(name("pepin"), name("pepïn")));
    assertEquals(0, comparator.compare(name("pepin"), name("PÉPIN")));
    assertEquals(0, comparator.compare(name("pepon"), name("pepôn")));
    assertEquals(0, comparator.compare(name("pepon"), name("pepön")));
    assertEquals(0, comparator.compare(name("pepun"), name("pepùn")));
    assertEquals(0, comparator.compare(name("pepun"), name("pepûn")));
    assertEquals(0, comparator.compare(name("pepun"), name("pepün")));
    assertEquals(0, comparator.compare(name("pepin"), name("pépîn")));
    assertTrue(comparator.compare(name("pepin"), name("peqin")) < 0);
    assertTrue(comparator.compare(name("peqin"), name("pepin")) > 0);
    assertTrue(comparator.compare(name("pepin"), name("pepin1")) < 0);
    assertTrue(comparator.compare(name("pepin1"), name("pepin")) > 0);
    assertTrue(comparator.compare(name("pepin"), name("péqîn")) < 0);
    assertTrue(comparator.compare(name("peqin"), name("pépîn")) > 0);
    assertTrue(comparator.compare(name(""), name("pepin")) < 0);
    assertTrue(comparator.compare(name("pepin"), name("")) > 0);
    // Test Polish, out of curiosity.
    assertEquals(0, comparator.compare(name("a"), name("ą")));
    assertEquals(0, comparator.compare(name("c"), name("ć")));
    assertEquals(0, comparator.compare(name("e"), name("ę")));
    // Doesn't work because ł in Unicode is not l with a slash, but its own character.
    //assertTrue(comparator.compare(name("l"), name("ł")) == 0);
    assertEquals(0, comparator.compare(name("n"), name("ń")));
    assertEquals(0, comparator.compare(name("o"), name("ó")));
    assertEquals(0, comparator.compare(name("s"), name("ś")));
    assertEquals(0, comparator.compare(name("z"), name("ź")));
    assertEquals(0, comparator.compare(name("z"), name("ż")));
  }

  @Test
  public void getConverter_Identity() {
    NormalizedComparator<String> comparator = new NormalizedComparator<>(s -> s);
    assertEquals("test", comparator.converter().apply("test"));
    assertEquals("abc", comparator.converter().apply("abc"));
    assertEquals("pépîn", comparator.converter().apply("pépîn"));
  }

  @Test
  public void getConverter_DatasetName() {
    NormalizedComparator<TestName> comparator = new NormalizedComparator<>(TestName::getName);
    assertEquals("test", comparator.converter().apply(name("test")));
    assertEquals("abc", comparator.converter().apply(name("abc")));
    assertEquals("pépîn", comparator.converter().apply(name("pépîn")));
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
