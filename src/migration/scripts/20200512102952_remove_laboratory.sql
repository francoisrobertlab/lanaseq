-- // remove laboratory
-- Migration SQL that makes the change goes here.

ALTER TABLE user
DROP FOREIGN KEY userLaboratory_ibfk;
ALTER TABLE user
DROP COLUMN laboratory_id;
DROP TABLE laboratory;

-- //@UNDO
-- SQL to undo the change goes here.

CREATE TABLE laboratory (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id)
);
INSERT INTO laboratory (id,name)
VALUES ('1', 'Informatics');
ALTER TABLE user
ADD COLUMN laboratory_id bigint(20),
ADD CONSTRAINT userLaboratory_ibfk FOREIGN KEY (laboratory_id) REFERENCES laboratory (id) ON UPDATE CASCADE;
UPDATE user
SET laboratory_id = 1;
ALTER TABLE user
MODIFY COLUMN laboratory_id bigint(20) NOT NULL;
