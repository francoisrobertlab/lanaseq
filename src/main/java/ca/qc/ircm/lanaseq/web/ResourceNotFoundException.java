package ca.qc.ircm.lanaseq.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Resource not found exception.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

  /**
   * Creates new ResourceNotFoundException.
   *
   * @param message error message
   */
  public ResourceNotFoundException(String message) {
    super(message);
  }
}
