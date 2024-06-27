-- // rename experiment to dataset
-- Migration SQL that makes the change goes here.

RENAME TABLE experiment TO dataset;

-- //@UNDO
-- SQL to undo the change goes here.

RENAME TABLE dataset TO experiment;
