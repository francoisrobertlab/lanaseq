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

CREATE TABLE IF NOT EXISTS laboratory (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  date DATETIME NOT NULL,
  PRIMARY KEY (id)
);
CREATE TABLE IF NOT EXISTS user (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  email varchar(255) NOT NULL,
  name varchar(255),
  hashed_password varchar(255),
  sign_attempts int NOT NULL DEFAULT 0,
  last_sign_attempt DATETIME,
  active tinyint NOT NULL DEFAULT 0,
  manager tinyint NOT NULL DEFAULT 0,
  admin tinyint NOT NULL DEFAULT 0,
  expired_password tinyint NOT NULL DEFAULT 0,
  laboratory_id bigint(20) NOT NULL,
  locale varchar(255),
  date DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY email (email),
  CONSTRAINT userLaboratory_ibfk FOREIGN KEY (laboratory_id) REFERENCES laboratory (id) ON UPDATE CASCADE
);
CREATE TABLE IF NOT EXISTS forgotpassword (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  user_id bigint(20) NOT NULL,
  request_moment datetime NOT NULL,
  confirm_number varchar(100) NOT NULL,
  used tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (id),
  KEY user (user_id),
  CONSTRAINT forgotpasswordUser_ibfk FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE IF NOT EXISTS experiment (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  owner_id bigint(20),
  date DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT experimentOwner_ibfk FOREIGN KEY (owner_id) REFERENCES user (id) ON UPDATE CASCADE
);

-- Spring Security ACL.
CREATE TABLE IF NOT EXISTS acl_sid (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  principal BOOLEAN NOT NULL,
  sid VARCHAR(100) NOT NULL,
  UNIQUE KEY unique_acl_sid (sid, principal)
);
CREATE TABLE IF NOT EXISTS acl_class (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  class VARCHAR(100) NOT NULL,
  UNIQUE KEY unique_acl_class (class)
);
CREATE TABLE IF NOT EXISTS acl_object_identity (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  object_id_class BIGINT UNSIGNED NOT NULL,
  object_id_identity VARCHAR(36) NOT NULL,
  parent_object BIGINT UNSIGNED,
  owner_sid BIGINT UNSIGNED,
  entries_inheriting BOOLEAN NOT NULL,
  UNIQUE KEY unique_acl_object_identity (object_id_class, object_id_identity),
  CONSTRAINT acl_object_identity_parent FOREIGN KEY (parent_object) REFERENCES acl_object_identity (id),
  CONSTRAINT acl_object_identity_class FOREIGN KEY (object_id_class) REFERENCES acl_class (id),
  CONSTRAINT acl_object_identity_owner FOREIGN KEY (owner_sid) REFERENCES acl_sid (id)
);
CREATE TABLE IF NOT EXISTS acl_entry (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  acl_object_identity BIGINT UNSIGNED NOT NULL,
  ace_order INTEGER NOT NULL,
  sid BIGINT UNSIGNED NOT NULL,
  mask INTEGER UNSIGNED NOT NULL,
  granting BOOLEAN NOT NULL,
  audit_success BOOLEAN NOT NULL,
  audit_failure BOOLEAN NOT NULL,
  UNIQUE KEY unique_acl_entry (acl_object_identity, ace_order),
  CONSTRAINT acl_entry_object FOREIGN KEY (acl_object_identity) REFERENCES acl_object_identity (id),
  CONSTRAINT acl_entry_acl FOREIGN KEY (sid) REFERENCES acl_sid (id)
);
