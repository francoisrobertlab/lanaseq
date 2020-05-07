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

import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.CANCEL;
import static ca.qc.ircm.lanaseq.Constants.PRIMARY;
import static ca.qc.ircm.lanaseq.Constants.SAVE;
import static ca.qc.ircm.lanaseq.Constants.THEME;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;
import static ca.qc.ircm.lanaseq.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lanaseq.user.UserProperties.MANAGER;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.user.User;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Datasets permissions dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetPermissionsDialog extends Dialog implements LocaleChangeObserver {
  public static final String CLASS_NAME = "dataset-permissions-dialog";
  public static final String HEADER = "header";
  public static final String MANAGERS = "managers";
  public static final String READ = "read";
  private static final long serialVersionUID = 3285639770914046262L;
  @SuppressWarnings("unused")
  private static final Logger logger = LoggerFactory.getLogger(DatasetPermissionsDialog.class);
  protected H2 header = new H2();
  protected Grid<User> managers = new Grid<>();
  protected Column<User> laboratory;
  protected Column<User> email;
  protected Column<User> read;
  protected Map<User, Checkbox> reads = new HashMap<>();
  protected TextField laboratoryFilter = new TextField();
  protected TextField emailFilter = new TextField();
  protected Button save = new Button();
  protected Button cancel = new Button();
  @Autowired
  private DatasetPermissionsDialogPresenter presenter;

  protected DatasetPermissionsDialog() {
  }

  protected DatasetPermissionsDialog(DatasetPermissionsDialogPresenter presenter) {
    this.presenter = presenter;
  }

  @PostConstruct
  void init() {
    setId(CLASS_NAME);
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    HorizontalLayout buttonsLayout = new HorizontalLayout();
    layout.add(header, managers, buttonsLayout);
    buttonsLayout.add(save, cancel);
    header.addClassName(HEADER);
    managers.addClassName(MANAGERS);
    laboratory =
        managers.addColumn(user -> user.getLaboratory().getName(), LABORATORY).setKey(LABORATORY);
    email = managers.addColumn(User::getEmail, EMAIL).setKey(EMAIL);
    read = managers.addComponentColumn(user -> read(user)).setKey(READ);
    managers.appendHeaderRow(); // Headers
    HeaderRow filters = managers.appendHeaderRow();
    filters.getCell(laboratory).setComponent(laboratoryFilter);
    laboratoryFilter.addValueChangeListener(e -> presenter.filterLaboratory(e.getValue()));
    laboratoryFilter.setValueChangeMode(ValueChangeMode.EAGER);
    laboratoryFilter.setSizeFull();
    filters.getCell(email).setComponent(emailFilter);
    emailFilter.addValueChangeListener(e -> presenter.filterEmail(e.getValue()));
    emailFilter.setValueChangeMode(ValueChangeMode.EAGER);
    emailFilter.setSizeFull();
    save.addClassName(SAVE);
    save.getElement().setAttribute(THEME, PRIMARY);
    save.setIcon(VaadinIcon.CHECK.create());
    save.addClickListener(e -> presenter.save());
    cancel.addClassName(CANCEL);
    cancel.setIcon(VaadinIcon.CLOSE.create());
    cancel.addClickListener(e -> presenter.cancel());
    presenter.init(this);
  }

  Checkbox read(User user) {
    if (reads.containsKey(user)) {
      return reads.get(user);
    }
    Checkbox checkbox = new Checkbox();
    checkbox.addClassName(READ);
    reads.put(user, checkbox);
    return checkbox;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(DatasetPermissionsDialog.class, getLocale());
    final AppResources userResources = new AppResources(User.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    updateHeader();
    String laboratoryHeader = userResources.message(LABORATORY);
    laboratory.setHeader(laboratoryHeader).setFooter(laboratoryHeader);
    String emailHeader = userResources.message(MANAGER);
    email.setHeader(emailHeader).setFooter(emailHeader);
    String readHeader = resources.message(READ);
    read.setHeader(readHeader).setFooter(readHeader);
    laboratoryFilter.setPlaceholder(webResources.message(ALL));
    emailFilter.setPlaceholder(webResources.message(ALL));
    save.setText(webResources.message(SAVE));
    cancel.setText(webResources.message(CANCEL));
  }

  private void updateHeader() {
    final AppResources resources = new AppResources(DatasetPermissionsDialog.class, getLocale());
    Dataset dataset = presenter.getDataset();
    if (dataset != null && dataset.getId() != null) {
      header.setText(resources.message(HEADER, dataset.getName()));
    } else {
      header.setText(resources.message(HEADER, ""));
    }
  }

  public Dataset getDataset() {
    return presenter.getDataset();
  }

  public void setDataset(Dataset dataset) {
    presenter.setDataset(dataset);
    updateHeader();
  }
}