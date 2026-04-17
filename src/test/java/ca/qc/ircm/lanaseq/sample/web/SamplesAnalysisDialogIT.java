package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.fireEvent;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import com.vaadin.flow.component.combobox.ComboBoxBase.CustomValueSetEvent;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link SamplesAnalysisDialog}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class SamplesAnalysisDialogIT extends SpringUIUnitTest {

  @Autowired
  private SampleRepository repository;
  @Autowired
  private AppConfiguration configuration;
  private final Random random = new Random();

  private byte[] writeFile(Path file) throws IOException {
    byte[] bytes = new byte[2048];
    random.nextBytes(bytes);
    Files.write(file, bytes, StandardOpenOption.CREATE);
    return bytes;
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
    SamplesView view = navigate(SamplesView.class);
    test(view.samples).select(1);
    test(view.analyze).click();
    SamplesAnalysisDialog dialog = $(SamplesAnalysisDialog.class).first();
    fireEvent(dialog.filenamePatterns,
        new CustomValueSetEvent<>(dialog.filenamePatterns, false, "*.fastq"));

    test(dialog.createFolder).click();

    assertTrue(dialog.isOpened());
    test($(ConfirmDialog.class).first()).confirm();
    assertFalse(dialog.isOpened());
    Path folder = configuration.getAnalysis().folder(List.of(sample));
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
    Assertions.assertEquals(2, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    Assertions.assertEquals(sample.getName(), samplesContent.get(1));
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
    SamplesView view = navigate(SamplesView.class);
    // Sample is randomly JS1 or JS2 because they have the same date. Use a stable select.
    view.samples.select(repository.findById(4L).orElseThrow());
    test(view.samples).select(1);
    test(view.analyze).click();
    SamplesAnalysisDialog dialog = $(SamplesAnalysisDialog.class).first();
    fireEvent(dialog.filenamePatterns,
        new CustomValueSetEvent<>(dialog.filenamePatterns, false, "*.fastq"));

    test(dialog.createFolder).click();

    assertTrue(dialog.isOpened());
    test($(ConfirmDialog.class).first()).confirm();
    assertFalse(dialog.isOpened());
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
    Assertions.assertEquals(3, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    Assertions.assertEquals(sample1.getName(), samplesContent.get(1));
    Assertions.assertEquals(sample2.getName(), samplesContent.get(2));
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
    SamplesView view = navigate(SamplesView.class);
    test(view.samples).select(1);
    test(view.samples).select(0);
    test(view.analyze).click();
    SamplesAnalysisDialog dialog = $(SamplesAnalysisDialog.class).first();

    test(dialog.createFolder).click();

    assertTrue(dialog.isOpened());
    test($(ConfirmDialog.class).first()).confirm();
    assertFalse(dialog.isOpened());
    Path folder = configuration.getAnalysis().folder(samples);
    assertTrue(Files.exists(folder));
    assertFalse(Files.exists(folder.resolve(sample1.getName() + "_R1.fastq")));
    assertFalse(Files.exists(folder.resolve(sample1.getName() + "_R2.fastq")));
    assertFalse(Files.exists(folder.resolve("a_R1.fastq")));
    Path samplesFile = folder.resolve("samples.txt");
    assertTrue(Files.exists(samplesFile));
    List<String> samplesContent = Files.readAllLines(samplesFile);
    Assertions.assertEquals(3, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    Assertions.assertEquals(sample1.getName(), samplesContent.get(1));
    Assertions.assertEquals(sample2.getName(), samplesContent.get(2));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertFalse(Files.exists(datasetMeta));
  }
}
