package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.Constants.messagePrefix;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.analysis.AnalysisService;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.sample.SampleService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.server.WebBrowser;
import com.vaadin.flow.spring.annotation.SpringComponent;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
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
public class SamplesAnalysisDialog extends Dialog implements LocaleChangeObserver {
  public static final String ID = "sample-analysis-dialog";
  public static final String HEADER = "header";
  public static final String MESSAGE = "message";
  public static final String CREATE_FOLDER = "createFolder";
  public static final String ERRORS = "errors";
  public static final String CREATE_FOLDER_EXCEPTION = property(CREATE_FOLDER, "exception");
  private static final String MESSAGE_PREFIX = messagePrefix(SamplesAnalysisDialog.class);
  private static final Logger logger = LoggerFactory.getLogger(SamplesAnalysisDialog.class);
  private static final long serialVersionUID = 3521519771905055445L;
  protected Div message = new Div();
  protected Button createFolder = new Button();
  protected ConfirmDialog confirm = new ConfirmDialog();
  protected ConfirmDialog errors = new ConfirmDialog();
  protected VerticalLayout errorsLayout = new VerticalLayout();
  private List<Sample> samples = new ArrayList<>();
  private transient SampleService service;
  private transient AnalysisService analysisService;
  private transient AppConfiguration configuration;

  @Autowired
  protected SamplesAnalysisDialog(SampleService service, AnalysisService analysisService,
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
    layout.add(message, createFolder, confirm, errors);
    layout.setSizeFull();
    message.setId(id(MESSAGE));
    createFolder.setId(id(CREATE_FOLDER));
    createFolder.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
    createFolder.addClickListener(e -> createFolder());
    confirm.setId(id(CONFIRM));
    confirm.addConfirmListener(e -> close());
    errors.setId(id(ERRORS));
    errors.setText(errorsLayout);
    errors.addConfirmListener(e -> close());
    addOpenedChangeListener(e -> {
      if (e.isOpened() && samples != null) {
        validate();
      }
    });
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER));
    message.setText(getTranslation(MESSAGE_PREFIX + MESSAGE));
    createFolder.setText(getTranslation(MESSAGE_PREFIX + CREATE_FOLDER));
    confirm.setHeader(getTranslation(MESSAGE_PREFIX + CONFIRM));
    confirm.setConfirmText(getTranslation(MESSAGE_PREFIX + property(CONFIRM, CONFIRM)));
    errors.setHeader(getTranslation(MESSAGE_PREFIX + ERRORS));
    errors.setConfirmText(getTranslation(MESSAGE_PREFIX + property(ERRORS, CONFIRM)));
    updateHeader();
  }

  private void updateHeader() {
    if (samples.size() > 1) {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, samples.size()));
    } else {
      setHeaderTitle(getTranslation(MESSAGE_PREFIX + HEADER, samples.size(),
          samples.stream().findFirst().map(Sample::getName).orElse("")));
    }
  }

  boolean validate() {
    List<String> errors = new ArrayList<>();
    analysisService.validateSamples(samples, getLocale(), error -> errors.add(error));
    if (!errors.isEmpty()) {
      errorsLayout.removeAll();
      errors.forEach(error -> errorsLayout.add(new Span(error)));
      this.errors.open();
    }
    createFolder.setEnabled(errors.isEmpty());
    return errors.isEmpty();
  }

  void createFolder() {
    if (validate()) {
      logger.debug("creating analysis folder for samples {}", samples);
      try {
        analysisService.copySamplesResources(samples);
        boolean unix = getUI().map(ui -> {
          WebBrowser browser = ui.getSession().getBrowser();
          return browser.isMacOSX() || browser.isLinux();
        }).orElse(false);
        String folder = configuration.getAnalysis().label(samples, unix);
        confirm.setText(getTranslation(MESSAGE_PREFIX + property(CONFIRM, "message"), folder));
        confirm.open();
      } catch (IOException e) {
        errorsLayout.removeAll();
        errorsLayout.add(new Span(getTranslation(MESSAGE_PREFIX + CREATE_FOLDER_EXCEPTION)));
        errors.open();
      } catch (IllegalArgumentException e) {
        // re-validate, something changed.
        validate();
      }
    }
  }

  public List<Long> getSampleIds() {
    return samples.stream().map(Sample::getId).collect(Collectors.toList());
  }

  public void setSampleId(Long id) {
    this.samples = Collections.nCopies(1, service.get(id).orElseThrow());
    updateHeader();
  }

  public void setSampleIds(List<Long> ids) {
    Objects.requireNonNull(ids, "ids parameter cannot be null");
    this.samples =
        ids.stream().map(id -> service.get(id).orElseThrow()).collect(Collectors.toList());
    updateHeader();
  }
}
