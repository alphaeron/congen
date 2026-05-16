--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment triangle bar handle
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('Triangle Bar Handle', 'Triangle-shaped handle for cable tower.')
ON CONFLICT (name) DO NOTHING;
