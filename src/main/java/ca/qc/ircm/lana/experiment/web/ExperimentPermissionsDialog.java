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

package ca.qc.ircm.lana.experiment.web;

import static ca.qc.ircm.lana.user.UserProperties.EMAIL;
import static ca.qc.ircm.lana.user.UserProperties.LABORATORY;
import static ca.qc.ircm.lana.user.UserProperties.MANAGER;
import static ca.qc.ircm.lana.web.WebConstants.ALL;
import static ca.qc.ircm.lana.web.WebConstants.CANCEL;
import static ca.qc.ircm.lana.web.WebConstants.PRIMARY;
import static ca.qc.ircm.lana.web.WebConstants.SAVE;
import static ca.qc.ircm.lana.web.WebConstants.THEME;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
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
import javax.inject.Inject;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Experiments permissions dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ExperimentPermissionsDialog extends Dialog
    implements LocaleChangeObserver, BaseComponent {
  private static final long serialVersionUID = 3285639770914046262L;
  public static final String CLASS_NAME = "experiment-permissions-dialog";
  public static final String HEADER = "header";
  public static final String MANAGERS = "managers";
  public static final String READ = "read";
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
  @Inject
  private ExperimentPermissionsDialogPresenter presenter;

  protected ExperimentPermissionsDialog() {
  }

  protected ExperimentPermissionsDialog(ExperimentPermissionsDialogPresenter presenter) {
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

  private Checkbox read(User user) {
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
    final MessageResource resources =
        new MessageResource(ExperimentPermissionsDialog.class, getLocale());
    final MessageResource userResources = new MessageResource(User.class, getLocale());
    final MessageResource webResources = new MessageResource(WebConstants.class, getLocale());
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
    final MessageResource resources =
        new MessageResource(ExperimentPermissionsDialog.class, getLocale());
    Experiment experiment = presenter.getExperiment();
    if (experiment != null && experiment.getId() != null) {
      header.setText(resources.message(HEADER, experiment.getName()));
    } else {
      header.setText(resources.message(HEADER, ""));
    }
  }

  public Experiment getExperiment() {
    return presenter.getExperiment();
  }

  public void setExperiment(Experiment experiment) {
    presenter.setExperiment(experiment);
    updateHeader();
  }
}
