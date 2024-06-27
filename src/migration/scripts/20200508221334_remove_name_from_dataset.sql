-- // remove name from dataset
-- Migration SQL that makes the change goes here.

ALTER TABLE dataset
DROP COLUMN name;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
ADD COLUMN name varchar(255);
UPDATE dataset
SET name = '';
ALTER TABLE dataset
MODIFY COLUMN name varchar(255) NOT NULL;
