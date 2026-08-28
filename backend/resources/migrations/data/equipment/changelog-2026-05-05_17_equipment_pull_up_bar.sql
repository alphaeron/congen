--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment pull-up bar
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('pull-up bar', 'A bar anchored above your head-level that allows you to hang without your feet touching the ground, which can be used to perform a variety of exercises such as pull-ups, chin-ups, and muscle-ups.');
