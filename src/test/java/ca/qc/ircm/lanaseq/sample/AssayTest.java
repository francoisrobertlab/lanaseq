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

package ca.qc.ircm.lanaseq.sample;

import static ca.qc.ircm.lanaseq.sample.Assay.CHEC_SEQ;
import static ca.qc.ircm.lanaseq.sample.Assay.CHIP_EXO;
import static ca.qc.ircm.lanaseq.sample.Assay.CHIP_SEQ;
import static ca.qc.ircm.lanaseq.sample.Assay.MNASE_SEQ;
import static ca.qc.ircm.lanaseq.sample.Assay.NET_SEQ;
import static ca.qc.ircm.lanaseq.sample.Assay.RNA_SEQ;
import static ca.qc.ircm.lanaseq.sample.Assay.getNullLabel;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.Test;

public class AssayTest {
  @Test
  public void getNullLabel_English() {
    Locale locale = Locale.ENGLISH;
    assertEquals("Not applicable", getNullLabel(locale));
  }

  @Test
  public void getNullLabel_French() {
    Locale locale = Locale.FRENCH;
    assertEquals("Non applicable", getNullLabel(locale));
  }

  @Test
  public void getLabel_English() {
    Locale locale = Locale.ENGLISH;
    assertEquals("ChEC-seq", CHEC_SEQ.getLabel(locale));
    assertEquals("ChIP-seq", CHIP_SEQ.getLabel(locale));
    assertEquals("ChIP-exo", CHIP_EXO.getLabel(locale));
    assertEquals("MNase-seq", MNASE_SEQ.getLabel(locale));
    assertEquals("NET-seq", NET_SEQ.getLabel(locale));
    assertEquals("RNA-seq", RNA_SEQ.getLabel(locale));
  }

  @Test
  public void getLabel_French() {
    Locale locale = Locale.FRENCH;
    assertEquals("ChEC-seq", CHEC_SEQ.getLabel(locale));
    assertEquals("ChIP-seq", CHIP_SEQ.getLabel(locale));
    assertEquals("ChIP-exo", CHIP_EXO.getLabel(locale));
    assertEquals("MNase-seq", MNASE_SEQ.getLabel(locale));
    assertEquals("NET-seq", NET_SEQ.getLabel(locale));
    assertEquals("RNA-seq", RNA_SEQ.getLabel(locale));
  }
}
