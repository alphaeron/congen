import { REQUEST } from './endpoint';
import type { UserConsent, UserDataExport, PrivacyPolicy } from './types';

/**
 * Records user consent for data processing.
 *
 * @param consent Whether consent is given (true) or withdrawn (false)
 * @returns Promise that resolves when consent is recorded
 */
export async function recordConsent(consent: boolean): Promise<UserConsent> {
  return REQUEST({
    method: 'POST',
    url: '/gdpr/consent',
    params: { consent },
  });
}

/**
 * Gets the current consent status for the authenticated user.
 *
 * @returns Promise that resolves to the user's current consent status
 */
export async function getConsentStatus(): Promise<UserConsent> {
  return REQUEST({
    method: 'GET',
    url: '/gdpr/consent',
  });
}

/**
 * Exports all personal data for the authenticated user.
 *
 * @returns Promise that resolves to the user's complete data export
 */
export async function exportUserData(): Promise<UserDataExport> {
  return REQUEST({
    method: 'GET',
    url: '/gdpr/export',
  });
}

/**
 * Fetches complete user data export including all workout data.
 *
 * This endpoint returns all user data in a single call, including:
 * - Training programs with complete workout hierarchy
 * - Workout stages with exercises and set schemes
 * - One-rep max records
 * - User preferences
 *
 * This is much more efficient than making separate API calls for each workout.
 *
 * @param options Optional configuration including forceRefresh flag
 * @returns Promise containing complete user data export
 */
export const getUserDataExport = async (options: { forceRefresh?: boolean } = {}): Promise<UserDataExport> => {
  return REQUEST({
    method: 'GET',
    url: '/gdpr/export',
    timeout: 5000, // 5 second timeout for large data export
    forceRefresh: options.forceRefresh,
  });
};

/**
 * Permanently deletes all personal data for the authenticated user.
 * This implements the GDPR "Right to be Forgotten" (Article 17).
 *
 * @param confirmation Must be "DELETE_ALL_MY_DATA" to confirm deletion
 * @returns Promise that resolves when deletion is complete
 */
export async function deleteAllPersonalData(confirmation: string): Promise<void> {
  return REQUEST({
    method: 'DELETE',
    url: '/gdpr/delete_all_data',
    params: { confirmation },
  });
}

/**
 * Gets the privacy policy and data processing information.
 * This endpoint does not require authentication.
 *
 * @returns Promise that resolves to the privacy policy information
 */
export async function getPrivacyPolicy(): Promise<PrivacyPolicy> {
  return REQUEST({
    method: 'GET',
    url: '/gdpr/privacy_policy',
  });
}
