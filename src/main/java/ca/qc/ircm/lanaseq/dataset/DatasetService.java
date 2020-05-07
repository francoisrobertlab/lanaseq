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

package ca.qc.ircm.lanaseq.dataset;

import static ca.qc.ircm.lanaseq.security.UserRole.ADMIN;

import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.security.AuthorizationService;
import ca.qc.ircm.lanaseq.security.UserAuthority;
import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.LaboratoryRepository;
import ca.qc.ircm.lanaseq.user.User;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.domain.GrantedAuthoritySid;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.Acl;
import org.springframework.security.acls.model.MutableAcl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.NotFoundException;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.security.acls.model.Sid;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Services for {@link Dataset}.
 */
@Service
@Transactional
public class DatasetService {
  @Autowired
  private DatasetRepository repository;
  @Autowired
  private LaboratoryRepository laboratoryRepository;
  @Autowired
  private MutableAclService aclService;
  @Autowired
  private AuthorizationService authorizationService;

  protected DatasetService() {
  }

  protected DatasetService(DatasetRepository repository, LaboratoryRepository laboratoryRepository,
      MutableAclService aclService, AuthorizationService authorizationService) {
    this.repository = repository;
    this.laboratoryRepository = laboratoryRepository;
    this.aclService = aclService;
    this.authorizationService = authorizationService;
  }

  /**
   * Returns dataset having specified id.
   *
   * @param id
   *          dataset's id
   * @return dataset having specified id
   */
  @PostAuthorize("returnObject == null || hasPermission(returnObject, 'read')")
  public Dataset get(Long id) {
    if (id == null) {
      return null;
    }

    return repository.findById(id).orElse(null);
  }

  /**
   * Returns all datasets for current user.
   * <p>
   * If current user is a regular user, returns all datasets owned by him.
   * </p>
   * <p>
   * If current user is a manager, returns all datasets made by users in his lab.
   * </p>
   * <p>
   * If current user is an admin, returns all datasets.
   * </p>
   *
   * @return all datasets for current user
   */
  @PostFilter("hasPermission(filterObject, 'read')")
  public List<Dataset> all() {
    if (authorizationService.hasRole(ADMIN)) {
      return repository.findAll();
    } else {
      return repository
          .findByOwnerLaboratory(authorizationService.getCurrentUser().getLaboratory());
    }
  }

  /**
   * Returns laboratories that can read dataset.
   *
   * @param dataset
   *          dataset
   * @return laboratories that can read dataset
   */
  @PreAuthorize("hasPermission(#dataset, 'write')")
  public Set<Laboratory> permissions(Dataset dataset) {
    Laboratory ownerLaboratory = dataset.getOwner().getLaboratory();
    ObjectIdentity oi = new ObjectIdentityImpl(dataset.getClass(), dataset.getId());
    try {
      Acl acl = aclService.readAclById(oi);
      List<Laboratory> allLaboratories = laboratoryRepository.findAll();
      HashSet<Laboratory> laboratories = new HashSet<>();
      for (Laboratory laboratory : allLaboratories) {
        Sid sid = new GrantedAuthoritySid(UserAuthority.laboratoryMember(laboratory));
        try {
          if (acl.isGranted(list(BasePermission.READ), list(sid), false)) {
            laboratories.add(laboratory);
          }
        } catch (NotFoundException e) {
          // Cannot read.
        }
      }
      laboratories.add(ownerLaboratory);
      return laboratories;
    } catch (NotFoundException e) {
      return Stream.of(ownerLaboratory).collect(Collectors.toCollection(HashSet::new));
    }
  }

  @SuppressWarnings("unchecked")
  private <T> List<T> list(T... values) {
    return Stream.of(values).collect(Collectors.toList());
  }

  /**
   * Saves dataset into database.
   *
   * @param dataset
   *          dataset
   */
  @PreAuthorize("hasPermission(#dataset, 'write')")
  public void save(Dataset dataset) {
    LocalDateTime now = LocalDateTime.now();
    if (dataset.getId() == null) {
      User user = authorizationService.getCurrentUser();
      dataset.setOwner(user);
      dataset.setDate(now);
    }
    if (dataset.getSamples() != null) {
      for (Sample sample : dataset.getSamples()) {
        if (sample.getId() == null) {
          sample.setDate(now);
        }
        sample.setDataset(dataset);
      }
    }
    repository.save(dataset);
  }

  /**
   * Saves dataset's permissions into database.
   *
   * @param dataset
   *          dataset
   * @param laboratories
   *          laboratories that can read dataset
   */
  @PreAuthorize("hasPermission(#dataset, 'write')")
  public void savePermissions(Dataset dataset, Collection<Laboratory> laboratories) {
    ObjectIdentity oi = new ObjectIdentityImpl(dataset.getClass(), dataset.getId());
    aclService.deleteAcl(oi, false);
    MutableAcl acl = aclService.createAcl(oi);
    for (Laboratory laboratory : laboratories) {
      acl.insertAce(acl.getEntries().size(), BasePermission.READ,
          new GrantedAuthoritySid(UserAuthority.laboratoryMember(laboratory)), true);
    }
  }
}
