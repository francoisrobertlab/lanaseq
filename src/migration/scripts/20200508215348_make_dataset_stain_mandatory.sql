-- // make dataset stain mandatory
-- Migration SQL that makes the change goes here.

ALTER TABLE dataset
MODIFY COLUMN strain varchar(255) NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
MODIFY COLUMN strain varchar(255);
