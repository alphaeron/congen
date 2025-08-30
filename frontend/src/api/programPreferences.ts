import { ENDPOINT } from './endpoint';
import type { ProgramPreferences } from './types';

/**
 * Get program preferences by program ID.
 *
 * @param programId The ID of the program
 * @returns Promise containing the program preferences
 */
export const getProgramPreferences = async (
  programId: number
): Promise<{ data: ProgramPreferences }> => {
  const response = await ENDPOINT.get(`/program_preferences/${programId}`);
  return response;
};

/**
 * Update program preferences.
 *
 * @param programId The ID of the program
 * @param sessionTimeLengthInMinutes The session time length in minutes
 * @returns Promise containing the updated program preferences
 */
export const updateProgramPreferences = async (
  programId: number,
  sessionTimeLengthInMinutes: number
): Promise<{ data: ProgramPreferences }> => {
  const response = await ENDPOINT.patch('/program_preferences/', null, {
    params: {
      program_id: programId,
      session_time_length_in_minutes: sessionTimeLengthInMinutes,
    },
  });
  return response;
};
