package ca.qc.ircm.lanaseq.protocol;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link ProtocolFile}.
 */
public interface ProtocolFileRepository extends JpaRepository<ProtocolFile, Long> {

  List<ProtocolFile> findByProtocol(Protocol protocol);

  List<ProtocolFile> findByProtocolAndDeletedFalse(Protocol protocol);

  List<ProtocolFile> findByProtocolAndDeletedTrue(Protocol protocol);
}
