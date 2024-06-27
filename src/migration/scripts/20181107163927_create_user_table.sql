-- // create user table
-- Migration SQL that makes the change goes here.

CREATE TABLE user (
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
  laboratory_id bigint(20),
  locale varchar(255),
  PRIMARY KEY (id),
  UNIQUE KEY email (email),
  KEY laboratory (laboratory_id),
  CONSTRAINT userLaboratory_ibfk FOREIGN KEY (laboratory_id) REFERENCES laboratory (id) ON UPDATE CASCADE
);


-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE user;
