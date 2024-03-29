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

import static ca.qc.ircm.lanaseq.Constants.ADD;
import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.EDIT;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.OWNER;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.DateRangeField;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
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
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Samples view.
 */
@Route(value = SamplesView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class SamplesView extends VerticalLayout
    implements LocaleChangeObserver, HasDynamicTitle, NotificationComponent {
  public static final String VIEW_NAME = "samples";
  public static final String ID = "samples-view";
  public static final String HEADER = "header";
  public static final String SAMPLES = "samples";
  public static final String MERGE = "merge";
  public static final String FILES = "files";
  public static final String ANALYZE = "analyze";
  public static final String MERGED = "merged";
  public static final String SAMPLES_REQUIRED = property(SAMPLES, "required");
  public static final String SAMPLES_MORE_THAN_ONE = property(SAMPLES, "moreThanOne");
  public static final String SAMPLES_CANNOT_WRITE = property(SAMPLES, "cannotWrite");
  public static final String MERGE_ERROR = property(MERGE, "error");
  public static final String EDIT_BUTTON =
      "<vaadin-button class='" + EDIT + "' theme='icon' @click='${edit}'>"
          + "<vaadin-icon icon='vaadin:edit' slot='prefix'></vaadin-icon>" + "</vaadin-button>";
  private static final long serialVersionUID = -6945706067250351889L;
  private static final Logger logger = LoggerFactory.getLogger(SamplesView.class);
  protected H2 header = new H2();
  protected Grid<Sample> samples = new Grid<>();
  protected Column<Sample> name;
  protected Column<Sample> protocol;
  protected Column<Sample> date;
  protected Column<Sample> owner;
  protected Column<Sample> edit;
  protected TextField nameFilter = new TextField();
  protected TextField protocolFilter = new TextField();
  protected DateRangeField dateFilter = new DateRangeField();
  protected TextField ownerFilter = new TextField();
  protected Div error = new Div();
  protected Button add = new Button();
  protected Button merge = new Button();
  protected Button files = new Button();
  protected Button analyze = new Button();
  @Autowired
  protected ObjectFactory<SampleDialog> dialogFactory;
  @Autowired
  protected ObjectFactory<SampleFilesDialog> filesDialogFactory;
  @Autowired
  protected ObjectFactory<SamplesAnalysisDialog> analysisDialogFactory;
  @Autowired
  private transient SamplesViewPresenter presenter;

  public SamplesView() {
  }

  SamplesView(SamplesViewPresenter presenter, ObjectFactory<SampleDialog> dialogFactory,
      ObjectFactory<SampleFilesDialog> filesDialogFactory,
      ObjectFactory<SamplesAnalysisDialog> analysisDialogFactory) {
    this.presenter = presenter;
    this.dialogFactory = dialogFactory;
    this.filesDialogFactory = filesDialogFactory;
    this.analysisDialogFactory = analysisDialogFactory;
  }

  @PostConstruct
  void init() {
    logger.debug("samples view");
    setId(ID);
    setHeightFull();
    add(header, samples, error, new HorizontalLayout(add, merge, files, analyze));
    expand(samples);
    header.setId(HEADER);
    samples.setId(SAMPLES);
    samples.setSelectionMode(SelectionMode.MULTI);
    name = samples.addColumn(sample -> sample.getName(), NAME).setKey(NAME).setSortProperty(NAME)
        .setComparator(NormalizedComparator.of(Sample::getName)).setFlexGrow(2);
    protocol = samples.addColumn(sample -> sample.getProtocol().getName(), PROTOCOL)
        .setKey(PROTOCOL).setSortProperty(PROTOCOL + "." + NAME)
        .setComparator(NormalizedComparator.of(sample -> sample.getProtocol().getName()))
        .setFlexGrow(1);
    date = samples
        .addColumn(new LocalDateRenderer<>(Sample::getDate, () -> DateTimeFormatter.ISO_LOCAL_DATE))
        .setKey(DATE).setSortProperty(DATE).setComparator(Comparator.comparing(Sample::getDate))
        .setFlexGrow(1);
    owner = samples.addColumn(sample -> sample.getOwner().getEmail(), OWNER).setKey(OWNER)
        .setSortProperty(OWNER + "." + EMAIL)
        .setComparator(NormalizedComparator.of(p -> p.getOwner().getEmail())).setFlexGrow(1);
    edit = samples.addColumn(
        LitRenderer.<Sample>of(EDIT_BUTTON).withFunction("edit", sample -> presenter.view(sample)))
        .setKey(EDIT).setSortable(false).setFlexGrow(0);
    samples.sort(GridSortOrder.desc(date).build());
    samples.addItemDoubleClickListener(e -> presenter.view(e.getItem()));
    samples.addItemClickListener(e -> {
      if (e.isCtrlKey() || e.isMetaKey()) {
        presenter.viewFiles(e.getItem());
      }
    });
    samples.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = samples.appendHeaderRow();
    filtersRow.getCell(name).setComponent(nameFilter);
    nameFilter.addValueChangeListener(e -> presenter.filterName(e.getValue()));
    nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    nameFilter.setSizeFull();
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
    error.addClassName(ERROR_TEXT);
    error.setVisible(false);
    add.setId(ADD);
    add.setIcon(VaadinIcon.PLUS.create());
    add.addClickListener(e -> presenter.add());
    merge.setId(MERGE);
    merge.setIcon(VaadinIcon.CONNECT.create());
    merge.addClickListener(e -> presenter.merge());
    files.setId(FILES);
    files.setIcon(VaadinIcon.FILE_O.create());
    files.addClickListener(e -> presenter.viewFiles());
    analyze.setId(ANALYZE);
    analyze.addClickListener(e -> presenter.analyze());
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(SamplesView.class, getLocale());
    final AppResources sampleResources = new AppResources(Sample.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    header.setText(resources.message(HEADER));
    String nameHeader = sampleResources.message(NAME);
    name.setHeader(nameHeader).setFooter(nameHeader);
    String protocolHeader = sampleResources.message(PROTOCOL);
    protocol.setHeader(protocolHeader).setFooter(protocolHeader);
    String dateHeader = sampleResources.message(DATE);
    date.setHeader(dateHeader).setFooter(dateHeader);
    String ownerHeader = sampleResources.message(OWNER);
    owner.setHeader(ownerHeader).setFooter(ownerHeader);
    String editHeader = webResources.message(EDIT);
    edit.setHeader(editHeader).setFooter(editHeader);
    nameFilter.setPlaceholder(webResources.message(ALL));
    protocolFilter.setPlaceholder(webResources.message(ALL));
    ownerFilter.setPlaceholder(webResources.message(ALL));
    add.setText(webResources.message(ADD));
    merge.setText(resources.message(MERGE));
    files.setText(resources.message(FILES));
    analyze.setText(resources.message(ANALYZE));
    presenter.localeChange(getLocale());
  }

  @Override
  public String getPageTitle() {
    AppResources resources = new AppResources(SamplesView.class, getLocale());
    AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }
}
