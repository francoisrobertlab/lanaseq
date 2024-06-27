package ca.qc.ircm.lanaseq.sample;

import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.user.User;
import com.querydsl.core.types.Predicate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * Repository for {@link Sample}.
 */
public interface SampleRepository
    extends JpaRepository<Sample, Long>, QuerydslPredicateExecutor<Sample> {
  public boolean existsByName(String name);

  public Page<Sample> findAllByOrderByIdDesc(Pageable pageable);

  public List<Sample> findByOwner(User owner);

  public boolean existsByProtocol(Protocol protocol);

  @Override
  @EntityGraph(attributePaths = { "protocol", "owner" })
  public Page<Sample> findAll(Predicate predicate, Pageable pageable);
}
