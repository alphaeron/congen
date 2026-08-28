--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add muscle neck
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('neck', 'You have more than 20 neck muscles, extending from the base of your skull and jaw down to your shoulder blades and collarbone. These muscles support and stabilize your head, neck and the upper part of your spine.');
