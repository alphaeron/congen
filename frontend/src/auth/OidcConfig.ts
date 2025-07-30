import { AuthProviderProps } from 'react-oidc-context';

import { DEPLOYMENT_ENVIRONMENT } from '../globals';

/**
 * Mapping of various environments to the corresponding backend endpoint.
 */
const _ENVIRONMENT_TO_ENDPOINT_MAPPING = {
  loc: 'http://localhost',
  staging: 'https://staging.congen.com',
  production: 'https://congen.com',
};

/**
 * The base URL for the backend API.
 */
const _BASE_URL =
  _ENVIRONMENT_TO_ENDPOINT_MAPPING[
    DEPLOYMENT_ENVIRONMENT as keyof typeof _ENVIRONMENT_TO_ENDPOINT_MAPPING
  ] || 'http://localhost';

/**
 * Constructs the OIDC configuration dynamically based on the environment.
 *
 * @return OIDC configuration object
 */
export const getOidcConfig = () => {
  const authority = DEPLOYMENT_ENVIRONMENT === 'loc' ? `${_BASE_URL}:8080/realms/congen` : `${_BASE_URL}/realms/congen`;
  const frontendHost = DEPLOYMENT_ENVIRONMENT === 'loc' ? `${_BASE_URL}:3000` : _BASE_URL;

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
    scope: 'openid profile email groups',
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
