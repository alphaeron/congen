--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment weight plate
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('weight plate', 'Circular weight plates used for various exercises and resistance training.')
ON CONFLICT (name) DO NOTHING;
