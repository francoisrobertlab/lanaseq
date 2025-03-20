-- // add public files to dataset
-- Migration SQL that makes the change goes here.

CREATE TABLE dataset_public_file (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  dataset_id bigint(20) NOT NULL,
  path varchar(255) NOT NULL,
  expiry_date DATE NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT datasetPublicFile_ibfk FOREIGN KEY (dataset_id) REFERENCES dataset (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE dataset_public_file;
