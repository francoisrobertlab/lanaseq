package ca.qc.ircm.lanaseq.files.web;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetPublicFile;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SamplePublicFile;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.processing.GeneratePropertyNames;
import ca.qc.ircm.processing.GeneratePropertyRequirements;
import java.time.LocalDate;
import java.util.Objects;

/**
 * File accessible by the public.
 */
@GeneratePropertyNames(requirements = GeneratePropertyRequirements.GETTER_OR_SETTER)
public class PublicFile {

  private final Dataset dataset;
  private final Sample sample;
  private final String filename;
  private final LocalDate expiryDate;
  private final String sampleName;
  private final User owner;

  /**
   * Creates a new PublicFile using DatasetPublicFile.
   *
   * @param datasetPublicFile dataset public file
   */
  public PublicFile(DatasetPublicFile datasetPublicFile) {
    dataset = datasetPublicFile.getDataset();
    sample = null;
    filename = datasetPublicFile.getPath();
    expiryDate = datasetPublicFile.getExpiryDate();
    sampleName = datasetPublicFile.getDataset().getName();
    owner = datasetPublicFile.getDataset().getOwner();
  }

  /**
   * Creates a new PublicFile using SamplePublicFile.
   *
   * @param samplePublicFile sample public file
   */
  public PublicFile(SamplePublicFile samplePublicFile) {
    dataset = null;
    sample = samplePublicFile.getSample();
    filename = samplePublicFile.getPath();
    expiryDate = samplePublicFile.getExpiryDate();
    sampleName = samplePublicFile.getSample().getName();
    owner = samplePublicFile.getSample().getOwner();
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PublicFile that = (PublicFile) o;
    return Objects.equals(dataset, that.dataset) && Objects.equals(sample, that.sample)
        && Objects.equals(filename, that.filename) && Objects.equals(expiryDate, that.expiryDate)
        && Objects.equals(sampleName, that.sampleName) && Objects.equals(owner, that.owner);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dataset, sample, filename, expiryDate, sampleName, owner);
  }

  public Dataset getDataset() {
    return dataset;
  }

  public Sample getSample() {
    return sample;
  }

  public String getFilename() {
    return filename;
  }

  public LocalDate getExpiryDate() {
    return expiryDate;
  }

  public String getSampleName() {
    return sampleName;
  }

  public User getOwner() {
    return owner;
  }
}
