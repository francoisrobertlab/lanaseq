-- // create changelog table
-- Migration SQL that makes the change goes here.

CREATE TABLE changelog (
  id decimal(20,0) NOT NULL,
  applied_at varchar(25) NOT NULL,
  description varchar(255) NOT NULL,
  PRIMARY KEY (id)
);


-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE changelog;
