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
