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
  locale varchar(255),
  date DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY email (email)
);
CREATE TABLE IF NOT EXISTS forgot_password (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  user_id bigint(20) NOT NULL,
  request_moment datetime NOT NULL,
  confirm_number varchar(100) NOT NULL,
  used tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (id),
  CONSTRAINT forgotpasswordUser_ibfk FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE IF NOT EXISTS protocol (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  owner_id bigint(20) NOT NULL,
  date DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT protocolOwner_ibfk FOREIGN KEY (owner_id) REFERENCES user (id) ON UPDATE CASCADE
);
CREATE TABLE IF NOT EXISTS protocol_file (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  protocol_id bigint(20) DEFAULT NULL,
  filename varchar(255) NOT NULL,
  content blob,
  deleted tinyint NOT NULL DEFAULT 0,
  date DATETIME NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT protocolfileProtocol_ibfk FOREIGN KEY (protocol_id) REFERENCES protocol (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE IF NOT EXISTS dataset (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  owner_id bigint(20) NOT NULL,
  editable tinyint NOT NULL DEFAULT 0,
  date DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (name),
  CONSTRAINT datasetOwner_ibfk FOREIGN KEY (owner_id) REFERENCES user (id) ON UPDATE CASCADE
);
CREATE TABLE IF NOT EXISTS sample (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  sample_id varchar(255) NOT NULL,
  replicate varchar(255),
  assay varchar(255) NOT NULL,
  type varchar(255),
  target varchar(255),
  strain varchar(255) NOT NULL,
  strain_description varchar(255),
  treatment varchar(255),
  date DATE NOT NULL,
  protocol_id bigint(20) NOT NULL,
  owner_id bigint(20) NOT NULL,
  editable tinyint NOT NULL DEFAULT 0,
  creation_date DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (name),
  CONSTRAINT sampleOwner_ibfk FOREIGN KEY (owner_id) REFERENCES user (id) ON UPDATE CASCADE
);
CREATE TABLE IF NOT EXISTS dataset_samples (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  dataset_id bigint(20) NOT NULL,
  samples_order int,
  samples_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT datasetSamplesDataset_ibfk FOREIGN KEY (dataset_id) REFERENCES dataset (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT datasetSamplesSample_ibfk FOREIGN KEY (samples_id) REFERENCES sample (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE TABLE IF NOT EXISTS dataset_tags (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  dataset_id bigint(20) NOT NULL,
  tags varchar(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT datasetTagsDataset_ibfk FOREIGN KEY (dataset_id) REFERENCES dataset (id) ON DELETE CASCADE ON UPDATE CASCADE
);
