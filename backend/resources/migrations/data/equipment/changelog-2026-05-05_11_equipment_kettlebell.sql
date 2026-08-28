--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment kettlebell
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('kettlebell', 'A cast-iron or cast-steel ball with a handle attached to the top.');
