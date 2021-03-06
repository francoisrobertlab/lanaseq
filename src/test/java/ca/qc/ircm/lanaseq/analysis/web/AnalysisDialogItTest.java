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

package ca.qc.ircm.lanaseq.analysis.web;

import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.ID;
import static ca.qc.ircm.lanaseq.analysis.web.AnalysisView.VIEW_NAME;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
import java.util.List;
import java.util.Random;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@TestBenchTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AnalysisDialogItTest extends AbstractTestBenchTestCase {
  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private AppConfiguration configuration;
  private Random random = new Random();

  @Before
  public void beforeTest() throws Throwable {
    setHome(temporaryFolder.newFolder("home").toPath());
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
    AnalysisViewElement view = $(AnalysisViewElement.class).id(ID);
    view.datasets().getCell(0, 0).doubleClick();
    AnalysisDialogElement dialog = view.dialog();
    assertTrue(optional(() -> dialog.header()).isPresent());
    assertTrue(optional(() -> dialog.message()).isPresent());
    assertTrue(optional(() -> dialog.create()).isPresent());
    assertTrue(optional(() -> dialog.confirm()).isPresent());
    assertTrue(optional(() -> dialog.errors()).isPresent());
  }

  @Test
  public void create() throws Throwable {
    Dataset dataset = repository.findById(2L).get();
    Sample sample1 = dataset.getSamples().get(0);
    Path sample1Folder = configuration.folder(sample1);
    Files.createDirectories(sample1Folder);
    Path fastq1 = sample1Folder.resolve(sample1.getName() + "_R1.fastq");
    final byte[] fastq1Content = writeFile(fastq1);
    Path fastq2 = sample1Folder.resolve(sample1.getName() + "_R2.fastq");
    final byte[] fastq2Content = writeFile(fastq2);
    Sample sample2 = dataset.getSamples().get(1);
    Path sample2Folder = configuration.folder(sample2);
    Files.createDirectories(sample2Folder);
    Path fastq3 = sample2Folder.resolve("a_R1.fastq");
    final byte[] fastq3Content = writeFile(fastq3);
    Path fastq4 = sample2Folder.resolve("a_R2.fastq");
    final byte[] fastq4Content = writeFile(fastq4);
    open();
    AnalysisViewElement view = $(AnalysisViewElement.class).id(ID);
    view.datasets().getCell(3, 0).doubleClick();
    AnalysisDialogElement dialog = view.dialog();

    dialog.create().click();
    Thread.sleep(2000); // Wait for file copy.

    assertTrue(dialog.isOpen());
    dialog.confirm().getConfirmButton().click();
    assertFalse(dialog.isOpen());
    Path folder = configuration.analysis(dataset);
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
}
