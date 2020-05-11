--
--    Copyright 2010-2016 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
--

-- // remove access control list
-- Migration SQL that makes the change goes here.

DROP TABLE acl_entry;
DROP TABLE acl_object_identity;
DROP TABLE acl_class;
DROP TABLE acl_sid;

-- //@UNDO
-- SQL to undo the change goes here.

CREATE TABLE acl_sid (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  principal BOOLEAN NOT NULL,
  sid VARCHAR(100) NOT NULL,
  UNIQUE KEY unique_acl_sid (sid, principal)
);
CREATE TABLE acl_class (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
  class VARCHAR(100) NOT NULL,
  UNIQUE KEY unique_acl_class (class)
);
CREATE TABLE acl_object_identity (
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
CREATE TABLE acl_entry (
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
