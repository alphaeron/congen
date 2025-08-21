import { ENDPOINT } from './endpoint';

/**
 * User program preferences interface.
 */
export interface UserProgramPreferences {
  user_id: string;
  program_days_per_week: number;
  session_time_length_in_minutes: number;
  created_at: string;
  updated_at: string;
}

/**
 * Create new user program preferences.
 *
 * @param userId The Keycloak identifier of the user
 * @param programDaysPerWeek The number of days per week for the program
 * @param sessionTimeLengthInMinutes The session time length in minutes
 * @returns Promise containing the created user program preferences
 */
export const createUserProgramPreferences = async (
  userId: string,
  programDaysPerWeek: number,
  sessionTimeLengthInMinutes: number
): Promise<{ data: UserProgramPreferences }> => {
  const response = await ENDPOINT.post('/user_program_preferences/', null, {
    params: {
      user_id: userId,
      program_days_per_week: programDaysPerWeek,
      session_time_length_in_minutes: sessionTimeLengthInMinutes,
    },
  });
  return response;
};

/**
 * Get user program preferences by user ID.
 *
 * @param userId The Keycloak identifier of the user
 * @returns Promise containing the user program preferences
 */
export const getUserProgramPreferences = async (
  userId: string
): Promise<{ data: UserProgramPreferences }> => {
  const response = await ENDPOINT.get(`/user_program_preferences/${userId}`);
  return response;
};

/**
 * Update user program preferences.
 *
 * @param userId The Keycloak identifier of the user
 * @param programDaysPerWeek The number of days per week for the program
 * @param sessionTimeLengthInMinutes The session time length in minutes
 * @returns Promise containing the updated user program preferences
 */
export const updateUserProgramPreferences = async (
  userId: string,
  programDaysPerWeek: number,
  sessionTimeLengthInMinutes: number
): Promise<{ data: UserProgramPreferences }> => {
  const response = await ENDPOINT.patch('/user_program_preferences/', null, {
    params: {
      user_id: userId,
      program_days_per_week: programDaysPerWeek,
      session_time_length_in_minutes: sessionTimeLengthInMinutes,
    },
  });
  return response;
};

/**
 * Delete user program preferences by user ID.
 *
 * @param userId The Keycloak identifier of the user
 * @returns Promise containing the deleted user program preferences
 */
export const deleteUserProgramPreferences = async (
  userId: string
): Promise<{ data: UserProgramPreferences }> => {
  const response = await ENDPOINT.delete(`/user_program_preferences/${userId}`);
  return response;
};
