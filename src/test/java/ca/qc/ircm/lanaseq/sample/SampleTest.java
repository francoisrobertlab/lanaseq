package ca.qc.ircm.lanaseq.sample;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.dataset.Assay;
import ca.qc.ircm.lanaseq.dataset.DatasetType;
import java.time.LocalDateTime;
import org.junit.Test;

public class SampleTest {
  @Test
  public void generateName() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_NullSampleId() {
    Sample sample = new Sample();
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_InvalidSampleId() {
    Sample sample = new Sample();
    sample.setSampleId("F*R 1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_FrenchSampleId() {
    Sample sample = new Sample();
    sample.setSampleId("FÙR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FUR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_NullReplicate() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_20200508", sample.getName());
  }

  @Test
  public void generateName_InvalidReplicate() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("#R 1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_FrenchReplicate() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1ï");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1i_20200508", sample.getName());
  }

  @Test
  public void getFilename_NullAssay() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_NullType() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_NullTarget() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_InvalidTarget() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt 16 ?");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_FrenchTarget() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Sptà16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spta16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_NullStrain() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_InvalidStrain() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR 101$");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_FrenchStrain() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yâFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yaFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_NullStrainWithDescription() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_NullStrainDescription() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_InvalidStrainDescription() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G 24D@");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_FrenchStrainDescription() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24DÉ");
    sample.setTreatment("IAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24DE_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_NullTreatment() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_InvalidTreatment() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("I AA!");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1_20200508", sample.getName());
  }

  @Test
  public void getFilename_FrenchTreatment() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IçAA");
    sample.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IcAA_R1_20200508", sample.getName());
  }

  @Test
  public void generateName_NullDate() {
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    sample.generateName();
    assertEquals("FR1_RNAseq_IP_Spt16_yFR101_G24D_IAA_R1", sample.getName());
  }

  @Test
  public void getFilename_AllNull() {
    Sample sample = new Sample();
    sample.generateName();
    assertEquals("", sample.getName());
  }
}
