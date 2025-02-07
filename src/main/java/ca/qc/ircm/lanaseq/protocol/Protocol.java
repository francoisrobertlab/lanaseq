package ca.qc.ircm.lanaseq.protocol;

import static ca.qc.ircm.lanaseq.FindbugsExplanations.ENTITY_EI_EXPOSE_REP;
import static ca.qc.ircm.lanaseq.UsedBy.HIBERNATE;
import static jakarta.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.Data;
import ca.qc.ircm.lanaseq.UsedBy;
import ca.qc.ircm.lanaseq.user.Owned;
import ca.qc.ircm.lanaseq.user.User;
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
import java.time.LocalDateTime;
import org.springframework.lang.Nullable;

/**
 * A protocol.
 */
@Entity
@GeneratePropertyNames
@SuppressFBWarnings(
    value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"},
    justification = ENTITY_EI_EXPOSE_REP)
public class Protocol implements Data, Owned, Serializable {

  @Serial
  private static final long serialVersionUID = 5424531974394821303L;
  /**
   * Database identifier.
   */
  @Id
  @Column(unique = true, nullable = false)
  @GeneratedValue(strategy = IDENTITY)
  private long id;
  /**
   * Name.
   */
  @Column(unique = true, nullable = false)
  @Size(max = 255)
  private String name;
  /**
   * Notes.
   */
  @Column
  private String note;
  /**
   * Creation date.
   */
  @Column(nullable = false)
  private LocalDateTime creationDate = LocalDateTime.now();
  /**
   * Owner.
   */
  @ManyToOne(optional = false)
  @JoinColumn
  private User owner;

  public Protocol() {
  }

  public Protocol(long id) {
    this.id = id;
  }

  public Protocol(String name) {
    this.name = name;
  }

  public Protocol(long id, String name) {
    this.id = id;
    this.name = name;
  }

  @Override
  public String toString() {
    return "Protocol [id=" + id + ", name=" + name + "]";
  }

  @Override
  public long getId() {
    return id;
  }

  @UsedBy(HIBERNATE)
  public void setId(long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  @Nullable
  public String getNote() {
    return note;
  }

  public void setNote(@Nullable String note) {
    this.note = note;
  }
}
