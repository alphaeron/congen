--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment physioball
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('physioball', 'A ball constructed of soft elastic, and filled with air.');
