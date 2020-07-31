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

-- // keep deleted protocol files
-- Migration SQL that makes the change goes here.

ALTER TABLE protocol_file
ADD COLUMN deleted tinyint NOT NULL DEFAULT 0 AFTER content,
ADD COLUMN date DATETIME DEFAULT NULL AFTER deleted;
UPDATE protocol_file
JOIN protocol ON protocol.id = protocol_file.protocol_id
SET protocol_file.date = protocol.date;
ALTER TABLE protocol_file
MODIFY COLUMN date DATETIME NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE protocol_file
DROP COLUMN deleted,
DROP COLUMN date;
