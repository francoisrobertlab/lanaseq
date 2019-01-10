package ca.qc.ircm.lana.user.web;

import ca.qc.ircm.lana.user.User;
import ca.qc.ircm.lana.user.UserFilter;
import com.vaadin.flow.function.SerializablePredicate;

/**
 * Serializable {@link UserFilter}.
 */
public class WebUserFilter extends UserFilter implements SerializablePredicate<User> {
  private static final long serialVersionUID = -2766717945938428268L;
}
