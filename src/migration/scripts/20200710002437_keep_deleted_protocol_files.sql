-- // keep deleted protocol files
-- Migration SQL that makes the change goes here.

ALTER TABLE protocol_file
ADD COLUMN deleted tinyint NOT NULL DEFAULT 0 AFTER content,
ADD COLUMN date DATETIME DEFAULT NULL AFTER deleted;
UPDATE protocol_file
JOIN protocol ON protocol.id = protocol_file.protocol_id
SET protocol_file.date = protocol.date;
ALTER TABLE protocol_file
MODIFY COLUMN date DATETIME NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE protocol_file
DROP COLUMN deleted,
DROP COLUMN date;
