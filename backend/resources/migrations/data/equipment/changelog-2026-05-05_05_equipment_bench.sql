--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment bench
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('bench', 'A bench with a foam pad, commonly used for the bench press or other similar exercises.');
