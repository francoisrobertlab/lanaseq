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

import static ca.qc.ircm.lana.experiment.ExperimentProperties.DATE;
import static ca.qc.ircm.lana.experiment.ExperimentProperties.NAME;
import static ca.qc.ircm.lana.text.Strings.normalize;
import static ca.qc.ircm.lana.user.UserRole.USER;
import static ca.qc.ircm.lana.web.WebConstants.APPLICATION_NAME;
import static ca.qc.ircm.lana.web.WebConstants.TITLE;

import ca.qc.ircm.lana.experiment.Experiment;
import ca.qc.ircm.lana.web.ViewLayout;
import ca.qc.ircm.lana.web.WebConstants;
import ca.qc.ircm.lana.web.component.BaseComponent;
import ca.qc.ircm.text.MessageResource;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.time.format.DateTimeFormatter;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;

/**
 * Experiments view.
 */
@Route(value = ExperimentsView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class ExperimentsView extends Composite<VerticalLayout>
    implements LocaleChangeObserver, HasDynamicTitle, BaseComponent {
  public static final String VIEW_NAME = "experiments";
  public static final String HEADER = "header";
  public static final String EXPERIMENTS = "experiments";
  public static final String ADD = "add";
  private static final long serialVersionUID = 2568742367790329628L;
  protected H2 header = new H2();
  protected Grid<Experiment> experiments = new Grid<>();
  protected Column<Experiment> name;
  protected Column<Experiment> date;
  protected Column<Experiment> view;
  protected Button add = new Button();
  protected ExperimentDialog experimentDialog = new ExperimentDialog();
  @Inject
  private transient ExperimentsViewPresenter presenter;

  public ExperimentsView() {
  }

  protected ExperimentsView(ExperimentsViewPresenter presenter) {
    this.presenter = presenter;
  }

  @PostConstruct
  void init() {
    VerticalLayout root = getContent();
    root.setId(VIEW_NAME);
    root.add(header);
    header.addClassName(HEADER);
    root.add(experiments);
    experiments.addClassName(EXPERIMENTS);
    name =
        experiments.addColumn(new ComponentRenderer<>(experiment -> viewButton(experiment)), NAME)
            .setKey(NAME).setComparator(
                (e1, e2) -> normalize(e1.getName()).compareToIgnoreCase(normalize(e2.getName())));
    date = experiments.addColumn(
        new LocalDateTimeRenderer<>(Experiment::getDate, DateTimeFormatter.ISO_LOCAL_DATE), DATE)
        .setKey(DATE);
    HorizontalLayout buttonsLayout = new HorizontalLayout();
    root.add(buttonsLayout);
    buttonsLayout.add(add);
    add.addClassName(ADD);
    add.addClickListener(e -> presenter.add());
    presenter.init(this);
  }

  private Button viewButton(Experiment experiment) {
    Button button = new Button();
    button.addClassName(NAME);
    button.setText(experiment.getName());
    button.addClickListener(e -> presenter.view(experiment));
    return button;
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    MessageResource resources = new MessageResource(ExperimentsView.class, getLocale());
    MessageResource userResources = new MessageResource(Experiment.class, getLocale());
    header.setText(resources.message(HEADER));
    String nameHeader = userResources.message(NAME);
    name.setHeader(nameHeader).setFooter(nameHeader);
    String dateHeader = userResources.message(DATE);
    date.setHeader(dateHeader).setFooter(dateHeader);
    add.setText(resources.message(ADD));
    add.setIcon(VaadinIcon.PLUS.create());
  }

  @Override
  public String getPageTitle() {
    MessageResource resources = new MessageResource(ExperimentsView.class, getLocale());
    MessageResource generalResources = new MessageResource(WebConstants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }
}
