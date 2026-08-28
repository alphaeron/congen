--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add muscle pec major
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('pec major', 'The primary functions are flexion, adduction, and internal rotation of the humerus. The pectoral major may colloquially be referred to as "pecs", "pectoral muscle", or "chest muscle", because it is the largest and most superficial muscle in the chest area.');
