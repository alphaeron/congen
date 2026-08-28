--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment ghr
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('ghr', 'A specialized piece of equipment used primarily to strengthen the glutes and hamstrings.');
