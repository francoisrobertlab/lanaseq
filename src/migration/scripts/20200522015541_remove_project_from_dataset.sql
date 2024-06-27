-- // remove project from dataset
-- Migration SQL that makes the change goes here.

ALTER TABLE dataset
DROP COLUMN project;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
ADD COLUMN project varchar(255) AFTER name;
