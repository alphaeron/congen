--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment v bar handle
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('V-Bar Handle', 'V-Bar handle for cable tower.')
ON CONFLICT (name) DO NOTHING;
