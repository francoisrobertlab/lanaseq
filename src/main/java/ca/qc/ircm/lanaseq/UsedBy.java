package ca.qc.ircm.lanaseq;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that this element is used a framework and not used directly.
 *
 * <p>This annotation can be used along <code>@SupressWarnings("unused")</code> to specify the
 * framework that is using the element.</p>
 */
@Target({ElementType.TYPE, ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UsedBy {

  /**
   * <a href="https://spring.io">Spring or Spring Boot</a>.
   */
  String SPRING = "Spring";
  /**
   * <a href="https://hibernate.org/orm/">Hibernate ORM</a>.
   */
  String HIBERNATE = "Hibernate";
  /**
   * <a href="https://vaadin.com">Vaadin</a>.
   */
  String VAADIN = "Vaadin";

  /**
   * Returns the framework that is using the element.
   *
   * @return framework that is using the element
   */
  @SuppressWarnings("UnusedReturnValue")
  String value();
}
