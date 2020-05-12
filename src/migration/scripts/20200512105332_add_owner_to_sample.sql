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

-- // add owner to sample
-- Migration SQL that makes the change goes here.

ALTER TABLE sample
ADD COLUMN owner_id bigint(20) AFTER samples_order,
ADD CONSTRAINT sampleOwner_ibfk FOREIGN KEY (owner_id) REFERENCES user (id) ON UPDATE CASCADE;
UPDATE sample
JOIN dataset ON dataset.id = sample.dataset_id
SET sample.owner_id = dataset.owner_id;
ALTER TABLE sample
MODIFY COLUMN owner_id bigint(20) NOT NULL;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE sample
DROP FOREIGN KEY sampleOwner_ibfk;
ALTER TABLE sample
DROP COLUMN owner_id;
