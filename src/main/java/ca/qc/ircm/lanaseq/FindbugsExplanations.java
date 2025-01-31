package ca.qc.ircm.lanaseq;

/**
 * Explanations for ignoring findbugs warnings.
 */
public class FindbugsExplanations {

  public static final String ENTITY_EI_EXPOSE_REP =
      "Entities should expose internal representation like objects and lists to allow modification";
  public static final String SPRING_BOOT_EI_EXPOSE_REP =
      "Expose internal representation for objects created by Spring Boot is acceptable";
}
