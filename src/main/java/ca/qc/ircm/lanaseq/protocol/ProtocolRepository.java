package ca.qc.ircm.lanaseq.protocol;

import ca.qc.ircm.lanaseq.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Protocol}.
 */
public interface ProtocolRepository extends JpaRepository<Protocol, Long> {
  boolean existsByName(String name);

  Optional<Protocol> findByName(String name);

  List<Protocol> findByOwner(User owner);
}
