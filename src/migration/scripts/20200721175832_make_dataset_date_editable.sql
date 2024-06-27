-- // make dataset date editable
-- Migration SQL that makes the change goes here.

ALTER TABLE dataset
CHANGE COLUMN date creation_date DATETIME NOT NULL,
ADD COLUMN date DATE DEFAULT NULL AFTER name;
UPDATE dataset
SET date = creation_date;
ALTER TABLE dataset
MODIFY COLUMN date DATE NOT NULL;


-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
DROP COLUMN date,
CHANGE COLUMN creation_date date DATETIME NOT NULL;
