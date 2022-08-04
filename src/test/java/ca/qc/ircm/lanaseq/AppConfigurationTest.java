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

package ca.qc.ircm.lanaseq;

import static org.junit.jupiter.api.Assertions.assertEquals;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.sample.Assay;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;

/**
 * Tests for {@link AppConfiguration}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class AppConfigurationTest {
  @Autowired
  private AppConfiguration appConfiguration;

  private Sample sample() {
    Sample sample = new Sample();
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    sample.setAssay(Assay.CHIP_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("my target");
    sample.setStrain("yFR213");
    sample.setStrainDescription("F56G");
    sample.setTreatment("37C");
    sample.setCreationDate(LocalDateTime.of(2019, 12, 8, 10, 20, 30));
    sample.setDate(LocalDate.of(2019, 12, 8));
    sample.generateName();
    return sample;
  }

  private Dataset dataset() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    dataset.setCreationDate(LocalDateTime.of(2019, 12, 8, 10, 20, 30));
    dataset.setDate(LocalDate.of(2019, 12, 8));
    Sample sample = new Sample();
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    sample.setAssay(Assay.CHIP_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("my target");
    sample.setStrain("yFR213");
    sample.setStrainDescription("F56G");
    sample.setTreatment("37C");
    sample.setDate(LocalDate.of(2019, 12, 8));
    dataset.getSamples().add(sample);
    sample = new Sample();
    sample.setSampleId("my sample2");
    dataset.getSamples().add(sample);
    dataset.generateName();
    return dataset;
  }

  private Collection<Dataset> datasets() {
    ArrayList<Dataset> datasets = new ArrayList<>();
    datasets.add(dataset());
    Dataset dataset = new Dataset();
    datasets.add(dataset);
    dataset.setSamples(new ArrayList<>());
    dataset.setCreationDate(LocalDateTime.of(2020, 1, 10, 11, 15, 20));
    dataset.setDate(LocalDate.of(2020, 1, 4));
    Sample sample = new Sample();
    sample.setSampleId("s1");
    sample.setReplicate("r1");
    sample.setAssay(Assay.CHIP_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("my target");
    sample.setStrain("yFR213");
    sample.setStrainDescription("F56G");
    sample.setTreatment("37C");
    dataset.getSamples().add(sample);
    sample = new Sample();
    sample.setSampleId("s2");
    dataset.getSamples().add(sample);
    dataset.generateName();
    return datasets;
  }

  @Test
  public void getLogFile() {
    assertEquals(Paths.get(System.getProperty("user.dir"), "test.log"),
        appConfiguration.getLogFile());
  }

  @Test
  public void getHome() {
    AppConfiguration.NetworkDrive home = appConfiguration.getHome();
    assertEquals(Paths.get(System.getProperty("user.home"), "lanaseq"), home.getFolder());
    assertEquals("\\\\lanaseq01\\lanaseq", home.getWindowsPath());
    assertEquals("smb://lanaseq01/lanaseq", home.getUnixPath());
  }

  private Path resolveHome(Path subfolder) {
    return appConfiguration.getHome().getFolder().resolve(subfolder);
  }

  @Test
  public void getSampleFolder() {
    assertEquals(Paths.get("sample"), appConfiguration.getSampleFolder());
  }

  @Test
  public void getDatasetFolder() {
    assertEquals(Paths.get("dataset"), appConfiguration.getDatasetFolder());
  }

  @Test
  public void folder_Sample2019() {
    Sample sample = sample();
    String name = sample.getName();
    assertEquals(resolveHome(appConfiguration.getSampleFolder()).resolve("2019/" + name),
        appConfiguration.folder(sample));
  }

  @Test
  public void folder_Sample2020() {
    Sample sample = new Sample();
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    sample.setAssay(Assay.MNASE_SEQ);
    sample.setType(SampleType.INPUT);
    sample.setTarget("polr2a");
    sample.setStrain("yFR111");
    sample.setCreationDate(LocalDateTime.of(2020, 4, 10, 7, 12, 43));
    sample.generateName();
    String name = sample.getName();
    assertEquals(resolveHome(appConfiguration.getSampleFolder()).resolve("2020/" + name),
        appConfiguration.folder(sample));
  }

  @Test
  public void folder_Dataset2019() {
    Dataset dataset = dataset();
    String name = dataset.getName();
    assertEquals(resolveHome(appConfiguration.getDatasetFolder()).resolve("2019/" + name),
        appConfiguration.folder(dataset));
  }

  @Test
  public void folder_Dataset2020() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    dataset.setCreationDate(LocalDateTime.of(2020, 4, 10, 7, 12, 43));
    Sample sample = new Sample();
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    sample.setAssay(Assay.MNASE_SEQ);
    sample.setType(SampleType.INPUT);
    sample.setTarget("polr2a");
    sample.setStrain("yFR111");
    dataset.getSamples().add(sample);
    sample = new Sample();
    sample.setSampleId("my sample2");
    dataset.getSamples().add(sample);
    dataset.generateName();
    String name = dataset.getName();
    assertEquals(resolveHome(appConfiguration.getDatasetFolder()).resolve("2020/" + name),
        appConfiguration.folder(dataset));
  }

  @Test
  public void folderLabel_Sample() {
    Sample sample = sample();
    assertEquals("\\\\lanaseq01\\lanaseq\\sample\\2019\\" + sample.getName(),
        appConfiguration.folderLabel(sample, false));
  }

  @Test
  public void folderLabel_SampleUnix() {
    Sample sample = sample();
    assertEquals("smb://lanaseq01/lanaseq/sample/2019/" + sample.getName(),
        appConfiguration.folderLabel(sample, true));
  }

  @Test
  public void folderLabel_Dataset() {
    Dataset dataset = dataset();
    assertEquals("\\\\lanaseq01\\lanaseq\\dataset\\2019\\" + dataset.getName(),
        appConfiguration.folderLabel(dataset, false));
  }

  @Test
  public void folderLabel_DatasetUnix() {
    Dataset dataset = dataset();
    assertEquals("smb://lanaseq01/lanaseq/dataset/2019/" + dataset.getName(),
        appConfiguration.folderLabel(dataset, true));
  }

  @Test
  public void getAnalysisFolder() {
    assertEquals(Paths.get("analysis"), appConfiguration.getAnalysisFolder());
  }

  @Test
  public void analysis() {
    assertEquals(resolveHome(appConfiguration.getAnalysisFolder()), appConfiguration.analysis());
  }

  @Test
  public void datasetAnalysis_Datasets() {
    Collection<Dataset> datasets = datasets();
    Path path = appConfiguration.datasetAnalysis(datasets);
    assertEquals(
        resolveHome(appConfiguration.getAnalysisFolder()).resolve("jonh_CHIP_SEQ_20191208"), path);
  }

  @Test
  public void datasetAnalysis_DatasetsNoSample() {
    Collection<Dataset> datasets = datasets();
    datasets.stream().forEach(ds -> ds.setSamples(new ArrayList<>()));
    Path path = appConfiguration.datasetAnalysis(datasets);
    assertEquals(resolveHome(appConfiguration.getAnalysisFolder()).resolve("jonh_20191208"), path);
  }

  @Test
  public void datasetAnalysis_OneDataset() {
    Dataset dataset = dataset();
    assertEquals(resolveHome(appConfiguration.getAnalysisFolder()).resolve(dataset.getName()),
        appConfiguration.datasetAnalysis(Arrays.asList(dataset)));
  }

  @Test
  public void sampleAnalysis() {
    Collection<Sample> samples =
        datasets().stream().flatMap(ds -> ds.getSamples().stream()).collect(Collectors.toList());
    Path path = appConfiguration.sampleAnalysis(samples);
    assertEquals(
        resolveHome(appConfiguration.getAnalysisFolder()).resolve("jonh_CHIP_SEQ_20191208"), path);
  }

  @Test
  public void sampleAnalysis_NoAssay() {
    Collection<Sample> samples =
        datasets().stream().flatMap(ds -> ds.getSamples().stream()).collect(Collectors.toList());
    samples.forEach(sa -> sa.setAssay(null));
    Path path = appConfiguration.sampleAnalysis(samples);
    assertEquals(resolveHome(appConfiguration.getAnalysisFolder()).resolve("jonh_20191208"), path);
  }

  @Test
  public void sampleAnalysis_One() {
    Sample sample = sample();
    assertEquals(resolveHome(appConfiguration.getAnalysisFolder()).resolve(sample.getName()),
        appConfiguration.sampleAnalysis(Arrays.asList(sample)));
  }

  @Test
  public void datasetAnalysisLabel_Datasets() {
    Collection<Dataset> datasets = datasets();
    assertEquals("\\\\lanaseq01\\lanaseq\\analysis\\jonh_CHIP_SEQ_20191208",
        appConfiguration.datasetAnalysisLabel(datasets, false));
  }

  @Test
  public void datasetAnalysisLabel_DatasetsUnix() {
    Collection<Dataset> datasets = datasets();
    assertEquals("smb://lanaseq01/lanaseq/analysis/jonh_CHIP_SEQ_20191208",
        appConfiguration.datasetAnalysisLabel(datasets, true));
  }

  @Test
  public void datasetAnalysisLabel_DatasetsNoSample() {
    Collection<Dataset> datasets = datasets();
    datasets.stream().forEach(ds -> ds.setSamples(new ArrayList<>()));
    assertEquals("\\\\lanaseq01\\lanaseq\\analysis\\jonh_20191208",
        appConfiguration.datasetAnalysisLabel(datasets, false));
  }

  @Test
  public void datasetAnalysisLabel_DatasetsNoSampleUnix() {
    Collection<Dataset> datasets = datasets();
    datasets.stream().forEach(ds -> ds.setSamples(new ArrayList<>()));
    assertEquals("smb://lanaseq01/lanaseq/analysis/jonh_20191208",
        appConfiguration.datasetAnalysisLabel(datasets, true));
  }

  @Test
  public void datasetAnalysisLabel_OneDataset() {
    Dataset dataset = dataset();
    assertEquals("\\\\lanaseq01\\lanaseq\\analysis\\" + dataset.getName(),
        appConfiguration.datasetAnalysisLabel(Arrays.asList(dataset), false));
  }

  @Test
  public void datasetAnalysisLabel_OneDatasetUnix() {
    Dataset dataset = dataset();
    assertEquals("smb://lanaseq01/lanaseq/analysis/" + dataset.getName(),
        appConfiguration.datasetAnalysisLabel(Arrays.asList(dataset), true));
  }

  @Test
  public void sampleAnalysisLabel() {
    Collection<Sample> samples =
        datasets().stream().flatMap(ds -> ds.getSamples().stream()).collect(Collectors.toList());
    assertEquals("\\\\lanaseq01\\lanaseq\\analysis\\jonh_CHIP_SEQ_20191208",
        appConfiguration.sampleAnalysisLabel(samples, false));
  }

  @Test
  public void sampleAnalysisLabel_Unix() {
    Collection<Sample> samples =
        datasets().stream().flatMap(ds -> ds.getSamples().stream()).collect(Collectors.toList());
    assertEquals("smb://lanaseq01/lanaseq/analysis/jonh_CHIP_SEQ_20191208",
        appConfiguration.sampleAnalysisLabel(samples, true));
  }

  @Test
  public void sampleAnalysisLabel_NoAssay() {
    Collection<Sample> samples =
        datasets().stream().flatMap(ds -> ds.getSamples().stream()).collect(Collectors.toList());
    samples.forEach(sa -> sa.setAssay(null));
    assertEquals("\\\\lanaseq01\\lanaseq\\analysis\\jonh_20191208",
        appConfiguration.sampleAnalysisLabel(samples, false));
  }

  @Test
  public void sampleAnalysisLabel_NoAssayUnix() {
    Collection<Sample> samples =
        datasets().stream().flatMap(ds -> ds.getSamples().stream()).collect(Collectors.toList());
    samples.forEach(sa -> sa.setAssay(null));
    assertEquals("smb://lanaseq01/lanaseq/analysis/jonh_20191208",
        appConfiguration.sampleAnalysisLabel(samples, true));
  }

  @Test
  public void sampleAnalysisLabel_OneDataset() {
    Sample sample = sample();
    assertEquals("\\\\lanaseq01\\lanaseq\\analysis\\" + sample.getName(),
        appConfiguration.sampleAnalysisLabel(Arrays.asList(sample), false));
  }

  @Test
  public void sampleAnalysisLabel_OneDatasetUnix() {
    Sample sample = sample();
    assertEquals("smb://lanaseq01/lanaseq/analysis/" + sample.getName(),
        appConfiguration.sampleAnalysisLabel(Arrays.asList(sample), true));
  }

  @Test
  public void analysisSymlinks() {
    assertEquals(true, appConfiguration.isAnalysisSymlinks());
  }

  @Test
  public void getAnalysisDeleteAge() {
    assertEquals(Duration.ofHours(48), appConfiguration.getAnalysisDeleteAge());
  }

  @Test
  public void getUploadFolder() {
    assertEquals(Paths.get("upload"), appConfiguration.getUploadFolder());
  }

  @Test
  public void upload() {
    assertEquals(resolveHome(appConfiguration.getUploadFolder()), appConfiguration.upload());
  }

  @Test
  public void upload_Sample() {
    Sample sample = sample();
    String name = sample.getName();
    assertEquals(resolveHome(appConfiguration.getUploadFolder()).resolve(name),
        appConfiguration.upload(sample));
  }

  @Test
  public void upload_Dataset2019() {
    Dataset dataset = dataset();
    String name = dataset.getName();
    assertEquals(resolveHome(appConfiguration.getUploadFolder()).resolve(name),
        appConfiguration.upload(dataset));
  }

  @Test
  public void uploadLabel_Sample() {
    Sample sample = sample();
    assertEquals("\\\\lanaseq01\\lanaseq\\upload\\" + sample.getName(),
        appConfiguration.uploadLabel(sample, false));
  }

  @Test
  public void uploadLabel_SampleUnix() {
    Sample sample = sample();
    assertEquals("smb://lanaseq01/lanaseq/upload/" + sample.getName(),
        appConfiguration.uploadLabel(sample, true));
  }

  @Test
  public void uploadLabel_Dataset() {
    Dataset dataset = dataset();
    assertEquals("\\\\lanaseq01\\lanaseq\\upload\\" + dataset.getName(),
        appConfiguration.uploadLabel(dataset, false));
  }

  @Test
  public void uploadLabel_DatasetUnix() {
    Dataset dataset = dataset();
    assertEquals("smb://lanaseq01/lanaseq/upload/" + dataset.getName(),
        appConfiguration.uploadLabel(dataset, true));
  }

  @Test
  public void getUploadDeleteAge() {
    assertEquals(Duration.ofHours(6), appConfiguration.getUploadDeleteAge());
  }

  @Test
  public void getUrl() {
    assertEquals("http://localhost:8080/myurl/subpath?param1=abc",
        appConfiguration.getUrl("/myurl/subpath?param1=abc"));
  }
}
