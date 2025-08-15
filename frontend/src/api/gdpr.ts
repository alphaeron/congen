import { ENDPOINT } from './endpoint';
import type { UserConsent, UserDataExport, PrivacyPolicy } from './types';

import type { AxiosResponse } from 'axios';

/**
 * Records user consent for data processing.
 *
 * @param consent Whether consent is given (true) or withdrawn (false)
 * @returns Promise that resolves when consent is recorded
 */
export async function recordConsent(consent: boolean): Promise<AxiosResponse<UserConsent>> {
  return ENDPOINT.post('/gdpr/consent', null, {
    params: { consent },
  });
}

/**
 * Gets the current consent status for the authenticated user.
 *
 * @returns Promise that resolves to the user's current consent status
 */
export async function getConsentStatus(): Promise<AxiosResponse<UserConsent>> {
  return ENDPOINT.get('/gdpr/consent');
}

/**
 * Exports all personal data for the authenticated user.
 *
 * @returns Promise that resolves to the user's complete data export
 */
export async function exportUserData(): Promise<AxiosResponse<UserDataExport>> {
  return ENDPOINT.get('/gdpr/export');
}

/**
 * Permanently deletes all personal data for the authenticated user.
 * This implements the GDPR "Right to be Forgotten" (Article 17).
 *
 * @param confirmation Must be "DELETE_ALL_MY_DATA" to confirm deletion
 * @returns Promise that resolves when deletion is complete
 */
export async function deleteAllPersonalData(confirmation: string): Promise<AxiosResponse<void>> {
  return ENDPOINT.post('/gdpr/delete_all_data', null, {
    params: { confirmation },
  });
}

/**
 * Gets the privacy policy and data processing information.
 * This endpoint does not require authentication.
 *
 * @returns Promise that resolves to the privacy policy information
 */
export async function getPrivacyPolicy(): Promise<AxiosResponse<PrivacyPolicy>> {
  return ENDPOINT.get('/gdpr/privacy_policy');
}
