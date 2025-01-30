package ca.qc.ircm.lanaseq.user.web;

import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.lanaseq.user.UserFilter;
import com.vaadin.flow.function.SerializablePredicate;
import java.io.Serial;

/**
 * Serializable {@link UserFilter}.
 */
public class WebUserFilter extends UserFilter implements SerializablePredicate<User> {

  @Serial
  private static final long serialVersionUID = -2766717945938428268L;
}
