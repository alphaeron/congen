--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment physioball
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('physioball', 'A ball constructed of soft elastic, and filled with air.');
