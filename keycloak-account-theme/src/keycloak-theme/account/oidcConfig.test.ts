import { getOidcConfig, getAuthProviderConfig } from './oidcConfig';

// Mock the globals module
jest.mock('../../globals', () => ({
  DEPLOYMENT_ENVIRONMENT: 'local',
  KEYCLOAK_URL: 'http://localhost:8080',
  BASE_URL: 'http://localhost',
}));

describe('oidcConfig', () => {
  describe('getOidcConfig', () => {
    it('returns correct OIDC configuration for local environment', () => {
      const config = getOidcConfig();

      expect(config.authority).toBe('http://localhost:8080/realms/congen');
      expect(config.client_id).toBe('account-console');
      expect(config.redirect_uri).toBe('http://localhost:8080/realms/congen/account/');
      expect(config.post_logout_redirect_uri).toBe('http://localhost:8080');
      expect(config.silent_redirect_uri).toBe('http://localhost:8080/realms/congen/account/');
    });
  });

  describe('getAuthProviderConfig', () => {
    it('returns correct AuthProvider configuration', () => {
      const config = getAuthProviderConfig();

      expect(config.authority).toBe('http://localhost:8080/realms/congen');
      expect(config.client_id).toBe('account-console');
      expect(config.redirect_uri).toBe('http://localhost:8080/realms/congen/account/');
      expect(config.post_logout_redirect_uri).toBe('http://localhost:8080');
      expect(config.silent_redirect_uri).toBe('http://localhost:8080/realms/congen/account/');
      expect(config.scope).toBe('openid profile email');
      expect(config.response_type).toBe('code');
      expect(config.loadUserInfo).toBe(true);
      expect(config.monitorSession).toBe(false);
      expect(config.code_challenge_method).toBe('S256');
      expect(config.automaticSilentRenew).toBe(true);
      expect(config.includeIdTokenInSilentRenew).toBe(false);
      expect(config.checkSessionInterval).toBe(0);
      expect(config.validateSubOnSilentRenew).toBe(false);
      expect(config.silentRequestTimeout).toBe(10000);
      expect(config.accessTokenExpiringNotificationTime).toBe(60);
    });

    it('includes correct metadata endpoints', () => {
      const config = getAuthProviderConfig();

      expect(config.metadata).toBeDefined();
      expect(config.metadata.authorization_endpoint).toBe(
        'http://localhost:8080/realms/congen/protocol/openid-connect/auth'
      );
      expect(config.metadata.token_endpoint).toBe(
        'http://localhost:8080/realms/congen/protocol/openid-connect/token'
      );
      expect(config.metadata.end_session_endpoint).toBe(
        'http://localhost:8080/realms/congen/protocol/openid-connect/logout'
      );
      expect(config.metadata.userinfo_endpoint).toBe(
        'http://localhost:8080/realms/congen/protocol/openid-connect/userinfo'
      );
      expect(config.metadata.jwks_uri).toBe(
        'http://localhost:8080/realms/congen/protocol/openid-connect/certs'
      );
      expect(config.metadata.issuer).toBe('http://localhost:8080/realms/congen');
    });

    it('includes onSigninCallback function', () => {
      const config = getAuthProviderConfig();

      expect(typeof config.onSigninCallback).toBe('function');
      expect(() => config.onSigninCallback()).not.toThrow();
    });
  });
});
