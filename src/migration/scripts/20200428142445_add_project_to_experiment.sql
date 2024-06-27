-- // add project to experiment
-- Migration SQL that makes the change goes here.

ALTER TABLE experiment
ADD COLUMN project varchar(255) AFTER name;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE experiment
DROP COLUMN project;
