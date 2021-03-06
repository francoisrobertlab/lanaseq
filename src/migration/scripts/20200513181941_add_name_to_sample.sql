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

-- // add name to sample
-- Migration SQL that makes the change goes here.

ALTER TABLE sample
ADD COLUMN name varchar(255) NOT NULL AFTER id;
UPDATE sample
SET name = CONCAT(IFNULL(CONCAT(sample_id, '_'), ''),
           IFNULL(CONCAT(assay, '_'), ''),
           IFNULL(CONCAT(type, '_'), ''),
           IFNULL(CONCAT(target, '_'), ''),
           IFNULL(CONCAT(strain, '_'), ''),
           IFNULL(CONCAT(strain_description, '_'), ''),
           IFNULL(CONCAT(treatment, '_'), ''),
           IFNULL(CONCAT(replicate, '_'), ''),
           DATE_FORMAT(date, '%Y%m%d'));
ALTER TABLE sample
ADD CONSTRAINT name_u UNIQUE (name);

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE sample
DROP INDEX name_u,
DROP COLUMN name;
