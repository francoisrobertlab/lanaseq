--
-- Copyright (c) 2018 Institut de recherches cliniques de Montreal (IRCM)
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU Affero General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU Affero General Public License
-- along with this program.  If not, see <http://www.gnu.org/licenses/>.
--

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
