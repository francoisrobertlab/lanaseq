package ca.qc.ircm.lanaseq.dataset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.sample.Sample;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.Test;

public class DatasetTest {
  @Test
  public void getFilename() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullAssay() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("IP_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullType() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullTarget() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_InvalidTarget() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt 16?");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_FrenchTarget() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Sptà16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spta16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullStrain() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_InvalidStrain() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR 101$");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_FrenchStrain() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yâFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yaFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullStrainWithDescription() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullStrainDescription() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_InvalidStrainDescription() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G 24D@");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_FrenchStrainDescription() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24DÉ");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24DE_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_NullTreatment() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_InvalidTreatment() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("I AA!");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_FrenchTreatment() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setAssay(Assay.RNA_SEQ);
    sample.setType(DatasetType.IMMUNO_PRECIPITATION);
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IçAA");
    dataset.getSamples().add(sample);
    dataset.setDate(LocalDateTime.of(2020, 5, 8, 15, 36, 21));
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IcAA_20200508", dataset.getFilename());
  }

  @Test
  public void getFilename_AllNull() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    dataset.getSamples().add(sample);
    assertEquals("", dataset.getFilename());
  }
}
