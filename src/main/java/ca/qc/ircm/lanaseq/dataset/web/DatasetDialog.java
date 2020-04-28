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

package ca.qc.ircm.lanaseq.dataset.web;

import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.PRIMARY;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.THEME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.ASSAY;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.PROJECT;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Assay;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.web.SavedEvent;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Dataset dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetDialog extends Dialog implements LocaleChangeObserver, NotificationComponent {
  private static final long serialVersionUID = 3285639770914046262L;
  public static final String ID = "dataset-dialog";
  public static final String HEADER = "header";
  public static final String SAVED = "saved";
  protected H3 header = new H3();
  protected TextField name = new TextField();
  protected TextField project = new TextField();
  protected ComboBox<Protocol> protocol = new ComboBox<>();
  protected ComboBox<Assay> assay = new ComboBox<>();
  protected HorizontalLayout buttonsLayout = new HorizontalLayout();
  protected Button save = new Button();
  protected Button cancel = new Button();
  @Autowired
  private DatasetDialogPresenter presenter;

  protected DatasetDialog() {
  }

  protected DatasetDialog(DatasetDialogPresenter presenter) {
    this.presenter = presenter;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  @PostConstruct
  void init() {
    setId(ID);
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.add(header, name, project, protocol, assay, buttonsLayout);
    buttonsLayout.add(save, cancel);
    header.setId(id(HEADER));
    name.setId(id(NAME));
    project.setId(id(PROJECT));
    protocol.setId(id(PROTOCOL));
    protocol.setItemLabelGenerator(Protocol::getName);
    protocol.setPreventInvalidInput(true);
    assay.setId(id(ASSAY));
    assay.setItemLabelGenerator(a -> a.getLabel(getLocale()));
    assay.setItems(Assay.values());
    assay.setPreventInvalidInput(true);
    save.setId(id(SAVE));
    save.getElement().setAttribute(THEME, PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> presenter.save(getLocale()));
    cancel.setId(id(CANCEL));
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> presenter.cancel());
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources datasetResources = new AppResources(Dataset.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    updateHeader();
    name.setLabel(datasetResources.message(NAME));
    project.setLabel(datasetResources.message(PROJECT));
    protocol.setLabel(datasetResources.message(PROTOCOL));
    assay.setLabel(datasetResources.message(ASSAY));
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
    presenter.localeChange(getLocale());
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(DatasetDialog.class, getLocale());
    Dataset dataset = presenter.getDataset();
    if (dataset != null && dataset.getId() != null) {
      header.setText(resources.message(HEADER, 1, dataset.getName()));
    } else {
      header.setText(resources.message(HEADER, 0));
    }
  }

  /**
   * Adds listener to be informed when an dataset was saved.
   *
   * @param listener
   *          listener
   * @return listener registration
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration addSavedListener(ComponentEventListener<SavedEvent<DatasetDialog>> listener) {
    return addListener((Class) SavedEvent.class, listener);
  }

  void fireSavedEvent() {
    fireEvent(new SavedEvent<>(this, true));
  }

  public Dataset getDataset() {
    return presenter.getDataset();
  }

  /**
   * Sets dataset.
   *
   * @param dataset
   *          dataset
   */
  public void setDataset(Dataset dataset) {
    presenter.setDataset(dataset);
    updateHeader();
  }
}
