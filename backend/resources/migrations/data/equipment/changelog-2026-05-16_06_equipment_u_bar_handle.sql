--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment u bar handle
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('U-Bar Handle', 'U-shaped handle for cable tower.')
ON CONFLICT (name) DO NOTHING;
