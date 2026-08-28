--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment sandbag
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('sandbag', 'Heavy bag filled with sand for functional strength training.')
ON CONFLICT (name) DO NOTHING;
