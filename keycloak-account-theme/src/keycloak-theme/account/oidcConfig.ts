import type { AuthProviderProps } from 'react-oidc-context';
import { KEYCLOAK_URL } from '../../globals';

/**
 * Constructs the OIDC configuration for Keycloak account theme.
 * Uses environment-based configuration like the frontend.
 */
export const getOidcConfig = () => {
  const authority = `${KEYCLOAK_URL}/realms/congen`;
  const keycloakHost = KEYCLOAK_URL;

  const config = {
    authority: authority,
    client_id: 'account-console',
    redirect_uri: `${keycloakHost}/realms/congen/account/`,
    post_logout_redirect_uri: keycloakHost,
    silent_redirect_uri: `${keycloakHost}/realms/congen/account/`,
  };

  return config;
};

/**
 * Creates the AuthProvider configuration for react-oidc-context.
 */
export const getAuthProviderConfig = (): AuthProviderProps => {
  const config = getOidcConfig();
  const authority = config.authority;

  const authProviderConfig = {
    authority: config.authority,
    client_id: config.client_id,
    redirect_uri: config.redirect_uri,
    post_logout_redirect_uri: config.post_logout_redirect_uri,
    silent_redirect_uri: config.silent_redirect_uri,
    scope: 'openid profile email',
    response_type: 'code',
    loadUserInfo: true, // Load user info to get updated profile data
    monitorSession: false, // Don't monitor session since we're in Keycloak
    code_challenge_method: 'S256',
    automaticSilentRenew: true, // Enable automatic renewal for token refresh
    includeIdTokenInSilentRenew: false,
    checkSessionInterval: 0, // Disable session checking
    validateSubOnSilentRenew: false,
    silentRequestTimeout: 10000,
    accessTokenExpiringNotificationTime: 60,
    onSigninCallback: () => {
      // Handle signin callback - do nothing since we're already in Keycloak
      // This prevents the "No matching state found in storage" error
    },
    metadata: {
      authorization_endpoint: `${authority}/protocol/openid-connect/auth`,
      token_endpoint: `${authority}/protocol/openid-connect/token`,
      end_session_endpoint: `${authority}/protocol/openid-connect/logout`,
      userinfo_endpoint: `${authority}/protocol/openid-connect/userinfo`,
      jwks_uri: `${authority}/protocol/openid-connect/certs`,
      issuer: authority,
    },
  };

  return authProviderConfig;
};
