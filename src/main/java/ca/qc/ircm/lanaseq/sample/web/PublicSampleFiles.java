package ca.qc.ircm.lanaseq.sample.web;

import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.web.ResourceNotFoundException;
import java.nio.file.Path;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves public sample files.
 */
@RestController
public class PublicSampleFiles {

  public static final String REST_MAPPING = "sample-file";
  private static final Logger logger = LoggerFactory.getLogger(PublicSampleFiles.class);
  private final SampleService service;

  /**
   * Creates instance of PublicSampleFiles.
   *
   * @param service sample service
   */
  @Autowired
  protected PublicSampleFiles(SampleService service) {
    this.service = service;
  }

  /**
   * Sends public file from sample.
   *
   * @param name     sample's name
   * @param filename filename
   * @return public file from sample if found, otherwise send not found error
   */
  @GetMapping("/" + REST_MAPPING + "/{name}/{filename}")
  @ResponseBody
  public FileSystemResource publicSampleFile(@PathVariable String name,
      @PathVariable String filename) {
    logger.debug("Trying to access public file {} of sample {}", filename, name);
    Optional<Path> optionalFile = service.publicFile(name, filename);
    if (optionalFile.isPresent()) {
      return new FileSystemResource(optionalFile.orElseThrow());
    } else {
      throw new ResourceNotFoundException(
          "Public file " + filename + " of sample " + name + " not found");
    }
  }

  /**
   * URL to use to reach {@link PublicSampleFiles#publicSampleFile(String, String)}.
   *
   * @param sample   sample
   * @param filename filename
   * @return URL to use to reach {@link PublicSampleFiles#publicSampleFile(String, String)}
   */
  public static String publicSampleFileUrl(Sample sample, String filename) {
    return REST_MAPPING + "/" + sample.getName() + "/" + filename;
  }
}
