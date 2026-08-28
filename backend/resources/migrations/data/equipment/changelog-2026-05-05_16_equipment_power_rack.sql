--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment power rack
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('power rack', 'A piece of weight training equipment that functions as a mechanical spotter for free weight barbell exercises without the movement restrictions imposed by equipment.');
