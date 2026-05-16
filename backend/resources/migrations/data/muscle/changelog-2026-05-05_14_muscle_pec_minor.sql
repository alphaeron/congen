--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle pec minor
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('pec minor', 'The primary actions of this muscle include the stabilization, depression, abduction or protraction, internal rotation and downward rotation of the scapula.');
