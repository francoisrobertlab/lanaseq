package ca.qc.ircm.lanaseq.message;

import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.user.User;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

/**
 * Services for {@link Message}.
 */
@Service
@Transactional
public class MessageService {

  private final MessageRepository repository;
  private final AuthenticatedUser authenticatedUser;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * Creates new MessageService.
   *
   * @param repository                message repository
   * @param authenticatedUser         authenticated user
   * @param applicationEventPublisher event publisher
   */
  @Autowired
  public MessageService(MessageRepository repository, AuthenticatedUser authenticatedUser,
      ApplicationEventPublisher applicationEventPublisher) {
    this.repository = repository;
    this.authenticatedUser = authenticatedUser;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  /**
   * Returns message having specified id.
   *
   * @param id message's id
   * @return message having specified id
   */
  @PostAuthorize("!returnObject.isPresent() || hasPermission(returnObject.get(), 'read')")
  public Optional<Message> get(long id) {
    return repository.findById(id);
  }

  /**
   * Returns all messages of authenticated user.
   *
   * @return all messages of authenticated user
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Message> all() {
    Optional<User> user = authenticatedUser.getUser();
    return repository.findByOwnerOrderByIdDesc(user.orElseThrow());
  }

  /**
   * Returns all unread messages of authenticated user.
   *
   * @return all unread messages of authenticated user
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Message> allUnread() {
    Optional<User> user = authenticatedUser.getUser();
    return repository.findByOwnerAndUnreadOrderByIdDesc(user.orElseThrow(), true);
  }

  /**
   * Saves message into database.
   *
   * @param message message
   */
  @PreAuthorize("hasPermission(#message, 'write')")
  public void save(Message message) {
    Objects.requireNonNull(message, "message parameter cannot be null");
    Objects.requireNonNull(message.getMessage(), "message's message cannot be null");
    User user = authenticatedUser.getUser().orElseThrow();
    if (message.getId() == 0) {
      message.setOwner(user);
      message.setDate(LocalDateTime.now());
      message.setUnread(true);
    }
    repository.save(message);
    applicationEventPublisher.publishEvent(new SavedMessageEvent(this, message));
  }
}
