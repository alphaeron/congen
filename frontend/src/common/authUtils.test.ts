import { clearAuthenticationState, isTokenExpired, isTokenExpiringSoon, isTokenMalformed, redirectToLogin, handleAuthenticationFailure, decodeToken, hasAnyPermission } from './authUtils';

// Mock window.location
const mockLocation = {
  href: '',
  pathname: '/dashboard',
};

Object.defineProperty(window, 'location', {
  value: mockLocation,
  writable: true,
});

// Mock localStorage and sessionStorage
const mockLocalStorage = {
  removeItem: jest.fn(),
  length: 0,
  key: jest.fn(),
};

const mockSessionStorage = {
  removeItem: jest.fn(),
};

Object.defineProperty(window, 'localStorage', {
  value: mockLocalStorage,
  writable: true,
});

Object.defineProperty(window, 'sessionStorage', {
  value: mockSessionStorage,
  writable: true,
});

// Mock console methods
const mockConsole = {
  warn: jest.fn(),
  log: jest.fn(),
};

Object.defineProperty(console, 'warn', {
  value: mockConsole.warn,
  writable: true,
});

Object.defineProperty(console, 'log', {
  value: mockConsole.log,
  writable: true,
});

describe('authUtils', () => {
  beforeEach(() => {
    jest.clearAllMocks();
    mockLocation.href = '';
    mockLocation.pathname = '/dashboard';
  });

  describe('decodeToken', () => {
    it('should decode valid JWT token', () => {
      const payload = { sub: '123', exp: Date.now() / 1000, name: 'Test User' };
      const token = `header.${btoa(JSON.stringify(payload))}.signature`;
      
      const result = decodeToken(token);
      
      expect(result).toEqual(payload);
    });

    it('should return null for token with invalid base64 payload', () => {
      expect(decodeToken('header.invalid-base64.signature')).toBeNull();
    });

    it('should return null for token with invalid JSON payload', () => {
      const invalidJson = btoa('{invalid json}');
      expect(decodeToken(`header.${invalidJson}.signature`)).toBeNull();
    });

    it('should return null for empty token', () => {
      expect(decodeToken('')).toBeNull();
    });

    it('should return null for malformed token', () => {
      expect(decodeToken('invalid-token')).toBeNull();
    });

    it('should handle tokens with missing parts gracefully', () => {
      // The original implementation doesn't validate token format, it just tries to decode
      // and returns null if it fails
      expect(decodeToken('header.payload')).toBeNull();
      expect(decodeToken('header.payload.signature.extra')).toBeNull();
    });
  });

  describe('hasAnyPermission', () => {
    it('should return false for null user', () => {
      const permissions = ['admin', 'user'];
      expect(hasAnyPermission(null, permissions)).toBe(false);
    });

    it('should return false for empty permissions array', () => {
      const user = {
        profile: {
          roles: ['admin', 'user']
        }
      };
      expect(hasAnyPermission(user as any, [])).toBe(false);
    });

    it('should return false for user with no roles', () => {
      const user = {
        profile: {}
      };
      const permissions = ['admin', 'user'];
      expect(hasAnyPermission(user as any, permissions)).toBe(false);
    });

    it('should return true when user has matching role in profile.roles', () => {
      const user = {
        profile: {
          roles: ['admin', 'moderator']
        }
      };
      const permissions = ['admin', 'user'];
      expect(hasAnyPermission(user as any, permissions)).toBe(true);
    });

    it('should return true when user has matching role in realm_access.roles', () => {
      const user = {
        profile: {
          realm_access: {
            roles: ['user', 'editor']
          }
        }
      };
      const permissions = ['admin', 'user'];
      expect(hasAnyPermission(user as any, permissions)).toBe(true);
    });

    it('should return false when user has no matching roles', () => {
      const user = {
        profile: {
          roles: ['moderator', 'editor']
        }
      };
      const permissions = ['admin', 'user'];
      expect(hasAnyPermission(user as any, permissions)).toBe(false);
    });

    it('should fall back to profile.roles when realm_access.roles is empty', () => {
      const user = {
        profile: {
          roles: [],
          realm_access: {
            roles: ['user']
          }
        }
      };
      const permissions = ['user'];
      // Should return true because it falls back to realm_access.roles
      expect(hasAnyPermission(user as any, permissions)).toBe(true);
    });
  });

  describe('clearAuthenticationState', () => {
    it('should clear OIDC user data from storage', () => {
      clearAuthenticationState();

      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('oidc.user:congen:congen-frontend');
      expect(mockSessionStorage.removeItem).toHaveBeenCalledWith('oidc.user:congen:congen-frontend');
    });

    it('should clear other auth-related storage items', () => {
      clearAuthenticationState();

      expect(mockSessionStorage.removeItem).toHaveBeenCalledWith('congen_redirect_after_password_change');
      expect(mockSessionStorage.removeItem).toHaveBeenCalledWith('congen_redirect_after_profile_edit');
    });

    it('should clear other OIDC-related keys from localStorage', () => {
      // Mock localStorage to have some OIDC keys
      mockLocalStorage.length = 3;
      mockLocalStorage.key
        .mockReturnValueOnce('oidc.some.key')
        .mockReturnValueOnce('other.key')
        .mockReturnValueOnce('oidc.another.key');

      clearAuthenticationState();

      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('oidc.some.key');
      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('oidc.another.key');
    });
  });

  describe('isTokenMalformed', () => {
    it('should return true for token with wrong number of parts', () => {
      expect(isTokenMalformed('header.payload')).toBe(true);
      expect(isTokenMalformed('header.payload.signature.extra')).toBe(true);
    });

    it('should return true for token with invalid base64 payload', () => {
      expect(isTokenMalformed('header.invalid-base64.signature')).toBe(true);
    });

    it('should return true for token with invalid JSON payload', () => {
      const invalidJson = btoa('{invalid json}');
      expect(isTokenMalformed(`header.${invalidJson}.signature`)).toBe(true);
    });

    it('should return false for valid JWT token', () => {
      const validPayload = btoa(JSON.stringify({ sub: '123', exp: Date.now() / 1000 }));
      expect(isTokenMalformed(`header.${validPayload}.signature`)).toBe(false);
    });
  });

  describe('isTokenExpired', () => {
    it('should return true for expired token', () => {
      // Create a token that expired 1 hour ago
      const expiredTime = Math.floor((Date.now() - 60 * 60 * 1000) / 1000);
      const payload = { exp: expiredTime };
      const token = `header.${btoa(JSON.stringify(payload))}.signature`;

      expect(isTokenExpired(token)).toBe(true);
    });

    it('should return false for valid token', () => {
      // Create a token that expires in 1 hour
      const futureTime = Math.floor((Date.now() + 60 * 60 * 1000) / 1000);
      const payload = { exp: futureTime };
      const token = `header.${btoa(JSON.stringify(payload))}.signature`;

      expect(isTokenExpired(token)).toBe(false);
    });

    it('should return true for malformed token', () => {
      expect(isTokenExpired('invalid.token')).toBe(true);
    });

    it('should return true for token with invalid payload', () => {
      const token = 'header.invalid-payload.signature';
      expect(isTokenExpired(token)).toBe(true);
    });
  });

  describe('isTokenExpiringSoon', () => {
    it('should return true for token expiring within window', () => {
      // Create a token that expires in 2 minutes (within 5 minute window)
      const futureTime = Math.floor((Date.now() + 2 * 60 * 1000) / 1000);
      const payload = { exp: futureTime };
      const token = `header.${btoa(JSON.stringify(payload))}.signature`;

      expect(isTokenExpiringSoon(token)).toBe(true);
    });

    it('should return false for token expiring outside window', () => {
      // Create a token that expires in 10 minutes (outside 5 minute window)
      const futureTime = Math.floor((Date.now() + 10 * 60 * 1000) / 1000);
      const payload = { exp: futureTime };
      const token = `header.${btoa(JSON.stringify(payload))}.signature`;

      expect(isTokenExpiringSoon(token)).toBe(false);
    });

    it('should use custom window size', () => {
      // Create a token that expires in 2 minutes
      const futureTime = Math.floor((Date.now() + 2 * 60 * 1000) / 1000);
      const payload = { exp: futureTime };
      const token = `header.${btoa(JSON.stringify(payload))}.signature`;

      // With 1 minute window, should return false
      expect(isTokenExpiringSoon(token, 60 * 1000)).toBe(false);
    });

    it('should return true for malformed token', () => {
      expect(isTokenExpiringSoon('invalid.token')).toBe(true);
    });
  });

  describe('redirectToLogin', () => {
    it('should redirect to login when not on login page', () => {
      mockLocation.pathname = '/dashboard';
      
      redirectToLogin();
      
      expect(mockLocation.href).toBe('/login');
    });

    it('should not redirect when already on login page', () => {
      mockLocation.pathname = '/login';
      
      redirectToLogin();
      
      expect(mockLocation.href).toBe('');
    });
  });

  describe('handleAuthenticationFailure', () => {
    it('should clear authentication state', () => {
      handleAuthenticationFailure('Test failure');
      
      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('oidc.user:congen:congen-frontend');
      expect(mockSessionStorage.removeItem).toHaveBeenCalledWith('oidc.user:congen:congen-frontend');
    });

    it('should redirect to login', () => {
      handleAuthenticationFailure('Test failure');
      
      expect(mockLocation.href).toBe('/login');
    });

    it('should use default reason when none provided', () => {
      handleAuthenticationFailure();
      
      // Should work without throwing errors
      expect(mockLocalStorage.removeItem).toHaveBeenCalledWith('oidc.user:congen:congen-frontend');
    });
  });
});
