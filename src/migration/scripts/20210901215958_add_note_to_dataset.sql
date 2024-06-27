-- // add note to dataset
-- Migration SQL that makes the change goes here.

ALTER TABLE dataset
ADD COLUMN note TEXT AFTER creation_date;


-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
DROP COLUMN note;
