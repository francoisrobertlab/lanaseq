package ca.qc.ircm.lanaseq.files.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import ca.qc.ircm.lanaseq.dataset.DatasetPublicFile;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.sample.SamplePublicFile;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link PublicFile}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class PublicFileTest {

  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private SampleRepository sampleRepository;

  @Test
  public void datasetPublicFile() {
    DatasetPublicFile datasetPublicFile = new DatasetPublicFile();
    datasetPublicFile.setDataset(datasetRepository.findById(6L).orElseThrow());
    datasetPublicFile.setPath("test.txt");
    datasetPublicFile.setExpiryDate(LocalDate.now().plusDays(1));
    PublicFile publicFile = new PublicFile(datasetPublicFile);
    assertEquals(datasetPublicFile.getDataset(), publicFile.getDataset());
    assertNull(publicFile.getSample());
    assertEquals(datasetPublicFile.getDataset().getName(), publicFile.getSampleName());
    assertEquals(datasetPublicFile.getExpiryDate(), publicFile.getExpiryDate());
    assertEquals(datasetPublicFile.getPath(), publicFile.getFilename());
    assertEquals(datasetPublicFile.getDataset().getOwner(), publicFile.getOwner());
  }

  @Test
  public void samplePublicFile() {
    SamplePublicFile samplePublicFile = new SamplePublicFile();
    samplePublicFile.setSample(sampleRepository.findById(10L).orElseThrow());
    samplePublicFile.setPath("test.txt");
    samplePublicFile.setExpiryDate(LocalDate.now().plusDays(1));
    PublicFile publicFile = new PublicFile(samplePublicFile);
    assertNull(publicFile.getDataset());
    assertEquals(samplePublicFile.getSample(), publicFile.getSample());
    assertEquals(samplePublicFile.getSample().getName(), publicFile.getSampleName());
    assertEquals(samplePublicFile.getExpiryDate(), publicFile.getExpiryDate());
    assertEquals(samplePublicFile.getPath(), publicFile.getFilename());
    assertEquals(samplePublicFile.getSample().getOwner(), publicFile.getOwner());
  }
}
