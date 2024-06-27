-- // add assay field to dataset
-- Migration SQL that makes the change goes here.

ALTER TABLE dataset
ADD COLUMN assay varchar(255) AFTER project;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
DROP COLUMN assay;
