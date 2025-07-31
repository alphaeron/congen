import { refreshToken, exchangeCodeForTokens } from './auth';

// Mock fetch globally
global.fetch = jest.fn();

describe('Auth API Functions', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('refreshToken', () => {
    it('should make correct token request for refresh', async () => {
      const mockResponse = {
        access_token: 'new-access-token',
        refresh_token: 'new-refresh-token',
        expires_in: 300,
        refresh_expires_in: 1800,
        token_type: 'Bearer',
      };

      (fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      });

      const result = await refreshToken('old-refresh-token');

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/realms/congen/protocol/openid-connect/token'),
        expect.objectContaining({
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: expect.stringContaining('grant_type=refresh_token'),
        })
      );

      expect(result).toEqual(mockResponse);
    });

    it('should handle refresh token errors', async () => {
      const errorResponse = {
        error: 'invalid_grant',
        error_description: 'Refresh token expired',
      };

      (fetch as jest.Mock).mockResolvedValueOnce({
        ok: false,
        json: async () => errorResponse,
      });

      await expect(refreshToken('invalid-token')).rejects.toThrow('Refresh token expired');
    });
  });

  describe('exchangeCodeForTokens', () => {
    beforeEach(() => {
      // Mock sessionStorage
      Object.defineProperty(window, 'sessionStorage', {
        value: {
          getItem: jest.fn(),
          removeItem: jest.fn(),
        },
        writable: true,
      });
    });

    it('should exchange code for tokens successfully', async () => {
      const mockResponse = {
        access_token: 'access-token',
        refresh_token: 'refresh-token',
        expires_in: 300,
        refresh_expires_in: 1800,
        token_type: 'Bearer',
      };

      (fetch as jest.Mock).mockResolvedValueOnce({
        ok: true,
        json: async () => mockResponse,
      });

      // Mock sessionStorage values
      (window.sessionStorage.getItem as jest.Mock)
        .mockReturnValueOnce('stored-state') // auth_state
        .mockReturnValueOnce('code-verifier'); // code_verifier

      const result = await exchangeCodeForTokens('auth-code', 'stored-state');

      expect(fetch).toHaveBeenCalledWith(
        expect.stringContaining('/realms/congen/protocol/openid-connect/token'),
        expect.objectContaining({
          method: 'POST',
          headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
          },
          body: expect.stringContaining('grant_type=authorization_code'),
        })
      );

      expect(result).toEqual(mockResponse);
    });

    it('should throw error on state mismatch', async () => {
      (window.sessionStorage.getItem as jest.Mock)
        .mockReturnValueOnce('different-state');

      await expect(exchangeCodeForTokens('auth-code', 'wrong-state')).rejects.toThrow('State mismatch - possible CSRF attack');
    });

    it('should throw error when code verifier is missing', async () => {
      (window.sessionStorage.getItem as jest.Mock)
        .mockReturnValueOnce('stored-state') // auth_state
        .mockReturnValueOnce(null); // code_verifier

      await expect(exchangeCodeForTokens('auth-code', 'stored-state')).rejects.toThrow('Code verifier not found');
    });
  });
}); 