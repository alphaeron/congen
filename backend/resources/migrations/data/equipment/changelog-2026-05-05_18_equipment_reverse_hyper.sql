--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment reverse hyper
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('reverse hyper', 'The Reverse Hyper is used for prehab, and rehab, to strengthen the lower back and perform many other exercise variations on it to avoid accommodation.');
