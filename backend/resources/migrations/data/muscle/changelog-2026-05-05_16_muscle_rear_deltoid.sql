--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle rear deltoid
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('rear deltoid', 'The rear deltoid helps move your arm backwards. They connect to the flat surface of your shoulder blade.');
