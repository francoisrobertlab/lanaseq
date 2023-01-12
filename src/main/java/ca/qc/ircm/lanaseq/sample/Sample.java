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

package ca.qc.ircm.lanaseq.sample;

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.DataWithFiles;
import ca.qc.ircm.lanaseq.dataset.DatasetProperties;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.text.Strings;
import ca.qc.ircm.lanaseq.user.Owned;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.processing.GeneratePropertyNames;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

/**
 * Sample.
 */
@Entity
@GeneratePropertyNames
public class Sample implements DataWithFiles, Owned, Serializable {
  public static final String NAME_ALREADY_EXISTS = property(DatasetProperties.NAME, ALREADY_EXISTS);
  private static final long serialVersionUID = -6336061129214438932L;
  /**
   * Database identifier.
   */
  @Id
  @Column(unique = true, nullable = false)
  @GeneratedValue(strategy = IDENTITY)
  private Long id;
  /**
   * Name that is used for files associated with sample.
   */
  @Column(nullable = false)
  @Size(max = 255)
  private String name;
  /**
   * Sample id as defined by user.
   */
  @Column(nullable = false)
  @Size(max = 255)
  private String sampleId;
  /**
   * Replicate number.
   */
  @Column(nullable = false)
  @Size(max = 255)
  private String replicate;
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
  private SampleType type;
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
   * True if sample can be edited.
   */
  @Column
  private boolean editable;
  /**
   * Date.
   */
  @Column(name = "experiment_date", nullable = false)
  private LocalDate date;
  /**
   * Time when sample was created.
   */
  @Column(nullable = false)
  private LocalDateTime creationDate;
  /**
   * Notes.
   */
  @Column
  private String note;
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

  public Sample() {
  }

  public Sample(Long id) {
    this.id = id;
  }

  public Sample(String sampleId) {
    this.sampleId = sampleId;
  }

  public Sample(Long id, String sampleId) {
    this.id = id;
    this.sampleId = sampleId;
  }

  @Override
  public String toString() {
    return "Sample [id=" + id + ", name=" + name + ", sampleId=" + sampleId + "]";
  }

  /**
   * Update sample's name based on it's properties.
   */
  public void generateName() {
    StringBuilder builder = new StringBuilder();
    builder.append(sampleId != null ? sampleId + "_" : "");
    builder
        .append(assay != null ? assay.getLabel(Locale.ENGLISH).replaceAll("[^\\w]", "") + "_" : "");
    builder.append(type != null ? type.getLabel(Locale.ENGLISH) + "_" : "");
    builder.append(target != null ? target + "_" : "");
    builder.append(strain != null ? strain + "_" : "");
    builder.append(strainDescription != null ? strainDescription + "_" : "");
    builder.append(treatment != null ? treatment + "_" : "");
    builder.append(replicate != null ? replicate + "_" : "");
    if (date != null) {
      builder.append(DateTimeFormatter.BASIC_ISO_DATE.format(date) + "_");
    }
    if (builder.length() > 0) {
      builder.deleteCharAt(builder.length() - 1);
    }
    String name = builder.toString();
    name = Strings.normalize(name);
    name = name.replaceAll("[^\\w-]", "");
    this.name = name;
  }

  @Override
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getSampleId() {
    return sampleId;
  }

  public void setSampleId(String sampleId) {
    this.sampleId = sampleId;
  }

  public String getReplicate() {
    return replicate;
  }

  public void setReplicate(String replicate) {
    this.replicate = replicate;
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }

  @Override
  public User getOwner() {
    return owner;
  }

  public void setOwner(User owner) {
    this.owner = owner;
  }

  public Assay getAssay() {
    return assay;
  }

  public void setAssay(Assay assay) {
    this.assay = assay;
  }

  public SampleType getType() {
    return type;
  }

  public void setType(SampleType type) {
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

  public Protocol getProtocol() {
    return protocol;
  }

  public void setProtocol(Protocol protocol) {
    this.protocol = protocol;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isEditable() {
    return editable;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
