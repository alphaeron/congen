--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment sliders
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('sliders', 'A pair of disks or carpet sliders that provide an unstable training surface, allowing you to simulate many different slideboard exercises');
