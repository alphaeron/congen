import { REQUEST } from './endpoint';
import type { Program, ProgramWithPreferences } from './types';

/**
 * Create a new program for the current user.
 *
 * @param name The name of the program
 * @param numDaysPerWeek The number of days per week for the program (defaults to 4)
 * @param userId The user ID
 * @return The created program
 */
export const createProgram = (
  name: string,
  numDaysPerWeek: number = 4,
  userId: string
): Promise<Program> => {
  return REQUEST({
    method: 'POST',
    url: '/program/',
    params: {
      user_id: userId,
      name,
      num_days_per_week: numDaysPerWeek,
      is_active: true, // New programs are always active
    },
  });
};

/**
 * Get all programs for the current user.
 *
 * @return List of programs for the current user
 */
export const getPrograms = (): Promise<Program[]> => {
  return REQUEST({
    method: 'GET',
    url: '/program/',
  });
};

/**
 * Get all programs with their preferences for the current user.
 *
 * @return List of programs with preferences for the current user
 */
export const getProgramsWithPreferences = (): Promise<Array<ProgramWithPreferences>> => {
  return REQUEST({
    method: 'GET',
    url: '/program/with-preferences',
  });
};

/**
 * Get a specific program by ID.
 *
 * @param id The program ID
 * @return The program details
 */
export const getProgram = (id: number): Promise<Program> => {
  return REQUEST({
    method: 'GET',
    url: `/program/${id}`,
  });
};

/**
 * Update an existing program.
 *
 * @param id The program ID
 * @param name The updated name
 * @param currentWeekNumber The updated current week number
 * @param isActive Whether the program should be active
 * @return The updated program
 */
export const updateProgram = (
  id: number,
  name: string,
  currentWeekNumber: number,
  isActive: boolean
): Promise<Program> => {
  return REQUEST({
    method: 'PATCH',
    url: `/program/${id}`,
    params: {
      name,
      current_week_number: currentWeekNumber,
      is_active: isActive,
    },
  });
};

/**
 * Delete a program.
 *
 * @param id The program ID
 * @return The deleted program
 */
export const deleteProgram = (id: number): Promise<Program> => {
  return REQUEST({
    method: 'DELETE',
    url: `/program/${id}`,
  });
};
