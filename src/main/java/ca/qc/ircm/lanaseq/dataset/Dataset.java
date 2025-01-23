package ca.qc.ircm.lanaseq.dataset;

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.FindbugsExplanations.ENTITY_EI_EXPOSE_REP;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static jakarta.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.Data;
import ca.qc.ircm.lanaseq.DataWithFiles;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.text.Strings;
import ca.qc.ircm.lanaseq.user.Owned;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.processing.GeneratePropertyNames;
import edu.umd.cs.findbugs.annotations.Nullable;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Dataset or group of samples.
 */
@Entity
@GeneratePropertyNames
@SuppressFBWarnings(
    value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" },
    justification = ENTITY_EI_EXPOSE_REP)
public class Dataset implements Data, DataWithFiles, Owned, Serializable {
  public static final String NAME_ALREADY_EXISTS = property(DatasetProperties.NAME, ALREADY_EXISTS);
  private static final long serialVersionUID = -8296884268335212959L;
  /**
   * Database identifier.
   */
  @Id
  @Column(unique = true, nullable = false)
  @GeneratedValue(strategy = IDENTITY)
  private long id;
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
  private Set<String> keywords = new HashSet<>();
  /**
   * Other filenames to look for in directories.
   */
  @ElementCollection
  private Set<String> filenames = new HashSet<>();
  /**
   * True if dataset can be edited.
   */
  @Column
  private boolean editable;
  /**
   * Date.
   */
  @Column(name = "experiment_date", nullable = false)
  private LocalDate date = LocalDate.now();
  /**
   * Time when dataset was created.
   */
  @Column(nullable = false)
  private LocalDateTime creationDate = LocalDateTime.now();
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
  private List<Sample> samples = new ArrayList<>();

  public Dataset() {
  }

  public Dataset(long id) {
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
    Consumer<String> appendNotNull = value -> {
      if (value != null) {
        builder.append(value).append("_");
      }
    };
    if (!samples.isEmpty()) {
      Sample first = samples.get(0);
      builder.append(first.getAssay().replaceAll("\\W", "")).append("_");
      if (first.getType() != null) {
        builder.append(first.getType().replaceAll("\\W", "")).append("_");
      }
      appendNotNull.accept(first.getTarget());
      appendNotNull.accept(first.getStrain());
      appendNotNull.accept(first.getStrainDescription());
      appendNotNull.accept(first.getTreatment());
    }
    builder.append(samples.stream().map(Sample::getSampleId).collect(Collectors.joining("-")))
        .append("_");
    if (date != null) {
      builder.append(DateTimeFormatter.BASIC_ISO_DATE.format(date)).append("_");
    }
    if (!builder.isEmpty()) {
      builder.deleteCharAt(builder.length() - 1);
    }
    String name = builder.toString();
    name = Strings.normalize(name);
    name = name.replaceAll("[^\\w-]", "");
    this.name = name;
  }

  @Override
  public long getId() {
    return id;
  }

  public void setId(long id) {
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

  @Nullable
  public String getNote() {
    return note;
  }

  public void setNote(@Nullable String note) {
    this.note = note;
  }

  public Set<String> getFilenames() {
    return filenames;
  }

  public void setFilenames(Set<String> filenames) {
    this.filenames = filenames;
  }
}
