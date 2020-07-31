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

-- // remove laboratory
-- Migration SQL that makes the change goes here.

ALTER TABLE user
DROP FOREIGN KEY userLaboratory_ibfk;
ALTER TABLE user
DROP COLUMN laboratory_id;
DROP TABLE laboratory;

-- //@UNDO
-- SQL to undo the change goes here.

CREATE TABLE laboratory (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id)
);
INSERT INTO laboratory (id,name)
VALUES ('1', 'Informatics');
ALTER TABLE user
ADD COLUMN laboratory_id bigint(20),
ADD CONSTRAINT userLaboratory_ibfk FOREIGN KEY (laboratory_id) REFERENCES laboratory (id) ON UPDATE CASCADE;
UPDATE user
SET laboratory_id = 1;
ALTER TABLE user
MODIFY COLUMN laboratory_id bigint(20) NOT NULL;
