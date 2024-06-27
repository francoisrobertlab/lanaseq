-- // make sample type customizable
-- Migration SQL that makes the change goes here.

UPDATE sample
SET type = CASE
    WHEN type = "IMMUNO_PRECIPITATION" THEN "IP"
    WHEN type = "INPUT" THEN "Input"
    END;

-- //@UNDO
-- SQL to undo the change goes here.

UPDATE sample
SET type = CASE
    WHEN type = "IP" THEN "IMMUNO_PRECIPITATION"
    WHEN type = "Input" THEN "INPUT"
    END;
