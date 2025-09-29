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
 * Replaces underscores with spaces in a string.
 *
 * @param str The string to format
 * @returns The formatted string with underscores replaced by spaces
 */
export function replaceUnderscoresWithSpaces(str: string): string {
  return str.replace(/_/g, ' ');
}

/**
 * Get the browser's preferred locale for date formatting.
 * 
 * @returns The browser's locale string
 */
function getBrowserLocale(): string {
  // Modern browsers support navigator.languages, which provides an array of preferred languages.
  if (navigator.languages && navigator.languages.length > 0) {
    return navigator.languages[0]; // Returns the most preferred language
  } 
  // Fallback for older browsers or if navigator.languages is not available.
  // navigator.language is generally supported by most browsers.
  else if (navigator.language) {
    return navigator.language;
  } 
  // Older Internet Explorer versions might use navigator.userLanguage.
  else if ((navigator as any).userLanguage) {
    return (navigator as any).userLanguage;
  }
  // Default to 'en' if no language information is found.
  return 'en'; 
}

/**
 * Safely formats a Date object to a readable format in the browser's timezone.
 *
 * @param dateInput The date to format (can be null, undefined, or Date object)
 * @param options Optional formatting options for toLocaleDateString
 * @returns Formatted date string or 'N/A' if invalid
 */
export function formatDate(
  dateInput: Date | null | undefined,
  options: Intl.DateTimeFormatOptions = {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  }
): string {
  if (!dateInput || !(dateInput instanceof Date) || isNaN(dateInput.getTime())) {
    return 'N/A';
  }

  // Display the date in the browser's timezone using the browser's locale
  return dateInput.toLocaleDateString(getBrowserLocale(), options);
}

/**
 * Get a date in YYYY-MM-DD format using the browser's timezone.
 * This is for display purposes only - backend should always receive UTC.
 *
 * @param dateInput The date to format
 * @returns Date string in YYYY-MM-DD format in browser's timezone
 */
export function getDateInBrowserTimezone(dateInput: Date): string {
  const year = dateInput.getFullYear();
  const month = String(dateInput.getMonth() + 1).padStart(2, '0');
  const day = String(dateInput.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * Convert weight to pounds based on user's preferred unit.
 *
 * @param weight The weight value to convert
 * @param preferredUnit The user's preferred unit for this exercise
 * @returns Weight converted to pounds
 */
export function convertWeightToPounds(
  weight: number,
  preferredUnit: 'KG' | 'LBS' | undefined
): number {
  if (!weight) return 0;

  switch (preferredUnit) {
    case 'KG':
      return weight * 2.20462; // Convert kg to lbs
    case 'LBS':
    default:
      return weight; // Already in lbs or default to lbs
  }
}

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
 * @param setVolume The volume for this set (weight * reps) - should be in consistent units (lbs)
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

/**
 * Formats weight with appropriate unit based on user preferences.
 *
 * @param weight The weight value to format
 * @param preferredUnit The user's preferred unit for this exercise
 * @param includeUnit Whether to include the unit in the output (default: true)
 * @returns Formatted weight string with unit
 */
export function formatWeightWithUnit(
  weight: number | null | undefined,
  preferredUnit: 'KG' | 'LBS' | undefined,
  includeUnit: boolean = true
): string {
  if (weight === null || weight === undefined || weight === 0) {
    return '-';
  }

  // All weights are stored in KG in the database
  // Convert to user's preferred unit if needed
  let displayWeight = weight;
  let displayUnit: string;

  if (preferredUnit === 'LBS') {
    // Convert from KG to LBS
    displayWeight = weight * 2.20462;
    displayUnit = 'lbs';
  } else {
    // Default to KG (no conversion needed)
    displayUnit = 'kg';
  }

  // Round to 2 decimal places for display
  const roundedWeight = Math.round(displayWeight * 100) / 100;

  if (includeUnit) {
    return `${roundedWeight} ${displayUnit}`;
  }

  return roundedWeight.toString();
}
