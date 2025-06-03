package ca.qc.ircm.lanaseq.protocol;

import static ca.qc.ircm.lanaseq.FindbugsExplanations.ENTITY_EI_EXPOSE_REP;
import static ca.qc.ircm.lanaseq.UsedBy.HIBERNATE;
import static jakarta.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.Data;
import ca.qc.ircm.lanaseq.UsedBy;
import ca.qc.ircm.processing.GeneratePropertyNames;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Size;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Protocol file.
 */
@Entity
@GeneratePropertyNames
@SuppressFBWarnings(value = {"EI_EXPOSE_REP",
    "EI_EXPOSE_REP2"}, justification = ENTITY_EI_EXPOSE_REP)
public class ProtocolFile implements Data, Serializable {

  @Serial
  private static final long serialVersionUID = 4522245557420544824L;
  /**
   * Protocol file database identifier.
   */
  @Id
  @Column(unique = true, nullable = false)
  @GeneratedValue(strategy = IDENTITY)
  private long id;
  /**
   * Filename as entered by user.
   */
  @Column(nullable = false)
  @Size(max = 255)
  private String filename;
  /**
   * Binary content of file.
   */
  @Column(nullable = false)
  private byte[] content;
  /**
   * True if file was deleted.
   */
  @Column(nullable = false)
  private boolean deleted;
  /**
   * Creation date.
   */
  @Column
  private LocalDateTime creationDate = LocalDateTime.now();
  /**
   * Protocol.
   */
  @ManyToOne(optional = false)
  private Protocol protocol;

  public ProtocolFile() {
  }

  public ProtocolFile(String filename) {
    this.filename = filename;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public byte[] getContent() {
    return content.clone();
  }

  public void setContent(byte[] content) {
    this.content = content.clone();
  }

  @Override
  public long getId() {
    return id;
  }

  @UsedBy(HIBERNATE)
  public void setId(long id) {
    this.id = id;
  }

  @UsedBy(HIBERNATE)
  public Protocol getProtocol() {
    return protocol;
  }

  public void setProtocol(Protocol protocol) {
    this.protocol = protocol;
  }

  public boolean isDeleted() {
    return deleted;
  }

  public void setDeleted(boolean deleted) {
    this.deleted = deleted;
  }

  public LocalDateTime getCreationDate() {
    return creationDate;
  }

  public void setCreationDate(LocalDateTime creationDate) {
    this.creationDate = creationDate;
  }
}
