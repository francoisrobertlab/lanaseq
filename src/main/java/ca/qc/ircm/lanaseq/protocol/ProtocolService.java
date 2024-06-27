package ca.qc.ircm.lanaseq.protocol;

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;
import static ca.qc.ircm.lanaseq.security.UserRole.MANAGER;
import static ca.qc.ircm.lanaseq.security.UserRole.USER;

import ca.qc.ircm.lanaseq.sample.SampleRepository;
import ca.qc.ircm.lanaseq.security.AuthenticatedUser;
import ca.qc.ircm.lanaseq.user.User;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
  @Autowired
  private ProtocolRepository repository;
  @Autowired
  private ProtocolFileRepository fileRepository;
  @Autowired
  private SampleRepository sampleRepository;
  @Autowired
  private AuthenticatedUser authenticatedUser;

  protected ProtocolService() {
  }

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
   * @param id
   *          protocol's id
   * @return protocol having specified id
   */
  @PostAuthorize("!returnObject.isPresent() || hasPermission(returnObject.get(), 'read')")
  public Optional<Protocol> get(Long id) {
    if (id == null) {
      return Optional.empty();
    }

    return repository.findById(id);
  }

  /**
   * Returns true if a protocol of the same name exists in user's laboratory, false otherwise.
   *
   * @param name
   *          protocol's name
   * @return true if a protocol of the same name exists in user's laboratory, false otherwise
   */
  @PreAuthorize("hasRole('" + USER + "')")
  public boolean nameExists(String name) {
    if (name == null) {
      return false;
    }

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
   * @param protocol
   *          protocol
   * @return all files linked to protocol
   */
  @PreAuthorize("hasPermission(#protocol, 'read')")
  public List<ProtocolFile> files(Protocol protocol) {
    if (protocol == null || protocol.getId() == null) {
      return new ArrayList<>();
    }
    return fileRepository.findByProtocolAndDeletedFalse(protocol);
  }

  /**
   * Returns all deleted files linked to protocol.
   *
   * @param protocol
   *          protocol
   * @return all deleted files linked to protocol
   */
  @PreAuthorize("hasPermission(#protocol, 'read') && hasAnyRole('" + MANAGER + "', '" + ADMIN
      + "')")
  public List<ProtocolFile> deletedFiles(Protocol protocol) {
    if (protocol == null || protocol.getId() == null) {
      return new ArrayList<>();
    }
    return fileRepository.findByProtocolAndDeletedTrue(protocol);
  }

  /**
   * Returns true if protocol can be deleted, false otherwise.
   *
   * @param protocol
   *          protocol
   * @return true if protocol can be deleted, false otherwise
   */
  @PreAuthorize("hasPermission(#protocol, 'read')")
  public boolean isDeletable(Protocol protocol) {
    if (protocol == null || protocol.getId() == null) {
      return false;
    }
    return !sampleRepository.existsByProtocol(protocol);
  }

  /**
   * Saves protocol into database.
   *
   * @param protocol
   *          protocol
   * @param files
   *          files protocol's files
   */
  @PreAuthorize("hasPermission(#protocol, 'write')")
  public void save(Protocol protocol, Collection<ProtocolFile> files) {
    if (files == null || files.isEmpty()) {
      throw new IllegalArgumentException("at least one file is required for protocols");
    }
    LocalDateTime now = LocalDateTime.now();
    if (protocol.getId() == null) {
      User user = authenticatedUser.getUser().orElse(null);
      protocol.setOwner(user);
      protocol.setCreationDate(now);
    } else {
      List<ProtocolFile> oldFiles = fileRepository.findByProtocolAndDeletedFalse(protocol);
      for (ProtocolFile file : oldFiles) {
        if (!files.stream().filter(f -> file.getId().equals(f.getId())).findAny().isPresent()) {
          file.setDeleted(true);
          fileRepository.save(file);
        }
      }
    }
    repository.save(protocol);
    for (ProtocolFile file : files) {
      if (file.getId() == null) {
        file.setProtocol(protocol);
        file.setCreationDate(now);
      }
      fileRepository.save(file);
    }
  }

  /**
   * Recovers protocol file.
   *
   * @param file
   *          protocol file
   */
  @PreAuthorize("hasPermission(#file.protocol, 'write')")
  public void recover(ProtocolFile file) {
    file.setDeleted(false);
    fileRepository.save(file);
  }

  /**
   * Deletes protocol.
   *
   * @param protocol
   *          protocol
   */
  @PreAuthorize("hasPermission(#protocol, 'write')")
  public void delete(Protocol protocol) {
    if (!isDeletable(protocol)) {
      throw new IllegalArgumentException("protocol cannot be deleted");
    }
    repository.delete(protocol);
  }
}
