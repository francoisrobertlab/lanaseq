-- // rename column date to creation_date in users table
-- Migration SQL that makes the change goes here.

ALTER TABLE users
CHANGE date creation_date DATETIME NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE users
CHANGE creation_date date DATETIME NOT NULL;
