/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.ID;
import static ca.qc.ircm.lanaseq.dataset.web.DatasetsView.VIEW_NAME;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.sample.Sample;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Integration tests for {@link DatasetsAnalysisDialog}.
 */
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class DatasetsAnalysisDialogItTest extends AbstractTestBenchTestCase {
  @TempDir
  Path temporaryFolder;
  @Autowired
  private DatasetRepository repository;
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
  public void fieldsExistence() throws Throwable {
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(ID);
    view.datasets().select(0);
    view.analyze().click();
    DatasetsAnalysisDialogElement dialog = view.analyzeDialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.message()).isPresent());
    assertTrue(optional(() -> dialog.create()).isPresent());
    assertTrue(optional(() -> dialog.confirm()).isPresent());
    assertTrue(optional(() -> dialog.errors()).isPresent());
  }

  @Test
  public void create_One() throws Throwable {
    Dataset dataset = repository.findById(2L).get();
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
    DatasetsViewElement view = $(DatasetsViewElement.class).id(ID);
    view.datasets().select(3);
    view.analyze().click();
    DatasetsAnalysisDialogElement dialog = view.analyzeDialog();

    dialog.create().click();
    Thread.sleep(2000); // Wait for file copy.

    assertTrue(dialog.isOpen());
    dialog.confirm().getConfirmButton().click();
    assertFalse(dialog.isOpen());
    Path folder = configuration.getAnalysis().folder(Arrays.asList(dataset));
    assertTrue(Files.exists(folder));
    assertTrue(Files.exists(folder.resolve(sample1.getName() + "_R1.fastq")));
    assertArrayEquals(fastq1Content,
        Files.readAllBytes(folder.resolve(sample1.getName() + "_R1.fastq")));
    assertTrue(Files.exists(folder.resolve(sample1.getName() + "_R2.fastq")));
    assertArrayEquals(fastq2Content,
        Files.readAllBytes(folder.resolve(sample1.getName() + "_R2.fastq")));
    assertTrue(Files.exists(folder.resolve(sample2.getName() + "_R1.fastq")));
    assertArrayEquals(fastq3Content,
        Files.readAllBytes(folder.resolve(sample2.getName() + "_R1.fastq")));
    assertTrue(Files.exists(folder.resolve(sample2.getName() + "_R2.fastq")));
    assertArrayEquals(fastq4Content,
        Files.readAllBytes(folder.resolve(sample2.getName() + "_R2.fastq")));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(3, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample1.getName(), samplesContent.get(1));
    assertEquals(sample2.getName(), samplesContent.get(2));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(dataset.getName() + "\t" + sample1.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(1));
  }

  @Test
  public void create_Many() throws Throwable {
    Dataset dataset = repository.findById(2L).get();
    Dataset dataset2 = repository.findById(7L).get();
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
    DatasetsViewElement view = $(DatasetsViewElement.class).id(ID);
    view.datasets().select(3);
    view.datasets().select(0);
    view.analyze().click();
    DatasetsAnalysisDialogElement dialog = view.analyzeDialog();

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
    assertTrue(Files.exists(folder.resolve(sample2.getName() + "_R1.fastq")));
    assertArrayEquals(fastq3Content,
        Files.readAllBytes(folder.resolve(sample2.getName() + "_R1.fastq")));
    assertTrue(Files.exists(folder.resolve(sample2.getName() + "_R2.fastq")));
    assertArrayEquals(fastq4Content,
        Files.readAllBytes(folder.resolve(sample2.getName() + "_R2.fastq")));
    assertTrue(Files.exists(folder.resolve(sample3.getName() + "_R1.fastq")));
    assertArrayEquals(fastq5Content,
        Files.readAllBytes(folder.resolve(sample3.getName() + "_R1.fastq")));
    assertTrue(Files.exists(folder.resolve(sample3.getName() + "_R2.fastq")));
    assertArrayEquals(fastq6Content,
        Files.readAllBytes(folder.resolve(sample3.getName() + "_R2.fastq")));
    Path samples = folder.resolve("samples.txt");
    assertTrue(Files.exists(samples));
    List<String> samplesContent = Files.readAllLines(samples);
    assertEquals(4, samplesContent.size());
    assertTrue(samplesContent.get(0).startsWith("#sample"));
    assertEquals(sample1.getName(), samplesContent.get(1));
    assertEquals(sample2.getName(), samplesContent.get(2));
    assertEquals(sample3.getName(), samplesContent.get(3));
    Path datasetMeta = folder.resolve("dataset.txt");
    assertTrue(Files.exists(datasetMeta));
    List<String> datasetMetaContent = Files.readAllLines(datasetMeta);
    assertEquals("#merge\tsamples", datasetMetaContent.get(0));
    assertEquals(dataset.getName() + "\t" + sample1.getName() + "\t" + sample2.getName(),
        datasetMetaContent.get(1));
    assertEquals(dataset2.getName() + "\t" + sample3.getName(), datasetMetaContent.get(2));
  }

  @Test
  public void create_Error() throws Throwable {
    Dataset dataset = repository.findById(8L).get();
    open();
    DatasetsViewElement view = $(DatasetsViewElement.class).id(ID);
    view.datasets().select(3);
    view.analyze().click();
    DatasetsAnalysisDialogElement dialog = view.analyzeDialog();

    dialog.create().click();

    assertTrue(dialog.isOpen());
    dialog.errors().getConfirmButton().click();
    assertFalse(dialog.isOpen());
    Path folder = configuration.getAnalysis().folder(Arrays.asList(dataset));
    assertFalse(Files.exists(folder));
  }
}
