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
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    sample.setCreationDate(LocalDateTime.of(2018, 12, 8, 10, 20, 30));
    sample.setDate(LocalDate.of(2019, 12, 8));
    sample.generateName();
    return sample;
  }

  private Dataset dataset() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    dataset.setCreationDate(LocalDateTime.of(2018, 12, 8, 10, 20, 30));
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
    dataset.setCreationDate(LocalDateTime.of(2019, 1, 10, 11, 15, 20));
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
    AppConfiguration.NetworkDrive<DataWithFiles> home = appConfiguration.getHome();
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
        appConfiguration.getHome().folder(sample));
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
    sample.setDate(LocalDate.of(2020, 4, 10));
    sample.generateName();
    String name = sample.getName();
    assertEquals(resolveHome(appConfiguration.getSampleFolder()).resolve("2020/" + name),
        appConfiguration.getHome().folder(sample));
  }

  @Test
  public void folder_Dataset2019() {
    Dataset dataset = dataset();
    String name = dataset.getName();
    assertEquals(resolveHome(appConfiguration.getDatasetFolder()).resolve("2019/" + name),
        appConfiguration.getHome().folder(dataset));
  }

  @Test
  public void folder_Dataset2020() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    dataset.setDate(LocalDate.of(2020, 4, 10));
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
        appConfiguration.getHome().folder(dataset));
  }

  @Test
  public void label_Sample() {
    Sample sample = sample();
    assertEquals("\\\\lanaseq01\\lanaseq\\sample\\2019\\" + sample.getName(),
        appConfiguration.getHome().label(sample, false));
  }

  @Test
  public void label_SampleUnix() {
    Sample sample = sample();
    assertEquals("smb://lanaseq01/lanaseq/sample/2019/" + sample.getName(),
        appConfiguration.getHome().label(sample, true));
  }

  @Test
  public void label_Dataset() {
    Dataset dataset = dataset();
    assertEquals("\\\\lanaseq01\\lanaseq\\dataset\\2019\\" + dataset.getName(),
        appConfiguration.getHome().label(dataset, false));
  }

  @Test
  public void label_DatasetUnix() {
    Dataset dataset = dataset();
    assertEquals("smb://lanaseq01/lanaseq/dataset/2019/" + dataset.getName(),
        appConfiguration.getHome().label(dataset, true));
  }

  @Test
  public void getAnalysisFolder() {
    assertEquals(Paths.get("analysis"), appConfiguration.getAnalysisFolder());
  }

  @Test
  public void analysis_Folder() {
    assertEquals(resolveHome(appConfiguration.getAnalysisFolder()),
        appConfiguration.getAnalysis().getFolder());
  }

  @Test
  public void analysis_Empty() {
    Collection<Dataset> datasets = new ArrayList<>();
    assertThrows(IllegalArgumentException.class,
        () -> appConfiguration.getAnalysis().folder(datasets));
  }

  @Test
  public void analysis_Datasets() {
    Collection<Dataset> datasets = datasets();
    Path path = appConfiguration.getAnalysis().folder(datasets);
    assertEquals(
        resolveHome(appConfiguration.getAnalysisFolder()).resolve("jonh_CHIP_SEQ_20191208"), path);
  }

  @Test
  public void analysis_DatasetsNoSample() {
    Collection<Dataset> datasets = datasets();
    datasets.stream().forEach(ds -> ds.setSamples(new ArrayList<>()));
    Path path = appConfiguration.getAnalysis().folder(datasets);
    assertEquals(resolveHome(appConfiguration.getAnalysisFolder()).resolve("jonh_20191208"), path);
  }

  @Test
  public void analysis_OneDataset() {
    Dataset dataset = dataset();
    assertEquals(resolveHome(appConfiguration.getAnalysisFolder()).resolve(dataset.getName()),
        appConfiguration.getAnalysis().folder(Arrays.asList(dataset)));
  }

  @Test
  public void analysis_Samples() {
    Collection<Sample> samples =
        datasets().stream().flatMap(ds -> ds.getSamples().stream()).collect(Collectors.toList());
    Path path = appConfiguration.getAnalysis().folder(samples);
    assertEquals(
        resolveHome(appConfiguration.getAnalysisFolder()).resolve("jonh_CHIP_SEQ_20191208"), path);
  }

  @Test
  public void analysis_NoAssay() {
    Collection<Sample> samples =
        datasets().stream().flatMap(ds -> ds.getSamples().stream()).collect(Collectors.toList());
    samples.forEach(sa -> sa.setAssay(null));
    Path path = appConfiguration.getAnalysis().folder(samples);
    assertEquals(resolveHome(appConfiguration.getAnalysisFolder()).resolve("jonh_20191208"), path);
  }

  @Test
  public void analysis_OneSample() {
    Sample sample = sample();
    assertEquals(resolveHome(appConfiguration.getAnalysisFolder()).resolve(sample.getName()),
        appConfiguration.getAnalysis().folder(Arrays.asList(sample)));
  }

  @Test
  public void analysisLabel_Datasets() {
    Collection<Dataset> datasets = datasets();
    assertEquals("\\\\lanaseq01\\lanaseq\\analysis\\jonh_CHIP_SEQ_20191208",
        appConfiguration.getAnalysis().label(datasets, false));
  }

  @Test
  public void analysisLabel_DatasetsUnix() {
    Collection<Dataset> datasets = datasets();
    assertEquals("smb://lanaseq01/lanaseq/analysis/jonh_CHIP_SEQ_20191208",
        appConfiguration.getAnalysis().label(datasets, true));
  }

  @Test
  public void analysisLabel_DatasetsNoSample() {
    Collection<Dataset> datasets = datasets();
    datasets.stream().forEach(ds -> ds.setSamples(new ArrayList<>()));
    assertEquals("\\\\lanaseq01\\lanaseq\\analysis\\jonh_20191208",
        appConfiguration.getAnalysis().label(datasets, false));
  }

  @Test
  public void analysisLabel_DatasetsNoSampleUnix() {
    Collection<Dataset> datasets = datasets();
    datasets.stream().forEach(ds -> ds.setSamples(new ArrayList<>()));
    assertEquals("smb://lanaseq01/lanaseq/analysis/jonh_20191208",
        appConfiguration.getAnalysis().label(datasets, true));
  }

  @Test
  public void analysisLabel_OneDataset() {
    Dataset dataset = dataset();
    assertEquals("\\\\lanaseq01\\lanaseq\\analysis\\" + dataset.getName(),
        appConfiguration.getAnalysis().label(Arrays.asList(dataset), false));
  }

  @Test
  public void analysisLabel_OneDatasetUnix() {
    Dataset dataset = dataset();
    assertEquals("smb://lanaseq01/lanaseq/analysis/" + dataset.getName(),
        appConfiguration.getAnalysis().label(Arrays.asList(dataset), true));
  }

  @Test
  public void analysisLabel_Samples() {
    Collection<Sample> samples =
        datasets().stream().flatMap(ds -> ds.getSamples().stream()).collect(Collectors.toList());
    assertEquals("\\\\lanaseq01\\lanaseq\\analysis\\jonh_CHIP_SEQ_20191208",
        appConfiguration.getAnalysis().label(samples, false));
  }

  @Test
  public void analysisLabel_SamplesUnix() {
    Collection<Sample> samples =
        datasets().stream().flatMap(ds -> ds.getSamples().stream()).collect(Collectors.toList());
    assertEquals("smb://lanaseq01/lanaseq/analysis/jonh_CHIP_SEQ_20191208",
        appConfiguration.getAnalysis().label(samples, true));
  }

  @Test
  public void analysisLabel_SamplesNoAssay() {
    Collection<Sample> samples =
        datasets().stream().flatMap(ds -> ds.getSamples().stream()).collect(Collectors.toList());
    samples.forEach(sa -> sa.setAssay(null));
    assertEquals("\\\\lanaseq01\\lanaseq\\analysis\\jonh_20191208",
        appConfiguration.getAnalysis().label(samples, false));
  }

  @Test
  public void analysisLabel_SamplesNoAssayUnix() {
    Collection<Sample> samples =
        datasets().stream().flatMap(ds -> ds.getSamples().stream()).collect(Collectors.toList());
    samples.forEach(sa -> sa.setAssay(null));
    assertEquals("smb://lanaseq01/lanaseq/analysis/jonh_20191208",
        appConfiguration.getAnalysis().label(samples, true));
  }

  @Test
  public void analysisLabel_OneSample() {
    Sample sample = sample();
    assertEquals("\\\\lanaseq01\\lanaseq\\analysis\\" + sample.getName(),
        appConfiguration.getAnalysis().label(Arrays.asList(sample), false));
  }

  @Test
  public void analysisLabel_OneSampleUnix() {
    Sample sample = sample();
    assertEquals("smb://lanaseq01/lanaseq/analysis/" + sample.getName(),
        appConfiguration.getAnalysis().label(Arrays.asList(sample), true));
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
  public void upload_Folder() {
    assertEquals(resolveHome(appConfiguration.getUploadFolder()),
        appConfiguration.getUpload().getFolder());
  }

  @Test
  public void upload_Sample() {
    Sample sample = sample();
    String name = sample.getName();
    assertEquals(resolveHome(appConfiguration.getUploadFolder()).resolve(name),
        appConfiguration.getUpload().folder(sample));
  }

  @Test
  public void upload_Dataset2019() {
    Dataset dataset = dataset();
    String name = dataset.getName();
    assertEquals(resolveHome(appConfiguration.getUploadFolder()).resolve(name),
        appConfiguration.getUpload().folder(dataset));
  }

  @Test
  public void uploadLabel_Sample() {
    Sample sample = sample();
    assertEquals("\\\\lanaseq01\\lanaseq\\upload\\" + sample.getName(),
        appConfiguration.getUpload().label(sample, false));
  }

  @Test
  public void uploadLabel_SampleUnix() {
    Sample sample = sample();
    assertEquals("smb://lanaseq01/lanaseq/upload/" + sample.getName(),
        appConfiguration.getUpload().label(sample, true));
  }

  @Test
  public void uploadLabel_Dataset() {
    Dataset dataset = dataset();
    assertEquals("\\\\lanaseq01\\lanaseq\\upload\\" + dataset.getName(),
        appConfiguration.getUpload().label(dataset, false));
  }

  @Test
  public void uploadLabel_DatasetUnix() {
    Dataset dataset = dataset();
    assertEquals("smb://lanaseq01/lanaseq/upload/" + dataset.getName(),
        appConfiguration.getUpload().label(dataset, true));
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
