package ca.qc.ircm.lanaseq.sample;

import static javax.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.Data;
import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.user.Owned;
import ca.qc.ircm.lanaseq.user.User;
import ca.qc.ircm.processing.GeneratePropertyNames;
import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
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
   * Name.
   */
  @Column(nullable = false)
  @Size(max = 255)
  private String name;
  /**
   * Replicate number.
   */
  @Column(nullable = false)
  @Size(max = 255)
  private String replicate;
  /**
   * Creation date.
   */
  @Column
  private LocalDateTime date;
  /**
   * Dataset.
   */
  @ManyToOne(optional = false)
  @JoinColumn
  private Dataset dataset;

  public Sample() {
  }

  public Sample(Long id) {
    this.id = id;
  }

  public Sample(String name) {
    this.name = name;
  }

  public Sample(Long id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((dataset == null) ? 0 : dataset.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
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
    if (dataset == null) {
      if (other.dataset != null)
        return false;
    } else if (!dataset.equals(other.dataset))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "Sample [id=" + id + ", name=" + name + "]";
  }

  @Override
  public User getOwner() {
    return dataset.getOwner();
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

  public String getReplicate() {
    return replicate;
  }

  public void setReplicate(String replicate) {
    this.replicate = replicate;
  }

  public Dataset getDataset() {
    return dataset;
  }

  public void setDataset(Dataset dataset) {
    this.dataset = dataset;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public void setDate(LocalDateTime date) {
    this.date = date;
  }
}
