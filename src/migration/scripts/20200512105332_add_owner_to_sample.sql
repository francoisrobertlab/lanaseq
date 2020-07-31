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
