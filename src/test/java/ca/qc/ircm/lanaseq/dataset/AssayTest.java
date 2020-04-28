package ca.qc.ircm.lanaseq.dataset;

import static ca.qc.ircm.lanaseq.dataset.Assay.CHIP_EXO;
import static ca.qc.ircm.lanaseq.dataset.Assay.CHIP_SEQ;
import static ca.qc.ircm.lanaseq.dataset.Assay.MNASE_SEQ;
import static ca.qc.ircm.lanaseq.dataset.Assay.NET_SEQ;
import static ca.qc.ircm.lanaseq.dataset.Assay.NULL;
import static ca.qc.ircm.lanaseq.dataset.Assay.RNA_SEQ;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.Test;

public class AssayTest {
  @Test
  public void getLabel_English() {
    Locale locale = Locale.ENGLISH;
    assertEquals("Not applicable", NULL.getLabel(locale));
    assertEquals("MNaseSeq", MNASE_SEQ.getLabel(locale));
    assertEquals("ChIPSeq", CHIP_SEQ.getLabel(locale));
    assertEquals("ChIPexo", CHIP_EXO.getLabel(locale));
    assertEquals("NETseq", NET_SEQ.getLabel(locale));
    assertEquals("RNAseq", RNA_SEQ.getLabel(locale));
  }

  @Test
  public void getLabel_French() {
    Locale locale = Locale.FRENCH;
    assertEquals("Non applicable", NULL.getLabel(locale));
    assertEquals("MNaseSeq", MNASE_SEQ.getLabel(locale));
    assertEquals("ChIPSeq", CHIP_SEQ.getLabel(locale));
    assertEquals("ChIPexo", CHIP_EXO.getLabel(locale));
    assertEquals("NETseq", NET_SEQ.getLabel(locale));
    assertEquals("RNAseq", RNA_SEQ.getLabel(locale));
  }
}
