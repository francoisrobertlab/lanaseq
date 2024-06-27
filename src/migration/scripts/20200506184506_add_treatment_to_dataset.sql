-- // add treatment to dataset
-- Migration SQL that makes the change goes here.

ALTER TABLE dataset
ADD COLUMN treatment varchar(255) AFTER strain_description;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
DROP COLUMN treatment;
