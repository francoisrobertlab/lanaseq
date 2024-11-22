package ca.qc.ircm.lanaseq.dataset;

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.FindbugsExplanations.ENTITY_EI_EXPOSE_REP;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static jakarta.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.DataNullableId;
import ca.qc.ircm.lanaseq.DataWithFiles;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.text.Strings;
import ca.qc.ircm.lanaseq.user.Owned;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.processing.GeneratePropertyNames;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * Dataset or group of samples.
 */
@Entity
@GeneratePropertyNames
@SuppressFBWarnings(
    value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" },
    justification = ENTITY_EI_EXPOSE_REP)
public class Dataset implements DataNullableId, DataWithFiles, Owned, Serializable {
  public static final String NAME_ALREADY_EXISTS = property(DatasetProperties.NAME, ALREADY_EXISTS);
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
   * Keywords.
   */
  @ElementCollection
  private Set<String> keywords;
  /**
   * Other filenames to look for in directories.
   */
  @ElementCollection
  private Set<String> filenames;
  /**
   * True if dataset can be edited.
   */
  @Column
  private boolean editable;
  /**
   * Date.
   */
  @Column(name = "experiment_date", nullable = false)
  private LocalDate date;
  /**
   * Time when dataset was created.
   */
  @Column(nullable = false)
  private LocalDateTime creationDate;
  /**
   * Notes.
   */
  @Column
  private String note;
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

  /**
   * Updates dataset's name with a new generated name.
   */
  public void generateName() {
    StringBuilder builder = new StringBuilder();
    Sample first =
        samples != null ? samples.stream().findFirst().orElse(new Sample()) : new Sample();
    builder.append(first.getAssay() != null ? first.getAssay().replaceAll("[^\\w]", "") + "_" : "");
    builder.append(first.getType() != null ? first.getType().replaceAll("[^\\w]", "") + "_" : "");
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

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDateTime creationDate) {
    this.creationDate = creationDate;
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

  public Set<String> getKeywords() {
    return keywords;
  }

  public void setKeywords(Set<String> keywords) {
    this.keywords = keywords;
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

  public Set<String> getFilenames() {
    return filenames;
  }

  public void setFilenames(Set<String> filenames) {
    this.filenames = filenames;
  }
}
