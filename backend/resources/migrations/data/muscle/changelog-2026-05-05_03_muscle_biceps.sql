--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add muscle biceps
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('biceps', 'The main functions of the biceps are the flexion and supination (outward rotation) of the forearm.');
