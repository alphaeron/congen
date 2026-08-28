--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment med ball
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('med ball', 'A medicine ball is a weighted ball often used for rehabilitation and strength training.');
