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
