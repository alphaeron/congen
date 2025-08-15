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
  personal_data: {
    profile: Record<string, unknown>;
    preferences: Record<string, unknown>;
    exercise_history: Record<string, unknown>[];
    programs: Record<string, unknown>[];
  };
  metadata: {
    exported_at: string;
    data_types: string[];
    total_records: number;
  };
} // end interface UserDataExport

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
