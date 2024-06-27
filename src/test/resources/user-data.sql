INSERT INTO users (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,locale,creation_date)
VALUES ('1', 'lanaseq@ircm.qc.ca', 'LANAseq Administrator', '$2a$10$JU0aj7Cc/7sWVkFXoHbWTuvVWEAwXFT1EhCX4S6Aa9JfSsKqLP8Tu', 1, '2018-12-13 17:40:12', 1, 1, 1, 0, null, '2018-11-20 09:30:00');
INSERT INTO users (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,locale,creation_date)
VALUES ('2', 'francois.robert@ircm.qc.ca', 'Francois Robert', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 0, '2018-12-17 15:40:12', 1, 1, 0, 0, 'en', '2018-11-20 09:45:21');
INSERT INTO users (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,locale,creation_date)
VALUES ('3', 'jonh.smith@ircm.qc.ca', 'Jonh Smith', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 2, '2018-12-07 15:40:12', 1, 0, 0, 0, null, '2018-11-20 09:48:47');
INSERT INTO users (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,locale,creation_date)
VALUES ('4', 'ava.martin@ircm.qc.ca', 'Ava Martin', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 0, '2018-11-26 15:35:12', 0, 1, 0, 0, null, '2018-11-21 09:31:43');
INSERT INTO users (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,locale,creation_date)
VALUES ('5', 'benoit.coulombe@ircm.qc.ca', 'Benoit Coulombe', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 0, '2018-11-26 15:40:12', 1, 1, 0, 0, null, '2018-11-21 09:36:43');
INSERT INTO users (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,locale,creation_date)
VALUES ('6', 'christian.poitras@ircm.qc.ca', 'Christian Poitras', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 3, '2018-12-17 15:20:12', 1, 0, 0, 1, null, '2018-11-21 10:14:53');
INSERT INTO users (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,locale,creation_date)
VALUES ('7', 'inactive.user@ircm.qc.ca', 'Inactive User', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 3, '2018-12-17 15:20:12', 0, 0, 0, 0, null, '2018-11-21 10:15:47');
INSERT INTO users (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,locale,creation_date)
VALUES ('8', 'virginie.calderon@ircm.qc.ca', 'Virginie Calderon', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 0, '2019-01-04 13:31:24', 1, 0, 1, 0, null, '2018-11-24 10:08:03');
INSERT INTO users (id,email,name,hashed_password,sign_attempts,last_sign_attempt,active,manager,admin,expired_password,locale,creation_date)
VALUES ('9', 'olivia.brown@ircm.qc.ca', 'Olivia Brown', '$2a$10$nGJQSCEj1xlQR/C.nEO8G.GQ4/wUCuGrRKNd0AV3oQp3FwzjtfyAq', 0, '2019-01-14 15:18:24', 1, 0, 0, 0, null, '2019-01-14 15:10:24');
ALTER TABLE users ALTER COLUMN id RESTART WITH 10;
INSERT INTO forgot_password (id,user_id,request_moment,confirm_number,used)
VALUES (7,9,'2014-09-03 11:39:47','803369922',0);
INSERT INTO forgot_password (id,user_id,request_moment,confirm_number,used)
VALUES (8,3,'2013-12-03 11:39:47','B1742054942',1);
INSERT INTO forgot_password (id,user_id,request_moment,confirm_number,used)
VALUES (9,9,CURRENT_TIMESTAMP,'174407008',0);
INSERT INTO forgot_password (id,user_id,request_moment,confirm_number,used)
VALUES (10,9,CURRENT_TIMESTAMP,'460559412',1);
ALTER TABLE forgot_password ALTER COLUMN id RESTART WITH 11;
