import axios, { AxiosHeaders } from 'axios';
import { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';

import { DEPLOYMENT_ENVIRONMENT } from '../globals';

/**
 * Mapping of various environments to the correspoinding backend endpoint.
 */
const _ENVIRONMENT_TO_ENDPOINT_MAPPING = {
  loc: 'http://localhost:8080/',
  staging: 'https://staging.congen.com/',
  production: 'https://congen.com/',
};

/**
 * The base URL for the backend API.
 */
const _BASE_URL =
  _ENVIRONMENT_TO_ENDPOINT_MAPPING[
    DEPLOYMENT_ENVIRONMENT as keyof typeof _ENVIRONMENT_TO_ENDPOINT_MAPPING
  ] || 'http://localhost:8080/';

/**
 * Congen backend endpoint.
 */
export const ENDPOINT = axios.create({
  baseURL: _BASE_URL,
  timeout: 2500,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Keycloak getter registration
let getKeycloak: (() => any) | null = null;
export const setKeycloakGetter = (getter: () => any) => {
  getKeycloak = getter;
};

// Add interceptor to inject JWT if available
ENDPOINT.interceptors.request.use(async config => {
  if (getKeycloak) {
    const keycloak = getKeycloak();
    if (keycloak && keycloak.authenticated && keycloak.token) {
      await keycloak.updateToken(70);
      let headers = config.headers;
      // Convert to AxiosHeaders if not already
      if (!(headers instanceof AxiosHeaders)) {
        headers = AxiosHeaders.from(headers || {});
      }
      headers.set('Authorization', `Bearer ${keycloak.token}`);
      config.headers = headers;
    }
  }
  return config;
});

/**
 * Congen backend request main helper.
 */
export const REQUEST = async <T>(options: AxiosRequestConfig): Promise<T> => {
  const onSuccess = (response: AxiosResponse<T>): T => {
    return response?.data;
  };

  const onError = (error: AxiosError) => {
    return Promise.reject(error.response?.data);
  };

  return ENDPOINT(options).then(onSuccess).catch(onError);
};
