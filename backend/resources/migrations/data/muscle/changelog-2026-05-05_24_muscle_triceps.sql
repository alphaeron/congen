--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle triceps
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('triceps', 'The triceps brachii is a large, thick muscle on the dorsal part of the upper arm. It often appears in the shape of a horseshoe on the posterior aspect of the arm. The primary function of the triceps is the extension of the elbow joint.');
