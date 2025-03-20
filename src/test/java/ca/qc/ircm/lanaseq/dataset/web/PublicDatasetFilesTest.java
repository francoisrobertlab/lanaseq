package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.dataset.web.PublicDatasetFiles.REST_MAPPING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.web.ResourceNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResultAssert;

@ServiceTestAnnotations
@AutoConfigureMockMvc
@WithAnonymousUser
public class PublicDatasetFilesTest {

  @TempDir
  Path temporaryFolder;
  @Autowired
  private PublicDatasetFiles publicDatasetFiles;
  @Autowired
  private MockMvcTester mvc;
  @MockitoBean
  private DatasetService service;
  @Autowired
  private DatasetRepository repository;

  @Test
  public void publicDatasetFile() throws IOException {
    Dataset dataset = repository.findById(6L).orElseThrow();
    Path datasetPath = temporaryFolder.resolve(dataset.getName());
    Files.createDirectories(datasetPath);
    Path path = datasetPath.resolve("ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw");
    String content = RandomStringUtils.insecure().nextAlphanumeric(200);
    Files.writeString(path, content);
    when(service.publicFile(any(), any())).thenReturn(Optional.of(path));
    FileSystemResource resource = publicDatasetFiles.publicDatasetFile(dataset.getName(),
        "ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw");
    assertEquals(path.toFile(), resource.getFile());
    verify(service).publicFile(dataset.getName(), "ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw");
  }

  @Test
  public void publicDatasetFile_Empty() {
    when(service.publicFile(any(), any())).thenReturn(Optional.empty());
    Dataset dataset = repository.findById(6L).orElseThrow();
    assertThrows(ResourceNotFoundException.class,
        () -> publicDatasetFiles.publicDatasetFile(dataset.getName(),
            "ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw"));
    verify(service).publicFile(dataset.getName(), "ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw");
  }

  @Test
  public void publicDatasetFile_InvalidPath_DoubleDot() {
    Dataset dataset = repository.findById(6L).orElseThrow();
    assertThrows(IllegalArgumentException.class,
        () -> publicDatasetFiles.publicDatasetFile(dataset.getName(),
            "../JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw"));
    verify(service, never()).publicFile(any(), any());
  }

  @Test
  void publicDatasetFile_InvalidPath_Slash() {
    Dataset dataset = repository.findById(6L).orElseThrow();
    assertThrows(IllegalArgumentException.class,
        () -> publicDatasetFiles.publicDatasetFile(dataset.getName(),
            "subfolder/JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw"));
    verify(service, never()).publicFile(any(), any());
  }

  @Test
  void publicDatasetFile_InvalidPath_Backslash() {
    Dataset dataset = repository.findById(6L).orElseThrow();
    assertThrows(IllegalArgumentException.class,
        () -> publicDatasetFiles.publicDatasetFile(dataset.getName(),
            "subfolder\\JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw"));
    verify(service, never()).publicFile(any(), any());
  }

  @Test
  public void publicDatasetFile_Mvc() throws IOException {
    Dataset dataset = repository.findById(6L).orElseThrow();
    Path datasetPath = temporaryFolder.resolve(dataset.getName());
    Files.createDirectories(datasetPath);
    Path path = datasetPath.resolve("ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw");
    String content = RandomStringUtils.insecure().nextAlphanumeric(200);
    Files.writeString(path, content);
    when(service.publicFile(any(), any())).thenReturn(Optional.of(path));
    MvcTestResultAssert resultAssert = mvc.get().uri(
            "/" + REST_MAPPING + "/" + dataset.getName() + "/ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw")
        .assertThat();
    resultAssert.doesNotHaveFailed();
    resultAssert.hasContentType(MediaType.APPLICATION_JSON);
    resultAssert.apply(result -> {
      assertEquals(content.length(), result.getResponse().getContentLength());
      assertEquals(content, result.getResponse().getContentAsString());
    });
  }

  @Test
  public void publicDatasetFile_Mvc_Empty() {
    when(service.publicFile(any(), any())).thenReturn(Optional.empty());
    Dataset dataset = repository.findById(6L).orElseThrow();
    MvcTestResultAssert resultAssert = mvc.get().uri(
            "/" + REST_MAPPING + "/" + dataset.getName() + "/ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw")
        .assertThat();
    resultAssert.hasFailed();
    resultAssert.failure().isInstanceOf(ResourceNotFoundException.class);
  }
}
