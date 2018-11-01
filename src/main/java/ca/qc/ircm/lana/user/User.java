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

import ca.qc.ircm.lana.Data;
import ca.qc.ircm.processing.GeneratePropertyNames;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * User.
 */
@Document(collection = "user")
@GeneratePropertyNames
public class User implements Data, Serializable {
  /**
   * Locale preference.
   */
  public static final String LOCALE = "locale";
  private static final long serialVersionUID = -3200958473089020837L;
  /**
   * Database identifier.
   */
  @Id
  private String id;
  private String name;
  private String email;
  private UserRole role;
  private String hashedPassword;
  private int signAttempts;
  private Instant lastSignAttempt;
  private boolean active;
  private boolean manager;
  private Laboratory laboratory;
  private Map<String, String> preferences;

  @Override
  public String toString() {
    return "User [id=" + id + ", email=" + email + "]";
  }

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
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

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
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

  public Map<String, String> getPreferences() {
    return preferences;
  }

  public void setPreferences(Map<String, String> preferences) {
    this.preferences = preferences;
  }
}
