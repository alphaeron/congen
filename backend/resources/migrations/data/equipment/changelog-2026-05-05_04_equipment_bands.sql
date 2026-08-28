--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment bands
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('bands', 'An elastic band that provides varying levels of resistance, depending on the band itself.');
