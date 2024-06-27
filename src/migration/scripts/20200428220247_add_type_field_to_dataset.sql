-- // add type field to dataset
-- Migration SQL that makes the change goes here.

ALTER TABLE dataset
ADD COLUMN type varchar(255) AFTER assay;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
DROP COLUMN type;
