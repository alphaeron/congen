--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Iron Neck
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Iron Neck', 'Iron neck tool.', 'core', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Iron Neck', 'neck');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Iron Neck', 'iron neck');
