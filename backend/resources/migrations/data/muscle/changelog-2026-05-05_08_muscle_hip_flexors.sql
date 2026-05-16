--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle hip flexors
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('hip flexors', 'Hip flexors are a group of muscles responsible for flexing the hip and raising the legs. These muscles are essential in movement as you use this muscle group every time you stand or take a step; they also help maintain your stability and posture.');
