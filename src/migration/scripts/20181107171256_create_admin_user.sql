--
-- Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with this program.  If not, see <http://www.gnu.org/licenses/>.
--

-- // create admin user
-- Migration SQL that makes the change goes here.

INSERT INTO user (id, email, name, role, hashed_password, active, expired_password)
VALUES (1, 'lana@ircm.qc.ca', 'Administrator', 'ADMIN', '$2a$10$lWDef/shwP7Tl2QAg8yDm.BITRMFWYKCZmG.MZ20cdLaf1UrxkDRO', 1, 1);

-- //@UNDO
-- SQL to undo the change goes here.

DELETE FROM user
WHERE id = 1;
