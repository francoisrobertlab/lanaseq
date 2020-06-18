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
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TAGS;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.protocol.web.ProtocolDialog;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.DateRangeField;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
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
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Datasets view.
 */
@Route(value = DatasetsView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class DatasetsView extends VerticalLayout
    implements LocaleChangeObserver, HasDynamicTitle, NotificationComponent {
  public static final String VIEW_NAME = "datasets";
  public static final String ID = styleName(VIEW_NAME, "view");
  public static final String HEADER = "header";
  public static final String DATASETS = "datasets";
  public static final String MERGE = "merge";
  public static final String ADD_FILES = "addFiles";
  public static final String MERGE_ERROR = property(MERGE, "error");
  public static final String DATASETS_REQUIRED = property(DATASETS, REQUIRED);
  public static final String DATASETS_MORE_THAN_ONE = property(DATASETS, "moreThanOne");
  public static final String DATASETS_CANNOT_WRITE = property(DATASETS, "cannotWrite");
  public static final String MERGED = "merged";
  private static final long serialVersionUID = 2568742367790329628L;
  protected H2 header = new H2();
  protected Grid<Dataset> datasets = new Grid<>();
  protected Column<Dataset> name;
  protected Column<Dataset> tags;
  protected Column<Dataset> protocol;
  protected Column<Dataset> date;
  protected Column<Dataset> owner;
  protected TextField nameFilter = new TextField();
  protected TextField tagsFilter = new TextField();
  protected TextField protocolFilter = new TextField();
  protected DateRangeField dateFilter = new DateRangeField();
  protected TextField ownerFilter = new TextField();
  protected Div error = new Div();
  protected Button add = new Button();
  protected Button merge = new Button();
  protected Button addFiles = new Button();
  @Autowired
  protected DatasetDialog dialog;
  @Autowired
  protected ProtocolDialog protocolDialog;
  @Autowired
  protected AddDatasetFilesDialog addFilesDialog;
  @Autowired
  private transient DatasetsViewPresenter presenter;

  public DatasetsView() {
  }

  protected DatasetsView(DatasetsViewPresenter presenter, DatasetDialog dialog,
      AddDatasetFilesDialog addFilesDialog, ProtocolDialog protocolDialog) {
    this.presenter = presenter;
    this.dialog = dialog;
    this.addFilesDialog = addFilesDialog;
    this.protocolDialog = protocolDialog;
  }

  @PostConstruct
  void init() {
    setId(ID);
    add(header, datasets, error, new HorizontalLayout(add, merge, addFiles), dialog, addFilesDialog,
        addFilesDialog);
    header.setId(HEADER);
    datasets.setId(DATASETS);
    datasets.setSelectionMode(SelectionMode.MULTI);
    datasets.addItemClickListener(e -> {
      if (e.isCtrlKey() || e.isMetaKey()) {
        presenter.addFiles(e.getItem(), getLocale());
      }
    });
    datasets.addItemDoubleClickListener(e -> {
      if (e.getColumn() == protocol && protocol(e.getItem()).getId() != null) {
        presenter.view(protocol(e.getItem()));
      } else {
        presenter.view(e.getItem());
      }
    });
    name = datasets.addColumn(dataset -> dataset.getName(), NAME).setKey(NAME)
        .setComparator(NormalizedComparator.of(Dataset::getName));
    tags = datasets
        .addColumn(dataset -> dataset.getTags().stream().collect(Collectors.joining(", ")), TAGS)
        .setKey(TAGS);
    protocol = datasets.addColumn(dataset -> protocol(dataset).getName(), PROTOCOL).setKey(PROTOCOL)
        .setComparator(NormalizedComparator.of(dataset -> protocol(dataset).getName()));
    date = datasets
        .addColumn(new LocalDateTimeRenderer<>(Dataset::getDate, DateTimeFormatter.ISO_LOCAL_DATE),
            DATE)
        .setKey(DATE);
    owner = datasets.addColumn(dataset -> dataset.getOwner().getEmail(), OWNER).setKey(OWNER)
        .setComparator(NormalizedComparator.of(e -> e.getOwner().getEmail()));
    datasets.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = datasets.appendHeaderRow();
    filtersRow.getCell(name).setComponent(nameFilter);
    nameFilter.addValueChangeListener(e -> presenter.filterName(e.getValue()));
    nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    nameFilter.setSizeFull();
    filtersRow.getCell(tags).setComponent(tagsFilter);
    tagsFilter.addValueChangeListener(e -> presenter.filterTags(e.getValue()));
    tagsFilter.setValueChangeMode(ValueChangeMode.EAGER);
    tagsFilter.setSizeFull();
    filtersRow.getCell(protocol).setComponent(protocolFilter);
    protocolFilter.addValueChangeListener(e -> presenter.filterProtocol(e.getValue()));
    protocolFilter.setValueChangeMode(ValueChangeMode.EAGER);
    protocolFilter.setSizeFull();
    filtersRow.getCell(date).setComponent(dateFilter);
    dateFilter.addValueChangeListener(e -> presenter.filterDate(e.getValue()));
    dateFilter.setSizeFull();
    filtersRow.getCell(owner).setComponent(ownerFilter);
    ownerFilter.addValueChangeListener(e -> presenter.filterOwner(e.getValue()));
    ownerFilter.setValueChangeMode(ValueChangeMode.EAGER);
    ownerFilter.setSizeFull();
    error.setId(ERROR_TEXT);
    add.setId(ADD);
    add.setIcon(VaadinIcon.PLUS.create());
    add.addClickListener(e -> presenter.add());
    merge.setId(MERGE);
    merge.setIcon(VaadinIcon.CONNECT.create());
    merge.addClickListener(e -> presenter.merge(getLocale()));
    addFiles.setId(ADD_FILES);
    addFiles.setIcon(VaadinIcon.FILE_ADD.create());
    addFiles.addClickListener(e -> presenter.addFiles(getLocale()));
    presenter.init(this);
  }

  private Protocol protocol(Dataset dataset) {
    return dataset.getSamples() != null
        ? dataset.getSamples().stream().findFirst().map(s -> s.getProtocol()).orElse(new Protocol())
        : new Protocol();
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(DatasetsView.class, getLocale());
    final AppResources datasetResources = new AppResources(Dataset.class, getLocale());
    final AppResources sampleResources = new AppResources(Sample.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER));
    String nameHeader = datasetResources.message(NAME);
    name.setHeader(nameHeader).setFooter(nameHeader);
    String tagsHeader = datasetResources.message(TAGS);
    tags.setHeader(tagsHeader).setFooter(tagsHeader);
    String protocolHeader = sampleResources.message(PROTOCOL);
    protocol.setHeader(protocolHeader).setFooter(protocolHeader);
    String dateHeader = datasetResources.message(DATE);
    date.setHeader(dateHeader).setFooter(dateHeader);
    String ownerHeader = datasetResources.message(OWNER);
    owner.setHeader(ownerHeader).setFooter(ownerHeader);
    nameFilter.setPlaceholder(webResources.message(ALL));
    tagsFilter.setPlaceholder(webResources.message(ALL));
    protocolFilter.setPlaceholder(webResources.message(ALL));
    ownerFilter.setPlaceholder(webResources.message(ALL));
    add.setText(webResources.message(ADD));
    merge.setText(resources.message(MERGE));
    addFiles.setText(resources.message(ADD_FILES));
  }

  @Override
  public String getPageTitle() {
    AppResources resources = new AppResources(DatasetsView.class, getLocale());
    AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }
}
