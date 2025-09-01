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

      expect(response).toEqual(mockResponse);
      expect(mock.history.post).toHaveLength(1);
      expect(mock.history.post[0].params).toEqual({ consent: true });
    });

    it('should record consent withdrawn', async () => {
      const mockResponse: UserConsent = {
        keycloak_id: 'test-user-123',
        data_processing_consent: false,
        consent_timestamp: undefined,
        updated_at: '2023-08-09T10:15:30Z',
      };
      mock.onPost('/gdpr/consent').reply(config => {
        expect(config.params).toEqual({ consent: false });
        return [200, mockResponse];
      });

      const response = await recordConsent(false);

      expect(response).toEqual(mockResponse);
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

      expect(response).toEqual(mockResponse);
      expect(mock.history.get).toHaveLength(1);
    });

    it('should handle no consent given', async () => {
      const mockResponse: UserConsent = {
        keycloak_id: 'test-user-123',
        data_processing_consent: false,
        consent_timestamp: undefined,
        updated_at: '2023-08-09T10:15:30Z',
      };
      mock.onGet('/gdpr/consent').reply(200, mockResponse);

      const response = await getConsentStatus();

      expect(response).toEqual(mockResponse);
    });
  });

  describe('exportUserData', () => {
    it('should export user data', async () => {
      const mockResponse: UserDataExport = {
        keycloak_id: 'test-user-123',
        name: 'Test User',
        created_at: '2023-08-09T10:15:30Z',
        updated_at: '2023-08-09T10:15:30Z',
        data_processing_consent: true,
        consent_timestamp: '2023-08-09T10:15:30Z',
        export_timestamp: '2023-08-09T10:15:30Z',
        user_equipment: [],
        user_exercise_preferences: [],
        user_program_preferences: {},
        user_one_rep_max: [],
        user_weight_unit_preferences: [],
        training_programs: [],
        audit_logs: [],
        data_retention_policies: [],
      };
      mock.onGet('/gdpr/export').reply(200, mockResponse);

      const response = await exportUserData();

      expect(response).toEqual(mockResponse);
      expect(mock.history.get).toHaveLength(1);
    });
  });

  describe('deleteAllPersonalData', () => {
    it('should delete all personal data with correct confirmation', async () => {
      mock.onDelete('/gdpr/delete_all_data').reply(config => {
        expect(config.params).toEqual({ confirmation: 'DELETE_ALL_MY_DATA' });
        return [200];
      });

      const response = await deleteAllPersonalData('DELETE_ALL_MY_DATA');

      expect(response).toBeUndefined();
      expect(mock.history.delete).toHaveLength(1);
      expect(mock.history.delete[0].params).toEqual({ confirmation: 'DELETE_ALL_MY_DATA' });
    });

    it('should reject deletion with incorrect confirmation', async () => {
      mock.onDelete('/gdpr/delete_all_data').reply(422, {
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

      expect(response).toEqual(mockResponse);
      expect(mock.history.get).toHaveLength(1);
    });

    it('should handle privacy policy API errors', async () => {
      mock.onGet('/gdpr/privacy_policy').reply(500, { message: 'Service unavailable' });

      try {
        await getPrivacyPolicy();
      } catch (error: unknown) {
        const axiosError = error as { response: { status: number; data: { message: string } } };
        expect(axiosError.response.status).toBe(500);
        expect(axiosError.response.data.message).toBe('Service unavailable');
      }
    });
  });

  describe('error handling', () => {
    it('should handle network errors for recordConsent', async () => {
      mock.onPost('/gdpr/consent').networkError();

      try {
        await recordConsent(true);
      } catch (error: unknown) {
        const axiosError = error as { message: string };
        expect(axiosError.message).toContain('Network Error');
      }
    });

    it('should handle network errors for getConsentStatus', async () => {
      mock.onGet('/gdpr/consent').networkError();

      try {
        await getConsentStatus();
      } catch (error: unknown) {
        const axiosError = error as { message: string };
        expect(axiosError.message).toContain('Network Error');
      }
    });

    it('should handle network errors for exportUserData', async () => {
      mock.onGet('/gdpr/export').networkError();

      try {
        await exportUserData();
      } catch (error: unknown) {
        const axiosError = error as { message: string };
        expect(axiosError.message).toContain('Network Error');
      }
    });

    it('should handle network errors for deleteAllPersonalData', async () => {
      mock.onDelete('/gdpr/delete_all_data').networkError();

      try {
        await deleteAllPersonalData('DELETE_ALL_MY_DATA');
      } catch (error: unknown) {
        const axiosError = error as { message: string };
        expect(axiosError.message).toContain('Network Error');
      }
    });

    it('should handle timeout errors', async () => {
      mock.onGet('/gdpr/consent').timeout();

      try {
        await getConsentStatus();
      } catch (error: unknown) {
        const axiosError = error as { code: string };
        expect(axiosError.code).toBe('ECONNABORTED');
      }
    });

    it('should handle malformed JSON responses', async () => {
      mock.onGet('/gdpr/consent').reply(200, 'invalid json');

      try {
        await getConsentStatus();
      } catch (error: unknown) {
        const axiosError = error as { message: string };
        expect(axiosError.message).toContain('Unexpected token');
      }
    });

    it('should handle 401 unauthorized responses', async () => {
      mock.onGet('/gdpr/consent').reply(401, { message: 'Unauthorized' });

      try {
        await getConsentStatus();
      } catch (error: unknown) {
        const axiosError = error as { response: { status: number; data: { message: string } } };
        expect(axiosError.response.status).toBe(401);
        expect(axiosError.response.data.message).toBe('Unauthorized');
      }
    });

    it('should handle 403 forbidden responses', async () => {
      mock.onGet('/gdpr/export').reply(403, { message: 'Forbidden' });

      try {
        await exportUserData();
      } catch (error: unknown) {
        const axiosError = error as { response: { status: number; data: { message: string } } };
        expect(axiosError.response.status).toBe(403);
        expect(axiosError.response.data.message).toBe('Forbidden');
      }
    });

    it('should handle 404 not found responses', async () => {
      mock.onGet('/gdpr/consent').reply(404, { message: 'User not found' });

      try {
        await getConsentStatus();
      } catch (error: unknown) {
        const axiosError = error as { response: { status: number; data: { message: string } } };
        expect(axiosError.response.status).toBe(404);
        expect(axiosError.response.data.message).toBe('User not found');
      }
    });

    it('should handle 429 rate limit responses', async () => {
      mock.onPost('/gdpr/consent').reply(429, { message: 'Too many requests' });

      try {
        await recordConsent(true);
      } catch (error: unknown) {
        const axiosError = error as { response: { status: number; data: { message: string } } };
        expect(axiosError.response.status).toBe(429);
        expect(axiosError.response.data.message).toBe('Too many requests');
      }
    });

    it('should handle 503 service unavailable responses', async () => {
      mock
        .onDelete('/gdpr/delete_all_data')
        .reply(503, { message: 'Service temporarily unavailable' });

      try {
        await deleteAllPersonalData('DELETE_ALL_MY_DATA');
      } catch (error: unknown) {
        const axiosError = error as { response: { status: number; data: { message: string } } };
        expect(axiosError.response.status).toBe(503);
        expect(axiosError.response.data.message).toBe('Service temporarily unavailable');
      }
    });
  });

  describe('validation edge cases', () => {
    it('should handle empty consent response', async () => {
      const emptyResponse: UserConsent = {
        keycloak_id: '',
        data_processing_consent: false,
        consent_timestamp: undefined,
        updated_at: '',
      };
      mock.onGet('/gdpr/consent').reply(200, emptyResponse);

      const response = await getConsentStatus();

      expect(response.status).toBe(200);
      expect(response.data).toEqual(emptyResponse);
    });

    it('should handle export with minimal data', async () => {
      const minimalExport: UserDataExport = {
        keycloak_id: 'test-user-123',
        name: 'Test User',
        created_at: '2023-08-09T10:15:30Z',
        updated_at: '2023-08-09T10:15:30Z',
        data_processing_consent: false,
        consent_timestamp: undefined,
        export_timestamp: '2023-08-09T10:15:30Z',
        user_equipment: [],
        user_exercise_preferences: [],
        user_program_preferences: undefined,
        user_one_rep_max: [],
        user_weight_unit_preferences: [],
        training_programs: [],
        audit_logs: [],
        data_retention_policies: [],
      };
      mock.onGet('/gdpr/export').reply(200, minimalExport);

      const response = await exportUserData();

      expect(response.status).toBe(200);
      expect(response.data).toEqual(minimalExport);
    });

    it('should handle very long confirmation text', async () => {
      const longConfirmation = 'DELETE_ALL_MY_DATA'.repeat(100);
      mock.onDelete('/gdpr/delete_all_data').reply(config => {
        expect(config.params).toEqual({ confirmation: longConfirmation });
        return [200];
      });

      const response = await deleteAllPersonalData(longConfirmation);

      expect(response.status).toBe(200);
      expect(mock.history.delete[0].params).toEqual({ confirmation: longConfirmation });
    });

    it('should handle special characters in confirmation text', async () => {
      const specialConfirmation = 'DELETE_ALL_MY_DATA_!@#$%^&*()';
      mock.onDelete('/gdpr/delete_all_data').reply(422, {
        error: "To delete all data, confirmation parameter must be 'DELETE_ALL_MY_DATA'",
      });

      try {
        await deleteAllPersonalData(specialConfirmation);
      } catch (error: unknown) {
        const axiosError = error as { response: { status: number; data: { error: string } } };
        expect(axiosError.response.status).toBe(422);
        expect(axiosError.response.data.error).toBe(
          "To delete all data, confirmation parameter must be 'DELETE_ALL_MY_DATA'"
        );
      }
    });
  });
});
