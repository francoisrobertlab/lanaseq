-- // rename user table to users
-- Migration SQL that makes the change goes here.

ALTER TABLE forgot_password
DROP CONSTRAINT forgotpasswordUser_ibfk;
ALTER TABLE protocol
DROP CONSTRAINT protocolOwner_ibfk;
ALTER TABLE dataset
DROP CONSTRAINT experimentOwner_ibfk;
ALTER TABLE sample
DROP CONSTRAINT sampleOwner_ibfk;
RENAME TABLE user TO users;
ALTER TABLE forgot_password
ADD CONSTRAINT forgotpasswordUser_ibfk FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE protocol
ADD CONSTRAINT protocolOwner_ibfk FOREIGN KEY (owner_id) REFERENCES users (id) ON UPDATE CASCADE;
ALTER TABLE dataset
ADD CONSTRAINT datasetOwner_ibfk FOREIGN KEY (owner_id) REFERENCES users (id) ON UPDATE CASCADE;
ALTER TABLE sample
ADD CONSTRAINT sampleOwner_ibfk FOREIGN KEY (owner_id) REFERENCES users (id) ON UPDATE CASCADE;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE forgot_password
DROP CONSTRAINT forgotpasswordUser_ibfk;
ALTER TABLE protocol
DROP CONSTRAINT protocolOwner_ibfk;
ALTER TABLE dataset
DROP CONSTRAINT datasetOwner_ibfk;
ALTER TABLE sample
DROP CONSTRAINT sampleOwner_ibfk;
RENAME TABLE users TO user;
ALTER TABLE forgot_password
ADD CONSTRAINT forgotpasswordUser_ibfk FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE protocol
ADD CONSTRAINT protocolOwner_ibfk FOREIGN KEY (owner_id) REFERENCES user (id) ON UPDATE CASCADE;
ALTER TABLE dataset
ADD CONSTRAINT experimentOwner_ibfk FOREIGN KEY (owner_id) REFERENCES user (id) ON UPDATE CASCADE;
ALTER TABLE sample
ADD CONSTRAINT sampleOwner_ibfk FOREIGN KEY (owner_id) REFERENCES user (id) ON UPDATE CASCADE;
