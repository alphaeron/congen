--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment dip bars
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('dip bars', 'Parallel bars used for performing dips and other bodyweight exercises.');
