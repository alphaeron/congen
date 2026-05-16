--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle erector spinae
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('erector spinae', 'The main actions of the erector spinae muscles are to extend the back, laterally flex the back, and maintain correct posture and curvature of the spinal column.');
