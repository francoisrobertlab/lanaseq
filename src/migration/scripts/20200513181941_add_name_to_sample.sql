-- // add name to sample
-- Migration SQL that makes the change goes here.

ALTER TABLE sample
ADD COLUMN name varchar(255) NOT NULL AFTER id;
UPDATE sample
SET name = CONCAT(IFNULL(CONCAT(sample_id, '_'), ''),
           IFNULL(CONCAT(assay, '_'), ''),
           IFNULL(CONCAT(type, '_'), ''),
           IFNULL(CONCAT(target, '_'), ''),
           IFNULL(CONCAT(strain, '_'), ''),
           IFNULL(CONCAT(strain_description, '_'), ''),
           IFNULL(CONCAT(treatment, '_'), ''),
           IFNULL(CONCAT(replicate, '_'), ''),
           DATE_FORMAT(date, '%Y%m%d'));
ALTER TABLE sample
ADD CONSTRAINT name_u UNIQUE (name);

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE sample
DROP INDEX name_u,
DROP COLUMN name;
