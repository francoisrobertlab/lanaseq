package ca.qc.ircm.lanaseq;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link FixFilesDateFolder}.
 */
@ServiceTestAnnotations
public class FixFilesDateFolderTest {
  private FixFilesDateFolder fixFilesDateFolder;
  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private SampleRepository sampleRepository;
  @MockitoBean
  private AppConfiguration configuration;
  @Mock
  private AppConfiguration.NetworkDrive<DataWithFiles> home;
  @TempDir
  Path tempDir;
  private List<Dataset> datasets;
  private List<Sample> samples;
  private final Map<DataWithFiles, List<Path>> files = new HashMap<>();

  @BeforeEach
  public void beforeTest() {
    fixFilesDateFolder = new FixFilesDateFolder(configuration, datasetRepository, sampleRepository);
    when(configuration.getHome()).thenReturn(home);
    when(configuration.getHome().folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return tempDir.resolve("datasets").resolve(String.valueOf(dataset.getDate().getYear()))
          .resolve(dataset.getName());
    });
    when(configuration.getHome().folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return tempDir.resolve("samples").resolve(String.valueOf(sample.getDate().getYear()))
          .resolve(sample.getName());
    });
    datasets = datasetRepository.findAll();
    samples = sampleRepository.findAll();
  }

  private void createFiles(DataWithFiles data, Path folder) throws IOException {
    files.put(data, new ArrayList<>());
    Files.createDirectories(folder);
    Path file = folder.resolve("test.txt");
    Files.createFile(file);
    files.get(data).add(file);
    file = folder.resolve(data.getName() + ".txt");
    Files.createFile(file);
    files.get(data).add(file);
    Path subfolder = folder.resolve("subfolder");
    Files.createDirectory(subfolder);
    file = folder.resolve("subfolder/" + data.getName() + ".txt");
    Files.createFile(file);
    files.get(data).add(file);
  }

  @Test
  public void fixFilesDateFolder() throws Throwable {
    int modifiedDatasets = 2;
    datasets.stream().limit(modifiedDatasets)
        .forEach(dataset -> dataset.setCreationDate(dataset.getCreationDate().minusYears(1)));
    int modifiedSamples = 3;
    samples.stream().limit(modifiedSamples)
        .forEach(sample -> sample.setCreationDate(sample.getCreationDate().minusYears(1)));
    Path datasetsBase = tempDir.resolve("datasets");
    for (Dataset dataset : datasets) {
      Path folder = datasetsBase.resolve(String.valueOf(dataset.getCreationDate().getYear()))
          .resolve(dataset.getName());
      createFiles(dataset, folder);
    }
    Path samplesBase = tempDir.resolve("samples");
    for (Sample sample : samples) {
      Path folder = samplesBase.resolve(String.valueOf(sample.getCreationDate().getYear()))
          .resolve(sample.getName());
      createFiles(sample, folder);
    }
    fixFilesDateFolder.fixFilesDateFolder();
    int i = -1;
    for (Dataset dataset : datasets) {
      i++;
      Path oldFolder = datasetsBase.resolve(String.valueOf(dataset.getCreationDate().getYear()))
          .resolve(dataset.getName());
      Path folder = datasetsBase.resolve(String.valueOf(dataset.getDate().getYear()))
          .resolve(dataset.getName());
      if (i < modifiedDatasets) {
        assertNotEquals(oldFolder, folder);
        assertFalse(Files.exists(oldFolder), oldFolder.toString());
      } else {
        assertEquals(oldFolder, folder);
      }
      for (Path file : files.get(dataset)) {
        if (i < modifiedDatasets) {
          assertFalse(Files.exists(file), file.toString());
        }
        assertTrue(Files.exists(folder.resolve(oldFolder.relativize(file))), file.toString());
      }
    }
    i = -1;
    for (Sample sample : samples) {
      i++;
      Path oldFolder = samplesBase.resolve(String.valueOf(sample.getCreationDate().getYear()))
          .resolve(sample.getName());
      Path folder =
          samplesBase.resolve(String.valueOf(sample.getDate().getYear())).resolve(sample.getName());
      if (i < modifiedSamples) {
        assertNotEquals(oldFolder, folder);
        assertFalse(Files.exists(oldFolder), oldFolder.toString());
      } else {
        assertEquals(oldFolder, folder);
      }
      for (Path file : files.get(sample)) {
        if (i < modifiedSamples) {
          assertFalse(Files.exists(file), file.toString());
        }
        assertTrue(Files.exists(folder.resolve(oldFolder.relativize(file))), file.toString());
      }
    }
  }

  @Test
  public void fixFilesDateFolder_ParentNotYear() throws Throwable {
    when(configuration.getHome().folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return tempDir.resolve("datasets").resolve(dataset.getName());
    });
    when(configuration.getHome().folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return tempDir.resolve("samples").resolve(sample.getName());
    });
    int modifiedDatasets = 2;
    datasets.stream().limit(modifiedDatasets)
        .forEach(dataset -> dataset.setCreationDate(dataset.getCreationDate().minusYears(1)));
    int modifiedSamples = 3;
    samples.stream().limit(modifiedSamples)
        .forEach(sample -> sample.setCreationDate(sample.getCreationDate().minusYears(1)));
    Path datasetsBase = tempDir.resolve("datasets");
    for (Dataset dataset : datasets) {
      Path folder = datasetsBase.resolve(dataset.getName());
      createFiles(dataset, folder);
    }
    Path samplesBase = tempDir.resolve("samples");
    for (Sample sample : samples) {
      Path folder = samplesBase.resolve(sample.getName());
      createFiles(sample, folder);
    }
    fixFilesDateFolder.fixFilesDateFolder();
    for (Dataset dataset : datasets) {
      Path folder = datasetsBase.resolve(dataset.getName());
      for (Path file : files.get(dataset)) {
        assertTrue(Files.exists(folder.resolve(folder.relativize(file))), file.toString());
      }
    }
    for (Sample sample : samples) {
      Path folder = samplesBase.resolve(sample.getName());
      for (Path file : files.get(sample)) {
        assertTrue(Files.exists(folder.resolve(folder.relativize(file))), file.toString());
      }
    }
  }

  @Test
  public void fixFilesDateFolder_ParentNotDateYear() throws Throwable {
    when(configuration.getHome().folder(any(Dataset.class))).then(i -> {
      Dataset dataset = i.getArgument(0);
      return tempDir.resolve("datasets")
          .resolve(String.valueOf(dataset.getCreationDate().getYear())).resolve(dataset.getName());
    });
    when(configuration.getHome().folder(any(Sample.class))).then(i -> {
      Sample sample = i.getArgument(0);
      return tempDir.resolve("samples").resolve(String.valueOf(sample.getCreationDate().getYear()))
          .resolve(sample.getName());
    });
    datasets.forEach(dataset -> dataset.setCreationDate(dataset.getCreationDate().minusYears(1)));
    samples.forEach(sample -> sample.setCreationDate(sample.getCreationDate().minusYears(1)));
    Path datasetsBase = tempDir.resolve("datasets");
    for (Dataset dataset : datasets) {
      Path folder = datasetsBase.resolve(String.valueOf(dataset.getCreationDate().getYear()))
          .resolve(dataset.getName());
      createFiles(dataset, folder);
    }
    Path samplesBase = tempDir.resolve("samples");
    for (Sample sample : samples) {
      Path folder = samplesBase.resolve(String.valueOf(sample.getCreationDate().getYear()))
          .resolve(sample.getName());
      createFiles(sample, folder);
    }
    fixFilesDateFolder.fixFilesDateFolder();
    for (Dataset dataset : datasets) {
      Path folder = datasetsBase.resolve(String.valueOf(dataset.getCreationDate().getYear()))
          .resolve(dataset.getName());
      for (Path file : files.get(dataset)) {
        assertTrue(Files.exists(folder.resolve(folder.relativize(file))), file.toString());
      }
    }
    for (Sample sample : samples) {
      Path folder = samplesBase.resolve(String.valueOf(sample.getCreationDate().getYear()))
          .resolve(sample.getName());
      for (Path file : files.get(sample)) {
        assertTrue(Files.exists(folder.resolve(folder.relativize(file))), file.toString());
      }
    }
  }
}
