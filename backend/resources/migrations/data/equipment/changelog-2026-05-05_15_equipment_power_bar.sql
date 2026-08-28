--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment power bar
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('power bar', 'A universal weightlifting bar that can be used to perform a variety of exercises.');
