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

package ca.qc.ircm.lana.user;

import static ca.qc.ircm.lana.user.UserRole.ADMIN;
import static ca.qc.ircm.lana.user.UserRole.MANAGER;
import static ca.qc.ircm.lana.user.UserRole.USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ca.qc.ircm.lana.Data;
import ca.qc.ircm.lana.security.AuthorizationService;
import ca.qc.ircm.lana.test.config.ServiceTestAnnotations;
import java.util.List;
import javax.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ServiceTestAnnotations
public class LaboratoryServiceTest {
  private LaboratoryService laboratoryService;
  @Inject
  private LaboratoryRepository laboratoryRepository;
  @Inject
  private UserRepository userRepository;
  @Mock
  private AuthorizationService authorizationService;

  @Before
  public void beforeTest() {
    laboratoryService = new LaboratoryService(laboratoryRepository, authorizationService);
  }

  @Test
  public void get() {
    Laboratory laboratory = laboratoryService.get(2L);

    assertNotNull(laboratory);
    assertEquals((Long) 2L, laboratory.getId());
    assertEquals("Chromatin and Genomic Expression", laboratory.getName());
    verify(authorizationService).checkRead(laboratory);
  }

  @Test
  public void get_Invalid() {
    Laboratory laboratory = laboratoryService.get(0L);

    assertNull(laboratory);
  }

  @Test
  public void get_Null() {
    Laboratory laboratory = laboratoryService.get(null);

    assertNull(laboratory);
  }

  @Test
  public void all() {
    when(authorizationService.currentUser()).thenReturn(userRepository.findById(3L).get());

    List<Laboratory> laboratories = laboratoryService.all();

    assertEquals(1, laboratories.size());
    assertTrue(Data.find(laboratories, 2).isPresent());
    verify(authorizationService).checkRole(USER);
  }

  @Test
  public void all_Manager() {
    when(authorizationService.currentUser()).thenReturn(userRepository.findById(2L).get());
    when(authorizationService.hasRole(MANAGER)).thenReturn(true);

    List<Laboratory> laboratories = laboratoryService.all();

    assertEquals(1, laboratories.size());
    assertTrue(Data.find(laboratories, 2).isPresent());
    verify(authorizationService).checkRole(USER);
  }

  @Test
  public void all_Admin() {
    when(authorizationService.currentUser()).thenReturn(userRepository.findById(1L).get());
    when(authorizationService.hasRole(ADMIN)).thenReturn(true);

    List<Laboratory> laboratories = laboratoryService.all();

    assertEquals(3, laboratories.size());
    assertTrue(Data.find(laboratories, 1).isPresent());
    assertTrue(Data.find(laboratories, 2).isPresent());
    assertTrue(Data.find(laboratories, 3).isPresent());
    verify(authorizationService).checkRole(USER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void save_New() {
    Laboratory laboratory = new Laboratory();
    laboratory.setName("New name");

    laboratoryService.save(laboratory);
  }

  @Test
  public void save_Update() {
    Laboratory laboratory = laboratoryRepository.findById(2L).orElse(null);
    laboratory.setName("New name");

    laboratoryService.save(laboratory);

    verify(authorizationService).checkWrite(laboratory);
    laboratory = laboratoryRepository.findById(2L).orElse(null);
    assertEquals("New name", laboratory.getName());
  }
}
