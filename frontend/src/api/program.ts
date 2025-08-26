import { REQUEST } from './endpoint';
import type { Program } from './types';

/**
 * Create a new program for the current user.
 *
 * @param name The name of the program
 * @param isActive Whether the program should be active (defaults to true)
 * @return The created program
 */
export const createProgram = (
  name: string,
  isActive: boolean = true,
  userId: string
): Promise<Program> => {
  return REQUEST({
    method: 'POST',
    url: '/program/',
    params: {
      user_id: userId,
      name,
      is_active: isActive,
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
