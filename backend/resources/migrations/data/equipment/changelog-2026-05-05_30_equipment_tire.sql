--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment tire
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('tire', 'Heavy tire used for flipping exercises and strongman training.')
ON CONFLICT (name) DO NOTHING;
