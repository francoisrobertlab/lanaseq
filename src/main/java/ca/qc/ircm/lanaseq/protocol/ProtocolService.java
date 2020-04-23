/*
 * Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ca.qc.ircm.lanaseq.protocol;

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;

import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.user.User;
import java.time.LocalDateTime;
import java.util.List;
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
  private AuthorizationService authorizationService;

  protected ProtocolService() {
  }

  protected ProtocolService(ProtocolRepository repository,
      AuthorizationService authorizationService) {
    this.repository = repository;
    this.authorizationService = authorizationService;
  }

  /**
   * Returns protocol having specified id.
   *
   * @param id
   *          protocol's id
   * @return protocol having specified id
   */
  @PostAuthorize("returnObject == null || hasPermission(returnObject, 'read')")
  public Protocol get(Long id) {
    if (id == null) {
      return null;
    }

    return repository.findById(id).orElse(null);
  }

  /**
   * Returns all protocols for current user.
   * <p>
   * If current user is a regular user, returns all protocols owned by him.
   * </p>
   * <p>
   * If current user is a manager, returns all protocols made by users in his lab.
   * </p>
   * <p>
   * If current user is an admin, returns all protocols.
   * </p>
   *
   * @return all protocols for current user
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Protocol> all() {
    if (authorizationService.hasRole(ADMIN)) {
      return repository.findAll();
    } else {
      return repository
          .findByOwnerLaboratory(authorizationService.getCurrentUser().getLaboratory());
    }
  }

  /**
   * Saves protocol into database.
   *
   * @param protocol
   *          protocol
   */
  @PreAuthorize("hasPermission(#protocol, 'write')")
  public void save(Protocol protocol) {
    if (protocol.getFiles() == null || protocol.getFiles().isEmpty()) {
      throw new IllegalArgumentException("at least one file is required for protocols");
    }
    if (protocol.getId() == null) {
      User user = authorizationService.getCurrentUser();
      protocol.setOwner(user);
      protocol.setDate(LocalDateTime.now());
    }
    repository.save(protocol);
  }
}
