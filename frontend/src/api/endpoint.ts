import axios, { AxiosHeaders } from 'axios';

import { BACKEND_URL } from '../globals';

import type { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';

/**
 * Congen backend endpoint.
 */
export const ENDPOINT = axios.create({
  baseURL: `${BACKEND_URL}/api/v1/`,
  timeout: 2500,
  withCredentials: true, // Include credentials in CORS requests
  headers: {
    'Content-Type': 'application/json',
    'X-Requested-With': 'XMLHttpRequest', // Force preflight requests
  },
});

// Token getter registration
let getToken: (() => string | null) | null = null;
export const setTokenGetter = (getter: () => string | null) => {
  getToken = getter;
};

// CSRF token management
let csrfToken: string | null = null;

// Function to fetch CSRF token from server
const fetchCsrfToken = async (): Promise<string | null> => {
  try {
    const response = await axios.get(`${BACKEND_URL}/api/v1/csrf`, {
      withCredentials: true,
    });
    return response.data.token;
  } catch (error) {
    console.warn('Failed to fetch CSRF token:', error);
    return null;
  }
};

// Add interceptor to inject JWT and CSRF token if available
ENDPOINT.interceptors.request.use(async config => {
  let headers = config.headers;
  // Convert to AxiosHeaders if not already
  if (!(headers instanceof AxiosHeaders)) {
    headers = AxiosHeaders.from(headers || {});
  }

  // Force preflight requests by adding custom headers
  headers.set('X-Requested-With', 'XMLHttpRequest');

  // Add CSRF token for state-changing operations
  if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(config.method?.toUpperCase() || '')) {
    if (!csrfToken) {
      csrfToken = await fetchCsrfToken();
    }
    if (csrfToken) {
      headers.set('X-CSRF-TOKEN', csrfToken);
    }
  }

  if (getToken) {
    const token = getToken();
    if (token) {
      // Check if token is about to expire (within 5 minutes)
      try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const exp = payload.exp * 1000; // Convert to milliseconds
        const now = Date.now();
        const fiveMinutes = 5 * 60 * 1000;

        if (exp - now < fiveMinutes) {
          // The OIDC library should handle silent renewal automatically
        }
      } catch {
        // Token decoding failed, continue with request
      }

      headers.set('Authorization', `Bearer ${token}`);
    }
  }

  config.headers = headers;
  return config;
});

// Add response interceptor to handle token refresh on 401 errors and CSRF token refresh on 403 errors
ENDPOINT.interceptors.response.use(
  response => response,
  async error => {
    if (error.response?.status === 401) {
      // The OIDC library should handle token refresh automatically
      // If this persists, the user will need to re-authenticate
    } else if (error.response?.status === 403 && error.response?.data?.error?.includes('CSRF')) {
      // CSRF token expired or invalid, fetch a new one
      csrfToken = await fetchCsrfToken();
      // Retry the original request
      if (csrfToken && error.config) {
        return ENDPOINT(error.config);
      }
    }
    return Promise.reject(error);
  }
);

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
