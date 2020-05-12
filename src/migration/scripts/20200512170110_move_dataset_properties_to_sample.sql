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

-- // move dataset properties to sample
-- Migration SQL that makes the change goes here.

ALTER TABLE sample
ADD COLUMN assay varchar(255) AFTER replicate,
ADD COLUMN type varchar(255) AFTER assay,
ADD COLUMN target varchar(255) AFTER type,
ADD COLUMN strain varchar(255) AFTER target,
ADD COLUMN strain_description varchar(255) AFTER strain,
ADD COLUMN treatment varchar(255) AFTER strain_description,
ADD COLUMN protocol_id bigint(20) AFTER treatment,
ADD CONSTRAINT sampleProtocol_ibfk FOREIGN KEY (protocol_id) REFERENCES protocol (id) ON UPDATE CASCADE;
UPDATE sample
JOIN dataset_samples ON dataset_samples.samples_id = sample.id
JOIN dataset ON dataset.id = dataset_samples.dataset_id
SET sample.assay = dataset.assay,
  sample.type = dataset.type,
  sample.target = dataset.target,
  sample.strain = dataset.strain,
  sample.strain_description = dataset.strain_description,
  sample.treatment = dataset.treatment,
  sample.protocol_id = dataset.protocol_id;
ALTER TABLE sample
MODIFY COLUMN protocol_id bigint(20) NOT NULL;
ALTER TABLE dataset
DROP FOREIGN KEY experimentProtocol_ibfk;
ALTER TABLE dataset
DROP COLUMN assay,
DROP COLUMN type,
DROP COLUMN target,
DROP COLUMN strain,
DROP COLUMN strain_description,
DROP COLUMN treatment,
DROP COLUMN protocol_id;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE dataset
ADD COLUMN assay varchar(255) AFTER project,
ADD COLUMN type varchar(255) AFTER assay,
ADD COLUMN target varchar(255) AFTER type,
ADD COLUMN strain varchar(255) AFTER target,
ADD COLUMN strain_description varchar(255) AFTER strain,
ADD COLUMN treatment varchar(255) AFTER strain_description,
ADD COLUMN protocol_id bigint(20) AFTER treatment,
ADD CONSTRAINT experimentProtocol_ibfk FOREIGN KEY (protocol_id) REFERENCES protocol (id) ON UPDATE CASCADE;
UPDATE dataset
JOIN dataset_samples ON dataset_samples.dataset_id = dataset.id
JOIN sample ON sample.id = dataset_samples.samples_id
SET dataset.assay = sample.assay,
  dataset.type = sample.type,
  dataset.target = sample.target,
  dataset.strain = sample.strain,
  dataset.strain_description = sample.strain_description,
  dataset.treatment = sample.treatment,
  dataset.protocol_id = sample.protocol_id;
ALTER TABLE dataset
MODIFY COLUMN protocol_id bigint(20) NOT NULL;
ALTER TABLE sample
DROP FOREIGN KEY sampleProtocol_ibfk;
ALTER TABLE sample
DROP COLUMN assay,
DROP COLUMN type,
DROP COLUMN target,
DROP COLUMN strain,
DROP COLUMN strain_description,
DROP COLUMN treatment,
DROP COLUMN protocol_id;
