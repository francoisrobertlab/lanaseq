-- // add owner to sample
-- Migration SQL that makes the change goes here.

ALTER TABLE sample
ADD COLUMN owner_id bigint(20) AFTER samples_order,
ADD CONSTRAINT sampleOwner_ibfk FOREIGN KEY (owner_id) REFERENCES user (id) ON UPDATE CASCADE;
UPDATE sample
JOIN dataset ON dataset.id = sample.dataset_id
SET sample.owner_id = dataset.owner_id;
ALTER TABLE sample
MODIFY COLUMN owner_id bigint(20) NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE sample
DROP FOREIGN KEY sampleOwner_ibfk;
ALTER TABLE sample
DROP COLUMN owner_id;
