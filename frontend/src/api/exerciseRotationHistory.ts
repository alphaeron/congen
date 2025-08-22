import { REQUEST } from './endpoint';
import type { ExerciseRotationHistory } from './types';

/**
 * Create a new exercise rotation history record.
 *
 * @param exerciseName The name of the exercise that was used
 * @param isAccessory Whether the exercise was used as an accessory movement
 * @return The created exercise rotation history record
 */
export const createExerciseRotationHistory = (
  exerciseName: string,
  isAccessory: boolean
): Promise<ExerciseRotationHistory> => {
  return REQUEST({
    method: 'POST',
    url: '/exercise_rotation_history/',
    params: {
      user_id: 'current', // Backend will resolve this to current user
      exercise_name: exerciseName,
      is_accessory: isAccessory,
    },
  });
};

/**
 * Get all exercise rotation history records for the current user.
 * The backend automatically filters to only return records for the authenticated user.
 *
 * @return List of exercise rotation history records for the current user
 */
export const getExerciseRotationHistory = (): Promise<ExerciseRotationHistory[]> => {
  return REQUEST({
    method: 'GET',
    url: '/exercise_rotation_history/',
  });
};

/**
 * Get exercise rotation history records filtered by accessory type.
 * The backend automatically filters to only return records for the authenticated user.
 *
 * @param isAccessory Whether to filter by accessory exercises
 * @return List of exercise rotation history records for the accessory type
 */
export const getExerciseRotationHistoryByAccessory = (
  isAccessory: boolean
): Promise<ExerciseRotationHistory[]> => {
  return REQUEST({
    method: 'GET',
    url: `/exercise_rotation_history/is_accessory/${isAccessory}`,
  });
};

/**
 * Get a specific exercise rotation history record by ID.
 *
 * @param id The exercise rotation history record ID
 * @return The exercise rotation history record details
 */
export const getExerciseRotationHistoryById = (id: number): Promise<ExerciseRotationHistory> => {
  return REQUEST({
    method: 'GET',
    url: `/exercise_rotation_history/${id}`,
  });
};

/**
 * Update an existing exercise rotation history record.
 *
 * @param id The exercise rotation history record ID
 * @param exerciseName The updated exercise name
 * @param isAccessory The updated accessory flag
 * @return The updated exercise rotation history record
 */
export const updateExerciseRotationHistory = (
  id: number,
  exerciseName: string,
  isAccessory: boolean
): Promise<ExerciseRotationHistory> => {
  return REQUEST({
    method: 'PATCH',
    url: `/exercise_rotation_history/${id}`,
    params: {
      user_id: 'current', // Backend will resolve this to current user
      exercise_name: exerciseName,
      is_accessory: isAccessory,
    },
  });
};

/**
 * Delete an exercise rotation history record.
 *
 * @param id The exercise rotation history record ID
 * @return The deleted exercise rotation history record
 */
export const deleteExerciseRotationHistory = (id: number): Promise<ExerciseRotationHistory> => {
  return REQUEST({
    method: 'DELETE',
    url: `/exercise_rotation_history/${id}`,
  });
};
