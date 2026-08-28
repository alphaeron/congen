--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment straight bar handle
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('Straight Bar Handle', 'Straight bar handle for cable tower.')
ON CONFLICT (name) DO NOTHING;
