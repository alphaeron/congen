--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment dumbbells
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('dumbbells', 'Dumbbells are small bars that fit in your hand and have equal weights on either side.');
