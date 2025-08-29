/**
 * Capitalize the first letter of a string.
 *
 * @param str The string to capitalize the first letter of.
 *
 * @return The string with the first letter capitalized.
 */
export function capitalizeFirstLetter(str: string): string {
  return str.charAt(0).toUpperCase() + str.slice(1);
} // end function capitalizeFirstLetter

/**
 * Capitalize the first letter in each word of a string and replace underscores with spaces.
 *
 * @param str The string to capitalize the letters in.
 *
 * @return The string with the first letter of each word capitalized and underscores replaced with spaces.
 */
export function capitalizeEachWord(str: string): string {
  return str
    .replace(/_/g, ' ')
    .split(' ')
    .map(s => s.charAt(0).toUpperCase() + s.slice(1))
    .join(' ');
} // capitalizeEachWord

/**
 * Categorizes exercise volume based on workout type and exercise properties.
 * 
 * This function determines whether an exercise's volume should be counted as
 * Max Effort, Dynamic Effort, or Accessory based on:
 * - Whether the exercise is marked as accessory
 * - The workout type (ME, DE, combined ME+DE)
 * - The exercise's body part (upper/lower)
 * 
 * @param exerciseInfo The exercise information from the database
 * @param workoutName The name of the workout (e.g., "ME Upper Day", "DE Lower Day")
 * @param setVolume The volume for this set (weight * reps)
 * 
 * @returns Object with categorized volumes
 */
export function categorizeExerciseVolume(
  exerciseInfo: { is_accessory: boolean; is_upper: boolean } | undefined,
  workoutName: string,
  setVolume: number
): { maxEffortVolume: number; dynamicEffortVolume: number; accessoryVolume: number } {
  const result = {
    maxEffortVolume: 0,
    dynamicEffortVolume: 0,
    accessoryVolume: 0,
  };

  if (exerciseInfo?.is_accessory) {
    // Accessory exercises go to accessory volume
    result.accessoryVolume = setVolume;
  } else {
    // Primary exercises (non-accessory) are categorized by workout type
    const workoutNameLower = workoutName.toLowerCase();
    
    if (workoutNameLower.includes('me') && !workoutNameLower.includes('de')) {
      // Pure Max Effort workout
      result.maxEffortVolume = setVolume;
    } else if (workoutNameLower.includes('de') && !workoutNameLower.includes('me')) {
      // Pure Dynamic Effort workout
      result.dynamicEffortVolume = setVolume;
    } else if (workoutNameLower.includes('me') && workoutNameLower.includes('de')) {
      // Combined ME+DE workout - categorize based on exercise body part
      if (workoutNameLower.includes('upper') && exerciseInfo?.is_upper) {
        // ME Upper part of combined workout
        result.maxEffortVolume = setVolume;
      } else if (workoutNameLower.includes('lower') && !exerciseInfo?.is_upper) {
        // DE Lower part of combined workout
        result.dynamicEffortVolume = setVolume;
      } else if (workoutNameLower.includes('lower') && exerciseInfo?.is_upper) {
        // DE Upper part of combined workout
        result.dynamicEffortVolume = setVolume;
      } else if (workoutNameLower.includes('upper') && !exerciseInfo?.is_upper) {
        // ME Lower part of combined workout
        result.maxEffortVolume = setVolume;
      } else {
        // Default for combined days - use body part to determine
        if (exerciseInfo?.is_upper) {
          result.maxEffortVolume = setVolume;
        } else {
          result.dynamicEffortVolume = setVolume;
        }
      }
    } else {
      // Default to dynamic effort for unknown workout types
      result.dynamicEffortVolume = setVolume;
    }
  }

  return result;
}
