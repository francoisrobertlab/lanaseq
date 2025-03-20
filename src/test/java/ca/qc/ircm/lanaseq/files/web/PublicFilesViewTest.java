package ca.qc.ircm.lanaseq.files.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.files.web.PublicFileProperties.EXPIRY_DATE;
import static ca.qc.ircm.lanaseq.files.web.PublicFileProperties.FILENAME;
import static ca.qc.ircm.lanaseq.files.web.PublicFileProperties.OWNER;
import static ca.qc.ircm.lanaseq.files.web.PublicFileProperties.SAMPLE_NAME;
import static ca.qc.ircm.lanaseq.files.web.PublicFilesView.DELETE_BUTTON;
import static ca.qc.ircm.lanaseq.files.web.PublicFilesView.DOWNLOAD_LINKS;
import static ca.qc.ircm.lanaseq.files.web.PublicFilesView.FILES;
import static ca.qc.ircm.lanaseq.files.web.PublicFilesView.ID;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.functions;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.rendererTemplate;
import static ca.qc.ircm.lanaseq.test.utils.VaadinTestUtils.validateIcon;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetPublicFile;
import ca.qc.ircm.lanaseq.dataset.DatasetRepository;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.dataset.web.PublicDatasetFiles;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SamplePublicFile;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.sample.web.PublicSampleFiles;
import ca.qc.ircm.lanaseq.test.config.ServiceTestAnnotations;
import ca.qc.ircm.lanaseq.user.User;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.FooterRow;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceWriter;
import com.vaadin.testbench.unit.SpringUIUnitTest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;
import org.springframework.data.domain.Range.Bound;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Tests for {@link PublicFilesView}.
 */
@ServiceTestAnnotations
@WithUserDetails("jonh.smith@ircm.qc.ca")
public class PublicFilesViewTest extends SpringUIUnitTest {

