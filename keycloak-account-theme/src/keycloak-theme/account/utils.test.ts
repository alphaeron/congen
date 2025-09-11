import { getFrontendUrl, getLogoutUrl, handleLogout, navigateToFrontend } from './utils';
import type { KcContext } from './KcContext';

// Mock process.env
const originalEnv = process.env;

// Mock window.location
const mockLocation = {
  href: '',
};

Object.defineProperty(window, 'location', {
  value: mockLocation,
  writable: true,
});

describe('utils', () => {
  beforeEach(() => {
    jest.resetModules();
    process.env = { ...originalEnv };
    mockLocation.href = '';
  });

  afterAll(() => {
    process.env = originalEnv;
  });

  describe('getFrontendUrl', () => {
    it('should return default localhost URL when REACT_APP_FRONTEND_URL is not set', () => {
      delete process.env.REACT_APP_FRONTEND_URL;
      expect(getFrontendUrl()).toBe('http://localhost:3000');
    });

    it('should return REACT_APP_FRONTEND_URL when set', () => {
      process.env.REACT_APP_FRONTEND_URL = 'https://staging.congen.com';
      expect(getFrontendUrl()).toBe('https://staging.congen.com');
    });
  });

  describe('getLogoutUrl', () => {
    const mockKcContext: KcContext = {
      authUrl: 'https://auth.example.com',
      realm: {
        name: 'test-realm',
      },
    } as KcContext;

    it('should generate correct logout URL', () => {
      const result = getLogoutUrl(mockKcContext);
      expect(result).toBe(
        'https://auth.example.com/realms/test-realm/protocol/openid-connect/logout'
      );
    });

    it('should handle missing realm name', () => {
      const contextWithoutRealm = {
        ...mockKcContext,
        realm: undefined,
      } as KcContext;

      const result = getLogoutUrl(contextWithoutRealm);
      expect(result).toBe(
        'https://auth.example.com/realms/undefined/protocol/openid-connect/logout'
      );
    });
  });

  describe('handleLogout', () => {
    const mockKcContext: KcContext = {
      authUrl: 'https://auth.example.com',
      realm: {
        name: 'test-realm',
      },
    } as KcContext;

    it('should redirect to logout URL', () => {
      handleLogout(mockKcContext);
      expect(mockLocation.href).toBe(
        'https://auth.example.com/realms/test-realm/protocol/openid-connect/logout'
      );
    });
  });

  describe('navigateToFrontend', () => {
    it('should navigate to frontend URL with default path', () => {
      process.env.REACT_APP_FRONTEND_URL = 'https://app.example.com';
      navigateToFrontend();
      expect(mockLocation.href).toBe('https://app.example.com');
    });

    it('should navigate to frontend URL with custom path', () => {
      process.env.REACT_APP_FRONTEND_URL = 'https://app.example.com';
      navigateToFrontend('/dashboard');
      expect(mockLocation.href).toBe('https://app.example.com/dashboard');
    });

    it('should use default localhost URL when REACT_APP_FRONTEND_URL is not set', () => {
      delete process.env.REACT_APP_FRONTEND_URL;
      navigateToFrontend('/profile');
      expect(mockLocation.href).toBe('http://localhost:3000/profile');
    });
  });
});
