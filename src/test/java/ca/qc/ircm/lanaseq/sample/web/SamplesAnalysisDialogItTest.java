package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.SamplesView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchTestCase;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SamplesAnalysisDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SamplesAnalysisDialogItTest extends AbstractTestBenchTestCase {
  @TempDir
  Path temporaryFolder;
  @Autowired
  private SampleRepository repository;
  @Autowired
  private AppConfiguration configuration;
  private Random random = new Random();

  @BeforeEach
  public void beforeTest() throws Throwable {
    setHome(Files.createDirectory(temporaryFolder.resolve("home")));
    setAnalysis(Files.createDirectory(temporaryFolder.resolve("analysis")));
  }

  private void open() {
    openView(VIEW_NAME);
  }

  private byte[] writeFile(Path file) throws IOException {
    byte[] bytes = new byte[2048];
    random.nextBytes(bytes);
    Files.write(file, bytes, StandardOpenOption.CREATE);
    return bytes;
  }

  @Test
  public void fieldsExistence() {
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(0);
    view.analyze().click();
    SamplesAnalysisDialogElement dialog = view.analyzeDialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.message()).isPresent());
    assertTrue(optional(() -> dialog.filenamePatterns()).isPresent());
    assertTrue(optional(() -> dialog.create()).isPresent());
  }

  @Test
  public void create_One() throws Throwable {
    Sample sample = repository.findById(10L).orElseThrow();
    Path sampleFolder = configuration.getHome().folder(sample);
    Files.createDirectories(sampleFolder);
    Path fastq1 = sampleFolder.resolve(sample.getName() + "_R1.fastq");
    final byte[] fastq1Content = writeFile(fastq1);
    Path fastq2 = sampleFolder.resolve(sample.getName() + "_R2.fastq");
    final byte[] fastq2Content = writeFile(fastq2);
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(1);
    view.analyze().click();
    SamplesAnalysisDialogElement dialog = view.analyzeDialog();
    dialog.filenamePatterns().sendKeys("*.fastq" + Keys.RETURN);

    dialog.create().click();
    Thread.sleep(2000); // Wait for file copy.

    assertTrue(dialog.isOpen());
    dialog.confirm().getConfirmButton().click();
    assertFalse(dialog.isOpen());
    Path folder = configuration.getAnalysis().folder(Arrays.asList(sample));
    assertTrue(Files.exists(folder));
    assertTrue(Files.exists(folder.resolve(sample.getName() + "_R1.fastq")));
    assertArrayEquals(fastq1Content,
        Files.readAllBytes(folder.resolve(sample.getName() + "_R1.fastq")));
    assertTrue(Files.exists(folder.resolve(sample.getName() + "_R2.fastq")));
    assertArrayEquals(fastq2Content,
        Files.readAllBytes(folder.resolve(sample.getName() + "_R2.fastq")));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(2, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample.getName(), samplesContent.get(1));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertFalse(Files.exists(datasetMeta));
  }

  @Test
  public void create_Many() throws Throwable {
    Sample sample1 = repository.findById(4L).orElseThrow();
    Sample sample2 = repository.findById(10L).orElseThrow();
    List<Sample> samples = Arrays.asList(sample1, sample2);
    Path sample1Folder = configuration.getHome().folder(sample1);
    Files.createDirectories(sample1Folder);
    Path fastq1 = sample1Folder.resolve(sample1.getName() + "_R1.fastq");
    final byte[] fastq1Content = writeFile(fastq1);
    Path fastq2 = sample1Folder.resolve(sample1.getName() + "_R2.fastq");
    final byte[] fastq2Content = writeFile(fastq2);
    Path sample2Folder = configuration.getHome().folder(sample2);
    Files.createDirectories(sample2Folder);
    Path fastq3 = sample2Folder.resolve("a_R1.fastq");
    final byte[] fastq3Content = writeFile(fastq3);
    Path fastq4 = sample2Folder.resolve("a_R2.fastq");
    final byte[] fastq4Content = writeFile(fastq4);
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(view.samples().name(3).startsWith("JS1") ? 3 : 2);
    view.samples().select(1);
    view.analyze().click();
    SamplesAnalysisDialogElement dialog = view.analyzeDialog();
    dialog.filenamePatterns().sendKeys("*.fastq" + Keys.RETURN);

    dialog.create().click();
    Thread.sleep(2000); // Wait for file copy.

    assertTrue(dialog.isOpen());
    dialog.confirm().getConfirmButton().click();
    assertFalse(dialog.isOpen());
    Path folder = configuration.getAnalysis().folder(samples);
    assertTrue(Files.exists(folder));
    assertTrue(Files.exists(folder.resolve(sample1.getName() + "_R1.fastq")));
    assertArrayEquals(fastq1Content,
        Files.readAllBytes(folder.resolve(sample1.getName() + "_R1.fastq")));
    assertTrue(Files.exists(folder.resolve(sample1.getName() + "_R2.fastq")));
    assertArrayEquals(fastq2Content,
        Files.readAllBytes(folder.resolve(sample1.getName() + "_R2.fastq")));
    assertTrue(Files.exists(folder.resolve("a_R1.fastq")));
    assertArrayEquals(fastq3Content, Files.readAllBytes(folder.resolve("a_R1.fastq")));
    assertTrue(Files.exists(folder.resolve("a_R2.fastq")));
    assertArrayEquals(fastq4Content, Files.readAllBytes(folder.resolve("a_R2.fastq")));
    Path samplesFile = folder.resolve("samples.txt");
    assertTrue(Files.exists(samplesFile));
    List<String> samplesContent = Files.readAllLines(samplesFile);
    assertEquals(3, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample1.getName(), samplesContent.get(1));
    assertEquals(sample2.getName(), samplesContent.get(2));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertFalse(Files.exists(datasetMeta));
  }

  @Test
  public void create_NoFilenamePattern() throws Throwable {
    Sample sample1 = repository.findById(10L).orElseThrow();
    Sample sample2 = repository.findById(11L).orElseThrow();
    List<Sample> samples = Arrays.asList(sample1, sample2);
    Path sample1Folder = configuration.getHome().folder(sample1);
    Files.createDirectories(sample1Folder);
    Path fastq1 = sample1Folder.resolve(sample1.getName() + "_R1.fastq");
    final byte[] fastq1Content = writeFile(fastq1);
    Path fastq2 = sample1Folder.resolve(sample1.getName() + "_R2.fastq");
    final byte[] fastq2Content = writeFile(fastq2);
    Path sample2Folder = configuration.getHome().folder(sample2);
    Files.createDirectories(sample2Folder);
    Path fastq3 = sample2Folder.resolve("a_R1.fastq");
    final byte[] fastq3Content = writeFile(fastq3);
    open();
    SamplesViewElement view = $(SamplesViewElement.class).waitForFirst();
    view.samples().select(1);
    view.samples().select(0);
    view.analyze().click();
    SamplesAnalysisDialogElement dialog = view.analyzeDialog();

    dialog.create().click();

    assertTrue(dialog.isOpen());
    dialog.confirm().getConfirmButton().click();
    assertFalse(dialog.isOpen());
    Path folder = configuration.getAnalysis().folder(samples);
    assertTrue(Files.exists(folder));
    assertFalse(Files.exists(folder.resolve(sample1.getName() + "_R1.fastq")));
    assertFalse(Files.exists(folder.resolve(sample1.getName() + "_R2.fastq")));
    assertFalse(Files.exists(folder.resolve("a_R1.fastq")));
    Path samplesFile = folder.resolve("samples.txt");
    assertTrue(Files.exists(samplesFile));
    List<String> samplesContent = Files.readAllLines(samplesFile);
    assertEquals(3, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample1.getName(), samplesContent.get(1));
    assertEquals(sample2.getName(), samplesContent.get(2));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertFalse(Files.exists(datasetMeta));
  }
}
