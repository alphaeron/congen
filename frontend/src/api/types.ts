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
  id: number;
  name: string;
  age: number;
  height: number;
  weight: number;
  created_at: string;
  updated_at: string;
  keycloak_user_id?: string;
  groups?: string[]; // Keycloak groups, these aren't tracked in the backend
  roles?: string[]; // Keycloak roles, these aren't tracked in the backend
} // end interface User
