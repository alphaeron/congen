--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle serratus anterior
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('serratus anterior', 'When the shoulder girdle is fixed, all three parts of the serratus anterior muscle work together to lift the ribs, assisting with respiration. The serratus anterior, also known as the "boxers muscle", is largely responsible for the protraction of the scapula, a movement that occurs when throwing a punch');
