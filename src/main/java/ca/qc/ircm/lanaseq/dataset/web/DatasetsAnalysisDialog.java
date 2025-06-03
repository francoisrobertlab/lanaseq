package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.analysis.AnalysisService;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.dataset.DatasetService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Analysis dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetsAnalysisDialog extends Dialog implements LocaleChangeObserver {

  public static final String ID = "analysis-dialog";
  public static final String HEADER = "header";
  public static final String MESSAGE = "message";
  public static final String FILENAME_PATTERNS = "filenamePatterns";
  public static final String CREATE_FOLDER = "createFolder";
  public static final String ERRORS = "errors";
  public static final String CREATE_FOLDER_EXCEPTION = property(CREATE_FOLDER, "exception");
  private static final String MESSAGE_PREFIX = messagePrefix(DatasetsAnalysisDialog.class);
  private static final Logger logger = LoggerFactory.getLogger(DatasetsAnalysisDialog.class);
  @Serial
  private static final long serialVersionUID = 3521519771905055445L;
  protected Div message = new Div();
  protected MultiSelectComboBox<String> filenamePatterns = new MultiSelectComboBox<>();
  protected Button createFolder = new Button();
  protected ConfirmDialog confirm = new ConfirmDialog();
  protected ConfirmDialog errors = new ConfirmDialog();
  private List<Dataset> datasets = new ArrayList<>();
  private final transient DatasetService service;
  private final transient AnalysisService analysisService;
  private final transient AppConfiguration configuration;

  @Autowired
  protected DatasetsAnalysisDialog(DatasetService service, AnalysisService analysisService,
      AppConfiguration configuration) {
    this.service = service;
    this.analysisService = analysisService;
    this.configuration = configuration;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  @PostConstruct
  void init() {
    setId(ID);
    setWidth("1000px");
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.add(message, filenamePatterns);
    layout.setSizeFull();
    getFooter().add(createFolder);
    message.setId(id(MESSAGE));
    filenamePatterns.setId(id(FILENAME_PATTERNS));
    filenamePatterns.setItems(List.of());
    filenamePatterns.setAllowCustomValue(true);
    filenamePatterns.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
    filenamePatterns.setMinWidth("20em");
    filenamePatterns.addCustomValueSetListener(e -> filenamePatterns.select(e.getDetail()));
    createFolder.setId(id(CREATE_FOLDER));
    createFolder.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    createFolder.addClickListener(e -> createFolder());
    confirm.setId(id(CONFIRM));
    confirm.addConfirmListener(e -> close());
    errors.setId(id(ERRORS));
    errors.addConfirmListener(e -> close());
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER));
    message.setText(getTranslation(MESSAGE_PREFIX + MESSAGE));
    filenamePatterns.setHelperText(getTranslation(MESSAGE_PREFIX + FILENAME_PATTERNS));
    createFolder.setText(getTranslation(MESSAGE_PREFIX + CREATE_FOLDER));
    confirm.setHeader(getTranslation(MESSAGE_PREFIX + CONFIRM));
    confirm.setConfirmText(getTranslation(MESSAGE_PREFIX + property(CONFIRM, CONFIRM)));
    errors.setHeader(getTranslation(MESSAGE_PREFIX + ERRORS));
    errors.setConfirmText(getTranslation(MESSAGE_PREFIX + property(ERRORS, CONFIRM)));
    updateHeader();
  }

  private void updateHeader() {
    if (datasets.size() > 1) {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, datasets.size()));
    } else {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, datasets.size(),
          datasets.stream().findFirst().map(Dataset::getName).orElse("")));
    }
  }

  void createFolder() {
    logger.debug("creating analysis folder for datasets {}", datasets);
    try {
      analysisService.copyDatasetsResources(datasets, filenamePatterns.getSelectedItems());
      boolean unix = getUI().map(ui -> {
        WebBrowser browser = ui.getSession().getBrowser();
        return browser.isMacOSX() || browser.isLinux();
      }).orElse(false);
      String folder = configuration.getAnalysis().label(datasets, unix);
      confirm.setText(getTranslation(MESSAGE_PREFIX + property(CONFIRM, "message"), folder));
      confirm.open();
    } catch (IOException e) {
      errors.setText(getTranslation(MESSAGE_PREFIX + CREATE_FOLDER_EXCEPTION));
      errors.open();
    }
  }

  public List<Long> getDatasetIds() {
    return datasets.stream().map(Dataset::getId).collect(Collectors.toList());
  }

  public void setDatasetIds(List<Long> ids) {
    Objects.requireNonNull(ids, "ids parameter cannot be null");
    this.datasets = ids.stream().map(id -> service.get(id).orElseThrow())
        .collect(Collectors.toList());
    updateHeader();
  }

  public void setDatasetId(Long id) {
    this.datasets = Collections.nCopies(1, service.get(id).orElseThrow());
    updateHeader();
  }
}
