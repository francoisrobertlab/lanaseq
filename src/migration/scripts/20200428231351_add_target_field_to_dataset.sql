-- // add target field to dataset
-- Migration SQL that makes the change goes here.

ALTER TABLE dataset
ADD COLUMN target varchar(255) AFTER type;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
DROP COLUMN target;
