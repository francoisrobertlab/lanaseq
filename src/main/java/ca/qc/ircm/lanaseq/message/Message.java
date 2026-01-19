package ca.qc.ircm.lanaseq.message;

import static ca.qc.ircm.lanaseq.FindbugsExplanations.ENTITY_EI_EXPOSE_REP;
import static jakarta.persistence.GenerationType.IDENTITY;

import ca.qc.ircm.lanaseq.Data;
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

/**
 * Message (notification) to show to user.
 */
@Entity
@GeneratePropertyNames
@SuppressFBWarnings(value = {"EI_EXPOSE_REP",
    "EI_EXPOSE_REP2"}, justification = ENTITY_EI_EXPOSE_REP)
public class Message implements Data, Owned, Serializable {

  @Serial
  private static final long serialVersionUID = 0;
  /**
   * Message identifier.
   */
  @Id
  @Column(unique = true, nullable = false)
  @GeneratedValue(strategy = IDENTITY)
  private long id;
  /**
   * Message to show to user.
   */
  @Column
  @Size(max = 255)
  private String message;
  /**
   * Color of the message.
   * <br>
   * `success`, `warning` and `error` are good examples.
   * {@see https://vaadin.com/docs/v24/styling/lumo/lumo-style-properties/color#warning}
   */
  @Column
  private String color;
  /**
   * True if message has not been read by the user.
   */
  @Column
  private boolean unread = true;
  /**
   * Time when message was created.
   */
  @Column(nullable = false)
  private LocalDateTime date = LocalDateTime.now();
  /**
   * Owner.
   */
  @ManyToOne(optional = false)
  @JoinColumn
  private User owner;

  /**
   * Create an empty message.
   */
  public Message() {
  }

  /**
   * Create an empty message.
   *
   * @param id database identifier
   */
  public Message(long id) {
    this.id = id;
  }

  /**
   * Create a message.
   *
   * @param id      database identifier
   * @param message message
   * @param owner   owner
   */
  public Message(long id, String message, User owner) {
    this.id = id;
    this.message = message;
    this.owner = owner;
  }

  @Override
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public boolean isUnread() {
    return unread;
  }

  public void setUnread(boolean unread) {
    this.unread = unread;
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
}
