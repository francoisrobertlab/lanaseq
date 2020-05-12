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

-- // remove laboratory
-- Migration SQL that makes the change goes here.

ALTER TABLE user
DROP FOREIGN KEY userLaboratory_ibfk;
ALTER TABLE user
DROP COLUMN laboratory_id;
DROP TABLE laboratory;

-- //@UNDO
-- SQL to undo the change goes here.

CREATE TABLE laboratory (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id)
);
INSERT INTO laboratory (id,name)
VALUES ('1', 'Informatics');
ALTER TABLE user
ADD COLUMN laboratory_id bigint(20),
ADD CONSTRAINT userLaboratory_ibfk FOREIGN KEY (laboratory_id) REFERENCES laboratory (id) ON UPDATE CASCADE;
UPDATE user
SET laboratory_id = 1;
ALTER TABLE user
MODIFY COLUMN laboratory_id bigint(20) NOT NULL;
