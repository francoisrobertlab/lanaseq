package ca.qc.ircm.lanaseq.web.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinServlet;

/**
 * Create URL for component.
 */
public interface UrlComponent {

  default String getUrl(Class<? extends Component> view) {
    return RouteConfiguration.forSessionScope().getUrl(view);
  }

  default <T, C extends Component & HasUrlParameter<T>> String getUrl(Class<? extends C> view,
      T parameter) {
    return RouteConfiguration.forSessionScope().getUrl(view, parameter);
  }

  default String getUrlWithContextPath(Class<? extends Component> view) {
    String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
    return contextPath + "/" + RouteConfiguration.forSessionScope().getUrl(view);
  }

  default <T, C extends Component & HasUrlParameter<T>> String getUrlWithContextPath(
      Class<? extends C> view, T parameter) {
    String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
    return contextPath + "/" + RouteConfiguration.forSessionScope().getUrl(view, parameter);
  }

  default String prependContextPath(String url) {
    String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
    return contextPath + "/" + url;
  }
}
