-- // add note to protocol
-- Migration SQL that makes the change goes here.

ALTER TABLE protocol
ADD COLUMN note TEXT AFTER date;


-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE protocol
DROP COLUMN note;
