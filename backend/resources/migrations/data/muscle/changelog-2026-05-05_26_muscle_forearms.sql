--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle forearms
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('forearms', 'The forearms are the muscles that extend the wrist and fingers. They are located on the front of the lower arm, between the elbow and the wrist.');
