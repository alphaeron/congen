--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add muscle teres major
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('teres major', 'The teres major functions synergistically with the latissimus dorsi to extend, adduct, and internally rotate the humerus.');
