package ca.qc.ircm.lanaseq.dataset;

import static ca.qc.ircm.lanaseq.dataset.DatasetType.IMMUNO_PRECIPITATION;
import static ca.qc.ircm.lanaseq.dataset.DatasetType.INPUT;
import static ca.qc.ircm.lanaseq.dataset.DatasetType.NULL;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.Test;

public class DatasetTypeTest {
  @Test
  public void getLabel_English() {
    Locale locale = Locale.ENGLISH;
    assertEquals("Not applicable", NULL.getLabel(locale));
    assertEquals("IP", IMMUNO_PRECIPITATION.getLabel(locale));
    assertEquals("Input", INPUT.getLabel(locale));
  }

  @Test
  public void getLabel_French() {
    Locale locale = Locale.FRENCH;
    assertEquals("Non applicable", NULL.getLabel(locale));
    assertEquals("IP", IMMUNO_PRECIPITATION.getLabel(locale));
    assertEquals("Input", INPUT.getLabel(locale));
  }
}
