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

import ca.qc.ircm.lanaseq.user.Laboratory;
import ca.qc.ircm.lanaseq.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Protocol}.
 */
public interface ProtocolRepository extends JpaRepository<Protocol, Long> {
  public boolean existsByNameAndOwnerLaboratory(String name, Laboratory laboratory);

  public Optional<Protocol> findByNameAndOwnerLaboratory(String name, Laboratory laboratory);

  public List<Protocol> findByOwner(User owner);

  public List<Protocol> findByOwnerLaboratory(Laboratory ownerLaboratory);
}