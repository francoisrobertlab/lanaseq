-- // create laboratory table
-- Migration SQL that makes the change goes here.

CREATE TABLE laboratory (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id)
);


-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE laboratory;
