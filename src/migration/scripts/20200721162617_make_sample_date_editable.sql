-- // make sample date editable
-- Migration SQL that makes the change goes here.

ALTER TABLE sample
CHANGE COLUMN date creation_date DATETIME NOT NULL,
ADD COLUMN date DATE DEFAULT NULL AFTER treatment;
UPDATE sample
SET date = creation_date;
ALTER TABLE sample
MODIFY COLUMN date DATE NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE sample
DROP COLUMN date,
CHANGE COLUMN creation_date date DATETIME NOT NULL;
