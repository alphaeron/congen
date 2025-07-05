--liquibase formatted sql

--changeset John Matty:4 labels:prod,test
--comment: Add workout program tables for conjugate powerlifting programs with stages and exercises.

CREATE SEQUENCE congen.program_id_seq;
CREATE TABLE program (
  id BIGSERIAL DEFAULT nextval('congen.program_id_seq') PRIMARY KEY,
  user_id INTEGER NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_program_user FOREIGN KEY(user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE SEQUENCE congen.programmed_workout_id_seq;
CREATE TABLE programmed_workout (
  id BIGSERIAL DEFAULT nextval('congen.programmed_workout_id_seq') PRIMARY KEY,
  program_id BIGINT NOT NULL,
  day_number SMALLINT NOT NULL CHECK (day_number > 0 AND day_number <= 365),  -- e.g., Day 1, Day 2...
  name VARCHAR(255),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_programmed_workout_program FOREIGN KEY(program_id) REFERENCES program(id) ON DELETE CASCADE
);

CREATE SEQUENCE congen.workout_stage_type_id_seq;
CREATE TABLE workout_stage_type (
  id SERIAL DEFAULT nextval('congen.workout_stage_type_id_seq') PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,  -- e.g., 'Warmup', 'Primary', 'Accessory', 'Cooldown'
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE SEQUENCE congen.workout_stage_id_seq;
CREATE TABLE workout_stage (
  id BIGSERIAL DEFAULT nextval('congen.workout_stage_id_seq') PRIMARY KEY,
  programmed_workout_id BIGINT NOT NULL,
  stage_type_id INTEGER NOT NULL,
  position SMALLINT NOT NULL CHECK (position > 0), -- ordering of stages in the workout
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_workout_stage_programmed_workout FOREIGN KEY(programmed_workout_id) REFERENCES programmed_workout(id) ON DELETE CASCADE,
  CONSTRAINT fk_workout_stage_stage_type FOREIGN KEY(stage_type_id) REFERENCES workout_stage_type(id) ON DELETE RESTRICT
);

CREATE SEQUENCE congen.programmed_exercise_id_seq;
CREATE TABLE programmed_exercise (
  id BIGSERIAL DEFAULT nextval('congen.programmed_exercise_id_seq') PRIMARY KEY,
  workout_stage_id BIGINT NOT NULL,
  exercise_name VARCHAR(255) NOT NULL,
  notes TEXT,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_programmed_exercise_exercise FOREIGN KEY(exercise_name) REFERENCES exercise(name) ON DELETE RESTRICT,
  CONSTRAINT fk_programmed_exercise_workout_stage FOREIGN KEY(workout_stage_id) REFERENCES workout_stage(id) ON DELETE CASCADE
);

CREATE SEQUENCE congen.set_scheme_id_seq;
CREATE TABLE set_scheme (
  id BIGSERIAL DEFAULT nextval('congen.set_scheme_id_seq') PRIMARY KEY,
  programmed_exercise_id BIGINT NOT NULL,
  set_number SMALLINT NOT NULL CHECK (set_number > 0),
  was_set_performed BOOLEAN DEFAULT TRUE,
  is_amrap BOOLEAN DEFAULT FALSE,
  is_emom BOOLEAN DEFAULT FALSE,
  use_tempo BOOLEAN DEFAULT FALSE,
  eccentric_tempo CHAR(1) CHECK (eccentric_tempo IN ('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')),
  isometric_tempo CHAR(1) CHECK (isometric_tempo IN ('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')),
  concentric_tempo CHAR(1) CHECK (concentric_tempo IN ('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')),
  target_weight NUMERIC(6,2) CHECK (target_weight > 0),
  performed_weight NUMERIC(6,2) CHECK (performed_weight > 0),
  target_rep_count SMALLINT CHECK (target_rep_count > 0 AND target_rep_count <= 1000),
  performed_rep_count SMALLINT CHECK (performed_rep_count > 0 AND performed_rep_count <= 1000),
  rest_seconds SMALLINT CHECK (rest_seconds >= 0 AND rest_seconds <= 3600),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_set_scheme_programmed_exercise FOREIGN KEY(programmed_exercise_id) REFERENCES programmed_exercise(id) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_program_name ON program(name);
CREATE INDEX idx_program_user_id ON program(user_id);
CREATE INDEX idx_program_created_at ON program(created_at);
CREATE INDEX idx_programmed_workout_program_id ON programmed_workout(program_id);
CREATE INDEX idx_programmed_workout_day_number ON programmed_workout(day_number);
CREATE INDEX idx_programmed_workout_program_day ON programmed_workout(program_id, day_number);
CREATE INDEX idx_workout_stage_programmed_workout_id ON workout_stage(programmed_workout_id);
CREATE INDEX idx_workout_stage_position ON workout_stage(position);
CREATE INDEX idx_workout_stage_programmed_position ON workout_stage(programmed_workout_id, position);
CREATE INDEX idx_programmed_exercise_workout_stage_id ON programmed_exercise(workout_stage_id);
CREATE INDEX idx_programmed_exercise_exercise_name ON programmed_exercise(exercise_name);
CREATE INDEX idx_set_scheme_programmed_exercise_id ON set_scheme(programmed_exercise_id);
CREATE INDEX idx_set_scheme_set_number ON set_scheme(set_number);
CREATE INDEX idx_set_scheme_exercise_set ON set_scheme(programmed_exercise_id, set_number);
CREATE INDEX idx_set_scheme_performed ON set_scheme(was_set_performed);
CREATE INDEX idx_set_scheme_amrap ON set_scheme(is_amrap);
CREATE INDEX idx_set_scheme_emom ON set_scheme(is_emom);

-- Add unique constraints to prevent duplicates
CREATE UNIQUE INDEX idx_programmed_workout_program_day_unique ON programmed_workout(program_id, day_number);
CREATE UNIQUE INDEX idx_workout_stage_position_unique ON workout_stage(programmed_workout_id, position);
CREATE UNIQUE INDEX idx_set_scheme_exercise_set_unique ON set_scheme(programmed_exercise_id, set_number); 