package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.test.config.AbstractTestBenchBrowser;
import ca.qc.ircm.lanaseq.test.config.TestBenchTestAnnotations;
import com.vaadin.testbench.BrowserTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.openqa.selenium.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link DatasetsAnalysisDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetsAnalysisDialogItTest extends AbstractTestBenchBrowser {

  @TempDir
  Path temporaryFolder;
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private AppConfiguration configuration;
  private final Random random = new Random();

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

  @BrowserTest
  public void fieldsExistence() {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().select(0);
    view.analyze().click();
    DatasetsAnalysisDialogElement dialog = view.analyzeDialog();
    assertTrue(optional(dialog::header).isPresent());
    assertTrue(optional(dialog::message).isPresent());
    assertTrue(optional(dialog::filenamePatterns).isPresent());
    assertTrue(optional(dialog::create).isPresent());
  }

  @BrowserTest
  public void create_One() throws Throwable {
    Dataset dataset = repository.findById(2L).orElseThrow();
    Sample sample1 = dataset.getSamples().get(0);
    Path sample1Folder = configuration.getHome().folder(sample1);
    Files.createDirectories(sample1Folder);
    Path fastq1 = sample1Folder.resolve(sample1.getName() + "_R1.fastq");
    final byte[] fastq1Content = writeFile(fastq1);
    Path fastq2 = sample1Folder.resolve(sample1.getName() + "_R2.fastq");
    final byte[] fastq2Content = writeFile(fastq2);
    Sample sample2 = dataset.getSamples().get(1);
    Path sample2Folder = configuration.getHome().folder(sample2);
    Files.createDirectories(sample2Folder);
    Path fastq3 = sample2Folder.resolve("a_R1.fastq");
    final byte[] fastq3Content = writeFile(fastq3);
    Path fastq4 = sample2Folder.resolve("a_R2.fastq");
    final byte[] fastq4Content = writeFile(fastq4);
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().select(3);
    view.analyze().click();
    DatasetsAnalysisDialogElement dialog = view.analyzeDialog();
    dialog.filenamePatterns().sendKeys("*.fastq" + Keys.RETURN);

    dialog.create().click();
    Thread.sleep(2000); // Wait for file copy.

    assertTrue(dialog.isOpen());
    dialog.confirm().getConfirmButton().click();
    assertFalse(dialog.isOpen());
    Path folder = configuration.getAnalysis().folder(List.of(dataset));
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
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    Assertions.assertEquals(3, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    Assertions.assertEquals(sample1.getName(), samplesContent.get(1));
    Assertions.assertEquals(sample2.getName(), samplesContent.get(2));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    Assertions.assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    Assertions.assertEquals(dataset.getName() + "\t" + sample1.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(1));
  }

  @BrowserTest
  public void create_Many() throws Throwable {
    Dataset dataset = repository.findById(2L).orElseThrow();
    Dataset dataset2 = repository.findById(7L).orElseThrow();
    List<Dataset> datasets = Arrays.asList(dataset, dataset2);
    Sample sample1 = dataset.getSamples().get(0);
    Path sample1Folder = configuration.getHome().folder(sample1);
    Files.createDirectories(sample1Folder);
    Path fastq1 = sample1Folder.resolve(sample1.getName() + "_R1.fastq");
    final byte[] fastq1Content = writeFile(fastq1);
    Path fastq2 = sample1Folder.resolve(sample1.getName() + "_R2.fastq");
    final byte[] fastq2Content = writeFile(fastq2);
    Sample sample2 = dataset.getSamples().get(1);
    Path sample2Folder = configuration.getHome().folder(sample2);
    Files.createDirectories(sample2Folder);
    Path fastq3 = sample2Folder.resolve("a_R1.fastq");
    final byte[] fastq3Content = writeFile(fastq3);
    Path fastq4 = sample2Folder.resolve("a_R2.fastq");
    final byte[] fastq4Content = writeFile(fastq4);
    Sample sample3 = dataset2.getSamples().get(0);
    Path sample3Folder = configuration.getHome().folder(sample3);
    Files.createDirectories(sample3Folder);
    Path fastq5 = sample3Folder.resolve(sample3.getName() + "_R1.fastq");
    final byte[] fastq5Content = writeFile(fastq5);
    Path fastq6 = sample3Folder.resolve(sample3.getName() + "_R2.fastq");
    final byte[] fastq6Content = writeFile(fastq6);
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().select(3);
    view.datasets().select(0);
    view.analyze().click();
    DatasetsAnalysisDialogElement dialog = view.analyzeDialog();
    dialog.filenamePatterns().sendKeys("*.fastq" + Keys.RETURN);

    dialog.create().click();
    Thread.sleep(2000); // Wait for file copy.

    assertTrue(dialog.isOpen());
    dialog.confirm().getConfirmButton().click();
    assertFalse(dialog.isOpen());
    Path folder = configuration.getAnalysis().folder(datasets);
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
    assertTrue(Files.exists(folder.resolve(sample3.getName() + "_R1.fastq")));
    assertArrayEquals(fastq5Content,
        Files.readAllBytes(folder.resolve(sample3.getName() + "_R1.fastq")));
    assertTrue(Files.exists(folder.resolve(sample3.getName() + "_R2.fastq")));
    assertArrayEquals(fastq6Content,
        Files.readAllBytes(folder.resolve(sample3.getName() + "_R2.fastq")));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    Assertions.assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    Assertions.assertEquals(sample1.getName(), samplesContent.get(1));
    Assertions.assertEquals(sample2.getName(), samplesContent.get(2));
    Assertions.assertEquals(sample3.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    Assertions.assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    Assertions.assertEquals(dataset.getName() + "\t" + sample1.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(1));
    Assertions.assertEquals(dataset2.getName() + "\t" + sample3.getName(),
        datasetMetaContent.get(2));
  }

  @BrowserTest
  public void create_NoFilenamePattern() throws Throwable {
    Dataset dataset = repository.findById(2L).orElseThrow();
    Sample sample1 = dataset.getSamples().get(0);
    Path sample1Folder = configuration.getHome().folder(sample1);
    Files.createDirectories(sample1Folder);
    Path fastq1 = sample1Folder.resolve(sample1.getName() + "_R1.fastq");
    final byte[] fastq1Content = writeFile(fastq1);
    Path fastq2 = sample1Folder.resolve(sample1.getName() + "_R2.fastq");
    final byte[] fastq2Content = writeFile(fastq2);
    Sample sample2 = dataset.getSamples().get(1);
    Path sample2Folder = configuration.getHome().folder(sample2);
    Files.createDirectories(sample2Folder);
    Path fastq3 = sample2Folder.resolve("a_R1.fastq");
    final byte[] fastq3Content = writeFile(fastq3);
    Path fastq4 = sample2Folder.resolve("a_R2.fastq");
    final byte[] fastq4Content = writeFile(fastq4);
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).waitForFirst();
    view.datasets().select(3);
    view.analyze().click();
    DatasetsAnalysisDialogElement dialog = view.analyzeDialog();

    dialog.create().click();

    assertTrue(dialog.isOpen());
    dialog.confirm().getConfirmButton().click();
    assertFalse(dialog.isOpen());
    Path folder = configuration.getAnalysis().folder(List.of(dataset));
    assertTrue(Files.exists(folder));
    assertFalse(Files.exists(folder.resolve(sample1.getName() + "_R1.fastq")));
    assertFalse(Files.exists(folder.resolve(sample1.getName() + "_R2.fastq")));
    assertFalse(Files.exists(folder.resolve("a_R1.fastq")));
    assertFalse(Files.exists(folder.resolve("a_R2.fastq")));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    Assertions.assertEquals(3, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    Assertions.assertEquals(sample1.getName(), samplesContent.get(1));
    Assertions.assertEquals(sample2.getName(), samplesContent.get(2));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    Assertions.assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    Assertions.assertEquals(dataset.getName() + "\t" + sample1.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(1));
  }
}
