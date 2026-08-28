--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add muscle rotator cuff
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('rotator cuff', 'During arm movements, the rotator muscles contract and prevent the sliding of the head of the humerus, allowing full range of motion and providing stability. Additionally, rotator cuff muscles help in the mobility of the shoulder joint by facilitating abduction, medial rotation, and lateral rotation.');
