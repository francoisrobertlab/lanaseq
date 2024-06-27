-- // rename admin to lanaseq
-- Migration SQL that makes the change goes here.

UPDATE user
SET email = 'lanaseq@ircm.qc.ca',
    name = 'LANAseq Administrator'
WHERE id = 1;

-- //@UNDO
-- SQL to undo the change goes here.

UPDATE user
SET email = 'lana@ircm.qc.ca',
    name = 'LANAseq Administrator'
WHERE id = 1;
