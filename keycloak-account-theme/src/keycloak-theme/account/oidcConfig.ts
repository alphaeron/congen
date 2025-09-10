import type { AuthProviderProps } from 'react-oidc-context';

/**
 * Environment configuration for the account theme.
 */
const DEPLOYMENT_ENVIRONMENT = 'local'; // Account theme runs in local environment
const BASE_URL = 'http://localhost';
const KEYCLOAK_URL = `${BASE_URL}:8080`;

/**
 * Constructs the OIDC configuration dynamically based on the environment.
 * Uses the same client as the frontend to share authentication sessions.
 */
export const getOidcConfig = () => {
  const authority = `${KEYCLOAK_URL}/realms/congen`;
  const accountUrl = `${window.location.origin}/realms/congen/account/`;

  const config = {
    authority: authority,
    client_id: 'congen-frontend', // Same client as frontend
    redirect_uri: accountUrl,
    post_logout_redirect_uri: accountUrl,
    silent_redirect_uri: accountUrl,
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
    loadUserInfo: true,
    monitorSession: true, // Enable session monitoring for better security
    // Enable PKCE for enhanced security
    code_challenge_method: 'S256',
    // Security enhancements
    automaticSilentRenew: true, // Automatically renew tokens before they expire
    silentRenewError: () => {
      // The error will be handled by the AuthContext error handler
    },
    // Additional security settings
    includeIdTokenInSilentRenew: false, // Don't include ID token in silent renew
    checkSessionInterval: 120000, // Check session every 120 seconds
    // Token validation
    validateSubOnSilentRenew: true, // Validate subject on token renewal
    // Silent renew configuration
    silentRequestTimeout: 10000, // 10 second timeout for silent requests
    accessTokenExpiringNotificationTime: 60, // Start renewal 60 seconds before expiry
    // Session monitoring callbacks
    onSigninSilent: () => {
      // Silent signin completed
    },
    onSigninSilentError: () => {
      // Redirect to login on silent signin failure
      window.location.href = '/login';
    },
    // User session callbacks
    onUserLoaded: () => {
      // User loaded
    },
    onUserUnloaded: () => {
      // User unloaded
    },
    onUserSignedOut: () => {
      // Clear any local state and redirect to login
      window.location.href = '/login';
    },
    onSilentRenewError: () => {
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
