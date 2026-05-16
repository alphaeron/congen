--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add equipment box
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('box', 'a piece of training equipment used for plyometric exercises requiring jumping or dropping off at certain heights.');
