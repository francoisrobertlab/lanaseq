--
--    Copyright 2010-2016 the original author or authors.
--
--    Licensed under the Apache License, Version 2.0 (the "License");
--    you may not use this file except in compliance with the License.
--    You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--    Unless required by applicable law or agreed to in writing, software
--    distributed under the License is distributed on an "AS IS" BASIS,
--    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--    See the License for the specific language governing permissions and
--    limitations under the License.
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
