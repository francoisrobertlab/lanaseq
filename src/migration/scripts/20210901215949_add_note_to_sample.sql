-- // add note to sample
-- Migration SQL that makes the change goes here.

ALTER TABLE sample
ADD COLUMN note TEXT AFTER creation_date;


-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE sample
DROP COLUMN note;
