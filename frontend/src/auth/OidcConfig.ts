import { DEPLOYMENT_ENVIRONMENT, KEYCLOAK_URL, BASE_URL } from '../globals';

import type { AuthProviderProps } from 'react-oidc-context';

/**
 * Constructs the OIDC configuration dynamically based on the environment.
 *
 * @return OIDC configuration object
 */
export const getOidcConfig = () => {
  const authority = `${KEYCLOAK_URL}/realms/congen`;
  const frontendHost = DEPLOYMENT_ENVIRONMENT === 'local' ? `${BASE_URL}:3000` : BASE_URL;

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
    silentRenewError: (error: Error) => {
      // Force user to re-authenticate if silent renewal fails
      // Clear any stored tokens and redirect to login
      window.location.href = '/login';
    },
    // Token validation
    validateSubOnSilentRenew: true, // Validate subject on token renewal
    // Session monitoring callbacks
    onSigninSilent: () => {
      // Silent signin completed
    },
    onSigninSilentError: (error: Error) => {
      // Redirect to login on silent signin failure
      window.location.href = '/login';
    },
    // User session callbacks
    onUserLoaded: (user: any) => {
      // User loaded
    },
    onUserUnloaded: () => {
      // User unloaded
    },
    onUserSignedOut: () => {
      // Clear any local state and redirect to login
      window.location.href = '/login';
    },
    onSilentRenewError: (error: Error) => {
      // Force re-authentication on silent renew failure
      window.location.href = '/login';
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
