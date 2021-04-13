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
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.DATE;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.NAME;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.OWNER;
import static ca.qc.ircm.lanaseq.dataset.DatasetProperties.TAGS;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.PROTOCOL;
import static ca.qc.ircm.lanaseq.user.UserProperties.EMAIL;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.DateRangeField;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridSortOrder;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Dataset grid.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class DatasetGrid extends Grid<Dataset> implements LocaleChangeObserver {
  private static final long serialVersionUID = -3052158575710045415L;
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
  @Autowired
  private transient DatasetGridPresenter presenter;

  public DatasetGrid() {
  }

  DatasetGrid(DatasetGridPresenter presenter) {
    this.presenter = presenter;
  }

  @PostConstruct
  void init() {
    name = addColumn(dataset -> dataset.getName(), NAME).setKey(NAME).setSortProperty(NAME)
        .setComparator(NormalizedComparator.of(Dataset::getName)).setFlexGrow(3);
    tags = addColumn(dataset -> dataset.getTags().stream().collect(Collectors.joining(", ")), TAGS)
        .setKey(TAGS).setSortable(false).setFlexGrow(1);
    protocol = addColumn(dataset -> protocol(dataset).getName(), PROTOCOL).setKey(PROTOCOL)
        .setSortable(false).setFlexGrow(1);
    date =
        addColumn(new LocalDateRenderer<>(Dataset::getDate, DateTimeFormatter.ISO_LOCAL_DATE), DATE)
            .setKey(DATE).setSortProperty(DATE).setFlexGrow(1);
    owner = addColumn(dataset -> dataset.getOwner().getEmail(), OWNER).setKey(OWNER)
        .setSortProperty(OWNER + "." + EMAIL)
        .setComparator(NormalizedComparator.of(e -> e.getOwner().getEmail())).setFlexGrow(1);
    sort(GridSortOrder.desc(date).build());
    appendHeaderRow(); // Headers.
    HeaderRow filtersRow = appendHeaderRow();
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
    presenter.init(this);
  }

  private Protocol protocol(Dataset dataset) {
    return dataset.getSamples() != null
        ? dataset.getSamples().stream().findFirst().map(s -> s.getProtocol()).orElse(new Protocol())
        : new Protocol();
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources datasetResources = new AppResources(Dataset.class, getLocale());
    final AppResources sampleResources = new AppResources(Sample.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
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
  }

  public void refreshDatasets() {
    presenter.refreshDatasets();
  }
}
