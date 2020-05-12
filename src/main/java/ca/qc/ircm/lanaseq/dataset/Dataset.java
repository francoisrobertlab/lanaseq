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

package ca.qc.ircm.lanaseq.dataset;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.Data;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.text.Strings;
import ca.qc.ircm.lanaseq.user.Owned;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.processing.GeneratePropertyNames;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.validation.constraints.Size;

/**
 * Dataset or group of samples.
 */
@Entity
@GeneratePropertyNames
public class Dataset implements Data, Owned, HasFiles, Serializable {
  private static final long serialVersionUID = -8296884268335212959L;
  /**
   * Database identifier.
   */
  @Id
  @Column(unique = true, nullable = false)
  @GeneratedValue(strategy = IDENTITY)
  private Long id;
  /**
   * Project.
   */
  @Column
  @Size(max = 255)
  private String project;
  /**
   * Assay.
   */
  @Column(nullable = false)
  @Enumerated(STRING)
  private Assay assay;
  /**
   * Type.
   */
  @Column
  @Enumerated(STRING)
  private DatasetType type;
  /**
   * Target.
   */
  @Column
  @Size(max = 255)
  private String target;
  /**
   * Strain.
   */
  @Column(nullable = false)
  @Size(max = 255)
  private String strain;
  /**
   * Strain description.
   */
  @Column
  @Size(max = 255)
  private String strainDescription;
  /**
   * Treatment.
   */
  @Column
  @Size(max = 255)
  private String treatment;
  /**
   * Creation date.
   */
  @Column
  private LocalDateTime date;
  /**
   * Protocol.
   */
  @ManyToOne(optional = false)
  @JoinColumn
  private Protocol protocol;
  /**
   * Owner.
   */
  @ManyToOne(optional = false)
  @JoinColumn
  private User owner;
  /**
   * Samples that are part of this submission.
   */
  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderColumn
  private List<Sample> samples;

  public Dataset() {
  }

  public Dataset(Long id) {
    this.id = id;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((assay == null) ? 0 : assay.hashCode());
    result = prime * result + ((date == null) ? 0 : date.hashCode());
    result = prime * result + ((project == null) ? 0 : project.hashCode());
    result = prime * result + ((strain == null) ? 0 : strain.hashCode());
    result = prime * result + ((strainDescription == null) ? 0 : strainDescription.hashCode());
    result = prime * result + ((target == null) ? 0 : target.hashCode());
    result = prime * result + ((treatment == null) ? 0 : treatment.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Dataset other = (Dataset) obj;
    if (assay != other.assay)
      return false;
    if (date == null) {
      if (other.date != null)
        return false;
    } else if (!date.equals(other.date))
      return false;
    if (project == null) {
      if (other.project != null)
        return false;
    } else if (!project.equals(other.project))
      return false;
    if (strain == null) {
      if (other.strain != null)
        return false;
    } else if (!strain.equals(other.strain))
      return false;
    if (strainDescription == null) {
      if (other.strainDescription != null)
        return false;
    } else if (!strainDescription.equals(other.strainDescription))
      return false;
    if (target == null) {
      if (other.target != null)
        return false;
    } else if (!target.equals(other.target))
      return false;
    if (treatment == null) {
      if (other.treatment != null)
        return false;
    } else if (!treatment.equals(other.treatment))
      return false;
    if (type != other.type)
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Dataset [id=" + id + ", getFilename()=" + getFilename() + "]";
  }

  @Override
  public String getFilename() {
    StringBuilder builder = new StringBuilder();
    builder.append(assay != null ? "_" + assay.getLabel(Locale.ENGLISH) : "");
    builder.append(type != null ? "_" + type.getLabel(Locale.ENGLISH) : "");
    builder.append(target != null ? "_" + target : "");
    builder.append(strain != null ? "_" + strain : "");
    builder.append(strainDescription != null ? "_" + strainDescription : "");
    builder.append(treatment != null ? "_" + treatment : "");
    DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
    builder.append(date != null ? "_" + formatter.format(date) : "");
    if (builder.length() > 0) {
      builder.deleteCharAt(0);
    }
    String filename = builder.toString();
    filename = Strings.normalize(filename);
    filename = filename.replaceAll("[^\\w-]", "");
    return filename;
  }

  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  @Override
  public User getOwner() {
    return owner;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }

  public Protocol getProtocol() {
    return protocol;
  }

  public void setProtocol(Protocol protocol) {
    this.protocol = protocol;
  }

  public Assay getAssay() {
    return assay;
  }

  public void setAssay(Assay assay) {
    this.assay = assay;
  }

  public DatasetType getType() {
    return type;
  }

  public void setType(DatasetType type) {
    this.type = type;
  }

  public String getTarget() {
    return target;
  }

  public void setTarget(String target) {
    this.target = target;
  }

  public String getStrain() {
    return strain;
  }

  public void setStrain(String strain) {
    this.strain = strain;
  }

  public String getStrainDescription() {
    return strainDescription;
  }

  public void setStrainDescription(String strainDescription) {
    this.strainDescription = strainDescription;
  }

  public String getTreatment() {
    return treatment;
  }

  public void setTreatment(String treatment) {
    this.treatment = treatment;
  }

  public List<Sample> getSamples() {
    return samples;
  }

  public void setSamples(List<Sample> samples) {
    this.samples = samples;
  }
}
