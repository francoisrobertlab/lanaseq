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

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.OWNER;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.PROJECT;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LocalDateTimeRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import java.time.format.DateTimeFormatter;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Datasets view.
 */
@Route(value = DatasetsView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class DatasetsView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {
  public static final String VIEW_NAME = "datasets";
  public static final String ID = styleName(VIEW_NAME, "view");
  public static final String HEADER = "header";
  public static final String DATASETS = "datasets";
  public static final String FILENAME = "filename";
  public static final String DATASETS_REQUIRED = property(DATASETS, REQUIRED);
  public static final String PERMISSIONS = "permissions";
  public static final String PERMISSIONS_DENIED = property(PERMISSIONS, "denied");
  private static final long serialVersionUID = 2568742367790329628L;
  protected H2 header = new H2();
  protected Grid<Dataset> datasets = new Grid<>();
  protected Column<Dataset> filename;
  protected Column<Dataset> name;
  protected Column<Dataset> project;
  protected Column<Dataset> protocol;
  protected Column<Dataset> date;
  protected Column<Dataset> owner;
  protected TextField filenameFilter = new TextField();
  protected TextField nameFilter = new TextField();
  protected TextField projectFilter = new TextField();
  protected TextField protocolFilter = new TextField();
  protected TextField ownerFilter = new TextField();
  protected Div error = new Div();
  protected Button add = new Button();
  protected Button permissions = new Button();
  @Autowired
  protected DatasetDialog datasetDialog;
  @Autowired
  protected ProtocolDialog protocolDialog;
  @Autowired
  protected DatasetPermissionsDialog datasetPermissionsDialog;
  @Autowired
  private transient DatasetsViewPresenter presenter;

  public DatasetsView() {
  }

  protected DatasetsView(DatasetsViewPresenter presenter, DatasetDialog datasetDialog,
      ProtocolDialog protocolDialog, DatasetPermissionsDialog datasetPermissionsDialog) {
    this.presenter = presenter;
    this.datasetDialog = datasetDialog;
    this.protocolDialog = protocolDialog;
    this.datasetPermissionsDialog = datasetPermissionsDialog;
  }

  @PostConstruct
  void init() {
    setId(ID);
    HorizontalLayout buttonsLayout = new HorizontalLayout();
    add(header, datasets, error, buttonsLayout);
    buttonsLayout.add(add, permissions);
    header.setId(HEADER);
    datasets.setId(DATASETS);
    datasets.addItemDoubleClickListener(e -> {
      if (e.getColumn() == protocol) {
        presenter.view(e.getItem().getProtocol());
      } else {
        presenter.view(e.getItem());
      }
    });
    filename = datasets.addColumn(dataset -> dataset.getFilename(), FILENAME).setKey(FILENAME)
        .setComparator(NormalizedComparator.of(Dataset::getFilename));
    name = datasets.addColumn(dataset -> dataset.getName(), NAME).setKey(NAME)
        .setComparator(NormalizedComparator.of(Dataset::getName));
    project = datasets.addColumn(dataset -> dataset.getProject(), PROJECT).setKey(PROJECT)
        .setComparator(NormalizedComparator.of(Dataset::getProject));
    protocol = datasets.addColumn(ex -> ex.getProtocol().getName(), PROTOCOL).setKey(PROTOCOL)
        .setComparator(NormalizedComparator.of(e -> e.getProtocol().getName()));
    date = datasets
        .addColumn(new LocalDateTimeRenderer<>(Dataset::getDate, DateTimeFormatter.ISO_LOCAL_DATE),
            DATE)
        .setKey(DATE);
    owner = datasets.addColumn(dataset -> dataset.getOwner().getEmail(), OWNER).setKey(OWNER)
        .setComparator(NormalizedComparator.of(e -> e.getOwner().getEmail()));
    datasets.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = datasets.appendHeaderRow();
    filtersRow.getCell(filename).setComponent(filenameFilter);
    filenameFilter.addValueChangeListener(e -> presenter.filterFilename(e.getValue()));
    filenameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    filenameFilter.setSizeFull();
    filtersRow.getCell(name).setComponent(nameFilter);
    nameFilter.addValueChangeListener(e -> presenter.filterName(e.getValue()));
    nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    nameFilter.setSizeFull();
    filtersRow.getCell(project).setComponent(projectFilter);
    projectFilter.addValueChangeListener(e -> presenter.filterProject(e.getValue()));
    projectFilter.setValueChangeMode(ValueChangeMode.EAGER);
    projectFilter.setSizeFull();
    filtersRow.getCell(protocol).setComponent(protocolFilter);
    protocolFilter.addValueChangeListener(e -> presenter.filterProtocol(e.getValue()));
    protocolFilter.setValueChangeMode(ValueChangeMode.EAGER);
    protocolFilter.setSizeFull();
    filtersRow.getCell(owner).setComponent(ownerFilter);
    ownerFilter.addValueChangeListener(e -> presenter.filterOwner(e.getValue()));
    ownerFilter.setValueChangeMode(ValueChangeMode.EAGER);
    ownerFilter.setSizeFull();
    error.setId(ERROR_TEXT);
    add.setId(ADD);
    add.addClickListener(e -> presenter.add());
    permissions.setId(PERMISSIONS);
    permissions.addClickListener(e -> presenter.permissions());
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(DatasetsView.class, getLocale());
    final AppResources datasetResources = new AppResources(Dataset.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER));
    String filenameHeader = datasetResources.message(FILENAME);
    filename.setHeader(filenameHeader).setFooter(filenameHeader);
    String nameHeader = datasetResources.message(NAME);
    name.setHeader(nameHeader).setFooter(nameHeader);
    String projectHeader = datasetResources.message(PROJECT);
    project.setHeader(projectHeader).setFooter(projectHeader);
    String protocolHeader = datasetResources.message(PROTOCOL);
    protocol.setHeader(protocolHeader).setFooter(protocolHeader);
    String dateHeader = datasetResources.message(DATE);
    date.setHeader(dateHeader).setFooter(dateHeader);
    String ownerHeader = datasetResources.message(OWNER);
    owner.setHeader(ownerHeader).setFooter(ownerHeader);
    filenameFilter.setPlaceholder(webResources.message(ALL));
    nameFilter.setPlaceholder(webResources.message(ALL));
    projectFilter.setPlaceholder(webResources.message(ALL));
    protocolFilter.setPlaceholder(webResources.message(ALL));
    ownerFilter.setPlaceholder(webResources.message(ALL));
    add.setText(webResources.message(ADD));
    add.setIcon(VaadinIcon.PLUS.create());
    permissions.setText(resources.message(PERMISSIONS));
    permissions.setIcon(VaadinIcon.LOCK.create());
    presenter.localeChange(getLocale());
  }

  @Override
  public String getPageTitle() {
    AppResources resources = new AppResources(DatasetsView.class, getLocale());
    AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }
}
