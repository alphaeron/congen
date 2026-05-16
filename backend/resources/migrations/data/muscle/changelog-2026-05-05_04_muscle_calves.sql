--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle calves
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('calves', 'Your calf muscle supports you when you stand and enables you to move your foot and your lower leg. It propels (pushes) you forward when you walk or run. It also allows you to jump, rotate your ankle, flex your foot and "lock" your knee.');
