package ca.qc.ircm.lanaseq.sample;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.Data;
import ca.qc.ircm.lanaseq.dataset.Assay;
import ca.qc.ircm.lanaseq.dataset.DatasetType;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.user.Owned;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.processing.GeneratePropertyNames;
import java.io.Serializable;
import java.time.LocalDateTime;
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
public class Sample implements Data, Owned, Serializable {
  private static final long serialVersionUID = -6336061129214438932L;
  /**
   * Database identifier.
   */
  @Id
  @Column(unique = true, nullable = false)
  @GeneratedValue(strategy = IDENTITY)
  private Long id;
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((sampleId == null) ? 0 : sampleId.hashCode());
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
    Sample other = (Sample) obj;
    if (sampleId == null) {
      if (other.sampleId != null)
        return false;
    } else if (!sampleId.equals(other.sampleId))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Sample [id=" + id + ", sampleId=" + sampleId + "]";
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

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
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

  public Protocol getProtocol() {
    return protocol;
  }

  public void setProtocol(Protocol protocol) {
    this.protocol = protocol;
  }
}
