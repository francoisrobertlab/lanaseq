-- // rename sample's name to sample_id
-- Migration SQL that makes the change goes here.

ALTER TABLE sample
CHANGE name sample_id varchar(255) NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE sample
CHANGE sample_id name varchar(255) NOT NULL;
