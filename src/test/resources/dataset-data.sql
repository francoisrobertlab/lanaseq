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
INSERT INTO protocol_file (id,protocol_id,filename,content,deleted,date)
VALUES (1,1,'FLAG Protocol.docx',FILE_READ('$[project.build.testOutputDirectory]/protocol/FLAG_Protocol.docx'),0,'2018-10-20 11:28:12');
INSERT INTO protocol_file (id,protocol_id,filename,content,deleted,date)
VALUES (2,2,'BioID Protocol.docx',FILE_READ('$[project.build.testOutputDirectory]/protocol/BioID_Protocol.docx'),0,'2018-11-18 9:31:14');
INSERT INTO protocol_file (id,protocol_id,filename,content,deleted,date)
VALUES (3,3,'Histone FLAG Protocol.docx',FILE_READ('$[project.build.testOutputDirectory]/protocol/Histone_FLAG_Protocol.docx'),1,'2018-10-20 9:58:12');
INSERT INTO protocol_file (id,protocol_id,filename,content,deleted,date)
VALUES (4,3,'Histone Protocol.docx',FILE_READ('$[project.build.testOutputDirectory]/protocol/Histone_FLAG_Protocol.docx'),0,'2018-10-21 9:58:12');
INSERT INTO dataset (id,name,date,owner_id,editable,creation_date)
VALUES ('1', 'MNaseSeq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020', '2018-10-20', '2', 1, '2018-10-20 13:28:12');
INSERT INTO dataset (id,name,date,owner_id,editable,creation_date)
VALUES ('2', 'ChIPSeq_Spt16_yFR101_G24D_JS1-JS2_20181022', '2018-10-22', '3', 1, '2018-10-22 9:48:20');
INSERT INTO dataset (id,name,date,owner_id,editable,creation_date)
VALUES ('3', '20181112', '2018-11-12', '3', 1, '2018-11-12 11:53:09');
INSERT INTO dataset (id,name,date,owner_id,editable,creation_date)
VALUES ('4', 'ChIPSeq_IP_yBC102_R103S_BC1-BC2_20181118', '2018-11-18', '5', 1, '2018-11-18 9:31:14');
INSERT INTO dataset (id,name,date,owner_id,editable,creation_date)
VALUES ('5', 'ChIPSeq_IP_polr2b_yBC103_WT_BC1_20181118', '2018-12-05', '5', 0, '2018-12-05 9:28:23');
INSERT INTO dataset (id,name,date,owner_id,editable,creation_date)
VALUES ('6', 'ChIPSeq_Spt16_yFR101_G24D_JS1_20181208', '2018-12-08', '3', 1, '2018-12-08 10:28:23');
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,date,protocol_id,owner_id,editable,creation_date)
VALUES (1, 'FR1_MNaseSeq_IP_polr2a_yFR100_WT_Rappa_R1_20181020', 'FR1', 'R1', 'MNASE_SEQ', 'IMMUNO_PRECIPITATION', 'polr2a', 'yFR100', 'WT', 'Rappa', '2018-10-20', '1', 2, 1, '2018-10-20 13:29:23');
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,date,protocol_id,owner_id,editable,creation_date)
VALUES (2, 'FR2_MNaseSeq_IP_polr2a_yFR100_WT_Rappa_R2_20181020', 'FR2', 'R2', 'MNASE_SEQ', 'IMMUNO_PRECIPITATION', 'polr2a', 'yFR100', 'WT', 'Rappa', '2018-10-20', '1', 2, 1, '2018-10-20 13:29:53');
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,date,protocol_id,owner_id,editable,creation_date)
VALUES (3, 'FR3_MNaseSeq_IP_polr2a_yFR100_WT_Rappa_R3_20181020', 'FR3', 'R3', 'MNASE_SEQ', 'IMMUNO_PRECIPITATION', 'polr2a', 'yFR100', 'WT', 'Rappa', '2018-10-20', '1', 2, 1, '2018-10-20 13:30:23');
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,date,protocol_id,owner_id,editable,creation_date)
VALUES (4, 'JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181022', 'JS1', 'R1', 'CHIP_SEQ', null, 'Spt16', 'yFR101', 'G24D', null, '2018-10-22', '3', 3, 1, '2018-10-22 9:50:20');
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,date,protocol_id,owner_id,editable,creation_date)
VALUES (5, 'JS2_ChIPSeq_Spt16_yFR101_G24D_R2_20181022', 'JS2', 'R2', 'CHIP_SEQ', null, 'Spt16', 'yFR101', 'G24D', null, '2018-10-22', '3', 3, 1, '2018-10-22 9:51:20');
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,date,protocol_id,owner_id,editable,creation_date)
VALUES (6, 'BC1_ChIPSeq_IP_yBC102_R103S_R1_20181118', 'BC1', 'R1', 'CHIP_SEQ', 'IMMUNO_PRECIPITATION', null, 'yBC102', 'R103S', null, '2018-11-18', '2', 5, 1, '2018-11-18 9:32:14');
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,date,protocol_id,owner_id,editable,creation_date)
VALUES (7, 'BC2_ChIPSeq_IP_yBC102_R103S_R2_20181118', 'BC2', 'R2', 'CHIP_SEQ', 'IMMUNO_PRECIPITATION', null, 'yBC102', 'R103S', null, '2018-11-18', '2', 5, 1, '2018-11-18 9:33:14');
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,date,protocol_id,owner_id,editable,creation_date)
VALUES (8, 'BC1_ChIPSeq_IP_polr2b_yBC103_WT_R1_20181118', 'BC1', 'R1', 'CHIP_SEQ', 'IMMUNO_PRECIPITATION', 'polr2b', 'yBC103', 'WT', null, '2018-12-05', '2', 5, 0, '2018-12-05 9:29:23');
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,date,protocol_id,owner_id,editable,creation_date)
VALUES (9, 'BC1_ChIPSeq_Input_polr2c_yBC201_WT_R1_20181208', 'BC1', 'R1', 'CHIP_SEQ', 'INPUT', 'polr2c', 'yBC201', 'WT', null, '2018-12-08', '2', 5, 1, '2018-12-08 9:29:23');
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,date,protocol_id,owner_id,editable,creation_date)
VALUES (10, 'JS1_ChIPSeq_Spt16_yFR101_G24D_R1_20181210', 'JS1', 'R1', 'CHIP_SEQ', null, 'Spt16', 'yFR101', 'G24D', null, '2018-12-10', '3', 3, 1, '2018-12-10 9:29:23');
INSERT INTO dataset_samples (id,dataset_id,samples_order,samples_id)
VALUES (1,1,0,1);
INSERT INTO dataset_samples (id,dataset_id,samples_order,samples_id)
VALUES (2,1,1,2);
INSERT INTO dataset_samples (id,dataset_id,samples_order,samples_id)
VALUES (3,1,2,3);
INSERT INTO dataset_samples (id,dataset_id,samples_order,samples_id)
VALUES (4,2,0,4);
INSERT INTO dataset_samples (id,dataset_id,samples_order,samples_id)
VALUES (5,2,1,5);
INSERT INTO dataset_samples (id,dataset_id,samples_order,samples_id)
VALUES (6,4,0,6);
INSERT INTO dataset_samples (id,dataset_id,samples_order,samples_id)
VALUES (7,4,1,7);
INSERT INTO dataset_samples (id,dataset_id,samples_order,samples_id)
VALUES (8,5,0,8);
INSERT INTO dataset_samples (id,dataset_id,samples_order,samples_id)
VALUES (9,6,0,4);
INSERT INTO dataset_tags (id,dataset_id,tags)
VALUES (1,1,'mnase');
INSERT INTO dataset_tags (id,dataset_id,tags)
VALUES (2,1,'ip');
INSERT INTO dataset_tags (id,dataset_id,tags)
VALUES (3,2,'chipseq');
INSERT INTO dataset_tags (id,dataset_id,tags)
VALUES (4,2,'ip');
INSERT INTO dataset_tags (id,dataset_id,tags)
VALUES (5,4,'chipseq');
INSERT INTO dataset_tags (id,dataset_id,tags)
VALUES (6,5,'chipseq');
INSERT INTO dataset_tags (id,dataset_id,tags)
VALUES (7,2,'G24D');
