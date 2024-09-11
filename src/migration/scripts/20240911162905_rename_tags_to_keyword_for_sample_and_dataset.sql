-- // rename tags to keyword for sample and dataset
-- Migration SQL that makes the change goes here.

ALTER TABLE sample_tags
DROP CONSTRAINT sampleTagsSample_ibfk;
RENAME TABLE sample_tags TO sample_keywords;
ALTER TABLE sample_keywords
RENAME COLUMN tags TO keywords,
ADD CONSTRAINT sampleKeywordsSample_ibfk FOREIGN KEY (sample_id) REFERENCES sample (id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE dataset_tags
DROP CONSTRAINT datasetTagsDataset_ibfk;
RENAME TABLE dataset_tags TO dataset_keywords;
ALTER TABLE dataset_keywords
RENAME COLUMN tags TO keywords,
ADD CONSTRAINT datasetKeywordsDataset_ibfk FOREIGN KEY (dataset_id) REFERENCES dataset (id) ON DELETE CASCADE ON UPDATE CASCADE;

-- //@UNDO
-- SQL to undo the change goes here.

ALTER TABLE sample_keywords
DROP CONSTRAINT sampleKeywordsSample_ibfk;
RENAME TABLE sample_keywords TO sample_tags;
ALTER TABLE sample_tags
RENAME COLUMN keywords TO tags,
ADD CONSTRAINT sampleTagsSample_ibfk FOREIGN KEY (sample_id) REFERENCES sample (id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE dataset_keywords
DROP CONSTRAINT datasetKeywordsDataset_ibfk;
RENAME TABLE dataset_keywords TO dataset_tags;
ALTER TABLE dataset_tags
RENAME COLUMN keywords TO tags,
ADD CONSTRAINT datasetTagsDataset_ibfk FOREIGN KEY (dataset_id) REFERENCES dataset (id) ON DELETE CASCADE ON UPDATE CASCADE;
