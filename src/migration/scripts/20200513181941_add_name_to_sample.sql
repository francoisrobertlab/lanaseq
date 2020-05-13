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

-- // add name to sample
-- Migration SQL that makes the change goes here.

ALTER TABLE sample
ADD COLUMN name varchar(255) NOT NULL AFTER id;
UPDATE sample
SET name = CONCAT(IFNULL(CONCAT(sample_id, '_'), ''),
           IFNULL(CONCAT(assay, '_'), ''),
           IFNULL(CONCAT(type, '_'), ''),
           IFNULL(CONCAT(target, '_'), ''),
           IFNULL(CONCAT(strain, '_'), ''),
           IFNULL(CONCAT(strain_description, '_'), ''),
           IFNULL(CONCAT(treatment, '_'), ''),
           IFNULL(CONCAT(replicate, '_'), ''),
           DATE_FORMAT(date, '%Y%m%d'));
ALTER TABLE sample
ADD CONSTRAINT name_u UNIQUE (name);

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE sample
DROP INDEX name_u,
DROP COLUMN name;
