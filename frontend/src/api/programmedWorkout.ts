import { REQUEST } from './endpoint';
import type { ProgrammedWorkout } from './types';

/**
 * Get all programmed workouts for the current user.
 *
 * @returns Promise containing a list of programmed workouts
 */
export const getProgrammedWorkouts = (): Promise<ProgrammedWorkout[]> => {
  return REQUEST({
    method: 'GET',
    url: '/programmed_workout/',
  });
};

/**
 * Get a specific programmed workout by ID.
 *
 * @param id The programmed workout ID
 * @returns Promise containing the programmed workout details
 */
export const getProgrammedWorkout = (id: number): Promise<ProgrammedWorkout> => {
  return REQUEST({
    method: 'GET',
    url: `/programmed_workout/${id}`,
  });
};

/**
 * Get all programmed workouts for a specific program.
 *
 * @param programId The program ID
 * @param week Optional week number to filter workouts (1-based)
 * @returns Promise containing a list of programmed workouts for the program
 */
export const getProgrammedWorkoutsByProgram = (programId: number, week?: number): Promise<ProgrammedWorkout[]> => {
  const params: Record<string, string | number> = {};
  if (week !== undefined) {
    params.week = week;
  }
  
  return REQUEST({
    method: 'GET',
    url: `/programmed_workout/program/${programId}`,
    params,
  });
};

/**
 * Update an existing programmed workout.
 *
 * @param id The programmed workout ID
 * @param programId The updated program ID
 * @param dayNumber The updated day number
 * @param name The updated name
 * @return The updated programmed workout
 */
export const updateProgrammedWorkout = (
  id: number,
  programId: number,
  dayNumber: number,
  name: string
): Promise<ProgrammedWorkout> => {
  return REQUEST({
    method: 'PATCH',
    url: `/programmed_workout/${id}`,
    params: {
      program_id: programId,
      day_number: dayNumber,
      name,
    },
  });
};
