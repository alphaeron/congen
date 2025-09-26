/**
 * A piece of exercise equipment.
 */
export interface Equipment {
  name: string;
  description: string;
} // end interface Equipment

/**
 * User performance metrics for gamified tracking.
 */
export interface UserPerformanceMetrics {
  keycloak_id: string;
  vo2_max?: number;
  strain?: number;
  recovery?: number;
  hrv?: number;
  sleep_score?: number;
  rem_sleep_minutes?: number;
  deep_sleep_minutes?: number;
  subjective_tiredness?: number;
  created_at: string;
  updated_at: string;
}

/**
 * User performance scores for gamified tracking.
 */
export interface UserPerformanceScores {
  keycloak_id: string;
  explosiveness_score?: number;
  aerobic_capacity_score?: number;
  recovery_score?: number;
  reaction_time_score?: number;
  mobility_score?: number;
  level: number;
  hp: number;
  hp_loss: number;
  mp: number;
  mp_loss: number;
  fatigue: number;
  fatigue_loss: number;
  skills: string[];
  created_at: string;
  updated_at: string;
}

/**
 * Individual user test result for a specific test protocol.
 */
export interface UserTestResult {
  id?: number;
  keycloak_id: string;
  week_start_timestamp: Date;
  test_name: string;
  status: 'PENDING' | 'COMPLETED' | 'SKIPPED';
  result_value?: number;
  created_at: string;
  updated_at: string;
}

/**
 * Test protocol configuration for weekly testing.
 */
export interface TestProtocol {
  test_name: string;
  display_name: string;
  description: string;
  unit: string;
  icon_name: string;
  is_required: boolean;
  display_order: number;
  radar_chart_color: string;
  radar_chart_enabled: boolean;
}

/**
 * Weight unit enum.
 */
export enum WeightUnit {
  KG = 'KG',
  LBS = 'LBS',
}

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
  age?: number;
  weight?: number;
  height?: number;
  gender?: string;
  created_at: Date;
  updated_at: Date;
  roles?: string[];
} // end interface User

/**
 * User consent record for GDPR compliance.
 * This matches the backend UserConsent model.
 */
export interface UserConsent {
  keycloak_id: string;
  data_processing_consent: boolean;
  consent_timestamp?: Date;
  updated_at: Date;
} // end interface UserConsent

/**
 * User data export structure for GDPR data portability.
 */
export interface UserDataExport {
  keycloak_id: string;
  name: string;
  age?: number;
  weight?: number;
  height?: number;
  gender?: string;
  created_at: Date;
  updated_at: Date;
  data_processing_consent: boolean;
  consent_timestamp?: Date;
  export_timestamp: Date;
  user_equipment: Record<string, unknown>[];
  user_exercise_preferences: Record<string, unknown>[];
  user_one_rep_max: Record<string, unknown>[];
  user_weight_unit_preferences: Record<string, unknown>[];
  training_programs: ProgramWithWorkouts[];
  audit_logs: Record<string, unknown>[];
  data_retention_policies: Record<string, unknown>[];
} // end interface UserDataExport

/**
 * Program with preferences structure returned by the backend.
 */
export interface ProgramWithPreferences {
  program: Program;
  program_preferences: ProgramPreferences;
} // end interface ProgramWithPreferences

/**
 * Program with complete workout hierarchy for export.
 */
export interface ProgramWithWorkouts {
  program: {
    id: number;
    user_id: string;
    name: string;
    current_week_number: number;
    created_at: Date;
    updated_at: Date;
    is_active: boolean;
  };
  program_preferences: ProgramPreferences;
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
    created_at: Date;
    updated_at: Date;
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
    created_at: Date;
    updated_at: Date;
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
    created_at: Date;
    updated_at: Date;
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
  created_at: Date;
  updated_at: Date;
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
  last_updated: Date;
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
  created_at: Date;
  updated_at: Date;
  is_active: boolean;
} // end interface Program

/**
 * Program preferences for workout frequency and duration.
 */
export interface ProgramPreferences {
  program_id: number;
  program_days_per_week: number;
  session_time_length_in_minutes: number;
  created_at: Date;
  updated_at: Date;
} // end interface ProgramPreferences

/**
 * A programmed workout within a program.
 */
export interface ProgrammedWorkout {
  id: number;
  program_id: number;
  day_number: number;
  name: string;
  created_at: Date;
  updated_at: Date;
} // end interface ProgrammedWorkout

/**
 * A workout stage within a programmed workout.
 */
export interface WorkoutStage {
  id: number;
  programmed_workout_id: number;
  stage_type_id: number;
  position: number;
  name: string;
  created_at: Date;
  updated_at: Date;
} // end interface WorkoutStage

/**
 * A programmed exercise within a workout stage.
 */
export interface ProgrammedExercise {
  id: number;
  workout_stage_id: number;
  exercise_name: string;
  position: number;
  notes?: string;
  created_at: Date;
  updated_at: Date;
} // end interface ProgrammedExercise

/**
 * User's one rep max for a specific exercise.
 */
export interface UserOneRepMax {
  user_id: string;
  exercise_name: string;
  one_rep_max: number;
  unit: string;
  created_at: Date;
  updated_at: Date;
} // end interface UserOneRepMax

/**
 * User's weight unit preference for a specific exercise.
 */
export interface UserWeightUnitPreference {
  user_id: string;
  exercise_name: string;
  preferred_unit: string;
  created_at: Date;
  updated_at: Date;
} // end interface UserWeightUnitPreference

/**
 * User's equipment preference.
 */
export interface UserEquipment {
  user_id: string;
  equipment_name: string;
  created_at: Date;
} // end interface UserEquipment

/**
 * User's weak muscle group for targeted accessory selection.
 */
export interface UserWeakMuscle {
  user_id: string;
  muscle_name: string;
  created_at: Date;
} // end interface UserWeakMuscle

/**
 * User's exercise preference (like/dislike).
 */
export interface UserExercisePreference {
  user_id: string;
  exercise_name: string;
  should_avoid: boolean;
  created_at: Date;
  updated_at: Date;
} // end interface UserExercisePreference

/**
 * Dashboard statistics and progress data.
 */
export interface DashboardStats {
  total_workouts: number;
  current_week: number;
  active_program?: Program;
  recent_one_rep_maxes: UserOneRepMax[];
} // end interface DashboardStats

/**
 * User's exercise pool response from the backend.
 */
export interface UserExercisePoolResponse {
  user_id: string;
  total_exercises: number;
  available_exercises: number;
  primary_exercises: Exercise[];
  accessory_exercises: Exercise[];
  user_equipment: UserEquipment[];
  user_preferences: UserExercisePreference[];
  previously_used_exercises: string[];
} // end interface UserExercisePoolResponse

/**
 * Wizard step enumeration for workout generation wizard.
 */
export enum WizardStep {
  WORKOUT_GENERATION = 'workout_generation',
  GENERATION_LOADING = 'generation_loading',
  ONE_REP_MAX_INPUT = 'one_rep_max_input',
  UPDATING_WORKOUT = 'updating_workout',
  UPDATING_WORKOUT_WITH_1RM = 'updating_workout_with_1rm',
}

/**
 * Form data for workout generation wizard.
 */
export interface WorkoutGenerationWizardData {
  programId: number;
  currentStep: WizardStep;
  generatedWorkout?: Program;
  declinedExercises: string[];
  declineAll: boolean;
}
