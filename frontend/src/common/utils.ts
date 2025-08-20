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
