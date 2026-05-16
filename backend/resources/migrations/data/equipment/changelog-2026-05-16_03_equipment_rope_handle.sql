--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment rope handle
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('Rope Handle', 'Rope handle for cable tower.')
ON CONFLICT (name) DO NOTHING;
