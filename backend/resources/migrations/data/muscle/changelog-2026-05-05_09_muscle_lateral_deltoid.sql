--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add muscle lateral deltoid
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('lateral deltoid', 'The lateral deltoid helps move your arm to the side, as well as up and down. They connect to your acromion, a bony nob on your shoulder blade.');
