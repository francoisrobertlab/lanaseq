package ca.qc.ircm.lanaseq.dataset;

import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.user.User;
import com.querydsl.core.types.Predicate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

/**
 * Repository for {@link Dataset}.
 */
public interface DatasetRepository
    extends JpaRepository<Dataset, Long>, QuerydslPredicateExecutor<Dataset> {

  boolean existsByName(String name);

  boolean existsBySamples(Sample sample);

  Page<Dataset> findAllByOrderByIdDesc(Pageable pageable);

  List<Dataset> findBySamples(Sample sample);

  List<Dataset> findByOwner(User owner);

  @EntityGraph(attributePaths = {"tags", "samples", "samples.protocol", "owner"})
  List<Dataset> findAllByIdIn(Iterable<Long> ids, Sort sort);

  @Override
  @EntityGraph(attributePaths = {"owner"})
  Page<Dataset> findAll(Predicate predicate, Pageable pageable);
}
