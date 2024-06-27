-- // rename column date to experiment_date in sample table
-- Migration SQL that makes the change goes here.

ALTER TABLE sample
CHANGE date experiment_date DATETIME NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE sample
CHANGE experiment_date date DATETIME NOT NULL;
