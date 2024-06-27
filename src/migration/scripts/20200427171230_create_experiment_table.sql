-- // create experiment table
-- Migration SQL that makes the change goes here.

CREATE TABLE experiment (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  protocol_id bigint(20) NOT NULL,
  owner_id bigint(20) NOT NULL,
  date DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY protocol (protocol_id),
  KEY owner (owner_id),
  CONSTRAINT experimentProtocol_ibfk FOREIGN KEY (protocol_id) REFERENCES protocol (id) ON UPDATE CASCADE,
  CONSTRAINT experimentOwner_ibfk FOREIGN KEY (owner_id) REFERENCES user (id) ON UPDATE CASCADE
);


-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE experiment;
