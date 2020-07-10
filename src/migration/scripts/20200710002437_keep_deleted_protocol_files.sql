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
