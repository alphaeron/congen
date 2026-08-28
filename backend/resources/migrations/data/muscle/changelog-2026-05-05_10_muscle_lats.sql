--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add muscle lats
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('lats', 'Functionally, the latissimus dorsi muscle belongs to the muscles of the scapular motion. This muscle is able to pull the inferior angle of the scapula in various directions, producing movements on the shoulder joint (internal rotation, adduction and extension of the arm).');
