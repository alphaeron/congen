--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise I Position Banded Tantrum
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('I Position Banded Tantrum', 'With your arms on a band in front of you in an I position as you lay on your stomach, hit the band as hard as you can, throwing your arms in the I position against the band.', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('I Position Banded Tantrum', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('I Position Banded Tantrum', 'rotator cuff');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('I Position Banded Tantrum', 'power rack');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('I Position Banded Tantrum', 'bands');
