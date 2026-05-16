--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add muscle rectus abdominis
--rollback SELECT 1

INSERT INTO muscle (name, description) VALUES ('rectus abdominis', 'The main actions of the rectus abdominis are flexion of the trunk (thoratic and lumbar spine), tensing the anterior abdominal wall and compression the contents of the abdomen, and also plays a role in core stability.');
