package ca.qc.ircm.lanaseq.sample;

import static ca.qc.ircm.lanaseq.Constants.ALREADY_EXISTS;
import static ca.qc.ircm.lanaseq.FindbugsExplanations.ENTITY_EI_EXPOSE_REP;
import static ca.qc.ircm.lanaseq.text.Strings.property;
import static jakarta.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.DataWithFiles;
import ca.qc.ircm.lanaseq.dataset.DatasetProperties;
import ca.qc.ircm.lanaseq.protocol.Protocol;
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
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

/**
 * Sample.
 */
@Entity
@GeneratePropertyNames
@SuppressFBWarnings(
    value = { "EI_EXPOSE_REP", "EI_EXPOSE_REP2" },
    justification = ENTITY_EI_EXPOSE_REP)
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
  @Size(max = 255)
  private String assay;
  /**
   * Type.
   */
  @Column
  @Size(max = 255)
  private String type;
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
   * Tags.
   */
  @ElementCollection
  private Set<String> tags;
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
    builder.append(assay != null ? assay.replaceAll("[^\\w]", "") + "_" : "");
    builder.append(type != null ? type.replaceAll("[^\\w]", "") + "_" : "");
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

  public String getAssay() {
    return assay;
  }

  public void setAssay(String assay) {
    this.assay = assay;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
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

  public Set<String> getTags() {
    return tags;
  }

  public void setTags(Set<String> tags) {
    this.tags = tags;
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
