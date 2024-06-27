-- // make sample assay customizable
-- Migration SQL that makes the change goes here.

UPDATE sample
SET assay = CASE
    WHEN assay = "CHEC_SEQ" THEN "ChEC-seq"
    WHEN assay = "CHEM_MAP" THEN "Chem-Map"
    WHEN assay = "CHIP_EXO" THEN "ChIP-exo"
    WHEN assay = "CHIP_SEQ" THEN "ChIP-seq"
    WHEN assay = "MICRO_C" THEN "Micro-C"
    WHEN assay = "MNASE_SEQ" THEN "MNase-seq"
    WHEN assay = "NET_SEQ" THEN "NET-seq"
    WHEN assay = "RNA_SEQ" THEN "RNA-seq"
    END;

-- //@UNDO
-- SQL to undo the change goes here.

UPDATE sample
SET assay = CASE
    WHEN assay = "ChEC-seq" THEN "CHEC_SEQ"
    WHEN assay = "Chem-Map" THEN "CHEM_MAP"
    WHEN assay = "ChIP-exo" THEN "CHIP_EXO"
    WHEN assay = "ChIP-seq" THEN "CHIP_SEQ"
    WHEN assay = "Micro-C" THEN "MICRO_C"
    WHEN assay = "MNase-seq" THEN "MNASE_SEQ"
    WHEN assay = "NET-seq" THEN "NET_SEQ"
    WHEN assay = "RNA-seq" THEN "RNA_SEQ"
    END;
