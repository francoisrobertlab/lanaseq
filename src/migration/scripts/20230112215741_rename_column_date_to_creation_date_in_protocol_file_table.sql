-- // rename column date to creation_date in protocol_file table
-- Migration SQL that makes the change goes here.

ALTER TABLE protocol_file
CHANGE date creation_date DATETIME NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE protocol_file
CHANGE creation_date date DATETIME NOT NULL;
