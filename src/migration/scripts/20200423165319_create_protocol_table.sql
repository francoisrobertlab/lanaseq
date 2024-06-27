-- // create protocol table
-- Migration SQL that makes the change goes here.

CREATE TABLE protocol (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  owner_id bigint(20) NOT NULL,
  date DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY owner (owner_id),
  CONSTRAINT protocolOwner_ibfk FOREIGN KEY (owner_id) REFERENCES user (id) ON UPDATE CASCADE
);
CREATE TABLE protocol_file (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  files_id bigint(20) DEFAULT NULL,
  filename varchar(255) NOT NULL,
  content longblob,
  PRIMARY KEY (id),
  KEY protocol (files_id),
  CONSTRAINT protocolfileProtocol_ibfk FOREIGN KEY (files_id) REFERENCES protocol (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE protocol_file;
DROP TABLE protocol;
