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

  return {
    authority: authority,
    client_id: 'congen-frontend',
    redirect_uri: frontendHost,
    post_logout_redirect_uri: frontendHost,
    silent_redirect_uri: `${frontendHost}/silent-renew.html`,
  };
};

/**
 * Creates the AuthProvider configuration for react-oidc-context.
 *
 * @return AuthProviderProps configuration
 */
export const getAuthProviderConfig = (): AuthProviderProps => {
  const config = getOidcConfig();
  const authority = config.authority;

  return {
    authority: config.authority,
    client_id: config.client_id,
    redirect_uri: config.redirect_uri,
    post_logout_redirect_uri: config.post_logout_redirect_uri,
    silent_redirect_uri: config.silent_redirect_uri,
    scope: 'openid profile email',
    response_type: 'code',
    metadata: {
      authorization_endpoint: `${authority}/protocol/openid-connect/auth`,
      token_endpoint: `${authority}/protocol/openid-connect/token`,
      end_session_endpoint: `${authority}/protocol/openid-connect/logout`,
      userinfo_endpoint: `${authority}/protocol/openid-connect/userinfo`,
      jwks_uri: `${authority}/protocol/openid-connect/certs`,
      issuer: authority,
    },
  };
};
