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

-- // create user table
-- Migration SQL that makes the change goes here.

CREATE TABLE User (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  email varchar(255) NOT NULL,
  name varchar(255),
  role varchar(255) NOT NULL,
  hashed_password varchar(255),
  sign_attempts int NOT NULL DEFAULT 0,
  last_sign_attempt DATETIME,
  active tinyint NOT NULL DEFAULT 0,
  manager tinyint NOT NULL DEFAULT 0,
  expired_password tinyint NOT NULL DEFAULT 0,
  laboratory_id bigint(20),
  locale varchar(255),
  PRIMARY KEY (id),
  UNIQUE KEY email (email),
  KEY laboratory (laboratory_id),
  CONSTRAINT userLaboratory_ibfk FOREIGN KEY (laboratory_id) REFERENCES Laboratory (id) ON UPDATE CASCADE
);


-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE User;
