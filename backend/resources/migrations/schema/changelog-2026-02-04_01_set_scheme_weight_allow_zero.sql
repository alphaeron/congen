--liquibase formatted sql

--changeset congen:set_scheme_weight_allow_zero_1 labels:prod,test
--comment: Allow target_weight >= 0 for bodyweight exercises (was > 0).
ALTER TABLE set_scheme DROP CONSTRAINT IF EXISTS set_scheme_target_weight_check;
ALTER TABLE set_scheme ADD CONSTRAINT set_scheme_target_weight_check CHECK (target_weight >= 0);

--changeset congen:set_scheme_weight_allow_zero_2 labels:prod,test
--comment: Allow performed_weight >= 0 for bodyweight exercises (was > 0).
ALTER TABLE set_scheme DROP CONSTRAINT IF EXISTS set_scheme_performed_weight_check;
ALTER TABLE set_scheme ADD CONSTRAINT set_scheme_performed_weight_check CHECK (performed_weight >= 0);
