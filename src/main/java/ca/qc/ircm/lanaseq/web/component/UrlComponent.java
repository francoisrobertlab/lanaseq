package ca.qc.ircm.lanaseq.web.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.VaadinServlet;

/**
 * Create URL for component.
 */
public interface UrlComponent {

  default String getUrl(Class<? extends Component> view) {
    return getUrl(RouteConfiguration.forSessionScope().getUrl(view));
  }

  default String getUrl(String view) {
    String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();
    return contextPath + "/" + view;
  }
}
