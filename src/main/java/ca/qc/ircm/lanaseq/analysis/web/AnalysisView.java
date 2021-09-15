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

package ca.qc.ircm.lanaseq.analysis.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetGrid;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Analysis view.
 */
@Route(value = AnalysisView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class AnalysisView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {
  public static final String VIEW_NAME = "analysis";
  public static final String ID = styleName(VIEW_NAME, "view");
  public static final String HEADER = "header";
  public static final String DATASETS = "datasets";
  public static final String ANALYZE = "analyze";
  public static final String ROBTOOLS = "robtools";
  public static final String ROBTOOLS_LINK =
      "https://github.com/francoisrobertlab/robtools/tree/master/computecanada";
  public static final String DATASETS_REQUIRED = property(DATASETS, REQUIRED);
  private static final long serialVersionUID = 6718796782451862327L;
  private static final Logger logger = LoggerFactory.getLogger(AnalysisView.class);
  protected H2 header = new H2();
  protected Div error = new Div();
  protected Button analyze = new Button();
  protected Anchor robtools = new Anchor();
  @Autowired
  protected DatasetGrid datasets;
  @Autowired
  protected AnalysisDialog dialog;
  @Autowired
  private transient AnalysisViewPresenter presenter;

  public AnalysisView() {
  }

  AnalysisView(AnalysisViewPresenter presenter, DatasetGrid datasets, AnalysisDialog dialog) {
    this.presenter = presenter;
    this.datasets = datasets;
    this.dialog = dialog;
  }

  @PostConstruct
  void init() {
    logger.debug("analysis view");
    setId(ID);
    setHeightFull();
    add(header, datasets, error, analyze, robtools);
    expand(datasets);
    header.setId(HEADER);
    datasets.setId(DATASETS);
    datasets.addItemDoubleClickListener(e -> presenter.analyze(e.getItem()));
    datasets.setSelectionMode(Grid.SelectionMode.MULTI);
    error.setId(ERROR_TEXT);
    error.addClassName(ERROR_TEXT);
    analyze.setId(ANALYZE);
    analyze.addClickListener(e -> presenter.analyze(datasets.getSelectedItems()));
    robtools.setId(ROBTOOLS);
    robtools.setHref(ROBTOOLS_LINK);
    robtools.setTarget("_blank");
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    AppResources resources = new AppResources(AnalysisView.class, getLocale());
    header.setText(resources.message(HEADER));
    analyze.setText(resources.message(ANALYZE));
    robtools.setText(resources.message(ROBTOOLS));
    presenter.localChange(getLocale());
  }

  @Override
  public String getPageTitle() {
    AppResources resources = new AppResources(AnalysisView.class, getLocale());
    AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }
}
