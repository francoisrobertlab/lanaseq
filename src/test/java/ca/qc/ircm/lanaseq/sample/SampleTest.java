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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Sample}.
 */
public class SampleTest {
  @Test
  public void generateName() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_NullSampleId() {
    Sample sample = new Sample();
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_InvalidSampleId() {
    Sample sample = new Sample();
    sample.setSampleId("F*R 1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_FrenchSampleId() {
    Sample sample = new Sample();
    sample.setSampleId("FÙR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FUR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_NullReplicate() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_20200508", sample.getName());
  }

  @Test
  public void generateName_InvalidReplicate() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("#R 1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_FrenchReplicate() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1ï");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1i_20200508", sample.getName());
  }

  @Test
  public void generateName_NullAssay() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_NullType() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_NullTarget() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_InvalidTarget() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt 16 ?");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_FrenchTarget() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Sptà16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spta16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_NullStrain() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_InvalidStrain() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR 101$");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_FrenchStrain() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yâFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yaFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_NullStrainWithDescription() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_NullStrainDescription() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_InvalidStrainDescription() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G 24D@");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_FrenchStrainDescription() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24DÉ");
    sample.setTreatment("IAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24DE_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_NullTreatment() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_InvalidTreatment() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("I AA!");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_FrenchTreatment() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IçAA");
    sample.setDate(LocalDate.of(2020, 5, 8));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IcAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_NullDate() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1", sample.getName());
  }

  @Test
  public void generateName_AllNull() {
    Sample sample = new Sample();
    sample.generateName();
    assertEquals("", sample.getName());
  }
}
