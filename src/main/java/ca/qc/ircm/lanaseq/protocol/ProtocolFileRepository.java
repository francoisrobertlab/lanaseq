package ca.qc.ircm.lanaseq.protocol;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link ProtocolFile}.
 */
public interface ProtocolFileRepository extends JpaRepository<ProtocolFile, Long> {
  public List<ProtocolFile> findByProtocol(Protocol protocol);

  public List<ProtocolFile> findByProtocolAndDeletedFalse(Protocol protocol);

  public List<ProtocolFile> findByProtocolAndDeletedTrue(Protocol protocol);
}
