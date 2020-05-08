package ca.qc.ircm.lanaseq.dataset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.Test;

public class DatasetTest {
  @Test
  public void getFilename() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Spt16");
    dataset.setStrain("yFR101");
    dataset.setStrainDescription("G24D");
    dataset.setTreatment("IAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullAssay() {
    Dataset dataset = new Dataset();
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Spt16");
    dataset.setStrain("yFR101");
    dataset.setStrainDescription("G24D");
    dataset.setTreatment("IAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("IP_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullType() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setTarget("Spt16");
    dataset.setStrain("yFR101");
    dataset.setStrainDescription("G24D");
    dataset.setTreatment("IAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullTarget() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setStrain("yFR101");
    dataset.setStrainDescription("G24D");
    dataset.setTreatment("IAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_InvalidTarget() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Spt 16?");
    dataset.setStrain("yFR101");
    dataset.setStrainDescription("G24D");
    dataset.setTreatment("IAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_FrenchTarget() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Sptà16");
    dataset.setStrain("yFR101");
    dataset.setStrainDescription("G24D");
    dataset.setTreatment("IAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spta16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullStrain() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Spt16");
    dataset.setTreatment("IAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_InvalidStrain() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Spt16");
    dataset.setStrain("yFR 101$");
    dataset.setStrainDescription("G24D");
    dataset.setTreatment("IAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_FrenchStrain() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Spt16");
    dataset.setStrain("yâFR101");
    dataset.setStrainDescription("G24D");
    dataset.setTreatment("IAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yaFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullStrainWithDescription() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Spt16");
    dataset.setStrainDescription("G24D");
    dataset.setTreatment("IAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullStrainDescription() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Spt16");
    dataset.setStrain("yFR101");
    dataset.setTreatment("IAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_InvalidStrainDescription() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Spt16");
    dataset.setStrain("yFR101");
    dataset.setStrainDescription("G 24D@");
    dataset.setTreatment("IAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_FrenchStrainDescription() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Spt16");
    dataset.setStrain("yFR101");
    dataset.setStrainDescription("G24DÉ");
    dataset.setTreatment("IAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24DE_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullTreatment() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Spt16");
    dataset.setStrain("yFR101");
    dataset.setStrainDescription("G24D");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_InvalidTreatment() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Spt16");
    dataset.setStrain("yFR101");
    dataset.setStrainDescription("G24D");
    dataset.setTreatment("I AA!");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_FrenchTreatment() {
    Dataset dataset = new Dataset();
    dataset.setAssay(Assay.RNA_SEQ);
    dataset.setType(DatasetType.IMMUNO_PRECIPITATION);
    dataset.setTarget("Spt16");
    dataset.setStrain("yFR101");
    dataset.setStrainDescription("G24D");
    dataset.setTreatment("IçAA");
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IcAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_AllNull() {
    Dataset dataset = new Dataset();
    assertEquals("", dataset.getFilename());
  }
}
