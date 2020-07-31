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

-- // add sample
-- Migration SQL that makes the change goes here.

CREATE TABLE sample (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  replicate varchar(255),
  samples_order int,
  dataset_id bigint(20) NOT NULL,
  date DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT sampleDataset_ibfk FOREIGN KEY (dataset_id) REFERENCES dataset (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE sample;
