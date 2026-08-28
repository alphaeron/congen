--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment airex pad
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('airex pad', 'An active therapy and training device made out of soft foam.');
