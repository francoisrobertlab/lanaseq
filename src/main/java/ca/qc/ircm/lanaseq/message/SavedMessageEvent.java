package ca.qc.ircm.lanaseq.message;

import org.springframework.context.ApplicationEvent;

/**
 * A message was saved in the database.
 */
public class SavedMessageEvent extends ApplicationEvent {

  private Message message;

  /**
   * Creates a new saved message event.
   *
   * @param source  the object on which the event initially occurred or with * which the event is
   *                associated (never {@code null})
   * @param message saved message
   */
  public SavedMessageEvent(Object source, Message message) {
    super(source);
    this.message = message;
  }

  public Message getMessage() {
    return message;
  }
}
