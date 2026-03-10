package ca.qc.ircm.lanaseq.security.web;

import com.vaadin.flow.server.auth.AccessCheckResult;
import com.vaadin.flow.server.auth.AnnotatedViewAccessChecker;
import com.vaadin.flow.server.auth.NavigationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Patches {@link AnnotatedViewAccessChecker} showing denied access for
 * {@link RememberMeAuthenticationToken}.
 */
public class RememberMeFixAnnotatedViewAccessChecker extends AnnotatedViewAccessChecker {

  private static final Logger logger = LoggerFactory.getLogger(
      RememberMeFixAnnotatedViewAccessChecker.class);

  @Override
  public AccessCheckResult check(NavigationContext context) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof RememberMeAuthenticationToken && context.getPrincipal() == null) {
      // Skip test, since principal in NavigationContext is invalid.
      logger.debug(
          "Skip view access check because principal in NavigationContext is invalid. Authentication is {}",
          authentication);
      return AccessCheckResult.neutral();
    }
    return super.check(context);
  }
}
