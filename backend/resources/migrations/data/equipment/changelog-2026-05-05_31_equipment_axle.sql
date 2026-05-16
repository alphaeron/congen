--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment axle
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('axle', 'Thick bar used for strongman exercises, requiring more grip strength than standard bars.')
ON CONFLICT (name) DO NOTHING;
