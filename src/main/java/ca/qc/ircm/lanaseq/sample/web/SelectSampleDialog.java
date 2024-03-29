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

import static ca.qc.ircm.lanaseq.Constants.ALL;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.DATE;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.NAME;
import static ca.qc.ircm.lanaseq.sample.SampleProperties.OWNER;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.text.NormalizedComparator;
import ca.qc.ircm.lanaseq.web.DateRangeField;
import ca.qc.ircm.lanaseq.web.SelectedEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.Grid.Column;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.LocalDateRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.shared.Registration;
import com.vaadin.flow.spring.annotation.SpringComponent;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;

/**
 * Select sample dialog.
 */
@SpringComponent
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SelectSampleDialog extends Dialog implements LocaleChangeObserver {
  public static final String ID = "select-sample-dialog";
  public static final String SAMPLES = "samples";
  private static final long serialVersionUID = -1701490833972618304L;
  protected Grid<Sample> samples = new Grid<>();
  protected Column<Sample> name;
  protected Column<Sample> date;
  protected Column<Sample> owner;
  protected TextField nameFilter = new TextField();
  protected DateRangeField dateFilter = new DateRangeField();
  protected TextField ownerFilter = new TextField();
  @Autowired
  private transient SelectSampleDialogPresenter presenter;

  public SelectSampleDialog() {
  }

  SelectSampleDialog(SelectSampleDialogPresenter presenter) {
    this.presenter = presenter;
  }

  public static String id(String baseId) {
    return styleName(ID, baseId);
  }

  @PostConstruct
  void init() {
    setId(ID);
    setWidth("1280px");
    VerticalLayout layout = new VerticalLayout();
    add(layout);
    layout.add(samples);
    layout.setSizeFull();
    layout.expand(samples);
    samples.setId(id(SAMPLES));
    name = samples.addColumn(sample -> sample.getName(), NAME).setKey(NAME)
        .setComparator(NormalizedComparator.of(Sample::getName));
    date = samples
        .addColumn(new LocalDateRenderer<>(Sample::getDate, () -> DateTimeFormatter.ISO_LOCAL_DATE))
        .setKey(DATE).setSortProperty(DATE).setComparator(Comparator.comparing(Sample::getDate));
    owner = samples.addColumn(sample -> sample.getOwner().getEmail(), OWNER).setKey(OWNER)
        .setComparator(NormalizedComparator.of(p -> p.getOwner().getEmail()));
    samples.addItemDoubleClickListener(e -> {
      presenter.select(e.getItem());
    });
    samples.appendHeaderRow(); // Headers.
    HeaderRow filtersRow = samples.appendHeaderRow();
    filtersRow.getCell(name).setComponent(nameFilter);
    nameFilter.addValueChangeListener(e -> presenter.filterName(e.getValue()));
    nameFilter.setValueChangeMode(ValueChangeMode.EAGER);
    nameFilter.setSizeFull();
    filtersRow.getCell(date).setComponent(dateFilter);
    dateFilter.addValueChangeListener(e -> presenter.filterDate(e.getValue()));
    dateFilter.setSizeFull();
    filtersRow.getCell(owner).setComponent(ownerFilter);
    ownerFilter.addValueChangeListener(e -> presenter.filterOwner(e.getValue()));
    ownerFilter.setValueChangeMode(ValueChangeMode.EAGER);
    ownerFilter.setSizeFull();
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources sampleResources = new AppResources(Sample.class, getLocale());
    final AppResources webResources = new AppResources(Constants.class, getLocale());
    String nameHeader = sampleResources.message(NAME);
    name.setHeader(nameHeader).setFooter(nameHeader);
    String dateHeader = sampleResources.message(DATE);
    date.setHeader(dateHeader).setFooter(dateHeader);
    String ownerHeader = sampleResources.message(OWNER);
    owner.setHeader(ownerHeader).setFooter(ownerHeader);
    nameFilter.setPlaceholder(webResources.message(ALL));
    ownerFilter.setPlaceholder(webResources.message(ALL));
  }

  /**
   * Adds listener to be informed when a sample was selected.
   *
   * @param listener
   *          listener
   * @return listener registration
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Registration addSelectedListener(
      ComponentEventListener<SelectedEvent<SelectSampleDialog, Sample>> listener) {
    return addListener((Class) SelectedEvent.class, listener);
  }

  void fireSelectedEvent(Sample sample) {
    fireEvent(new SelectedEvent<>(this, true, sample));
  }
}
