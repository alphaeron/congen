--liquibase formatted sql

--changeset alphaeron:10 labels:prod,test
--comment: Add band weight field to set_scheme table for Dynamic Effort exercises.

-- Add band weight field to set_scheme table
ALTER TABLE set_scheme 
ADD COLUMN band_weight_lbs NUMERIC(6,2) CHECK (band_weight_lbs >= 0);

-- Add index for band-related queries
CREATE INDEX idx_set_scheme_band_weight_lbs ON set_scheme(band_weight_lbs); 