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

-- // create changelog table
-- Migration SQL that makes the change goes here.

CREATE TABLE changelog (
  id decimal(20,0) NOT NULL,
  applied_at varchar(25) NOT NULL,
  description varchar(255) NOT NULL,
  PRIMARY KEY (id)
);


-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE changelog;
