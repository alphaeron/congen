--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add muscle teres minor
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('teres minor', 'As a rotator cuff muscle, the teres minor stabilizes the ball-and-socket glenohumeral joint by helping hold the humeral head (ball) into the shallow glenoid cavity of the scapula (socket). The teres minor also laterally or externally rotates the arm at the shoulder joint.');
