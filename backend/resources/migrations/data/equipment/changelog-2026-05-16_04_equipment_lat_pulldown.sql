--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment lat pulldown
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('Lat Pulldown', 'A lat pulldown machine or configuration/attachment.')
ON CONFLICT (name) DO NOTHING;
