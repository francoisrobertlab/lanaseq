package ca.qc.ircm.lanaseq.user.web;

import org.springframework.lang.Nullable;

/**
 * Stores password and a confirmation that should match the password.
 */
public class Passwords {
  private String password;
  private String confirmPassword;

  @Nullable
  public String getPassword() {
    return password;
  }

  public void setPassword(@Nullable String password) {
    this.password = password;
  }

  @Nullable
  public String getConfirmPassword() {
    return confirmPassword;
  }

  public void setConfirmPassword(@Nullable String confirmPassword) {
    this.confirmPassword = confirmPassword;
  }
}