  private static final String MESSAGE_PREFIX = messagePrefix(PublicFilesView.class);
  private static final String PUBLIC_FILE_PREFIX = messagePrefix(PublicFile.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  private PublicFilesView view;
  @MockitoBean
  private DatasetService datasetService;
  @MockitoBean
  private SampleService sampleService;
  @MockitoBean
  private AppConfiguration configuration;
  @Autowired
  private DatasetRepository datasetRepository;
  @Autowired
  private SampleRepository sampleRepository;
  private final Locale locale = Locale.ENGLISH;
  private final List<DatasetPublicFile> datasetPublicFiles = new ArrayList<>();
  private final List<SamplePublicFile> samplePublicFiles = new ArrayList<>();
  private long datasetPublicFileId = 1;
  private long samplePublicFileId = 1;

  @BeforeEach
  public void beforeTest() {
    datasetPublicFiles.add(datasetPublicFile(datasetRepository.findById(6L).orElseThrow(),
        "ChIPseq_Spt16_yFR101_G24D_JS1_20181208-cov.bw", LocalDate.now().plusDays(1)));
    datasetPublicFiles.add(datasetPublicFile(datasetRepository.findById(6L).orElseThrow(),
        "ChIPseq_Spt16_yFR101_G24D_JS1_20181208-spike-cov.bw", LocalDate.now().plusDays(1)));
    datasetPublicFiles.add(datasetPublicFile(datasetRepository.findById(7L).orElseThrow(),
        "ChIPseq_Spt16_yFR101_G24D_JS3_20181211-cov.bw", LocalDate.now().plusDays(5)));
    datasetPublicFiles.add(datasetPublicFile(datasetRepository.findById(7L).orElseThrow(),
        "ChIPseq_Spt16_yFR101_G24D_JS3_20181211-spike-cov.bw", LocalDate.now().plusDays(5)));
    samplePublicFiles.add(samplePublicFile(sampleRepository.findById(10L).orElseThrow(),
        "JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210-cov.bw", LocalDate.now().plusDays(1)));
    samplePublicFiles.add(samplePublicFile(sampleRepository.findById(10L).orElseThrow(),
        "JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210-spike-cov.bw", LocalDate.now().plusDays(2)));
    samplePublicFiles.add(samplePublicFile(sampleRepository.findById(11L).orElseThrow(),
        "JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211-cov.bw", LocalDate.now().plusDays(8)));
    samplePublicFiles.add(samplePublicFile(sampleRepository.findById(11L).orElseThrow(),
        "JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211-spike-cov.bw", LocalDate.now().plusDays(8)));
    samplePublicFiles.add(samplePublicFile(sampleRepository.findById(9L).orElseThrow(),
        "BC1_ChIPseq_Input_polr2c_yBC201_WT_R1_20181208-cov.bw", LocalDate.now().plusDays(12)));
    when(datasetService.publicFiles()).thenReturn(datasetPublicFiles);
    when(sampleService.publicFiles()).thenReturn(samplePublicFiles);
    when(configuration.getUrl(any())).then(i -> "https://localhost" + i.getArgument(0));
    UI.getCurrent().setLocale(locale);
    view = navigate(PublicFilesView.class);
  }

  private DatasetPublicFile datasetPublicFile(Dataset dataset, String filename,
      LocalDate expiryDate) {
    DatasetPublicFile datasetPublicFile = new DatasetPublicFile();
    datasetPublicFile.setId(datasetPublicFileId++);
    datasetPublicFile.setDataset(dataset);
    datasetPublicFile.setPath(filename);
    datasetPublicFile.setExpiryDate(expiryDate);
    return datasetPublicFile;
  }

  private SamplePublicFile samplePublicFile(Sample sample, String filename, LocalDate expiryDate) {
    SamplePublicFile samplePublicFile = new SamplePublicFile();
    samplePublicFile.setId(samplePublicFileId++);
    samplePublicFile.setSample(sample);
    samplePublicFile.setPath(filename);
    samplePublicFile.setExpiryDate(expiryDate);
    return samplePublicFile;
  }

  @Test
  public void styles() {
    assertEquals(ID, view.getId().orElse(""));
    assertEquals(FILES, view.files.getId().orElse(""));
    assertEquals(DOWNLOAD_LINKS, view.downloadLinks.getId().orElse(""));
    assertTrue(view.downloadLinks.getElement().hasAttribute("download"));
    assertEquals("", view.downloadLinks.getElement().getAttribute("download"));
    assertEquals(view.downloadLinksButton, test(view.downloadLinks).find(Button.class).first());
    validateIcon(VaadinIcon.DOWNLOAD.create(), view.downloadLinksButton.getIcon());
  }

  @Test
  public void labels() {
    HeaderRow headerRow = view.files.getHeaderRows().get(0);
    FooterRow footerRow = view.files.getFooterRows().get(0);
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + FILENAME),
        headerRow.getCell(view.filename).getText());
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + FILENAME),
        footerRow.getCell(view.filename).getText());
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + EXPIRY_DATE),
        headerRow.getCell(view.expiryDate).getText());
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + EXPIRY_DATE),
        footerRow.getCell(view.expiryDate).getText());
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + SAMPLE_NAME),
        headerRow.getCell(view.sampleName).getText());
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + OWNER),
        footerRow.getCell(view.owner).getText());
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + OWNER),
        footerRow.getCell(view.owner).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + DELETE),
        footerRow.getCell(view.delete).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + DELETE),
        footerRow.getCell(view.delete).getText());
    Button downloadLinksButton = test(view.downloadLinks).find(Button.class).first();
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DOWNLOAD_LINKS),
        downloadLinksButton.getText());
  }

  @Test
  public void localeChange() {
    Locale locale = Locale.FRENCH;
    UI.getCurrent().setLocale(locale);
    HeaderRow headerRow = view.files.getHeaderRows().get(0);
    FooterRow footerRow = view.files.getFooterRows().get(0);
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + FILENAME),
        headerRow.getCell(view.filename).getText());
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + FILENAME),
        footerRow.getCell(view.filename).getText());
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + EXPIRY_DATE),
        headerRow.getCell(view.expiryDate).getText());
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + EXPIRY_DATE),
        footerRow.getCell(view.expiryDate).getText());
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + SAMPLE_NAME),
        headerRow.getCell(view.sampleName).getText());
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + OWNER),
        footerRow.getCell(view.owner).getText());
    assertEquals(view.getTranslation(PUBLIC_FILE_PREFIX + OWNER),
        footerRow.getCell(view.owner).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + DELETE),
        footerRow.getCell(view.delete).getText());
    assertEquals(view.getTranslation(CONSTANTS_PREFIX + DELETE),
        footerRow.getCell(view.delete).getText());
    Button downloadLinksButton = test(view.downloadLinks).find(Button.class).first();
    assertEquals(view.getTranslation(MESSAGE_PREFIX + DOWNLOAD_LINKS),
        downloadLinksButton.getText());
  }

  @Test
  public void getPageTitle() {
    assertEquals(view.getTranslation(MESSAGE_PREFIX + TITLE,
        view.getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME)), view.getPageTitle());
  }

  @Test
  public void files() {
    List<PublicFile> publicFiles = view.files.getListDataView().getItems().toList();
    for (DatasetPublicFile datasetPublicFile : datasetPublicFiles) {
      PublicFile publicFile = new PublicFile(datasetPublicFile);
      assertTrue(publicFiles.contains(publicFile));
    }
    for (SamplePublicFile samplePublicFile : samplePublicFiles) {
      PublicFile publicFile = new PublicFile(samplePublicFile);
      assertTrue(publicFiles.contains(publicFile));
    }
    assertEquals(datasetPublicFiles.size() + samplePublicFiles.size(), publicFiles.size());
    List<GridSortOrder<PublicFile>> sortOrders = view.files.getSortOrder();
    assertEquals(1, sortOrders.size());
    assertEquals(view.expiryDate, sortOrders.get(0).getSorted());
    assertEquals(SortDirection.DESCENDING, sortOrders.get(0).getDirection());
  }

  @Test
  public void files_Columns() {
    assertEquals(5, view.files.getColumns().size());
    assertNotNull(view.files.getColumnByKey(FILENAME));
    assertTrue(view.files.getColumnByKey(FILENAME).isSortable());
    assertNotNull(view.files.getColumnByKey(EXPIRY_DATE));
    assertTrue(view.files.getColumnByKey(EXPIRY_DATE).isSortable());
    assertNotNull(view.files.getColumnByKey(SAMPLE_NAME));
    assertTrue(view.files.getColumnByKey(SAMPLE_NAME).isSortable());
    assertNotNull(view.files.getColumnByKey(OWNER));
    assertTrue(view.files.getColumnByKey(OWNER).isSortable());
    assertNotNull(view.files.getColumnByKey(DELETE));
    assertFalse(view.files.getColumnByKey(DELETE).isSortable());
  }

  @Test
  public void files_ColumnsValueProvider() {
    List<PublicFile> publicFiles = view.files.getListDataView().getItems().toList();
    for (int i = 0; i < publicFiles.size(); i++) {
      PublicFile publicFile = publicFiles.get(i);
      assertEquals(publicFile.getFilename(),
          test(view.files).getCellText(i, view.files.getColumns().indexOf(view.filename)));
      DateTimeFormatter expiryDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE;
      assertEquals(expiryDateFormatter.format(publicFile.getExpiryDate()),
          test(view.files).getCellText(i, view.files.getColumns().indexOf(view.expiryDate)));
      assertEquals(publicFile.getSampleName(),
          test(view.files).getCellText(i, view.files.getColumns().indexOf(view.sampleName)));
      assertEquals(publicFile.getOwner().getEmail(),
          test(view.files).getCellText(i, view.files.getColumns().indexOf(view.owner)));
      Renderer<PublicFile> deleteRawRenderer = view.files.getColumnByKey(DELETE).getRenderer();
      assertInstanceOf(LitRenderer.class, deleteRawRenderer);
      LitRenderer<PublicFile> deleteRenderer = (LitRenderer<PublicFile>) deleteRawRenderer;
      assertEquals(DELETE_BUTTON, rendererTemplate(deleteRenderer));
      assertTrue(functions(deleteRenderer).containsKey("deletePublicFile"));
      functions(deleteRenderer).get("deletePublicFile").accept(publicFile, null);
    }
  }

  @Test
  public void files_Delete() {
    LitRenderer<PublicFile> deleteRenderer = (LitRenderer<PublicFile>) view.files.getColumnByKey(
        DELETE).getRenderer();
    List<PublicFile> publicFiles = view.files.getListDataView().getItems().toList();
    for (PublicFile publicFile : publicFiles) {
      functions(deleteRenderer).get("deletePublicFile").accept(publicFile, null);
      if (publicFile.getDataset() != null) {
        verify(datasetService).revokePublicFileAccess(publicFile.getDataset(),
            Paths.get(publicFile.getFilename()));
      } else {
        verify(sampleService).revokePublicFileAccess(publicFile.getSample(),
            Paths.get(publicFile.getFilename()));
      }
    }
    verify(datasetService, atLeast(5)).publicFiles();
    verify(sampleService, atLeast(5)).publicFiles();
  }

  @Test
  public void files_FilenameColumnComparator() {
    Comparator<PublicFile> comparator = view.filename.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(filename("éê"), filename("ee")));
    assertTrue(comparator.compare(filename("a"), filename("e")) < 0);
    assertTrue(comparator.compare(filename("a"), filename("é")) < 0);
    assertTrue(comparator.compare(filename("e"), filename("a")) > 0);
    assertTrue(comparator.compare(filename("é"), filename("a")) > 0);
  }

  private PublicFile filename(String filename) {
    SamplePublicFile samplePublicFile = new SamplePublicFile();
    samplePublicFile.setSample(new Sample());
    samplePublicFile.getSample().setOwner(new User());
    samplePublicFile.setExpiryDate(LocalDate.now());
    samplePublicFile.setPath(filename);
    return new PublicFile(samplePublicFile);
  }

  @Test
  public void files_ExpiryDateColumnComparator() {
    Comparator<PublicFile> comparator = view.expiryDate.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(expiryDate(LocalDate.now()), expiryDate(LocalDate.now())));
    assertTrue(
        comparator.compare(expiryDate(LocalDate.now()), expiryDate(LocalDate.now().plusDays(1)))
            < 0);
    assertTrue(
        comparator.compare(expiryDate(LocalDate.now().minusDays(1)), expiryDate(LocalDate.now()))
            < 0);
    assertTrue(
        comparator.compare(expiryDate(LocalDate.now()), expiryDate(LocalDate.now().minusDays(1)))
            > 0);
    assertTrue(
        comparator.compare(expiryDate(LocalDate.now().plusDays(1)), expiryDate(LocalDate.now()))
            > 0);
  }

  private PublicFile expiryDate(LocalDate expiryDate) {
    SamplePublicFile samplePublicFile = new SamplePublicFile();
    samplePublicFile.setSample(new Sample());
    samplePublicFile.getSample().setOwner(new User());
    samplePublicFile.setExpiryDate(expiryDate);
    samplePublicFile.setPath("");
    return new PublicFile(samplePublicFile);
  }

  @Test
  public void files_SampleNameColumnComparator() {
    Comparator<PublicFile> comparator = view.sampleName.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(sampleName("éê"), sampleName("ee")));
    assertTrue(comparator.compare(sampleName("a"), sampleName("e")) < 0);
    assertTrue(comparator.compare(sampleName("a"), sampleName("é")) < 0);
    assertTrue(comparator.compare(sampleName("e"), sampleName("a")) > 0);
    assertTrue(comparator.compare(sampleName("é"), sampleName("a")) > 0);
  }

  private PublicFile sampleName(String name) {
    SamplePublicFile samplePublicFile = new SamplePublicFile();
    samplePublicFile.setSample(new Sample());
    samplePublicFile.getSample().setName(name);
    samplePublicFile.getSample().setOwner(new User());
    samplePublicFile.setExpiryDate(LocalDate.now());
    samplePublicFile.setPath("");
    return new PublicFile(samplePublicFile);
  }

  @Test
  public void files_OwnerColumnComparator() {
    Comparator<PublicFile> comparator = view.owner.getComparator(SortDirection.ASCENDING);
    assertEquals(0, comparator.compare(owner("éê"), owner("ee")));
    assertTrue(comparator.compare(owner("a"), owner("e")) < 0);
    assertTrue(comparator.compare(owner("a"), owner("é")) < 0);
    assertTrue(comparator.compare(owner("e"), owner("a")) > 0);
    assertTrue(comparator.compare(owner("é"), owner("a")) > 0);
  }

  private PublicFile owner(String email) {
    SamplePublicFile samplePublicFile = new SamplePublicFile();
    samplePublicFile.setSample(new Sample());
    samplePublicFile.getSample().setOwner(new User());
    samplePublicFile.getSample().getOwner().setEmail(email);
    samplePublicFile.setExpiryDate(LocalDate.now());
    samplePublicFile.setPath("");
    return new PublicFile(samplePublicFile);
  }

  @Test
  public void filterFilename() {
    assertEquals(9, view.files.getListDataView().getItemCount());
    String beforeFilterHref = view.downloadLinks.getHref();
    view.filenameFilter.setValue("JS1");
    assertEquals(4, view.files.getListDataView().getItemCount());
    assertNotEquals(beforeFilterHref, view.downloadLinks.getHref());
    beforeFilterHref = view.downloadLinks.getHref();
    view.filenameFilter.setValue("");
    assertEquals(9, view.files.getListDataView().getItemCount());
    assertNotEquals(beforeFilterHref, view.downloadLinks.getHref());
  }

  @Test
  public void filterExpiryDate() {
    assertEquals(9, view.files.getListDataView().getItemCount());
    String beforeFilterHref = view.downloadLinks.getHref();
    view.expiryDateFilter.setValue(
        Range.rightUnbounded(Bound.inclusive(LocalDate.now().plusDays(6))));
    assertEquals(3, view.files.getListDataView().getItemCount());
    assertNotEquals(beforeFilterHref, view.downloadLinks.getHref());
    beforeFilterHref = view.downloadLinks.getHref();
    view.expiryDateFilter.setValue(Range.unbounded());
    assertEquals(9, view.files.getListDataView().getItemCount());
    assertNotEquals(beforeFilterHref, view.downloadLinks.getHref());
  }

  @Test
  public void filterSampleName() {
    assertEquals(9, view.files.getListDataView().getItemCount());
    String beforeFilterHref = view.downloadLinks.getHref();
    view.sampleNameFilter.setValue("JS1");
    assertEquals(4, view.files.getListDataView().getItemCount());
    assertNotEquals(beforeFilterHref, view.downloadLinks.getHref());
    beforeFilterHref = view.downloadLinks.getHref();
    view.sampleNameFilter.setValue("");
    assertEquals(9, view.files.getListDataView().getItemCount());
    assertNotEquals(beforeFilterHref, view.downloadLinks.getHref());
  }

  @Test
  public void filterOwner() {
    assertEquals(9, view.files.getListDataView().getItemCount());
    String beforeFilterHref = view.downloadLinks.getHref();
    view.ownerFilter.setValue("jonh");
    assertEquals(8, view.files.getListDataView().getItemCount());
    assertNotEquals(beforeFilterHref, view.downloadLinks.getHref());
    beforeFilterHref = view.downloadLinks.getHref();
    view.ownerFilter.setValue("");
    assertEquals(9, view.files.getListDataView().getItemCount());
    assertNotEquals(beforeFilterHref, view.downloadLinks.getHref());
  }

  @Test
  public void links() throws IOException {
    assertNotEquals("", view.downloadLinks.getHref());
    StreamResource resource = view.links();
    assertEquals("links.txt", resource.getName());
    StreamResourceWriter writer = resource.getWriter();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    writer.accept(output, UI.getCurrent().getSession());
    String[] lines = output.toString(StandardCharsets.UTF_8).split("\n");
    List<PublicFile> publicFiles = view.files.getListDataView().getItems().toList();
    assertEquals(9, lines.length);
    assertEquals(9, publicFiles.size());
    for (int i = 0; i < publicFiles.size(); i++) {
      PublicFile publicFile = publicFiles.get(i);
      if (publicFile.getDataset() != null) {
        assertEquals(
            "https://localhost/" + PublicDatasetFiles.publicDatasetFileUrl(publicFile.getDataset(),
                publicFile.getFilename()), lines[i]);
      } else {
        assertEquals(
            "https://localhost/" + PublicSampleFiles.publicSampleFileUrl(publicFile.getSample(),
                publicFile.getFilename()), lines[i]);
      }
    }
  }

  @Test
  public void links_filterFilename() throws IOException {
    view.filenameFilter.setValue("JS1");
    assertNotEquals("", view.downloadLinks.getHref());
    StreamResource resource = view.links();
    assertEquals("links.txt", resource.getName());
    StreamResourceWriter writer = resource.getWriter();
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    writer.accept(output, UI.getCurrent().getSession());
    String[] lines = output.toString(StandardCharsets.UTF_8).split("\n");
    List<PublicFile> publicFiles = view.files.getListDataView().getItems().toList();
    assertEquals(4, lines.length);
    assertEquals(4, publicFiles.size());
    for (int i = 0; i < publicFiles.size(); i++) {
      PublicFile publicFile = publicFiles.get(i);
      if (publicFile.getDataset() != null) {
        assertEquals(
            "https://localhost/" + PublicDatasetFiles.publicDatasetFileUrl(publicFile.getDataset(),
                publicFile.getFilename()), lines[i]);
      } else {
        assertEquals(
            "https://localhost/" + PublicSampleFiles.publicSampleFileUrl(publicFile.getSample(),
                publicFile.getFilename()), lines[i]);
      }
    }
  }
}
