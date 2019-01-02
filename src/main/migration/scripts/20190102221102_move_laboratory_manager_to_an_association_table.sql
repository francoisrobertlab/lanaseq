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

-- // move laboratory manager to an association table
-- Migration SQL that makes the change goes here.

CREATE TABLE manager (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  user_id bigint(20) NOT NULL,
  laboratory_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (user_id,laboratory_id),
  CONSTRAINT manager_ibfk_1 FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT manager_ibfk_2 FOREIGN KEY (laboratory_id) REFERENCES laboratory (id) ON DELETE CASCADE ON UPDATE CASCADE
);
INSERT INTO manager (user_id, laboratory_id)
SELECT user.id, laboratory.id
FROM user
JOIN laboratory ON user.laboratory_id = laboratory.id
WHERE user.manager = 1;
ALTER TABLE user
DROP COLUMN manager;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE user
ADD COLUMN manager tinyint NOT NULL DEFAULT 0 AFTER active;
UPDATE user
JOIN manager ON user.id = manager.user_id
SET user.manager = 1;
DROP TABLE manager;
