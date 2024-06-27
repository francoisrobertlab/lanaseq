-- // all users must have a laboratory
-- Migration SQL that makes the change goes here.

UPDATE laboratory
SET id = id + 1
ORDER BY id DESC;
INSERT INTO laboratory (id,name)
VALUES ('1', 'Informatics');
UPDATE user
SET laboratory_id = 1
WHERE laboratory_id IS NULL;
ALTER TABLE user
CHANGE laboratory_id laboratory_id bigint(20) NOT NULL; 


-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE user
CHANGE laboratory_id laboratory_id bigint(20); 
UPDATE user
SET laboratory_id = NULL
WHERE laboratory_id = 1;
DELETE FROM laboratory
WHERE id = 1;
UPDATE laboratory
SET id = id - 1
ORDER BY id ASC;
