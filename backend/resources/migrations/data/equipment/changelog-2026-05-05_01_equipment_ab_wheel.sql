--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment ab wheel
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('ab wheel', 'A wheel device with handles on either side.');
