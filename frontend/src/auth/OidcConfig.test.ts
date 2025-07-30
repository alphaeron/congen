// Mock the globals module before importing the module under test
jest.mock('../globals', () => ({
  DEPLOYMENT_ENVIRONMENT: 'loc',
}));

import { getOidcConfig, getAuthProviderConfig } from './OidcConfig';

describe('OidcConfig', () => {
  describe('getOidcConfig', () => {
    it('should return local configuration when environment is loc', () => {
      const config = getOidcConfig();

      expect(config).toEqual({
        authority: 'http://localhost:8080/realms/congen',
        client_id: 'congen-frontend',
        redirect_uri: 'http://localhost:3000',
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
        redirect_uri: 'http://localhost:3000',
        post_logout_redirect_uri: 'http://localhost:3000',
        silent_redirect_uri: 'http://localhost:3000/silent-renew.html',
        scope: 'openid profile email',
        response_type: 'code',
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
    });
  });
});

// Test staging environment configuration
describe('OidcConfig - Staging Environment', () => {
  let mockGlobals: any;

  beforeEach(() => {
    // Clear module cache
    jest.resetModules();
    
    // Create a new mock for staging environment
    mockGlobals = {
      DEPLOYMENT_ENVIRONMENT: 'staging',
    };
    
    // Mock the globals module with staging environment
    jest.doMock('../globals', () => mockGlobals);
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
      redirect_uri: 'https://staging.congen.com',
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
      redirect_uri: 'https://staging.congen.com',
      post_logout_redirect_uri: 'https://staging.congen.com',
      silent_redirect_uri: 'https://staging.congen.com/silent-renew.html',
      scope: 'openid profile email',
      response_type: 'code',
    });
  });
});
