package ca.qc.ircm.lanaseq.analysis.web;

import static ca.qc.ircm.lanaseq.Constants.APPLICATION_NAME;
import static ca.qc.ircm.lanaseq.Constants.TITLE;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;
import static ca.qc.ircm.lanaseq.text.Strings.styleName;

import ca.qc.ircm.lanaseq.AppResources;
import ca.qc.ircm.lanaseq.Constants;
import ca.qc.ircm.lanaseq.web.ViewLayout;
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

/**
 * Configure analysis platform.
 */
@Route(value = ConfigureAnalysisView.VIEW_NAME, layout = ViewLayout.class)
@RolesAllowed({ USER })
public class ConfigureAnalysisView extends VerticalLayout
    implements LocaleChangeObserver, HasDynamicTitle {
  private static final long serialVersionUID = -5677475354645779031L;
  public static final String VIEW_NAME = "configure-analysis";
  public static final String ID = styleName(VIEW_NAME, "view");
  public static final String HEADER = "header";
  public static final String MESSAGE = "message";
  public static final String SEQTOOLS = "seqtools";
  public static final String SEQTOOLS_LINK =
      "https://github.com/francoisrobertlab/seqtools/blob/master/ComputeCanada.md";
  protected H2 header = new H2();
  protected Div message = new Div();
  protected Anchor seqtools = new Anchor();

  @PostConstruct
  void init() {
    setId(ID);
    add(header, message, seqtools);
    header.setId(HEADER);
    message.setId(MESSAGE);
    seqtools.setId(SEQTOOLS);
    seqtools.setHref(SEQTOOLS_LINK);
    seqtools.setTarget("_blank");
  }

  @Override
  public void localeChange(LocaleChangeEvent event) {
    AppResources resources = new AppResources(ConfigureAnalysisView.class, getLocale());
    header.setText(resources.message(HEADER));
    message.setText(resources.message(MESSAGE));
    seqtools.setText(resources.message(SEQTOOLS));
  }

  @Override
  public String getPageTitle() {
    AppResources resources = new AppResources(ConfigureAnalysisView.class, getLocale());
    AppResources generalResources = new AppResources(Constants.class, getLocale());
    return resources.message(TITLE, generalResources.message(APPLICATION_NAME));
  }
}
