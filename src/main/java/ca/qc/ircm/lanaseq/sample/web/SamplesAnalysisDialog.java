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

package ca.qc.ircm.lanaseq.sample.web;

import static ca.qc.ircm.lanaseq.Constants.CONFIRM;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppConfiguration;
import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.analysis.AnalysisService;
import ca.qc.ircm.lanaseq.sample.Sample;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.PostConstruct;
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
  private static final Logger logger = LoggerFactory.getLogger(SamplesAnalysisDialog.class);
  private static final long serialVersionUID = 3521519771905055445L;
  protected Div message = new Div();
  protected Button createFolder = new Button();
  protected ConfirmDialog confirm = new ConfirmDialog();
  protected ConfirmDialog errors = new ConfirmDialog();
  protected VerticalLayout errorsLayout = new VerticalLayout();
  private Collection<Sample> samples;
  private AnalysisService service;
  private AppConfiguration configuration;

  @Autowired
  protected SamplesAnalysisDialog(AnalysisService service, AppConfiguration configuration) {
    this.service = service;
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
    AppResources resources = new AppResources(SamplesAnalysisDialog.class, getLocale());
    setHeaderTitle(resources.message(HEADER));
    message.setText(resources.message(MESSAGE));
    createFolder.setText(resources.message(CREATE_FOLDER));
    confirm.setHeader(resources.message(CONFIRM));
    confirm.setConfirmText(resources.message(property(CONFIRM, CONFIRM)));
    errors.setHeader(resources.message(ERRORS));
    errors.setConfirmText(resources.message(property(ERRORS, CONFIRM)));
    updateHeader();
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(SamplesAnalysisDialog.class, getLocale());
    if (samples != null && samples.size() > 1) {
      setHeaderTitle(resources.message(HEADER, samples.size()));
    } else {
      setHeaderTitle(resources.message(HEADER, samples.size(),
          samples.stream().findFirst().map(Sample::getName).orElse("")));
    }
  }

  boolean validate() {
    List<String> errors = new ArrayList<>();
    service.validateSamples(samples, getLocale(), error -> errors.add(error));
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
      AppResources resources = new AppResources(SamplesAnalysisDialog.class, getLocale());
      try {
        service.copySamplesResources(samples);
        boolean unix = getUI().map(ui -> {
          WebBrowser browser = ui.getSession().getBrowser();
          return browser.isMacOSX() || browser.isLinux();
        }).orElse(false);
        String folder = configuration.getAnalysis().label(samples, unix);
        confirm.setText(resources.message(property(CONFIRM, "message"), folder));
        confirm.open();
      } catch (IOException e) {
        errorsLayout.removeAll();
        errorsLayout.add(new Span(resources.message(CREATE_FOLDER_EXCEPTION)));
        errors.open();
      } catch (IllegalArgumentException e) {
        // re-validate, something changed.
        validate();
      }
    }
  }

  public void setSample(Sample sample) {
    this.samples = Collections.nCopies(1, sample);
    updateHeader();
  }

  public void setSamples(List<Sample> samples) {
    this.samples = samples;
    updateHeader();
  }
}
