--liquibase formatted sql

--changeset alphaeron:7 labels:prod,test
--comment: Rename user_program_preferences table to program_preferences.
ALTER TABLE user_program_preferences RENAME TO program_preferences;

--changeset alphaeron:8 labels:prod,test
--comment: Drop the old foreign key constraint.
ALTER TABLE program_preferences DROP CONSTRAINT fk_user_program_preferences_user;

--changeset alphaeron:9 labels:prod,test
--comment: Add program_id column.
ALTER TABLE program_preferences ADD COLUMN program_id BIGINT;

--changeset alphaeron:10 labels:prod,test
--comment: Update program_id based on user_id (assuming one program per user for now).
UPDATE program_preferences 
SET program_id = (SELECT id FROM program WHERE program.user_id = program_preferences.user_id LIMIT 1);

--changeset alphaeron:11 labels:prod,test
--comment: Make program_id NOT NULL after data migration.
ALTER TABLE program_preferences ALTER COLUMN program_id SET NOT NULL;

--changeset alphaeron:12 labels:prod,test
--comment: Add primary key constraint on program_id.
ALTER TABLE program_preferences ADD PRIMARY KEY (program_id);

--changeset alphaeron:13 labels:prod,test
--comment: Drop the old user_id column.
ALTER TABLE program_preferences DROP COLUMN user_id;

--changeset alphaeron:14 labels:prod,test
--comment: Add new foreign key constraint.
ALTER TABLE program_preferences 
ADD CONSTRAINT fk_program_preferences_program 
FOREIGN KEY(program_id) REFERENCES program(id) ON DELETE CASCADE;

--changeset alphaeron:15 labels:prod,test
--comment: Add index for program preferences.
CREATE INDEX idx_program_preferences_program_id ON program_preferences(program_id);
