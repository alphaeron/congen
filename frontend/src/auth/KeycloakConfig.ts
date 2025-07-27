import Keycloak from 'keycloak-js';

import { DEPLOYMENT_ENVIRONMENT } from '../globals';

/**
 * Keycloak configuration mapping for different environments.
 */
const KEYCLOAK_CONFIG = {
  loc: {
    url: 'http://localhost:8080/auth',
    realm: 'congen',
    clientId: 'congen-frontend',
  },
  staging: {
    url: 'https://staging.congen.com/auth',
    realm: 'congen',
    clientId: 'congen-frontend',
  },
  production: {
    url: 'https://congen.com/auth',
    realm: 'congen',
    clientId: 'congen-frontend',
  },
};

/**
 * Gets the Keycloak configuration for the current environment.
 *
 * @return Keycloak configuration object
 */
export const getKeycloakConfig = () => {
  const config = KEYCLOAK_CONFIG[DEPLOYMENT_ENVIRONMENT as keyof typeof KEYCLOAK_CONFIG];

  if (!config) {
    throw new Error(`No Keycloak configuration found for environment: ${DEPLOYMENT_ENVIRONMENT}`);
  }

  return config;
};

/**
 * Creates and initializes a Keycloak instance.
 *
 * @return Promise that resolves to the initialized Keycloak instance
 */
export const initKeycloak = async (): Promise<Keycloak> => {
  const config = getKeycloakConfig();

  const keycloak = new Keycloak({
    url: config.url,
    realm: config.realm,
    clientId: config.clientId,
  });

  await keycloak.init({
    onLoad: 'check-sso',
    silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
    checkLoginIframe: false,
  });

  return keycloak;
};
