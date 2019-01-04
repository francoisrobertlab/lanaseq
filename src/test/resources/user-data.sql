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

INSERT INTO laboratory (id,name)
VALUES ('1', 'Informatics');
INSERT INTO laboratory (id,name)
VALUES ('2', 'Chromatin and Genomic Expression');
INSERT INTO laboratory (id,name)
VALUES ('3', 'Translational Proteomics Research Unit');
INSERT INTO user (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,laboratory_id,locale)
VALUES ('1', 'lana@ircm.qc.ca', 'Lana Administrator', '$2a$10$JU0aj7Cc/7sWVkFXoHbWTuvVWEAwXFT1EhCX4S6Aa9JfSsKqLP8Tu', 1, '2018-12-13 17:40:12', 1, 1, 1, 0, 1, null);
INSERT INTO user (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,laboratory_id,locale)
VALUES ('2', 'francois.robert@ircm.qc.ca', 'Francois Robert', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 0, '2018-12-17 15:40:12', 1, 1, 0, 0, 2, 'en');
INSERT INTO user (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,laboratory_id,locale)
VALUES ('3', 'jonh.smith@ircm.qc.ca', 'Jonh Smith', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 2, '2018-12-07 15:40:12', 1, 0, 0, 0, 2, null);
INSERT INTO user (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,laboratory_id,locale)
VALUES ('4', 'benoit.coulombe@ircm.qc.ca', 'Benoit Coulombe', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 0, '2018-11-26 15:40:12', 1, 1, 0, 0, 3, null);
INSERT INTO user (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,laboratory_id,locale)
VALUES ('5', 'christian.poitras@ircm.qc.ca', 'Christian Poitras', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 3, '2018-12-17 15:20:12', 1, 0, 0, 1, 3, null);
INSERT INTO user (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,laboratory_id,locale)
VALUES ('6', 'inactive.user@ircm.qc.ca', 'Inactive User', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 3, '2018-12-17 15:20:12', 0, 0, 0, 0, 3, null);
INSERT INTO user (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,laboratory_id,locale)
VALUES ('7', 'virginie.calderon@ircm.qc.ca', 'Virginie Calderon', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 0, '2019-01-04 13:31:24', 1, 0, 1, 0, 1, null);
