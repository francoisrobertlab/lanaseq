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

-- // create join table for dataset's samples
-- Migration SQL that makes the change goes here.

CREATE TABLE dataset_samples (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  dataset_id bigint(20) NOT NULL,
  samples_order int,
  samples_id bigint(20) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT datasetSamplesDataset_ibfk FOREIGN KEY (dataset_id) REFERENCES dataset (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT datasetSamplesSample_ibfk FOREIGN KEY (samples_id) REFERENCES sample (id) ON DELETE CASCADE ON UPDATE CASCADE
);
INSERT INTO dataset_samples (dataset_id, samples_order, samples_id)
SELECT dataset_id, samples_order, id
FROM sample;
ALTER TABLE sample
DROP FOREIGN KEY sampleDataset_ibfk;
ALTER TABLE sample
DROP COLUMN samples_order,
DROP COLUMN dataset_id;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE sample
ADD COLUMN samples_order int AFTER replicate,
ADD COLUMN dataset_id bigint(20) AFTER samples_order,
ADD CONSTRAINT sampleDataset_ibfk FOREIGN KEY (dataset_id) REFERENCES dataset (id) ON DELETE CASCADE ON UPDATE CASCADE;
UPDATE sample
JOIN dataset_samples ON dataset_samples.samples_id = sample.id
SET sample.dataset_id = dataset_samples.dataset_id,
  sample.samples_order = dataset_samples.samples_order;
ALTER TABLE sample
MODIFY COLUMN dataset_id bigint(20) NOT NULL;
DROP TABLE dataset_samples;
