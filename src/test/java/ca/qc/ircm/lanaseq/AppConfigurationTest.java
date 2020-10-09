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

import static org.junit.Assert.assertEquals;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.sample.Assay;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleType;
import ca.qc.ircm.lanaseq.test.config.NonTransactionalTestAnnotations;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@NonTransactionalTestAnnotations
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
    sample.generateName();
    return sample;
  }

  private Dataset dataset() {
    Dataset dataset = new Dataset();
    dataset.setSamples(new ArrayList<>());
    dataset.setCreationDate(LocalDateTime.of(2019, 12, 8, 10, 20, 30));
    Sample sample = new Sample();
    sample.setSampleId("my sample");
    sample.setReplicate("my replicate");
    sample.setAssay(Assay.CHIP_SEQ);
    sample.setType(SampleType.IMMUNO_PRECIPITATION);
    sample.setTarget("my target");
    sample.setStrain("yFR213");
    sample.setStrainDescription("F56G");
    sample.setTreatment("37C");
    dataset.getSamples().add(sample);
    sample = new Sample();
    sample.setSampleId("my sample2");
    dataset.getSamples().add(sample);
    dataset.generateName();
    return dataset;
  }

  @Test
  public void getLogFile() {
    assertEquals(Paths.get(System.getProperty("user.dir"), "test.log"),
        appConfiguration.getLogFile());
  }

  @Test
  public void getHome() {
    assertEquals(Paths.get(System.getProperty("user.home"), "lanaseq"), appConfiguration.getHome());
  }

  @Test
  public void getSampleHome() {
    assertEquals(Paths.get(System.getProperty("user.home"), "lanaseq", "sample"),
        appConfiguration.getSampleHome());
  }

  @Test
  public void getDatasetHome() {
    assertEquals(Paths.get(System.getProperty("user.home"), "lanaseq", "dataset"),
        appConfiguration.getDatasetHome());
  }

  @Test
  public void folder_Sample2019() {
    Sample sample = sample();
    String name = sample.getName();
    assertEquals(appConfiguration.getSampleHome().resolve("2019/" + name),
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
    assertEquals(appConfiguration.getSampleHome().resolve("2020/" + name),
        appConfiguration.folder(sample));
  }

  @Test
  public void folder_Dataset2019() {
    Dataset dataset = dataset();
    String name = dataset.getName();
    assertEquals(appConfiguration.getDatasetHome().resolve("2019/" + name),
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
    assertEquals(appConfiguration.getDatasetHome().resolve("2020/" + name),
        appConfiguration.folder(dataset));
  }

  @Test
  public void folderLabel_Sample() {
    Sample sample = sample();
    assertEquals("lanaseq\\sample\\2019\\" + sample.getName(),
        appConfiguration.folderLabel(sample, false));
  }

  @Test
  public void folderLabel_SampleUnix() {
    Sample sample = sample();
    assertEquals("lanaseq/sample/2019/" + sample.getName(),
        appConfiguration.folderLabel(sample, true));
  }

  @Test
  public void folderLabel_Dataset() {
    Dataset dataset = dataset();
    assertEquals("lanaseq\\dataset\\2019\\" + dataset.getName(),
        appConfiguration.folderLabel(dataset, false));
  }

  @Test
  public void folderLabel_DatasetUnix() {
    Dataset dataset = dataset();
    assertEquals("lanaseq/dataset/2019/" + dataset.getName(),
        appConfiguration.folderLabel(dataset, true));
  }

  @Test
  public void folderNetwork() {
    assertEquals("\\\\lanaseq01\\lanaseq", appConfiguration.folderNetwork(false));
  }

  @Test
  public void folderNetwork_Unix() {
    assertEquals("smb://lanaseq01/lanaseq", appConfiguration.folderNetwork(true));
  }

  @Test
  public void getAnalysis() {
    assertEquals(Paths.get(System.getProperty("user.home"), "lanaseq/analysis"),
        appConfiguration.getAnalysis());
  }

  @Test
  public void analysis_Dataset() {
    Dataset dataset = dataset();
    assertEquals(Paths.get(System.getProperty("user.home"), "lanaseq/analysis", dataset.getName()),
        appConfiguration.analysis(dataset));
  }

  @Test
  public void analysisLabel_Dataset() {
    Dataset dataset = dataset();
    assertEquals("lanaseq\\analysis\\" + dataset.getName(),
        appConfiguration.analysisLabel(dataset, false));
  }

  @Test
  public void analysisLabel_DatasetUnix() {
    Dataset dataset = dataset();
    assertEquals("lanaseq/analysis/" + dataset.getName(),
        appConfiguration.analysisLabel(dataset, true));
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
  public void getUpload() {
    assertEquals(Paths.get(System.getProperty("user.home"), "lanaseq/upload"),
        appConfiguration.getUpload());
  }

  @Test
  public void upload_Sample() {
    Sample sample = sample();
    String name = sample.getName();
    assertEquals(appConfiguration.getUpload().resolve(name), appConfiguration.upload(sample));
  }

  @Test
  public void upload_Dataset2019() {
    Dataset dataset = dataset();
    String name = dataset.getName();
    assertEquals(appConfiguration.getUpload().resolve(name), appConfiguration.upload(dataset));
  }

  @Test
  public void uploadLabel_Sample() {
    Sample sample = sample();
    assertEquals("lanaseq\\upload\\" + sample.getName(),
        appConfiguration.uploadLabel(sample, false));
  }

  @Test
  public void uploadLabel_SampleUnix() {
    Sample sample = sample();
    assertEquals("lanaseq/upload/" + sample.getName(), appConfiguration.uploadLabel(sample, true));
  }

  @Test
  public void uploadLabel_Dataset() {
    Dataset dataset = dataset();
    assertEquals("lanaseq\\upload\\" + dataset.getName(),
        appConfiguration.uploadLabel(dataset, false));
  }

  @Test
  public void uploadLabel_DatasetUnix() {
    Dataset dataset = dataset();
    assertEquals("lanaseq/upload/" + dataset.getName(),
        appConfiguration.uploadLabel(dataset, true));
  }

  @Test
  public void uploadNetwork() {
    assertEquals("\\\\lanaseq01\\lanaseq\\upload", appConfiguration.uploadNetwork(false));
  }

  @Test
  public void uploadNetwork_Unix() {
    assertEquals("smb://lanaseq01/lanaseq/upload", appConfiguration.uploadNetwork(true));
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
