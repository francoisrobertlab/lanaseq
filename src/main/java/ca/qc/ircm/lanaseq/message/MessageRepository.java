package ca.qc.ircm.lanaseq.message;

import ca.qc.ircm.lanaseq.user.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * Repository for {@link Message}.
 */
public interface MessageRepository extends JpaRepository<Message, Long>,
    QuerydslPredicateExecutor<Message> {

  /**
   * Finds all messages associated with owner.
   *
   * @param owner owner
   * @return all messages associated with owner
   */
  List<Message> findByOwner(User owner);
}
