package ca.qc.ircm.lanaseq.files.web;

import static ca.qc.ircm.lanaseq.files.web.PublicFilesView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SamplePublicFileRepository;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.SigninView;
import com.vaadin.browserless.SpringBrowserlessTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link PublicFilesView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class PublicFilesViewIT extends SpringBrowserlessTest {

  @Autowired
  private SampleRepository sampleRepository;
  @Autowired
  private SamplePublicFileRepository samplePublicFileRepository;

  @Test
  @WithAnonymousUser
  public void security_Anonymous() {
    navigate(VIEW_NAME, SigninView.class);
  }

  @Test
  public void delete() {
    Sample sample = sampleRepository.findById(11L).orElseThrow();
    assertTrue(samplePublicFileRepository.findBySampleAndPath(sample,
        "JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bw").isPresent());
    PublicFilesView view = navigate(PublicFilesView.class);
    Assertions.assertEquals(4, test(view.files).size());

    test(view.files).invokeLitRendererFunction(0, view.delete.getKey(), "deletePublicFile");

    Assertions.assertEquals(3, test(view.files).size());
    assertFalse(samplePublicFileRepository.findBySampleAndPath(sample,
        "JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bw").isPresent());
  }
}
