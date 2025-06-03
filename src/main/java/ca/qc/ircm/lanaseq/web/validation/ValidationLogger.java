package ca.qc.ircm.lanaseq.web.validation;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs validation information.
 */
public class ValidationLogger {

  private static final Logger logger = LoggerFactory.getLogger(ValidationLogger.class);

  /**
   * Logs binder's validation errors.
   *
   * @param status binder's validation status
   */
  public static void logValidation(BinderValidationStatus<?> status) {
    status.getFieldValidationErrors().forEach(error -> {
      Component field = ((Component) error.getField());
      logger.trace("Validation error {} for field {} with value {} in binder {}",
          error.getMessage(), field.getId().orElse(field.getElement().getAttribute("class")),
          error.getField().getValue(), status.getBinder().getBean());
    });
    status.getBeanValidationErrors().forEach(
        error -> logger.trace("Validation error {} in binder {}", error.getErrorMessage(),
            status.getBinder().getBean()));
  }
}
