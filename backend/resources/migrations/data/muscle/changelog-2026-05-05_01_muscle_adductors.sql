--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle adductors
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('adductors', 'The function of the adductor muscles is to pull the thighs together and rotate the upper leg inwards, as well as stabilizing the hip.');
