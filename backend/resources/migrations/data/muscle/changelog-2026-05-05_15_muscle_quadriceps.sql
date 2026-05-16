--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle quadriceps
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('quadriceps', 'The main functions of the quadriceps are to extend the leg at the knee joint and flex the thigh at the hip joint. They also help stabilize the knee by holding the patella inside a groove in the femur, or thigh bone.');
