--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment curl bar handle
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('Curl Bar Handle', 'Curl bar handle for cable tower.')
ON CONFLICT (name) DO NOTHING;
