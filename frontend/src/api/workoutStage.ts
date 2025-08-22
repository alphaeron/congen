import { REQUEST } from './endpoint';
import type { WorkoutStage } from './types';

/**
 * Get all workout stages for a specific programmed workout.
 *
 * @param programmedWorkoutId The ID of the programmed workout
 * @returns Promise containing a list of workout stages
 */
export const getWorkoutStagesByWorkout = (programmedWorkoutId: number): Promise<WorkoutStage[]> => {
  return REQUEST({
    method: 'GET',
    url: `/workout_stage/workout/${programmedWorkoutId}`,
  });
};

/**
 * Get a specific workout stage by ID.
 *
 * @param id The workout stage ID
 * @returns Promise containing the workout stage details
 */
export const getWorkoutStage = (id: number): Promise<WorkoutStage> => {
  return REQUEST({
    method: 'GET',
    url: `/workout_stage/${id}`,
  });
};
