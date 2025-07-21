import {
  Exercise,
  ExerciseEquipment,
  ExerciseMuscle,
  Muscle,
  Equipment,
} from "../api/types";

/**
 * A piece of exercise equipment.
 */
export const EQUIPMENT: Equipment = {
  name: "equipmentName",
  description: "equipmentDescription",
};

/**
 * An individual exercise.
 */
export const EXERCISE: Exercise = {
  name: "exerciseName",
  description: "exerciseDescription",
  movementType: "movementType",
  isUnilateral: false,
  isUpper: true,
  isAccessory: false,
};

/**
 * A piece of equipment that is an option or in some cases required to perform an exercise.
 */
export const EXERCISE_EQUIPMENT: ExerciseEquipment = {
  exerciseName: "exerciseName",
  equipmentName: "equipmentName",
};

/**
 * A muscle targeted by an exercise.
 */
export const EXERCISE_MUSCLE: ExerciseMuscle = {
  exerciseName: "exerciseName",
  muscleName: "muscleName",
};

/**
 * A muscle.
 */
export const MUSCLE: Muscle = {
  name: "muscleName",
  description: "muscleDescription",
};
