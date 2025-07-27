import { getKeycloakConfig } from './KeycloakConfig';

// Mock the globals module
jest.mock('../globals', () => ({
  DEPLOYMENT_ENVIRONMENT: 'loc',
}));

describe('KeycloakConfig', () => {
  describe('getKeycloakConfig', () => {
    it('should return local configuration when environment is loc', () => {
      // The default environment is 'loc' from the mock
      const config = getKeycloakConfig();

      expect(config).toEqual({
        url: 'http://localhost:8081/auth',
        realm: 'congen',
        clientId: 'congen-frontend',
      });
    });

    it('should return staging configuration when environment is staging', () => {
      // Temporarily override the mock
      const mockGlobals = require('../globals');
      mockGlobals.DEPLOYMENT_ENVIRONMENT = 'staging';

      const config = getKeycloakConfig();

      expect(config).toEqual({
        url: 'https://staging.congen.com/auth',
        realm: 'congen',
        clientId: 'congen-frontend',
      });

      // Reset to default
      mockGlobals.DEPLOYMENT_ENVIRONMENT = 'loc';
    });

    it('should return production configuration when environment is production', () => {
      // Temporarily override the mock
      const mockGlobals = require('../globals');
      mockGlobals.DEPLOYMENT_ENVIRONMENT = 'production';

      const config = getKeycloakConfig();

      expect(config).toEqual({
        url: 'https://congen.com/auth',
        realm: 'congen',
        clientId: 'congen-frontend',
      });

      // Reset to default
      mockGlobals.DEPLOYMENT_ENVIRONMENT = 'loc';
    });

    it('should throw error for unknown environment', () => {
      // Temporarily override the mock
      const mockGlobals = require('../globals');
      mockGlobals.DEPLOYMENT_ENVIRONMENT = 'unknown';

      expect(() => getKeycloakConfig()).toThrow(
        'No Keycloak configuration found for environment: unknown'
      );

      // Reset to default
      mockGlobals.DEPLOYMENT_ENVIRONMENT = 'loc';
    });
  });
});
