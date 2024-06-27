-- // add creation date for users
-- Migration SQL that makes the change goes here.

ALTER TABLE user
ADD COLUMN date DATETIME DEFAULT NULL AFTER locale;
UPDATE user
SET date = now();
ALTER TABLE user
CHANGE COLUMN date date DATETIME NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE user
DROP COLUMN date;
