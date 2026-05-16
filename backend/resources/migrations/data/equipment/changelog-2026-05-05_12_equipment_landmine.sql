--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment landmine
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('landmine', 'A barbell anchored to the floor with a weight on the other end.');
