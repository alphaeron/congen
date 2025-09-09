/**
 * Tests for Keycloak Account API Client
 */

import { KeycloakAccountApiClient, createApiClient } from './client';
import { API_ENDPOINTS } from './types';

// Mock fetch
global.fetch = jest.fn();

describe('KeycloakAccountApiClient', () => {
  const mockBaseUrl = 'http://localhost:8080';
  const mockRealm = 'congen';
  const mockAccessToken = 'mock-access-token';

  beforeEach(() => {
    (fetch as jest.Mock).mockClear();
  });

  describe('constructor', () => {
    it('should initialize with correct parameters', () => {
      const client = new KeycloakAccountApiClient(mockBaseUrl, mockRealm, mockAccessToken);
      expect(client).toBeDefined();
    });
  });

  describe('getUserProfile', () => {
    it('should make GET request to user profile endpoint', async () => {
      const client = new KeycloakAccountApiClient(mockBaseUrl, mockRealm, mockAccessToken);
      const mockUserProfile = {
        id: 'user-123',
        username: 'testuser',
        email: 'test@example.com',
        firstName: 'Test',
        lastName: 'User',
      };

      (fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => mockUserProfile,
      });

      const result = await client.getUserProfile();

      expect(fetch).toHaveBeenCalledWith(
        `${mockBaseUrl}${API_ENDPOINTS.USER_PROFILE.replace('{realm}', mockRealm)}`,
        expect.objectContaining({
          method: 'GET',
          headers: expect.objectContaining({
            'Authorization': `Bearer ${mockAccessToken}`,
            'Content-Type': 'application/json',
          }),
        })
      );

      expect(result.success).toBe(true);
      expect(result.data).toEqual(mockUserProfile);
    });

    it('should handle API errors', async () => {
      const client = new KeycloakAccountApiClient(mockBaseUrl, mockRealm, mockAccessToken);

      (fetch as jest.Mock).mockResolvedValueOnce({
        ok: false,
        status: 401,
        statusText: 'Unauthorized',
        json: async () => ({ errorMessage: 'Invalid token' }),
      });

      const result = await client.getUserProfile();

      expect(result.success).toBe(false);
      expect(result.error).toBe('Invalid token');
    });
  });

  describe('updateUserProfile', () => {
    it('should make PUT request to update user profile', async () => {
      const client = new KeycloakAccountApiClient(mockBaseUrl, mockRealm, mockAccessToken);
      const updateData = {
        email: 'newemail@example.com',
        firstName: 'NewFirst',
        lastName: 'NewLast',
      };

      (fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => ({ ...updateData, id: 'user-123' }),
      });

      const result = await client.updateUserProfile(updateData);

      expect(fetch).toHaveBeenCalledWith(
        `${mockBaseUrl}${API_ENDPOINTS.UPDATE_PROFILE.replace('{realm}', mockRealm)}`,
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify(updateData),
        })
      );

      expect(result.success).toBe(true);
    });
  });

  describe('changePassword', () => {
    it('should make PUT request to change password', async () => {
      const client = new KeycloakAccountApiClient(mockBaseUrl, mockRealm, mockAccessToken);
      const passwordData = {
        currentPassword: 'oldpass',
        newPassword: 'newpass',
        confirmPassword: 'newpass',
      };

      (fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        status: 204,
      });

      const result = await client.changePassword(passwordData);

      expect(fetch).toHaveBeenCalledWith(
        `${mockBaseUrl}${API_ENDPOINTS.CHANGE_PASSWORD.replace('{realm}', mockRealm)}`,
        expect.objectContaining({
          method: 'PUT',
          body: JSON.stringify(passwordData),
        })
      );

      expect(result.success).toBe(true);
    });
  });
});

describe('createApiClient', () => {
  it('should create client with valid kcContext', () => {
    const kcContext = {
      authUrl: 'http://localhost:8080',
      realm: 'congen',
      accessToken: 'mock-token',
    };

    const client = createApiClient(kcContext);
    expect(client).toBeDefined();
  });

  it('should return null with missing authUrl', () => {
    const kcContext = {
      realm: 'congen',
      accessToken: 'mock-token',
    };

    const client = createApiClient(kcContext);
    expect(client).toBeNull();
  });

  it('should return null with missing accessToken', () => {
    const kcContext = {
      authUrl: 'http://localhost:8080',
      realm: 'congen',
    };

    const client = createApiClient(kcContext);
    expect(client).toBeNull();
  });
});
