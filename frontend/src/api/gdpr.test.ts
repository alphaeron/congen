import MockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import {
  recordConsent,
  getConsentStatus,
  exportUserData,
  deleteAllPersonalData,
  getPrivacyPolicy,
} from './gdpr';
import type { UserConsent, UserDataExport, PrivacyPolicy } from './types';

// Mock the endpoint
const mock = new MockAdapter(ENDPOINT);

describe('GDPR API', () => {
  beforeEach(() => {
    mock.reset();
  });

  afterAll(() => {
    mock.restore();
  });

  describe('recordConsent', () => {
    it('should record consent given', async () => {
      const mockResponse: UserConsent = {
        keycloak_id: 'test-user-123',
        data_processing_consent: true,
        consent_timestamp: '2023-08-09T10:15:30Z',
        updated_at: '2023-08-09T10:15:30Z',
      };
      mock.onPost('/gdpr/consent').reply(config => {
        expect(config.params).toEqual({ consent: true });
        return [200, mockResponse];
      });

      const response = await recordConsent(true);

      expect(response.status).toBe(200);
      expect(response.data).toEqual(mockResponse);
      expect(mock.history.post).toHaveLength(1);
      expect(mock.history.post[0].params).toEqual({ consent: true });
    });

    it('should record consent withdrawn', async () => {
      const mockResponse: UserConsent = {
        keycloak_id: 'test-user-123',
        data_processing_consent: false,
        consent_timestamp: null,
        updated_at: '2023-08-09T10:15:30Z',
      };
      mock.onPost('/gdpr/consent').reply(config => {
        expect(config.params).toEqual({ consent: false });
        return [200, mockResponse];
      });

      const response = await recordConsent(false);

      expect(response.status).toBe(200);
      expect(response.data).toEqual(mockResponse);
      expect(mock.history.post).toHaveLength(1);
      expect(mock.history.post[0].params).toEqual({ consent: false });
    });
  });

  describe('getConsentStatus', () => {
    it('should get consent status', async () => {
      const mockResponse: UserConsent = {
        keycloak_id: 'test-user-123',
        data_processing_consent: true,
        consent_timestamp: '2023-08-09T10:15:30Z',
        updated_at: '2023-08-09T10:15:30Z',
      };
      mock.onGet('/gdpr/consent').reply(200, mockResponse);

      const response = await getConsentStatus();

      expect(response.status).toBe(200);
      expect(response.data).toEqual(mockResponse);
      expect(mock.history.get).toHaveLength(1);
    });

    it('should handle no consent given', async () => {
      const mockResponse: UserConsent = {
        keycloak_id: 'test-user-123',
        data_processing_consent: false,
        consent_timestamp: null,
        updated_at: '2023-08-09T10:15:30Z',
      };
      mock.onGet('/gdpr/consent').reply(200, mockResponse);

      const response = await getConsentStatus();

      expect(response.status).toBe(200);
      expect(response.data).toEqual(mockResponse);
    });
  });

  describe('exportUserData', () => {
    it('should export user data', async () => {
      const mockResponse: UserDataExport = {
        personal_data: {
          profile: { name: 'Test User', email: 'test@example.com' },
          preferences: { units: 'KG' },
          exercise_history: [],
          programs: [],
        },
        metadata: {
          exported_at: '2023-08-09T10:15:30Z',
          data_types: ['profile', 'preferences', 'exercise_history', 'programs'],
          total_records: 1,
        },
      };
      mock.onGet('/gdpr/export').reply(200, mockResponse);

      const response = await exportUserData();

      expect(response.status).toBe(200);
      expect(response.data).toEqual(mockResponse);
      expect(mock.history.get).toHaveLength(1);
    });
  });

  describe('deleteAllPersonalData', () => {
    it('should delete all personal data with correct confirmation', async () => {
      mock.onPost('/gdpr/delete_all_data').reply(config => {
        expect(config.params).toEqual({ confirmation: 'DELETE_ALL_MY_DATA' });
        return [200];
      });

      const response = await deleteAllPersonalData('DELETE_ALL_MY_DATA');

      expect(response.status).toBe(200);
      expect(response.data).toBeUndefined();
      expect(mock.history.post).toHaveLength(1);
      expect(mock.history.post[0].params).toEqual({ confirmation: 'DELETE_ALL_MY_DATA' });
    });

    it('should reject deletion with incorrect confirmation', async () => {
      mock.onPost('/gdpr/delete_all_data').reply(422, {
        error: "To delete all data, confirmation parameter must be 'DELETE_ALL_MY_DATA'",
      });

      try {
        await deleteAllPersonalData('WRONG_CONFIRMATION');
      } catch (error: unknown) {
        const axiosError = error as { response: { status: number; data: { error: string } } };
        expect(axiosError.response.status).toBe(422);
        expect(axiosError.response.data.error).toBe(
          "To delete all data, confirmation parameter must be 'DELETE_ALL_MY_DATA'"
        );
      }
    });
  });

  describe('getPrivacyPolicy', () => {
    it('should get privacy policy without authentication', async () => {
      const mockResponse: PrivacyPolicy = {
        data_controller: {
          name: 'Congen Fitness Application',
          contact: 'privacy@congen.app',
        },
        data_processing: {
          purposes: ['Fitness tracking', 'Workout generation'],
          legal_basis: ['Consent', 'Legitimate interest'],
          data_types: ['Profile information', 'Exercise history'],
          retention_periods: {
            user_profile: '7 years after account deletion',
            exercise_history: '3 years',
          },
        },
        user_rights: {
          access: 'You can request a copy of your data',
          rectification: 'You can correct inaccurate data',
          erasure: 'You can request deletion of your data',
          portability: 'You can export your data',
          objection: 'You can object to data processing',
          complaint: 'You can file a complaint with the data protection authority',
        },
        last_updated: '2023-08-09T00:00:00Z',
        version: '1.0.0',
      };
      mock.onGet('/gdpr/privacy_policy').reply(200, mockResponse);

      const response = await getPrivacyPolicy();

      expect(response.status).toBe(200);
      expect(response.data).toEqual(mockResponse);
      expect(mock.history.get).toHaveLength(1);
    });
  });
});
