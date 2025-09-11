/**
 * Tests for Keycloak Account API Client
 */

import { KeycloakAccountApiClient, createApiClient } from './client';

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
        `${mockBaseUrl}/realms/${mockRealm}/account/?userProfileMetadata=true`,
        expect.objectContaining({
          method: 'GET',
          headers: expect.objectContaining({
            Authorization: `Bearer ${mockAccessToken}`,
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
        text: async () => 'Invalid token',
      });

      const result = await client.getUserProfile();

      expect(result.success).toBe(false);
      expect(result.error).toBe('Invalid token');
    });
  });

  describe('updateUserProfile', () => {
    it('should make POST request to update user profile', async () => {
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
        `${mockBaseUrl}/realms/${mockRealm}/account/`,
        expect.objectContaining({
          method: 'POST',
          body: expect.stringContaining('"firstName":"NewFirst"'),
        })
      );

      expect(result.success).toBe(true);
    });
  });

  describe('changePassword', () => {
    it('should make POST request to change password', async () => {
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
        `${mockBaseUrl}/realms/${mockRealm}/account/credentials/password/`,
        expect.objectContaining({
          method: 'POST',
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

  it('should create client with missing authUrl using KEYCLOAK_URL', () => {
    const kcContext = {
      realm: 'congen',
      accessToken: 'mock-token',
    };

    const client = createApiClient(kcContext);
    expect(client).toBeDefined();
    // The client will use empty string for accessToken since getToken is not set up
    expect(client?.getAccessToken()).toBe('');
  });

  it('should create client with missing accessToken using empty string', () => {
    const kcContext = {
      authUrl: 'http://localhost:8080',
      realm: 'congen',
    };

    const client = createApiClient(kcContext);
    expect(client).toBeDefined();
    expect(client?.getAccessToken()).toBe('');
  });
});
