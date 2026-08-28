--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add muscle upper back
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('upper back', 'The intrinsic muscles of the scapula include the rotator cuff muscles, teres major, subscapularis, teres minor, and infraspinatus. These muscles attach the scapular surface and assist with abduction and external and internal rotation of the glenohumeral joint.');
