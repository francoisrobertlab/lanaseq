-- // add filenames to sample
-- Migration SQL that makes the change goes here.

CREATE TABLE sample_filenames (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  sample_id bigint(20) NOT NULL,
  filenames varchar(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT sampleFilenamesSample_ibfk FOREIGN KEY (sample_id) REFERENCES sample (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE sample_filenames;
