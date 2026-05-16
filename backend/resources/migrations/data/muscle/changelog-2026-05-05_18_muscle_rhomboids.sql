--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle rhomboids
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('rhomboids', 'Functionally, the rhomboid muscles retract, elevate and rotate the scapula. They also protract the medial border of the scapula, keeping it in position at the posterior thoracic wall.');
