/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
