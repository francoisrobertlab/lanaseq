INSERT INTO protocol (id,name,owner_id,creation_date,note)
VALUES ('1', 'FLAG', '3', '2018-10-20 11:28:12', 'First FLAG protocol');
INSERT INTO protocol (id,name,owner_id,creation_date,note)
VALUES ('2', 'BioID', '5', '2018-11-18 9:31:14', null);
INSERT INTO protocol (id,name,owner_id,creation_date,note)
VALUES ('3', 'Histone FLAG', '2', '2018-10-20 9:58:12', null);
INSERT INTO protocol (id,name,owner_id,creation_date,note)
VALUES ('4', 'Deletable protocol', '2', '2018-10-24 9:58:12', null);
ALTER TABLE protocol ALTER COLUMN id RESTART WITH 5;
INSERT INTO protocol_file (id,protocol_id,filename,content,deleted,creation_date)
VALUES (1,1,'FLAG Protocol.docx',FILE_READ('$[project.build.testOutputDirectory]/protocol/FLAG_Protocol.docx'),0,'2018-10-20 11:28:12');
INSERT INTO protocol_file (id,protocol_id,filename,content,deleted,creation_date)
VALUES (2,2,'BioID Protocol.docx',FILE_READ('$[project.build.testOutputDirectory]/protocol/BioID_Protocol.docx'),0,'2018-11-18 9:31:14');
INSERT INTO protocol_file (id,protocol_id,filename,content,deleted,creation_date)
VALUES (3,3,'Histone FLAG Protocol.docx',FILE_READ('$[project.build.testOutputDirectory]/protocol/Histone_FLAG_Protocol.docx'),1,'2018-10-20 9:58:12');
INSERT INTO protocol_file (id,protocol_id,filename,content,deleted,creation_date)
VALUES (4,3,'Histone Protocol.docx',FILE_READ('$[project.build.testOutputDirectory]/protocol/Histone_FLAG_Protocol.docx'),0,'2018-10-21 9:58:12');
INSERT INTO protocol_file (id,protocol_id,filename,content,deleted,creation_date)
VALUES (5,4,'Deletable protocol.docx',FILE_READ('$[project.build.testOutputDirectory]/protocol/Histone_FLAG_Protocol.docx'),0,'2018-10-21 9:58:12');
ALTER TABLE protocol_file ALTER COLUMN id RESTART WITH 6;
INSERT INTO dataset (id,name,experiment_date,owner_id,editable,creation_date,note)
VALUES ('1', 'MNaseseq_IP_polr2a_yFR100_WT_Rappa_FR1-FR2-FR3_20181020', '2018-10-20', '2', 1, '2018-10-20 13:28:12', 'robtools version 2');
INSERT INTO dataset (id,name,experiment_date,owner_id,editable,creation_date,note)
VALUES ('2', 'ChIPseq_Spt16_yFR101_G24D_JS1-JS2_20181022', '2018-10-22', '3', 1, '2018-10-22 9:48:20', null);
INSERT INTO dataset (id,name,experiment_date,owner_id,editable,creation_date,note)
VALUES ('3', '20181112', '2018-11-12', '3', 1, '2018-11-12 11:53:09', null);
INSERT INTO dataset (id,name,experiment_date,owner_id,editable,creation_date,note)
VALUES ('4', 'ChIPseq_IP_yBC102_R103S_BC1-BC2_20181118', '2018-11-18', '5', 1, '2018-11-18 9:31:14', null);
INSERT INTO dataset (id,name,experiment_date,owner_id,editable,creation_date,note)
VALUES ('5', 'ChIPseq_IP_polr2b_yBC103_WT_BC1_20181118', '2018-12-05', '5', 0, '2018-12-05 9:28:23', null);
INSERT INTO dataset (id,name,experiment_date,owner_id,editable,creation_date,note)
VALUES ('6', 'ChIPseq_Spt16_yFR101_G24D_JS1_20181208', '2018-12-08', '3', 1, '2018-12-08 10:28:23', null);
INSERT INTO dataset (id,name,experiment_date,owner_id,editable,creation_date,note)
VALUES ('7', 'ChIPseq_Spt16_yFR101_G24D_JS3_20181211', '2018-12-11', '3', 1, '2018-12-11 10:28:23', null);
INSERT INTO dataset (id,name,experiment_date,owner_id,editable,creation_date,note)
VALUES ('8', '20181211', '2018-10-19', '3', 1, '2018-12-12 10:30:10', null);
ALTER TABLE dataset ALTER COLUMN id RESTART WITH 9;
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,experiment_date,protocol_id,owner_id,editable,creation_date,note)
VALUES (1, 'FR1_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R1_20181020', 'FR1', 'R1', 'MNase-seq', 'IP', 'polr2a', 'yFR100', 'WT', 'Rappa', '2018-10-20', '1', 2, 1, '2018-10-20 13:29:23', 'robtools version 2');
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,experiment_date,protocol_id,owner_id,editable,creation_date,note)
VALUES (2, 'FR2_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R2_20181020', 'FR2', 'R2', 'MNase-seq', 'IP', 'polr2a', 'yFR100', 'WT', 'Rappa', '2018-10-20', '1', 2, 1, '2018-10-20 13:29:53', null);
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,experiment_date,protocol_id,owner_id,editable,creation_date,note)
VALUES (3, 'FR3_MNaseseq_IP_polr2a_yFR100_WT_Rappa_R3_20181020', 'FR3', 'R3', 'MNase-seq', 'IP', 'polr2a', 'yFR100', 'WT', 'Rappa', '2018-10-20', '1', 2, 1, '2018-10-20 13:30:23', null);
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,experiment_date,protocol_id,owner_id,editable,creation_date,note)
VALUES (4, 'JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181022', 'JS1', 'R1', 'ChIP-seq', null, 'Spt16', 'yFR101', 'G24D', null, '2018-10-22', '3', 3, 1, '2018-10-22 9:50:20', null);
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,experiment_date,protocol_id,owner_id,editable,creation_date,note)
VALUES (5, 'JS2_ChIPseq_Spt16_yFR101_G24D_R2_20181022', 'JS2', 'R2', 'ChIP-seq', null, 'Spt16', 'yFR101', 'G24D', null, '2018-10-22', '3', 3, 1, '2018-10-22 9:51:20', null);
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,experiment_date,protocol_id,owner_id,editable,creation_date,note)
VALUES (6, 'BC1_ChIPseq_IP_yBC102_R103S_R1_20181118', 'BC1', 'R1', 'ChIP-seq', 'IP', null, 'yBC102', 'R103S', null, '2018-11-18', '2', 5, 1, '2018-11-18 9:32:14', null);
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,experiment_date,protocol_id,owner_id,editable,creation_date,note)
VALUES (7, 'BC2_ChIPseq_IP_yBC102_R103S_R2_20181118', 'BC2', 'R2', 'ChIP-seq', 'IP', null, 'yBC102', 'R103S', null, '2018-11-18', '2', 5, 1, '2018-11-18 9:33:14', null);
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,experiment_date,protocol_id,owner_id,editable,creation_date,note)
VALUES (8, 'BC1_ChIPseq_IP_polr2b_yBC103_WT_R1_20181118', 'BC1', 'R1', 'ChIP-seq', 'IP', 'polr2b', 'yBC103', 'WT', null, '2018-12-05', '2', 5, 0, '2018-12-05 9:29:23', null);
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,experiment_date,protocol_id,owner_id,editable,creation_date,note)
VALUES (9, 'BC1_ChIPseq_Input_polr2c_yBC201_WT_R1_20181208', 'BC1', 'R1', 'ChIP-seq', 'Input', 'polr2c', 'yBC201', 'WT', null, '2018-12-08', '2', 5, 1, '2018-12-08 9:29:23', null);
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,experiment_date,protocol_id,owner_id,editable,creation_date,note)
VALUES (10, 'JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210', 'JS1', 'R1', 'ChIP-seq', null, 'Spt16', 'yFR101', 'G24D', null, '2018-12-10', '3', 3, 1, '2018-12-10 9:29:23', null);
INSERT INTO sample (id,name,sample_id,replicate,assay,type,target,strain,strain_description,treatment,experiment_date,protocol_id,owner_id,editable,creation_date,note)
VALUES (11, 'JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211', 'JS3', 'R1', 'ChIP-seq', null, 'Spt16', 'yFR101', 'G24D', null, '2018-12-11', '3', 3, 1, '2018-12-11 9:50:20', null);
ALTER TABLE sample ALTER COLUMN id RESTART WITH 12;
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
INSERT INTO dataset_samples (id,dataset_id,samples_order,samples_id)
VALUES (10,7,0,11);
ALTER TABLE dataset_samples ALTER COLUMN id RESTART WITH 11;
INSERT INTO dataset_keywords (id,dataset_id,keywords)
VALUES (1,1,'mnase');
INSERT INTO dataset_keywords (id,dataset_id,keywords)
VALUES (2,1,'ip');
INSERT INTO dataset_keywords (id,dataset_id,keywords)
VALUES (3,2,'chipseq');
INSERT INTO dataset_keywords (id,dataset_id,keywords)
VALUES (4,2,'ip');
INSERT INTO dataset_keywords (id,dataset_id,keywords)
VALUES (5,4,'chipseq');
INSERT INTO dataset_keywords (id,dataset_id,keywords)
VALUES (6,5,'chipseq');
INSERT INTO dataset_keywords (id,dataset_id,keywords)
VALUES (7,2,'G24D');
INSERT INTO dataset_keywords (id,dataset_id,keywords)
VALUES (8,7,'Spt16');
ALTER TABLE dataset_keywords ALTER COLUMN id RESTART WITH 9;
INSERT INTO dataset_filenames (id,dataset_id,filenames)
VALUES (1,2,'OF_20241118_ROB');
ALTER TABLE dataset_filenames ALTER COLUMN id RESTART WITH 2;
INSERT INTO dataset_public_file (id,dataset_id,path,expiry_date)
VALUES (1,6,'ChIPseq_Spt16_yFR101_G24D_JS1_20181208.bw','2025-01-10');
INSERT INTO dataset_public_file (id,dataset_id,path,expiry_date)
VALUES (2,7,'ChIPseq_Spt16_yFR101_G24D_JS3_20181211.bw','2025-01-12');
ALTER TABLE dataset_public_file ALTER COLUMN id RESTART WITH 3;
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (1,1,'mnase');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (2,1,'ip');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (3,2,'mnase');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (4,2,'ip');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (5,3,'mnase');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (6,3,'ip');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (7,4,'chipseq');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (8,4,'ip');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (9,5,'chipseq');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (10,5,'ip');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (11,6,'chipseq');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (12,7,'chipseq');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (13,8,'chipseq');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (14,4,'G24D');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (15,5,'G24D');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (16,10,'Spt16');
INSERT INTO sample_keywords (id,sample_id,keywords)
VALUES (17,11,'Spt16');
ALTER TABLE sample_keywords ALTER COLUMN id RESTART WITH 18;
INSERT INTO sample_filenames (id,sample_id,filenames)
VALUES (1,4,'OF_20241118_ROB_01');
INSERT INTO sample_filenames (id,sample_id,filenames)
VALUES (2,5,'OF_20241118_ROB_02');
ALTER TABLE sample_filenames ALTER COLUMN id RESTART WITH 3;
INSERT INTO sample_public_file (id,sample_id,path,expiry_date)
VALUES (1,10,'JS1_ChIPseq_Spt16_yFR101_G24D_R1_20181210.bw','2025-01-13');
INSERT INTO sample_public_file (id,sample_id,path,expiry_date)
VALUES (2,11,'JS3_ChIPseq_Spt16_yFR101_G24D_R1_20181211.bw','2025-01-15');
ALTER TABLE sample_public_file ALTER COLUMN id RESTART WITH 3;
