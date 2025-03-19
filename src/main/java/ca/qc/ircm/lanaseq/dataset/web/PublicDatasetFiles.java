package ca.qc.ircm.lanaseq.dataset.web;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
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
public class PublicDatasetFiles {

  public static final String REST_MAPPING = "dataset-file";
  private static final Logger logger = LoggerFactory.getLogger(PublicDatasetFiles.class);
  private final DatasetService service;

  /**
   * Creates instance of PublicDatasetFiles.
   *
   * @param service dataset service
   */
  @Autowired
  protected PublicDatasetFiles(DatasetService service) {
    this.service = service;
  }

  /**
   * Sends public file from dataset.
   *
   * @param name     dataset's name
   * @param filename filename
   * @return public file from dataset if found, otherwise send not found error
   */
  @GetMapping("/" + REST_MAPPING + "/{name}/{filename}")
  @ResponseBody
  public FileSystemResource publicDatasetFile(@PathVariable String name,
      @PathVariable String filename) {
    logger.debug("Trying to access public file {} of sample {}", filename, name);
    Optional<Path> optionalFile = service.publicFile(name, filename);
    if (optionalFile.isPresent()) {
      return new FileSystemResource(optionalFile.orElseThrow());
    } else {
      throw new ResourceNotFoundException(
          "Public file " + filename + " of dataset " + name + " not found");
    }
  }

  /**
   * URL to use to reach {@link PublicDatasetFiles#publicDatasetFile(String, String)}.
   *
   * @param dataset  dataset
   * @param filename filename
   * @return URL to use to reach {@link PublicDatasetFiles#publicDatasetFile(String, String)}
   */
  public static String publicDatasetFileUrl(Dataset dataset, String filename) {
    return REST_MAPPING + "/" + dataset.getName() + "/" + filename;
  }
}
