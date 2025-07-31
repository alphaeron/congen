import { Exercise, ExerciseEquipment, ExerciseMuscle, Muscle, Equipment } from '../api/types';

/**
 * A piece of exercise equipment.
 */
export const EQUIPMENT: Equipment = {
  name: 'equipmentName',
  description: 'equipmentDescription',
};

/**
 * An individual exercise.
 */
export const EXERCISE: Exercise = {
  name: 'exerciseName',
  description: 'exerciseDescription',
  movement_type: 'movementType',
  is_unilateral: true,
  is_upper: true,
  is_accessory: false,
};

/**
 * A piece of equipment that is an option or in some cases required to perform an exercise.
 */
export const EXERCISE_EQUIPMENT: ExerciseEquipment = {
  exercise_name: 'exerciseName',
  equipment_name: 'equipmentName',
};

/**
 * A muscle targeted by an exercise.
 */
export const EXERCISE_MUSCLE: ExerciseMuscle = {
  exercise_name: 'exerciseName',
  muscle_name: 'muscleName',
};

/**
 * A muscle.
 */
export const MUSCLE: Muscle = {
  name: 'muscleName',
  description: 'muscleDescription',
};
