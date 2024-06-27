-- // add editable to dataset
-- Migration SQL that makes the change goes here.

ALTER TABLE dataset
ADD COLUMN editable TINYINT NOT NULL DEFAULT 0 AFTER owner_id;
UPDATE dataset
SET editable = 1;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
DROP COLUMN editable;
