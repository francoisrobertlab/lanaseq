package ca.qc.ircm.lanaseq.user;

import static jakarta.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.Data;
import ca.qc.ircm.processing.GeneratePropertyNames;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import org.springframework.lang.Nullable;

/**
 * User.
 */
@Entity
@Table(name = User.TABLE_NAME)
@GeneratePropertyNames
public class User implements Data, Owned, Serializable {

  public static final String TABLE_NAME = "users";
  public static final long ROBOT_ID = 1;
  @Serial
  private static final long serialVersionUID = -3200958473089020837L;
  /**
   * Database identifier.
   */
  @Id
  @Column(unique = true, nullable = false)
  @GeneratedValue(strategy = IDENTITY)
  private long id;
  /**
   * User's email, also serves for signin.
   */
  @Column(unique = true, nullable = false)
  @Size(max = 255)
  private String email;
  /**
   * User's real name.
   */
  @Column(nullable = false)
  @Size(max = 255)
  private String name;
  /**
   * User's hashed password.
   */
  @Column
  @Size(max = 255)
  private String hashedPassword;
  /**
   * User's number of sign attempts since last success.
   */
  @Column(nullable = false)
  private int signAttempts;
  /**
   * User's last sign attempt.
   */
  @Column
  private LocalDateTime lastSignAttempt;
  /**
   * True if user is active.
   * <p>
   * Inactive user cannot access application.
   * </p>
   */
  @Column(nullable = false)
  private boolean active;
  /**
   * True if user is a manager of his lab.
   */
  @Column(nullable = false)
  private boolean manager;
  /**
   * True if user is an administrator.
   */
  @Column(nullable = false)
  private boolean admin;
  /**
   * True if user's password is expired.
   */
  @Column(nullable = false)
  private boolean expiredPassword;
  /**
   * User's prefered locale.
   */
  @Column
  private Locale locale;
  /**
   * Creation date.
   */
  @Column(nullable = false)
  private LocalDateTime creationDate = LocalDateTime.now();

  public User() {
  }

  public User(long id) {
    this.id = id;
  }

  public User(String email) {
    this.email = email;
  }

  public User(long id, String email) {
    this.id = id;
    this.email = email;
  }

  @Override
  public String toString() {
    return "User [id=" + id + ", email=" + email + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((email == null) ? 0 : email.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof User other)) {
      return false;
    }
    return Objects.equals(email, other.email);
  }

  @Override
  public User getOwner() {
    return this;
  }

  @Override
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @Nullable
  public String getHashedPassword() {
    return hashedPassword;
  }

  public void setHashedPassword(@Nullable String hashedPassword) {
    this.hashedPassword = hashedPassword;
  }

  public int getSignAttempts() {
    return signAttempts;
  }

  public void setSignAttempts(int signAttempts) {
    this.signAttempts = signAttempts;
  }

  @Nullable
  public LocalDateTime getLastSignAttempt() {
    return lastSignAttempt;
  }

  public void setLastSignAttempt(@Nullable LocalDateTime lastSignAttempt) {
    this.lastSignAttempt = lastSignAttempt;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public boolean isManager() {
    return manager;
  }

  public void setManager(boolean manager) {
    this.manager = manager;
  }

  @Nullable
  public Locale getLocale() {
    return locale;
  }

  public void setLocale(@Nullable Locale locale) {
    this.locale = locale;
  }

  public boolean isExpiredPassword() {
    return expiredPassword;
  }

  public void setExpiredPassword(boolean expiredPassword) {
    this.expiredPassword = expiredPassword;
  }

  public boolean isAdmin() {
    return admin;
  }

  public void setAdmin(boolean admin) {
    this.admin = admin;
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }
}
