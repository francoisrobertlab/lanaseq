package ca.qc.ircm.lanaseq.sample;

import static ca.qc.ircm.lanaseq.FindbugsExplanations.ENTITY_EI_EXPOSE_REP;
import static jakarta.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.Data;
import ca.qc.ircm.processing.GeneratePropertyNames;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

/**
 * Public file for a sample.
 */
@Entity
@GeneratePropertyNames
@SuppressFBWarnings(value = {"EI_EXPOSE_REP",
    "EI_EXPOSE_REP2"}, justification = ENTITY_EI_EXPOSE_REP)
public class SamplePublicFile implements Data, Serializable {

  @Serial
  private static final long serialVersionUID = 2886919827868620963L;
  /**
   * Database identifier.
   */
  @Id
  @Column(unique = true, nullable = false)
  @GeneratedValue(strategy = IDENTITY)
  private long id;
  /**
   * File path that is made public.
   */
  @Column(nullable = false)
  @Size(max = 255)
  private String path;
  /**
   * Sample of the file.
   */
  @ManyToOne(optional = false)
  @JoinColumn
  private Sample sample;
  /**
   * Date when file cease to be public.
   */
  @Column(nullable = false)
  private LocalDate expiryDate = LocalDate.now();

  @Override
  public String toString() {
    return "SamplePublicFile{" + "id=" + id + ", path='" + path + '\'' + ", sample=" + sample
        + ", expiryDate=" + expiryDate + '}';
  }

  @Override
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public Sample getSample() {
    return sample;
  }

  public void setSample(Sample sample) {
    this.sample = sample;
  }

  public LocalDate getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(LocalDate expiryDate) {
    this.expiryDate = expiryDate;
  }
}
