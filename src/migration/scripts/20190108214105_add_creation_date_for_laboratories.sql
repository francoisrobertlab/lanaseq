-- // add creation date for laboratories
-- Migration SQL that makes the change goes here.

ALTER TABLE laboratory
ADD COLUMN date DATETIME DEFAULT NULL AFTER name;
UPDATE laboratory
SET date = now();
ALTER TABLE laboratory
CHANGE COLUMN date date DATETIME NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE laboratory
DROP COLUMN date;
