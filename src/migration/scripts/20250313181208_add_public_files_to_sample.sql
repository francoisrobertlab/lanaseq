-- // add public files to sample
-- Migration SQL that makes the change goes here.

CREATE TABLE sample_public_file (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  sample_id bigint(20) NOT NULL,
  path varchar(255) NOT NULL,
  expiry_date DATE NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT samplePublicFile_ibfk FOREIGN KEY (sample_id) REFERENCES sample (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE sample_public_file;
