--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle anterior deltoid
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('anterior deltoid', 'The anterior deltoid helps move your arm forward. They connect to your clavicle.');
