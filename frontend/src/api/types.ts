/**
 * A piece of exercise equipment.
 */
export interface Equipment {
  name: string;
  description: string;
} // end interface Equipment

/**
 * An individual exercise.
 */
export interface Exercise {
  name: string;
  description: string;
  movement_type: string;
  is_unilateral: boolean;
  is_upper: boolean;
  is_accessory: boolean;
} // end interface Exercise

/**
 * A piece of equipment that is an option or in some cases required to perform an exercise.
 */
export interface ExerciseEquipment {
  exercise_name: string;
  equipment_name: string;
} // end interface ExerciseEquipment

/**
 * A muscle targeted by an exercise.
 */
export interface ExerciseMuscle {
  exercise_name: string;
  muscle_name: string;
} // end interface ExerciseMuscle

/**
 * A muscle.
 */
export interface Muscle {
  name: string;
  description: string;
} // end interface Muscle

/**
 * User profile information for workout generation.
 */
export interface User {
  keycloak_id: string;
  name: string;
  created_at: string;
  updated_at: string;
  roles?: string[];
} // end interface User

/**
 * User consent record for GDPR compliance.
 * This matches the backend UserConsent model.
 */
export interface UserConsent {
  keycloak_id: string;
  data_processing_consent: boolean;
  consent_timestamp?: string;
  updated_at: string;
} // end interface UserConsent

/**
 * User data export structure for GDPR data portability.
 */
export interface UserDataExport {
  keycloak_id: string;
  name: string;
  created_at: string;
  updated_at: string;
  data_processing_consent: boolean;
  consent_timestamp?: string;
  export_timestamp: string;
  user_equipment: Record<string, unknown>[];
  user_exercise_preferences: Record<string, unknown>[];
  user_program_preferences?: Record<string, unknown>;
  user_one_rep_max: Record<string, unknown>[];
  user_weight_unit_preferences: Record<string, unknown>[];
  exercise_rotation_history: Record<string, unknown>[];
  training_programs: ProgramWithWorkouts[];
  audit_logs: Record<string, unknown>[];
  data_retention_policies: Record<string, unknown>[];
} // end interface UserDataExport

/**
 * Program with complete workout hierarchy for export.
 */
export interface ProgramWithWorkouts {
  program: {
    id: number;
    user_id: string;
    name: string;
    current_week_number: number;
    created_at: string;
    updated_at: string;
    is_active: boolean;
  };
  workouts: ProgrammedWorkoutWithStages[];
} // end interface ProgramWithWorkouts

/**
 * Programmed workout with complete stage hierarchy for export.
 */
export interface ProgrammedWorkoutWithStages {
  workout: {
    id: number;
    program_id: number;
    day_number: number;
    name: string;
    created_at: string;
    updated_at: string;
  };
  stages: WorkoutStageWithExercises[];
} // end interface ProgrammedWorkoutWithStages

/**
 * Workout stage with complete exercise hierarchy for export.
 */
export interface WorkoutStageWithExercises {
  stage: {
    id: number;
    programmed_workout_id: number;
    stage_type_id: number;
    position: number;
    name: string;
    created_at: string;
    updated_at: string;
  };
  exercises: ProgrammedExerciseWithSetSchemes[];
} // end interface WorkoutStageWithExercises

/**
 * Programmed exercise with complete set scheme hierarchy for export.
 */
export interface ProgrammedExerciseWithSetSchemes {
  exercise: {
    id: number;
    workout_stage_id: number;
    exercise_name: string;
    position: number;
    notes?: string;
    created_at: string;
    updated_at: string;
  };
  set_schemes: SetScheme[];
} // end interface ProgrammedExerciseWithSetSchemes

/**
 * Set scheme for a programmed exercise.
 */
export interface SetScheme {
  id: number;
  programmed_exercise_id: number;
  set_number: number;
  is_amrap: boolean;
  is_emom: boolean;
  use_tempo: boolean;
  eccentric_tempo?: string;
  isometric_tempo?: string;
  concentric_tempo?: string;
  target_weight?: number;
  performed_weight?: number;
  target_rep_count?: number;
  performed_rep_count?: number;
  rest_seconds?: number;
  created_at: string;
  updated_at: string;
  band_weight_lbs?: Record<string, unknown>;
} // end interface SetScheme

/**
 * Privacy policy response structure with GDPR compliance information.
 */
export interface PrivacyPolicy {
  data_controller: {
    name: string;
    contact: string;
    dpo?: string;
  };
  data_processing: {
    purposes: string[];
    legal_basis: string[];
    data_types: string[];
    retention_periods: Record<string, string>;
  };
  user_rights: {
    access: string;
    rectification: string;
    erasure: string;
    portability: string;
    objection: string;
    complaint: string;
  };
  last_updated: string;
  version: string;
} // end interface PrivacyPolicy

/**
 * A workout program created for a user.
 */
export interface Program {
  id: number;
  user_id: string;
  name: string;
  current_week_number: number;
  created_at: string;
  updated_at: string;
  is_active: boolean;
} // end interface Program

/**
 * A programmed workout within a program.
 */
export interface ProgrammedWorkout {
  id: number;
  program_id: number;
  day_number: number;
  name: string;
  created_at: string;
  updated_at: string;
} // end interface ProgrammedWorkout

/**
 * User's one rep max for a specific exercise.
 */
export interface UserOneRepMax {
  user_id: string;
  exercise_name: string;
  one_rep_max: number;
  unit: string;
  created_at: string;
  updated_at: string;
} // end interface UserOneRepMax

/**
 * Exercise rotation history tracking when exercises were used.
 */
export interface ExerciseRotationHistory {
  id: number;
  user_id: string;
  exercise_name: string;
  is_accessory: boolean;
  created_at: string;
  updated_at: string;
} // end interface ExerciseRotationHistory

/**
 * Dashboard statistics and progress data.
 */
export interface DashboardStats {
  total_workouts: number;
  current_week: number;
  active_program?: Program;
  recent_one_rep_maxes: UserOneRepMax[];
  exercise_rotation_history: ExerciseRotationHistory[];
} // end interface DashboardStats
