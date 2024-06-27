-- // create admin user
-- Migration SQL that makes the change goes here.

INSERT INTO user (id, email, name, hashed_password, active, admin, expired_password)
VALUES (1, 'lana@ircm.qc.ca', 'Administrator', '$2a$10$lWDef/shwP7Tl2QAg8yDm.BITRMFWYKCZmG.MZ20cdLaf1UrxkDRO', 1, 1, 1);

-- //@UNDO
-- SQL to undo the change goes here.

DELETE FROM user
WHERE id = 1;
