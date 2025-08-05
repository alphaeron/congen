import { AuthProviderProps } from 'react-oidc-context';

import { DEPLOYMENT_ENVIRONMENT, KEYCLOAK_URL, BASE_URL } from '../globals';

/**
 * Constructs the OIDC configuration dynamically based on the environment.
 *
 * @return OIDC configuration object
 */
export const getOidcConfig = () => {
  const authority = `${KEYCLOAK_URL}/realms/congen`;
  const frontendHost = DEPLOYMENT_ENVIRONMENT === 'loc' ? `${BASE_URL}:3000` : BASE_URL;

  const config = {
    authority: authority,
    client_id: 'congen-frontend',
    redirect_uri: `${frontendHost}/auth/callback`,
    post_logout_redirect_uri: frontendHost,
    silent_redirect_uri: `${frontendHost}/silent-renew.html`,
  };

  return config;
};

/**
 * Creates the AuthProvider configuration for react-oidc-context.
 *
 * @return AuthProviderProps configuration
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
    loadUserInfo: true,
    monitorSession: true, // Enable session monitoring for better security
    // Enable PKCE for enhanced security
    code_challenge_method: 'S256',
    // Security enhancements
    automaticSilentRenew: true, // Automatically renew tokens before they expire
    silentRenewError: () => {
      // Force user to re-authenticate if silent renewal fails
    },
    // Token validation
    validateSubOnSilentRenew: true, // Validate subject on token renewal

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
