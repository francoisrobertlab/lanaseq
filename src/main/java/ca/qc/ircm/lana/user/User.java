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

package ca.qc.ircm.lana.user;

import static javax.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lana.Data;
import ca.qc.ircm.processing.GeneratePropertyNames;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

/**
 * User.
 */
@Entity
@GeneratePropertyNames
public class User implements Data, Owned, Serializable {
  private static final long serialVersionUID = -3200958473089020837L;
  /**
   * Database identifier.
   */
  @Id
  @Column(unique = true, nullable = false)
  @GeneratedValue(strategy = IDENTITY)
  private Long id;
  /**
   * User's email, also serves for signin.
   */
  @Column(unique = true, nullable = false)
  @Size(max = 255)
  private String email;
  /**
   * User's real name.
   */
  @Column
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
  @Column
  private int signAttempts;
  /**
   * User's last sign attempt.
   */
  @Column
  private Instant lastSignAttempt;
  /**
   * True if user is active.
   * <p>
   * Inactive user cannot access application.
   * </p>
   */
  @Column
  private boolean active;
  /**
   * True if user is a manager of his lab.
   */
  @Column
  private boolean manager;
  /**
   * True if user is an administrator.
   */
  @Column
  private boolean admin;
  /**
   * True if user's password is expired.
   */
  @Column
  private boolean expiredPassword;
  /**
   * User's lab.
   */
  @ManyToOne(optional = false)
  @JoinColumn
  private Laboratory laboratory;
  /**
   * User's prefered locale.
   */
  @Column
  private Locale locale;
  /**
   * Creation date.
   */
  @Column
  private LocalDateTime date;

  public User() {
  }

  public User(Long id) {
    this.id = id;
  }

  public User(Long id, String email) {
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
    if (!(obj instanceof User)) {
      return false;
    }
    User other = (User) obj;
    if (email == null) {
      if (other.email != null) {
        return false;
      }
    } else if (!email.equals(other.email)) {
      return false;
    }
    return true;
  }

  @Override
  public User getOwner() {
    return this;
  }

  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
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

  public String getHashedPassword() {
    return hashedPassword;
  }

  public void setHashedPassword(String hashedPassword) {
    this.hashedPassword = hashedPassword;
  }

  public int getSignAttempts() {
    return signAttempts;
  }

  public void setSignAttempts(int signAttempts) {
    this.signAttempts = signAttempts;
  }

  public Instant getLastSignAttempt() {
    return lastSignAttempt;
  }

  public void setLastSignAttempt(Instant lastSignAttempt) {
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

  public Laboratory getLaboratory() {
    return laboratory;
  }

  public void setLaboratory(Laboratory laboratory) {
    this.laboratory = laboratory;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
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

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }
}
