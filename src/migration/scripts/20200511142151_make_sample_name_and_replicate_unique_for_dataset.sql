-- // make sample name and replicate unique for dataset
-- Migration SQL that makes the change goes here.

ALTER TABLE sample
ADD CONSTRAINT name_u UNIQUE (name, dataset_id),
ADD CONSTRAINT replicate_u UNIQUE (replicate, dataset_id);

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE sample
DROP INDEX name_u,
DROP INDEX replicate_u;
