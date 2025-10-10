/**
 * Deployment environment.  Default to local.
 */
export const DEPLOYMENT_ENVIRONMENT = process.env.REACT_APP_DEPLOYMENT_ENVIRONMENT ?? 'local';

/**
 * Mapping of various environments to the corresponding backend endpoint.
 */
const _ENVIRONMENT_TO_ENDPOINT_MAPPING = {
  local: 'http://localhost',
  'local-persist': 'http://localhost',
  staging: 'https://staging.congen.com',
  production: 'https://congen.com',
};

/**
 * The base URL for the backend API.
 */
export const BASE_URL =
  _ENVIRONMENT_TO_ENDPOINT_MAPPING[
    DEPLOYMENT_ENVIRONMENT as keyof typeof _ENVIRONMENT_TO_ENDPOINT_MAPPING
  ] || 'http://localhost';

/**
 * The Keycloak URL for the current environment.
 */
export const KEYCLOAK_URL = (DEPLOYMENT_ENVIRONMENT === 'local' || DEPLOYMENT_ENVIRONMENT === 'local-persist') ? `${BASE_URL}:8080` : BASE_URL;

/**
 * The backend API URL for the current environment.
 */
export const BACKEND_URL = (DEPLOYMENT_ENVIRONMENT === 'local' || DEPLOYMENT_ENVIRONMENT === 'local-persist') ? `${BASE_URL}:8888` : BASE_URL;
