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
  movementType: string;
  isUnilateral: boolean;
  isUpper: boolean;
  isAccessory: boolean;
} // end interface Exercise

/**
 * A piece of equipment that is an option or in some cases required to perform an exercise.
 */
export interface ExerciseEquipment {
  exerciseName: string;
  equipmentName: string;
} // end interface ExerciseEquipment

/**
 * A muscle targeted by an exercise.
 */
export interface ExerciseMuscle {
  exerciseName: string;
  muscleName: string;
} // end interface ExerciseMuscle

/**
 * A muscle.
 */
export interface Muscle {
  name: string;
  description: string;
}
