-- // rename column date to experiment_date in dataset table
-- Migration SQL that makes the change goes here.

ALTER TABLE dataset
CHANGE date experiment_date DATETIME NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
CHANGE experiment_date date DATETIME NOT NULL;
