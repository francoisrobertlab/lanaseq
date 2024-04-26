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

package ca.qc.ircm.lanaseq.security;

import ca.qc.ircm.lanaseq.dataset.Dataset;
import ca.qc.ircm.lanaseq.protocol.Protocol;
import ca.qc.ircm.lanaseq.sample.Sample;
import ca.qc.ircm.lanaseq.user.User;
import java.io.Serializable;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Implementation of {@link PermissionEvaluator}.
 */
@Component("permissionEvaluator")
@Primary
public class PermissionEvaluatorDelegator implements PermissionEvaluator {
  private static Logger logger = LoggerFactory.getLogger(PermissionEvaluatorDelegator.class);
  private UserPermissionEvaluator userPermissionEvaluator;
  private DatasetPermissionEvaluator datasetPermissionEvaluator;
  private ProtocolPermissionEvaluator protocolPermissionEvaluator;
  private SamplePermissionEvaluator samplePermissionEvaluator;

  @Autowired
  protected PermissionEvaluatorDelegator(UserPermissionEvaluator userPermissionEvaluator,
      DatasetPermissionEvaluator datasetPermissionEvaluator,
      ProtocolPermissionEvaluator protocolPermissionEvaluator,
      SamplePermissionEvaluator samplePermissionEvaluator) {
    this.userPermissionEvaluator = userPermissionEvaluator;
    this.datasetPermissionEvaluator = datasetPermissionEvaluator;
    this.protocolPermissionEvaluator = protocolPermissionEvaluator;
    this.samplePermissionEvaluator = samplePermissionEvaluator;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Object targetDomainObject,
      Object permission) {
    if (targetDomainObject instanceof User) {
      return userPermissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
    } else if (targetDomainObject instanceof Dataset) {
      return datasetPermissionEvaluator.hasPermission(authentication, targetDomainObject,
          permission);
    } else if (targetDomainObject instanceof Protocol) {
      return protocolPermissionEvaluator.hasPermission(authentication, targetDomainObject,
          permission);
    } else if (targetDomainObject instanceof Sample) {
      return samplePermissionEvaluator.hasPermission(authentication, targetDomainObject,
          permission);
    }
    return false;
  }

  @Override
  public boolean hasPermission(Authentication authentication, Serializable targetId,
      String targetType, Object permission) {
    if (targetType.equals(User.class.getName())) {
      return userPermissionEvaluator.hasPermission(authentication, targetId, targetType,
          permission);
    } else if (targetType.equals(Dataset.class.getName())) {
      return datasetPermissionEvaluator.hasPermission(authentication, targetId, targetType,
          permission);
    } else if (targetType.equals(Protocol.class.getName())) {
      return protocolPermissionEvaluator.hasPermission(authentication, targetId, targetType,
          permission);
    } else if (targetType.equals(Sample.class.getName())) {
      return samplePermissionEvaluator.hasPermission(authentication, targetId, targetType,
          permission);
    }
    return false;
  }

  public boolean hasCollectionPermission(Authentication authentication,
      Collection<? extends Object> targetDomainObjects, Object permission) {
    return !targetDomainObjects.stream().map(ob -> hasPermission(authentication, ob, permission))
        .filter(allowed -> !allowed).findFirst().isPresent();
  }
}
