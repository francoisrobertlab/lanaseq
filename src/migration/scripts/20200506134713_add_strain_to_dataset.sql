-- // add strain to dataset
-- Migration SQL that makes the change goes here.

ALTER TABLE dataset
ADD COLUMN strain varchar(255) AFTER target,
ADD COLUMN strain_description varchar(255) AFTER strain;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
DROP COLUMN strain,
DROP COLUMN strain_description;
