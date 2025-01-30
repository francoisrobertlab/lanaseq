package ca.qc.ircm.lanaseq.dataset;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.sample.Sample;
import java.time.LocalDate;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Dataset}.
 */
public class DatasetTest {

  private void copy(Sample from, Sample to) {
    to.setAssay(from.getAssay());
    to.setType(from.getType());
    to.setTarget(from.getTarget());
    to.setStrain(from.getStrain());
    to.setStrainDescription(from.getStrainDescription());
    to.setTreatment(from.getTreatment());
    to.setProtocol(from.getProtocol());
  }

  @Test
  public void generateName() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_FR1-FR2_20200508", dataset.getName());
  }

  @Test
  public void generateName_InvalidSampleId() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR 1%");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR 2)");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_FR1-FR2_20200508", dataset.getName());
  }

  @Test
  public void generateName_FrenchSampleId() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FRï1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FRê2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_FRi1-FRe2_20200508", dataset.getName());
  }

  @Test
  public void generateName_NullType() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_Spt16_yFR101_G24D_IAA_FR1-FR2_20200508", dataset.getName());
  }

  @Test
  public void generateName_NullTarget() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_yFR101_G24D_IAA_FR1-FR2_20200508", dataset.getName());
  }

  @Test
  public void generateName_InvalidTarget() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setTarget("Spt 16?");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_FR1-FR2_20200508", dataset.getName());
  }

  @Test
  public void generateName_FrenchTarget() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setTarget("Sptà16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_Spta16_yFR101_G24D_IAA_FR1-FR2_20200508", dataset.getName());
  }

  @Test
  public void generateName_InvalidStrain() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setTarget("Spt16");
    sample.setStrain("yFR 101$");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_FR1-FR2_20200508", dataset.getName());
  }

  @Test
  public void generateName_FrenchStrain() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setTarget("Spt16");
    sample.setStrain("yâFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_Spt16_yaFR101_G24D_IAA_FR1-FR2_20200508", dataset.getName());
  }

  @Test
  public void generateName_NullStrainDescription() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_Spt16_yFR101_IAA_FR1-FR2_20200508", dataset.getName());
  }

  @Test
  public void generateName_InvalidStrainDescription() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G 24D@");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_FR1-FR2_20200508", dataset.getName());
  }

  @Test
  public void generateName_FrenchStrainDescription() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24DÉ");
    sample.setTreatment("IAA");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_Spt16_yFR101_G24DE_IAA_FR1-FR2_20200508", dataset.getName());
  }

  @Test
  public void generateName_NullTreatment() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_FR1-FR2_20200508", dataset.getName());
  }

  @Test
  public void generateName_InvalidTreatment() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("I AA!");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IAA_FR1-FR2_20200508", dataset.getName());
  }

  @Test
  public void generateName_FrenchTreatment() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    Sample sample = new Sample();
    sample.setSampleId("FR1");
    sample.setReplicate("R1");
    sample.setAssay("RNA-seq");
    sample.setType("IP");
    sample.setTarget("Spt16");
    sample.setStrain("yFR101");
    sample.setStrainDescription("G24D");
    sample.setTreatment("IçAA");
    dataset.getSamples().add(sample);
    Sample sample2 = new Sample();
    copy(sample, sample2);
    sample2.setSampleId("FR2");
    sample2.setReplicate("R2");
    dataset.getSamples().add(sample2);
    dataset.setDate(LocalDate.of(2020, 5, 8));
    dataset.generateName();
    assertEquals("RNAseq_IP_Spt16_yFR101_G24D_IcAA_FR1-FR2_20200508", dataset.getName());
  }
}
