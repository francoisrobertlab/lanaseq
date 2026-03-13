package ca.qc.ircm.lanaseq.security;

/**
 * User roles.
 */
public interface UserRole {

  String USER = "ROLE_USER";
  String MANAGER = "ROLE_MANAGER";
  String ADMIN = "ROLE_ADMIN";
  String ROLE_PREVIOUS_ADMINISTRATOR = "ROLE_PREVIOUS_ADMINISTRATOR";

  /**
   * Returns all user roles.
   *
   * @return all user roles
   */
  static String[] roles() {
    return new String[]{USER, MANAGER, ADMIN};
  }
}
