package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.sample.web.PublicSampleFiles.REST_MAPPING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
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
public class PublicSampleFilesTest {

  @TempDir
  Path temporaryFolder;
  @Autowired
  private PublicSampleFiles publicSampleFiles;
  @Autowired
  private MockMvcTester mvc;
  @MockitoBean
  private SampleService service;
  @Autowired
  private SampleRepository repository;

  @Test
  void publicSampleFile() throws IOException {
    Sample sample = repository.findById(10L).orElseThrow();
    Path samplePath = temporaryFolder.resolve(sample.getName());
    Files.createDirectories(samplePath);
    Path path = samplePath.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw");
    String content = RandomStringUtils.insecure().nextAlphanumeric(200);
    Files.writeString(path, content);
    when(service.publicFile(any(), any())).thenReturn(Optional.of(path));
    FileSystemResource resource = publicSampleFiles.publicSampleFile(sample.getName(),
        "JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw");
    assertEquals(path.toFile(), resource.getFile());
    verify(service).publicFile(sample.getName(), "JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw");
  }

  @Test
  void publicSampleFile_Empty() {
    when(service.publicFile(any(), any())).thenReturn(Optional.empty());
    Sample sample = repository.findById(10L).orElseThrow();
    assertThrows(ResourceNotFoundException.class,
        () -> publicSampleFiles.publicSampleFile(sample.getName(),
            "JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw"));
    verify(service).publicFile(sample.getName(), "JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw");
  }

  @Test
  void publicSampleFile_InvalidPath_DoubleDot() {
    Sample sample = repository.findById(10L).orElseThrow();
    assertThrows(IllegalArgumentException.class,
        () -> publicSampleFiles.publicSampleFile(sample.getName(),
            "../JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw"));
    verify(service, never()).publicFile(any(), any());
  }

  @Test
  void publicSampleFile_InvalidPath_Slash() {
    Sample sample = repository.findById(10L).orElseThrow();
    assertThrows(IllegalArgumentException.class,
        () -> publicSampleFiles.publicSampleFile(sample.getName(),
            "subfolder/JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw"));
    verify(service, never()).publicFile(any(), any());
  }

  @Test
  void publicSampleFile_InvalidPath_Backslash() {
    Sample sample = repository.findById(10L).orElseThrow();
    assertThrows(IllegalArgumentException.class,
        () -> publicSampleFiles.publicSampleFile(sample.getName(),
            "subfolder\\JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw"));
    verify(service, never()).publicFile(any(), any());
  }

  @Test
  void publicSampleFile_Mvc() throws IOException {
    Sample sample = repository.findById(10L).orElseThrow();
    Path samplePath = temporaryFolder.resolve(sample.getName());
    Files.createDirectories(samplePath);
    Path path = samplePath.resolve("JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw");
    String content = RandomStringUtils.insecure().nextAlphanumeric(200);
    Files.writeString(path, content);
    when(service.publicFile(any(), any())).thenReturn(Optional.of(path));
    MvcTestResultAssert resultAssert = mvc.get().uri("/" + REST_MAPPING + "/" + sample.getName()
        + "/JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw").assertThat();
    resultAssert.doesNotHaveFailed();
    resultAssert.hasContentType(MediaType.APPLICATION_JSON);
    resultAssert.apply(result -> {
      assertEquals(content.length(), result.getResponse().getContentLength());
      assertEquals(content, result.getResponse().getContentAsString());
    });
  }

  @Test
  void publicSampleFile_Mvc_Empty() {
    when(service.publicFile(any(), any())).thenReturn(Optional.empty());
    Sample sample = repository.findById(10L).orElseThrow();
    MvcTestResultAssert resultAssert = mvc.get().uri("/" + REST_MAPPING + "/" + sample.getName()
        + "/JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw").assertThat();
    resultAssert.hasFailed();
    resultAssert.failure().isInstanceOf(ResourceNotFoundException.class);
  }
}
