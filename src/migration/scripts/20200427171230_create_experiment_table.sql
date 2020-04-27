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

-- // create experiment table
-- Migration SQL that makes the change goes here.

CREATE TABLE experiment (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  protocol_id bigint(20) NOT NULL,
  owner_id bigint(20) NOT NULL,
  date DATETIME NOT NULL,
  PRIMARY KEY (id),
  KEY protocol (protocol_id),
  KEY owner (owner_id),
  CONSTRAINT experimentProtocol_ibfk FOREIGN KEY (protocol_id) REFERENCES protocol (id) ON UPDATE CASCADE,
  CONSTRAINT experimentOwner_ibfk FOREIGN KEY (owner_id) REFERENCES user (id) ON UPDATE CASCADE
);


-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE experiment;
