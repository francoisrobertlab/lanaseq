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

package ca.qc.ircm.lanaseq.user;

import static javax.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.Data;
import ca.qc.ircm.processing.GeneratePropertyNames;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.Size;

/**
 * Laboratory.
 */
@Entity
@GeneratePropertyNames
public class Laboratory implements Data, Serializable {
  private static final long serialVersionUID = -1289327816693718726L;
  /**
   * Database identifier.
   */
  @Id
  @Column(unique = true, nullable = false)
  @GeneratedValue(strategy = IDENTITY)
  private Long id;
  @Column(nullable = false)
  @Size(max = 255)
  private String name;
  /**
   * Creation date.
   */
  @Column
  private LocalDateTime date;

  public Laboratory() {
  }

  public Laboratory(Long id) {
    this.id = id;
  }

  public Laboratory(String name) {
    this.name = name;
  }

  public Laboratory(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public String toString() {
    return "Laboratory [id=" + id + ", name=" + name + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((date == null) ? 0 : date.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    if (!(obj instanceof Laboratory)) {
      return false;
    }
    Laboratory other = (Laboratory) obj;
    if (date == null) {
      if (other.date != null) {
        return false;
      }
    } else if (!date.equals(other.date)) {
      return false;
    }
    if (name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!name.equals(other.name)) {
      return false;
    }
    return true;
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

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }
}
