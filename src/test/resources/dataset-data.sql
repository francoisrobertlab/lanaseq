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

INSERT INTO protocol (id,name,owner_id,date)
VALUES ('1', 'FLAG', '3', '2018-10-20 11:28:12');
INSERT INTO protocol (id,name,owner_id,date)
VALUES ('2', 'BioID', '5', '2018-11-18 9:31:14');
INSERT INTO protocol (id,name,owner_id,date)
VALUES ('3', 'Histone FLAG', '2', '2018-10-20 9:58:12');
INSERT INTO protocol_file (id,files_id,filename,content)
VALUES (1,1,'FLAG Protocol.docx',FILE_READ('$[project.build.testOutputDirectory]/protocol/FLAG_Protocol.docx'));
INSERT INTO protocol_file (id,files_id,filename,content)
VALUES (2,2,'BioID Protocol.docx',FILE_READ('$[project.build.testOutputDirectory]/protocol/BioID_Protocol.docx'));
INSERT INTO protocol_file (id,files_id,filename,content)
VALUES (3,3,'Histone FLAG Protocol.docx',FILE_READ('$[project.build.testOutputDirectory]/protocol/Histone_FLAG_Protocol.docx'));
INSERT INTO dataset (id,name,project,assay,type,target,protocol_id,owner_id,date)
VALUES ('1', 'POLR2A DNA location', 'polymerase', 'MNASE_SEQ', 'IMMUNO_PRECIPITATION', 'polr2a', '1', '2', '2018-10-20 13:28:12');
INSERT INTO dataset (id,name,project,assay,type,target,protocol_id,owner_id,date)
VALUES ('2', 'Histone location', 'histone', null, null, 'Spt16', '3', '3', '2018-10-22 9:48:20');
INSERT INTO dataset (id,name,project,assay,type,target,protocol_id,owner_id,date)
VALUES ('3', 'POLR1A location', 'polymerase', 'MNASE_SEQ', 'INPUT', 'polr1a', '1', '3', '2018-11-12 11:53:09');
INSERT INTO dataset (id,name,project,assay,type,target,protocol_id,owner_id,date)
VALUES ('4', 'POLR2A', 'polymerase 2', 'CHIP_SEQ', 'IMMUNO_PRECIPITATION', null, '2', '5', '2018-11-18 9:31:14');
INSERT INTO dataset (id,name,project,assay,type,target,protocol_id,owner_id,date)
VALUES ('5', 'POLR2B', 'polymerase 2', 'CHIP_SEQ', 'IMMUNO_PRECIPITATION', 'polr2b', '2', '5', '2018-12-05 9:28:23');
