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

-- // make dataset date editable
-- Migration SQL that makes the change goes here.

ALTER TABLE dataset
CHANGE COLUMN date creation_date DATETIME NOT NULL,
ADD COLUMN date DATE DEFAULT NULL AFTER name;
UPDATE dataset
SET date = creation_date;
ALTER TABLE dataset
MODIFY COLUMN date DATE NOT NULL;


-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
DROP COLUMN date,
CHANGE COLUMN creation_date date DATETIME NOT NULL;
