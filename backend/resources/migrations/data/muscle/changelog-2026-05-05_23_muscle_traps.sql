--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add muscle traps
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('traps', 'The function of the trapezius is to stabilize and move the scapula. The upper fibers can elevate and upwardly rotate the scapula and extend the neck. The middle fibers adduct (medially retract) the scapula. The lower fibers depress and aid the upper fibers in upwardly rotating the scapula.');
