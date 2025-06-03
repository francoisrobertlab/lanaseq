package ca.qc.ircm.lanaseq.protocol;

import static ca.qc.ircm.lanaseq.UsedBy.SPRING;
import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;

import ca.qc.ircm.lanaseq.UsedBy;
import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.user.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Services for {@link Protocol}.
 */
@Service
@Transactional
public class ProtocolService {

  private final ProtocolRepository repository;
  private final ProtocolFileRepository fileRepository;
  private final SampleRepository sampleRepository;
  private final AuthenticatedUser authenticatedUser;

  @Autowired
  @UsedBy(SPRING)
  protected ProtocolService(ProtocolRepository repository, ProtocolFileRepository fileRepository,
      SampleRepository sampleRepository, AuthenticatedUser authenticatedUser) {
    this.repository = repository;
    this.fileRepository = fileRepository;
    this.sampleRepository = sampleRepository;
    this.authenticatedUser = authenticatedUser;
  }

  /**
   * Returns protocol having specified id.
   *
   * @param id protocol's id
   * @return protocol having specified id
   */
  @PostAuthorize("!returnObject.isPresent() || hasPermission(returnObject.get(), 'read')")
  public Optional<Protocol> get(long id) {
    return repository.findById(id);
  }

  /**
   * Returns true if a protocol of the same name exists in user's laboratory, false otherwise.
   *
   * @param name protocol's name
   * @return true if a protocol of the same name exists in user's laboratory, false otherwise
   */
  @PreAuthorize("hasRole('" + USER + "')")
  public boolean nameExists(String name) {
    Objects.requireNonNull(name, "name parameter cannot be null");
    return repository.existsByName(name);
  }

  /**
   * Returns all protocols.
   *
   * @return all protocols
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Protocol> all() {
    return repository.findAll();
  }

  /**
   * Returns all files linked to protocol.
   *
   * @param protocol protocol
   * @return all files linked to protocol
   */
  @PreAuthorize("hasPermission(#protocol, 'read')")
  public List<ProtocolFile> files(Protocol protocol) {
    Objects.requireNonNull(protocol, "protocol parameter cannot be null");
    if (protocol.getId() == 0) {
      return new ArrayList<>();
    }
    return fileRepository.findByProtocolAndDeletedFalse(protocol);
  }

  /**
   * Returns all deleted files linked to protocol.
   *
   * @param protocol protocol
   * @return all deleted files linked to protocol
   */
  @PreAuthorize(
      "hasPermission(#protocol, 'read') && hasAnyRole('" + MANAGER + "', '" + ADMIN + "')")
  public List<ProtocolFile> deletedFiles(Protocol protocol) {
    Objects.requireNonNull(protocol, "protocol parameter cannot be null");
    return fileRepository.findByProtocolAndDeletedTrue(protocol);
  }

  /**
   * Returns true if protocol can be deleted, false otherwise.
   *
   * @param protocol protocol
   * @return true if protocol can be deleted, false otherwise
   */
  @PreAuthorize("hasPermission(#protocol, 'read')")
  public boolean isDeletable(Protocol protocol) {
    Objects.requireNonNull(protocol, "protocol parameter cannot be null");
    return !sampleRepository.existsByProtocol(protocol);
  }

  /**
   * Saves protocol into database.
   *
   * @param protocol protocol
   * @param files    files protocol's files
   */
  @PreAuthorize("hasPermission(#protocol, 'write')")
  public void save(Protocol protocol, Collection<ProtocolFile> files) {
    Objects.requireNonNull(protocol, "protocol parameter cannot be null");
    Objects.requireNonNull(files, "files parameter cannot be null");
    if (files.isEmpty()) {
      throw new IllegalArgumentException("at least one file is required for protocols");
    }
    LocalDateTime now = LocalDateTime.now();
    if (protocol.getId() == 0) {
      User user = authenticatedUser.getUser().orElseThrow();
      protocol.setOwner(user);
      protocol.setCreationDate(now);
    } else {
      List<ProtocolFile> oldFiles = fileRepository.findByProtocolAndDeletedFalse(protocol);
      for (ProtocolFile file : oldFiles) {
        if (files.stream().noneMatch(f -> file.getId() == f.getId())) {
          file.setDeleted(true);
          fileRepository.save(file);
        }
      }
    }
    repository.save(protocol);
    for (ProtocolFile file : files) {
      if (file.getId() == 0) {
        file.setProtocol(protocol);
        file.setCreationDate(now);
      }
      fileRepository.save(file);
    }
  }

  /**
   * Recovers protocol file.
   *
   * @param file protocol file
   */
  @PreAuthorize("hasPermission(#file.protocol, 'write')")
  public void recover(ProtocolFile file) {
    Objects.requireNonNull(file, "file parameter cannot be null");
    file.setDeleted(false);
    fileRepository.save(file);
  }

  /**
   * Deletes protocol.
   *
   * @param protocol protocol
   */
  @PreAuthorize("hasPermission(#protocol, 'write')")
  public void delete(Protocol protocol) {
    Objects.requireNonNull(protocol, "protocol parameter cannot be null");
    if (!isDeletable(protocol)) {
      throw new IllegalArgumentException("protocol cannot be deleted");
    }
    repository.delete(protocol);
  }
}
