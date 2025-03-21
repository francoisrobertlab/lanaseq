package ca.qc.ircm.lanaseq.files.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.DELETE;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.UsedBy.VAADIN;
import static ca.qc.ircm.lanaseq.files.web.PublicFileProperties.EXPIRY_DATE;
import static ca.qc.ircm.lanaseq.files.web.PublicFileProperties.FILENAME;
import static ca.qc.ircm.lanaseq.files.web.PublicFileProperties.OWNER;
import static ca.qc.ircm.lanaseq.files.web.PublicFileProperties.SAMPLE_NAME;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.normalizedCollator;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.UsedBy;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import ca.qc.ircm.lanaseq.dataset.web.PublicDatasetFiles;
import ca.qc.ircm.lanaseq.sample.SampleService;
import ca.qc.ircm.lanaseq.sample.web.PublicSampleFiles;
import ca.qc.ircm.lanaseq.web.DateRangeField;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.customfield.CustomFieldVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.security.RolesAllowed;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Range;

/**
 * Public files view.
 */
@Route(value = PublicFilesView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({USER})
public class PublicFilesView extends VerticalLayout implements LocaleChangeObserver,
    HasDynamicTitle {

  public static final String VIEW_NAME = "public-files";
  public static final String ID = "public-files-view";
  public static final String FILES = "files";
  public static final String DOWNLOAD_LINKS = "downloadLinks";
  public static final String DELETE_BUTTON =
      "<vaadin-button theme='" + ButtonVariant.LUMO_ICON.getVariantName() + " "
          + ButtonVariant.LUMO_ERROR.getVariantName() + "' @click='${deletePublicFile}'>"
          + "<vaadin-icon icon='vaadin:trash' slot='prefix'></vaadin-icon>" + "</vaadin-button>";
  private static final String MESSAGE_PREFIX = messagePrefix(PublicFilesView.class);
  private static final String PUBLIC_FILE_PREFIX = messagePrefix(PublicFile.class);
  private static final String CONSTANTS_PREFIX = messagePrefix(Constants.class);
  @Serial
  private static final long serialVersionUID = 7973788019109579051L;
  private static final Logger logger = LoggerFactory.getLogger(PublicFilesView.class);
  protected Grid<PublicFile> files = new Grid<>();
  protected Column<PublicFile> filename;
  protected Column<PublicFile> expiryDate;
  protected Column<PublicFile> sampleName;
  protected Column<PublicFile> owner;
  protected Column<PublicFile> delete;
  protected TextField filenameFilter = new TextField();
  protected DateRangeField expiryDateFilter = new DateRangeField();
  protected TextField sampleNameFilter = new TextField();
  protected TextField ownerFilter = new TextField();
  protected Anchor downloadLinks = new Anchor();
  protected Button downloadLinksButton = new Button();
  private final PublicFileFilter filter = new PublicFileFilter();
  private final transient DatasetService datasetService;
  private final transient SampleService sampleService;
  private final transient AppConfiguration configuration;

  /**
   * Creates new instance of PublicFilesView.
   *
   * @param datasetService dataset service
   * @param sampleService  sample service
   * @param configuration  configuration
   */
  @Autowired
  @UsedBy(VAADIN)
  protected PublicFilesView(DatasetService datasetService, SampleService sampleService,
      AppConfiguration configuration) {
    this.datasetService = datasetService;
    this.sampleService = sampleService;
    this.configuration = configuration;
    init();
  }

  private void init() {
    logger.debug("public files view");
    setId(ID);
    setHeightFull();
    add(files, new HorizontalLayout(downloadLinks));
    expand(files);
    files.setId(FILES);
    filename = files.addColumn(PublicFile::getFilename, FILENAME).setKey(FILENAME)
        .setComparator(Comparator.comparing(PublicFile::getFilename, normalizedCollator()))
        .setFlexGrow(3);
    expiryDate = files.addColumn(
            new LocalDateRenderer<>(PublicFile::getExpiryDate, () -> DateTimeFormatter.ISO_LOCAL_DATE))
        .setComparator(PublicFile::getExpiryDate).setKey(EXPIRY_DATE).setFlexGrow(0)
        .setWidth("13em");
    sampleName = files.addColumn(PublicFile::getSampleName, SAMPLE_NAME).setKey(SAMPLE_NAME)
        .setComparator(Comparator.comparing(PublicFile::getSampleName, normalizedCollator()))
        .setFlexGrow(1);
    owner = files.addColumn(pf -> pf.getOwner().getEmail(), OWNER)
        .setComparator(Comparator.comparing(pf -> pf.getOwner().getEmail(), normalizedCollator()))
        .setKey(OWNER).setFlexGrow(1);
    delete = files.addColumn(LitRenderer.<PublicFile>of(DELETE_BUTTON)
            .withFunction("deletePublicFile", this::deletePublicFile)).setKey(DELETE).setFlexGrow(0)
        .setWidth("6em");
    files.sort(GridSortOrder.desc(expiryDate).build());
    files.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = files.appendHeaderRow();
    filtersRow.getCell(filename).setComponent(filenameFilter);
    filenameFilter.addValueChangeListener(e -> filterFilename(e.getValue()));
    filenameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    filenameFilter.setSizeFull();
    filtersRow.getCell(expiryDate).setComponent(expiryDateFilter);
    expiryDateFilter.addValueChangeListener(e -> filterExpiryDate(e.getValue()));
    expiryDateFilter.setSizeFull();
    expiryDateFilter.addThemeVariants(CustomFieldVariant.LUMO_SMALL);
    filtersRow.getCell(sampleName).setComponent(sampleNameFilter);
    sampleNameFilter.addValueChangeListener(e -> filterSampleName(e.getValue()));
    sampleNameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    sampleNameFilter.setSizeFull();
    filtersRow.getCell(owner).setComponent(ownerFilter);
    ownerFilter.addValueChangeListener(e -> filterOwner(e.getValue()));
    ownerFilter.setValueChangeMode(ValueChangeMode.EAGER);
    ownerFilter.setSizeFull();
    downloadLinks.setId(DOWNLOAD_LINKS);
    downloadLinks.getElement().setAttribute("download", true);
    downloadLinks.add(downloadLinksButton);
    downloadLinksButton.setIcon(VaadinIcon.DOWNLOAD.create());
    loadPublicFiles();
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    String filenameHeader = getTranslation(PUBLIC_FILE_PREFIX + FILENAME);
    filename.setHeader(filenameHeader).setFooter(filenameHeader);
    String expiryDateHeader = getTranslation(PUBLIC_FILE_PREFIX + EXPIRY_DATE);
    expiryDate.setHeader(expiryDateHeader).setFooter(expiryDateHeader);
    String sampleNameHeader = getTranslation(PUBLIC_FILE_PREFIX + SAMPLE_NAME);
    sampleName.setHeader(sampleNameHeader).setFooter(sampleNameHeader);
    String ownerHeader = getTranslation(PUBLIC_FILE_PREFIX + OWNER);
    owner.setHeader(ownerHeader).setFooter(ownerHeader);
    String deleteHeader = getTranslation(CONSTANTS_PREFIX + DELETE);
    delete.setHeader(deleteHeader).setFooter(deleteHeader);
    downloadLinksButton.setText(getTranslation(MESSAGE_PREFIX + DOWNLOAD_LINKS));
  }

  @Override
  public String getPageTitle() {
    return getTranslation(MESSAGE_PREFIX + TITLE,
        getTranslation(CONSTANTS_PREFIX + APPLICATION_NAME));
  }

  private void loadPublicFiles() {
    files.setItems(Stream.concat(datasetService.publicFiles().stream().map(PublicFile::new),
        sampleService.publicFiles().stream().map(PublicFile::new)).toList());
    files.getListDataView().setFilter(filter);
    downloadLinks.setHref(links());
  }

  private void filterFilename(String value) {
    filter.filenameContains = value.isEmpty() ? null : value;
    files.getDataProvider().refreshAll();
    downloadLinks.setHref(links());
  }

  private void filterExpiryDate(Range<LocalDate> value) {
    filter.expiryDateRange = value;
    files.getDataProvider().refreshAll();
    downloadLinks.setHref(links());
  }

  private void filterSampleName(String value) {
    filter.sampleNameContains = value.isEmpty() ? null : value;
    files.getDataProvider().refreshAll();
    downloadLinks.setHref(links());
  }

  private void filterOwner(String value) {
    filter.ownerContains = value.isEmpty() ? null : value;
    files.getDataProvider().refreshAll();
    downloadLinks.setHref(links());
  }

  private void deletePublicFile(PublicFile publicFile) {
    if (publicFile.getDataset() != null) {
      datasetService.revokePublicFileAccess(publicFile.getDataset(),
          Paths.get(publicFile.getFilename()));
      loadPublicFiles();
    } else {
      sampleService.revokePublicFileAccess(publicFile.getSample(),
          Paths.get(publicFile.getFilename()));
      loadPublicFiles();
    }
  }

  StreamResource links() {
    String links = files.getListDataView().getItems()
        .map(pf -> configuration.getUrl(linkEnding(pf))).collect(Collectors.joining("\n"));
    return new StreamResource("links.txt",
        (output, session) -> output.write(links.getBytes(StandardCharsets.UTF_8)));
  }

  private String linkEnding(PublicFile publicFile) {
    if (publicFile.getDataset() != null) {
      return PublicDatasetFiles.publicDatasetFileUrl(publicFile.getDataset(),
          publicFile.getFilename());
    } else {
      return PublicSampleFiles.publicSampleFileUrl(publicFile.getSample(),
          publicFile.getFilename());
    }
  }
}
