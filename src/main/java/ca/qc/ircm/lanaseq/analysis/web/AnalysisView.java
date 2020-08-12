package ca.qc.ircm.lanaseq.analysis.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.dataset.web.DatasetGrid;
import ca.qc.ircm.lanaseq.web.ViewLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;
import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Analysis view.
 */
@Route(value = AnalysisView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class AnalysisView extends VerticalLayout implements LocaleChangeObserver, HasDynamicTitle {
  private static final long serialVersionUID = 6718796782451862327L;
  public static final String VIEW_NAME = "analysis";
  public static final String ID = styleName(VIEW_NAME, "view");
  public static final String HEADER = "header";
  public static final String DATASETS = "datasets";
  protected H2 header = new H2();
  @Autowired
  protected DatasetGrid datasets;
  @Autowired
  protected AnalysisDialog dialog;
  @Autowired
  private AnalysisViewPresenter presenter;

  public AnalysisView() {
  }

  AnalysisView(AnalysisViewPresenter presenter, DatasetGrid datasets, AnalysisDialog dialog) {
    this.presenter = presenter;
    this.datasets = datasets;
    this.dialog = dialog;
  }

  @PostConstruct
  void init() {
    setId(ID);
    add(header, datasets, dialog);
    header.setId(HEADER);
    datasets.setId(DATASETS);
    datasets.addItemDoubleClickListener(e -> presenter.view(e.getItem()));
    presenter.init(this);
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    AppResources resources = new AppResources(AnalysisView.class, getLocale());
    header.setText(resources.message(HEADER));
    presenter.localChange(getLocale());
  }

  @Override
  public String getPageTitle() {
    AppResources resources = new AppResources(AnalysisView.class, getLocale());
    AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }
}
