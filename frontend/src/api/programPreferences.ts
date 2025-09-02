import { REQUEST } from './endpoint';
import type { ProgramPreferences } from './types';

/**
 * Get program preferences by program ID.
 *
 * @param programId The ID of the program
 * @returns Promise containing the program preferences
 */
export const getProgramPreferences = async (programId: number): Promise<ProgramPreferences> => {
  return REQUEST({
    method: 'GET',
    url: `/program_preferences/${programId}`,
  });
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
): Promise<ProgramPreferences> => {
  return REQUEST({
    method: 'PATCH',
    url: '/program_preferences/',
    params: {
      program_id: programId,
      session_time_length_in_minutes: sessionTimeLengthInMinutes,
    },
  });
};
