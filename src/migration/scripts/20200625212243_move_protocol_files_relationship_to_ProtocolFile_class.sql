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

-- // move protocol files relationship to ProtocolFile class
-- Migration SQL that makes the change goes here.

ALTER TABLE protocol_file
DROP FOREIGN KEY protocolfileProtocol_ibfk;
ALTER TABLE protocol_file
CHANGE files_id protocol_id bigint(20) DEFAULT NULL;
ALTER TABLE protocol_file
ADD CONSTRAINT protocolfileProtocol_ibfk FOREIGN KEY (protocol_id) REFERENCES protocol (id) ON DELETE CASCADE ON UPDATE CASCADE;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE protocol_file
DROP FOREIGN KEY protocolfileProtocol_ibfk;
ALTER TABLE protocol_file
CHANGE protocol_id files_id bigint(20) DEFAULT NULL;
ALTER TABLE protocol_file
ADD CONSTRAINT protocolfileProtocol_ibfk FOREIGN KEY (files_id) REFERENCES protocol (id) ON DELETE CASCADE ON UPDATE CASCADE;
