--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment trx
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('trx', 'The TRX System, also known as Total Resistance Exercises, refers to a specialized form of suspension training.');
