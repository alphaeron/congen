--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add equipment sled
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('sled', 'A weighted sled that can be dragged or pushed for conditioning and strength training.');
