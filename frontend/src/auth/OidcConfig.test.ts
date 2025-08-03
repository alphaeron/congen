// Mock environment variables
const originalEnv = process.env;

beforeAll(() => {
  process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT = 'loc';
});

afterAll(() => {
  process.env = originalEnv;
});

// Mock the globals module before importing the module under test
jest.mock('../globals', () => ({
  DEPLOYMENT_ENVIRONMENT: 'loc',
  KEYCLOAK_URL: 'http://localhost:8080',
  BASE_URL: 'http://localhost',
}));

import { getOidcConfig, getAuthProviderConfig } from './OidcConfig';

describe('OidcConfig', () => {
  describe('getOidcConfig', () => {
    it('should return local configuration when environment is loc', () => {
      const config = getOidcConfig();

      expect(config).toEqual({
        authority: 'http://localhost:8080/realms/congen',
        client_id: 'congen-frontend',
        redirect_uri: 'http://localhost:3000/auth/callback',
        post_logout_redirect_uri: 'http://localhost:3000',
        silent_redirect_uri: 'http://localhost:3000/silent-renew.html',
      });
    });

    it('should construct URLs correctly from base URL', () => {
      const config = getOidcConfig();

      // Verify the structure is correct
      expect(config.authority).toContain('localhost:8080');
      expect(config.authority).toContain('/realms/congen');
      expect(config.redirect_uri).toContain('localhost:3000');
      expect(config.client_id).toBe('congen-frontend');
    });
  });

  describe('getAuthProviderConfig', () => {
    it('should return complete AuthProvider configuration', () => {
      const config = getAuthProviderConfig();

      expect(config).toEqual({
        authority: 'http://localhost:8080/realms/congen',
        client_id: 'congen-frontend',
        redirect_uri: 'http://localhost:3000/auth/callback',
        post_logout_redirect_uri: 'http://localhost:3000',
        silent_redirect_uri: 'http://localhost:3000/silent-renew.html',
        scope: 'openid profile email',
        response_type: 'code',
        loadUserInfo: true,
        monitorSession: false,
        metadata: {
          authorization_endpoint:
            'http://localhost:8080/realms/congen/protocol/openid-connect/auth',
          token_endpoint: 'http://localhost:8080/realms/congen/protocol/openid-connect/token',
          end_session_endpoint:
            'http://localhost:8080/realms/congen/protocol/openid-connect/logout',
          userinfo_endpoint: 'http://localhost:8080/realms/congen/protocol/openid-connect/userinfo',
          jwks_uri: 'http://localhost:8080/realms/congen/protocol/openid-connect/certs',
          issuer: 'http://localhost:8080/realms/congen',
        },
      });
    });

    it('should include all required OIDC properties', () => {
      const config = getAuthProviderConfig();

      expect(config).toHaveProperty('authority');
      expect(config).toHaveProperty('client_id');
      expect(config).toHaveProperty('redirect_uri');
      expect(config).toHaveProperty('post_logout_redirect_uri');
      expect(config).toHaveProperty('silent_redirect_uri');
      expect(config).toHaveProperty('scope');
      expect(config).toHaveProperty('response_type');
      expect(config).toHaveProperty('loadUserInfo');
      expect(config).toHaveProperty('monitorSession');
      expect(config).toHaveProperty('metadata');
    });
  });
});

// Test staging environment configuration
describe('OidcConfig - Staging Environment', () => {
  beforeEach(() => {
    // Clear module cache
    jest.resetModules();

    // Mock the globals module with staging environment
    jest.doMock('../globals', () => ({
      DEPLOYMENT_ENVIRONMENT: 'staging',
      KEYCLOAK_URL: 'https://staging.congen.com',
      BASE_URL: 'https://staging.congen.com',
    }));
  });

  afterEach(() => {
    // Restore the original mock
    jest.dontMock('../globals');
    jest.resetModules();
  });

  it('should return staging configuration when environment is staging', async () => {
    // Re-import the module with staging environment
    const { getOidcConfig } = await import('./OidcConfig');

    const config = getOidcConfig();

    expect(config).toEqual({
      authority: 'https://staging.congen.com/realms/congen',
      client_id: 'congen-frontend',
      redirect_uri: 'https://staging.congen.com/auth/callback',
      post_logout_redirect_uri: 'https://staging.congen.com',
      silent_redirect_uri: 'https://staging.congen.com/silent-renew.html',
    });
  });

  it('should return staging AuthProvider configuration', async () => {
    // Re-import the module with staging environment
    const { getAuthProviderConfig } = await import('./OidcConfig');

    const config = getAuthProviderConfig();

    expect(config).toEqual({
      authority: 'https://staging.congen.com/realms/congen',
      client_id: 'congen-frontend',
      redirect_uri: 'https://staging.congen.com/auth/callback',
      post_logout_redirect_uri: 'https://staging.congen.com',
      silent_redirect_uri: 'https://staging.congen.com/silent-renew.html',
      scope: 'openid profile email',
      response_type: 'code',
      loadUserInfo: true,
      monitorSession: false,
      metadata: {
        authorization_endpoint:
          'https://staging.congen.com/realms/congen/protocol/openid-connect/auth',
        token_endpoint: 'https://staging.congen.com/realms/congen/protocol/openid-connect/token',
        end_session_endpoint:
          'https://staging.congen.com/realms/congen/protocol/openid-connect/logout',
        userinfo_endpoint:
          'https://staging.congen.com/realms/congen/protocol/openid-connect/userinfo',
        jwks_uri: 'https://staging.congen.com/realms/congen/protocol/openid-connect/certs',
        issuer: 'https://staging.congen.com/realms/congen',
      },
    });
  });
});
