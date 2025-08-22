import { REQUEST } from './endpoint';
import type { ProgrammedWorkout } from './types';

/**
 * Create a new programmed workout.
 *
 * @param programId The ID of the program this workout belongs to
 * @param dayNumber The day number within the program
 * @param name The name of the workout
 * @return The created programmed workout
 */
export const createProgrammedWorkout = (
  programId: number,
  dayNumber: number,
  name: string
): Promise<ProgrammedWorkout> => {
  return REQUEST({
    method: 'POST',
    url: '/programmed_workout/',
    params: {
      program_id: programId,
      day_number: dayNumber,
      name,
    },
  });
};

/**
 * Get all programmed workouts for the current user.
 *
 * @return List of programmed workouts for the current user
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
 * @return The programmed workout details
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
 * @return List of programmed workouts for the program
 */
export const getProgrammedWorkoutsByProgram = (programId: number): Promise<ProgrammedWorkout[]> => {
  return REQUEST({
    method: 'GET',
    url: `/programmed_workout/program/${programId}`,
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

/**
 * Delete a programmed workout.
 *
 * @param id The programmed workout ID
 * @return The deleted programmed workout
 */
export const deleteProgrammedWorkout = (id: number): Promise<ProgrammedWorkout> => {
  return REQUEST({
    method: 'DELETE',
    url: `/programmed_workout/${id}`,
  });
};
