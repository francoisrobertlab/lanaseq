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

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.ERROR_TEXT;
import static ca.qc.ircm.lanaseq.Constants.REQUIRED;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.property;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import ca.qc.ircm.lanaseq.web.component.NotificationComponent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Datasets view.
 */
@Route(value = DatasetsView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class DatasetsView extends VerticalLayout
    implements LocaleChangeObserver, HasDynamicTitle, NotificationComponent {
  public static final String VIEW_NAME = "datasets";
  public static final String ID = "datasets-view";
  public static final String HEADER = "header";
  public static final String DATASETS = "datasets";
  public static final String MERGE = "merge";
  public static final String FILES = "files";
  public static final String ANALYZE = "analyze";
  public static final String MERGE_ERROR = property(MERGE, "error");
  public static final String DATASETS_REQUIRED = property(DATASETS, REQUIRED);
  public static final String DATASETS_MORE_THAN_ONE = property(DATASETS, "moreThanOne");
  public static final String MERGED = "merged";
  private static final long serialVersionUID = 2568742367790329628L;
  private static final Logger logger = LoggerFactory.getLogger(DatasetsView.class);
  protected H2 header = new H2();
  protected Div error = new Div();
  protected Button merge = new Button();
  protected Button files = new Button();
  protected Button analyze = new Button();
  @Autowired
  protected DatasetGrid datasets;
  @Autowired
  protected ObjectFactory<DatasetDialog> dialogFactory;
  @Autowired
  protected ObjectFactory<DatasetFilesDialog> filesDialogFactory;
  @Autowired
  protected ObjectFactory<DatasetsAnalysisDialog> analysisDialogFactory;
  @Autowired
  private transient DatasetsViewPresenter presenter;

  public DatasetsView() {
  }

  protected DatasetsView(DatasetsViewPresenter presenter, DatasetGrid datasets,
      ObjectFactory<DatasetDialog> dialogFactory,
      ObjectFactory<DatasetFilesDialog> filesDialogFactory,
      ObjectFactory<DatasetsAnalysisDialog> analysisDialogFactory) {
    this.presenter = presenter;
    this.datasets = datasets;
    this.dialogFactory = dialogFactory;
    this.filesDialogFactory = filesDialogFactory;
    this.analysisDialogFactory = analysisDialogFactory;
  }

  @PostConstruct
  void init() {
    logger.debug("datasets view");
    setId(ID);
    setHeightFull();
    add(header, datasets, error, new HorizontalLayout(merge, files, analyze));
    expand(datasets);
    header.setId(HEADER);
    datasets.setSelectionMode(SelectionMode.MULTI);
    datasets.addItemClickListener(e -> {
      if (e.isCtrlKey() || e.isMetaKey()) {
        presenter.viewFiles(e.getItem());
      }
    });
    datasets.addEditListener(e -> presenter.view(e.getItem()));
    datasets.addItemDoubleClickListener(e -> presenter.view(e.getItem()));
    error.setId(ERROR_TEXT);
    error.addClassName(ERROR_TEXT);
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

  private Protocol protocol(Dataset dataset) {
    return dataset.getSamples() != null
        ? dataset.getSamples().stream().findFirst().map(s -> s.getProtocol()).orElse(new Protocol())
        : new Protocol();
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    final AppResources resources = new AppResources(DatasetsView.class, getLocale());
    header.setText(resources.message(HEADER));
    merge.setText(resources.message(MERGE));
    files.setText(resources.message(FILES));
    analyze.setText(resources.message(ANALYZE));
    presenter.localeChange(getLocale());
  }

  @Override
  public String getPageTitle() {
    AppResources resources = new AppResources(DatasetsView.class, getLocale());
    AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }
}
