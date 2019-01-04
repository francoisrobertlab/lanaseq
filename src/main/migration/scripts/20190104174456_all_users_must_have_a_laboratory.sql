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

-- // all users must have a laboratory
-- Migration SQL that makes the change goes here.

UPDATE laboratory
SET id = id + 1
ORDER BY id DESC;
INSERT INTO laboratory (id,name)
VALUES ('1', 'Informatics');
UPDATE user
SET laboratory_id = 1
WHERE laboratory_id IS NULL;


-- //@UNDO
-- SQL to undo the change goes here.

UPDATE user
SET laboratory_id = NULL
WHERE laboratory_id = 1;
DELETE FROM laboratory
WHERE id = 1;
UPDATE laboratory
SET id = id - 1
ORDER BY id ASC;
