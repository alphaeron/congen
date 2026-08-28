--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add muscle obliques
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('obliques', 'Rotating and twisting the trunk of the body. Stabilizing the core of the body. Moving the spine in any direction possible. Assisting in bending the trunk from side to side.');
