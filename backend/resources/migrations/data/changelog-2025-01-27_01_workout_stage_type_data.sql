--liquibase formatted sql

--changeset alphaeron:workout_stage_type_seed labels:prod,test
--comment: Populate workout_stage_type table with common stage types for conjugate powerlifting programs.

INSERT INTO workout_stage_type (name) VALUES
  ('Warmup'),
  ('Primary'),
  ('Secondary'),
  ('Accessory'),
  ('Cooldown'),
  ('Mobility'),
  ('Conditioning'); 