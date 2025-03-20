package ca.qc.ircm.lanaseq.dataset;

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
 * Public file for a dataset.
 */
@Entity
@GeneratePropertyNames
@SuppressFBWarnings(value = {"EI_EXPOSE_REP",
    "EI_EXPOSE_REP2"}, justification = ENTITY_EI_EXPOSE_REP)
public class DatasetPublicFile implements Data, Serializable {

  @Serial
  private static final long serialVersionUID = -256595354568842142L;
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
   * Dataset of the file.
   */
  @ManyToOne(optional = false)
  @JoinColumn
  private Dataset dataset;
  /**
   * Date when file cease to be public.
   */
  @Column(nullable = false)
  private LocalDate expiryDate = LocalDate.now();

  @Override
  public String toString() {
    return "DatasetPublicFile{" + "id=" + id + ", path='" + path + '\'' + ", dataset=" + dataset
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

  public Dataset getDataset() {
    return dataset;
  }

  public void setDataset(Dataset dataset) {
    this.dataset = dataset;
  }

  public LocalDate getExpiryDate() {
    return expiryDate;
  }

  public void setExpiryDate(LocalDate expiryDate) {
    this.expiryDate = expiryDate;
  }
}
