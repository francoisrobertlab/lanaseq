-- // add editable to sample
-- Migration SQL that makes the change goes here.

ALTER TABLE sample
ADD COLUMN editable TINYINT NOT NULL DEFAULT 0 AFTER owner_id;
UPDATE sample
SET editable = 1;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE sample
DROP COLUMN editable;
