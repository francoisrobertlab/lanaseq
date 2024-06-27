-- // add tags to sample
-- Migration SQL that makes the change goes here.

CREATE TABLE sample_tags (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  sample_id bigint(20) NOT NULL,
  tags varchar(255) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT sampleTagsSample_ibfk FOREIGN KEY (sample_id) REFERENCES sample (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE sample_tags;
