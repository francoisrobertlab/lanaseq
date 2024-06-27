package ca.qc.ircm.lanaseq.test.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * Finds annotations on method or class.
 */
public class AnnotationFinder {
  /**
   * Returns annotation if present on class or any superclass, or null if annotation is not present.
   *
   * @param type
   *          class on which to search for annotation
   * @param annotationClass
   *          annotation to look for
   * @return annotation if present on class or any superclass, or null if annotation is not present
   */
  public static <A extends Annotation> Optional<A> findAnnotation(Class<?> type,
      Class<A> annotationClass) {
    return findAnnotation(type, null, annotationClass);
  }

  /**
   * Returns annotation if present on method, class or superclass, or null if annotation is not
   * present.
   *
   * @param type
   *          class on which to search for annotation
   * @param method
   *          method on which to search for annotation
   * @param annotationClass
   *          annotation to look for
   * @return annotation if present on method, class or superclass, or null if annotation is not
   *         present
   */
  public static <A extends Annotation> Optional<A> findAnnotation(Class<?> type, Method method,
      Class<A> annotationClass) {
    A annotation = method != null ? AnnotationUtils.findAnnotation(method, annotationClass) : null;
    if (annotation == null) {
      annotation = AnnotationUtils.findAnnotation(type, annotationClass);
    }
    return Optional.ofNullable(annotation);
  }
}
