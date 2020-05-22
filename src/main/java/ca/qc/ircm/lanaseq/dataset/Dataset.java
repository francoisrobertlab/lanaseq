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

import static javax.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.Data;
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
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
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
public class Dataset implements Data, Owned, Serializable {
  private static final long serialVersionUID = -8296884268335212959L;
  /**
   * Database identifier.
   */
  @Id
  @Column(unique = true, nullable = false)
  @GeneratedValue(strategy = IDENTITY)
  private Long id;
  /**
   * Name that is used for files associated with dataset.
   */
  @Column
  @Size(max = 255)
  private String name;
  /**
   * Tags.
   */
  @ElementCollection
  private Set<String> tags;
  /**
   * Creation date.
   */
  @Column
  private LocalDateTime date;
  /**
   * Owner.
   */
  @ManyToOne(optional = false)
  @JoinColumn
  private User owner;
  /**
   * Samples that are part of this submission.
   */
  @OneToMany
  @OrderColumn
  private List<Sample> samples;

  public Dataset() {
  }

  public Dataset(Long id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return "Dataset [id=" + id + ", name=" + name + "]";
  }

  public void generateName() {
    StringBuilder builder = new StringBuilder();
    Sample first =
        samples != null ? samples.stream().findFirst().orElse(new Sample()) : new Sample();
    builder.append(first.getAssay() != null ? first.getAssay().getLabel(Locale.ENGLISH) + "_" : "");
    builder.append(first.getType() != null ? first.getType().getLabel(Locale.ENGLISH) + "_" : "");
    builder.append(first.getTarget() != null ? first.getTarget() + "_" : "");
    builder.append(first.getStrain() != null ? first.getStrain() + "_" : "");
    builder.append(first.getStrainDescription() != null ? first.getStrainDescription() + "_" : "");
    builder.append(first.getTreatment() != null ? first.getTreatment() + "_" : "");
    if (samples != null) {
      StringBuilder samplesBuilder = new StringBuilder();
      for (Sample sample : samples) {
        samplesBuilder.append(sample.getSampleId() != null ? sample.getSampleId() + "-" : "");
      }
      if (samplesBuilder.length() > 0) {
        samplesBuilder.setCharAt(samplesBuilder.length() - 1, '_');
      }
      builder.append(samplesBuilder);
    }
    DateTimeFormatter formatter = DateTimeFormatter.BASIC_ISO_DATE;
    builder.append(date != null ? formatter.format(date) + "_" : "");
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

  @Override
  public User getOwner() {
    return owner;
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

  public List<Sample> getSamples() {
    return samples;
  }

  public void setSamples(List<Sample> samples) {
    this.samples = samples;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<String> getTags() {
    return tags;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags;
  }
}
