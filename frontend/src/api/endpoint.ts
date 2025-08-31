import axios, { AxiosHeaders } from 'axios';

import { BACKEND_URL } from '../globals';

import type { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';

/**
 * Converts Unix timestamps (in seconds) to Date objects.
 * Recursively processes objects and arrays to find timestamp fields.
 */
function convertTimestampsToDates(obj: any): any {
  if (obj === null || obj === undefined) {
    return obj;
  }

  if (Array.isArray(obj)) {
    return obj.map(convertTimestampsToDates);
  }

  if (typeof obj === 'object') {
    const converted: any = {};
    for (const [key, value] of Object.entries(obj)) {
      if (key === 'created_at' || key === 'updated_at' || key === 'consent_timestamp' || key === 'export_timestamp' || key === 'last_updated' || key === 'timestamp') {
        // Convert Unix timestamp in seconds to Date object, or ISO date string to Date object
        if (typeof value === 'number') {
          converted[key] = new Date(value * 1000);
        } else if (typeof value === 'string' && (value.includes('T') || value.includes('Z') || /^\d{4}-\d{2}-\d{2}/.test(value))) {
          // Handle ISO date strings like "2025-08-25T00:00:00Z" or "2025-08-25"
          converted[key] = new Date(value);
        } else {
          converted[key] = value;
        }
      } else {
        converted[key] = convertTimestampsToDates(value);
      }
    }
    return converted;
  }

  return obj;
}

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

// Add interceptor to inject JWT token if available
ENDPOINT.interceptors.request.use(async config => {
  let headers = config.headers;
  // Convert to AxiosHeaders if not already
  if (!(headers instanceof AxiosHeaders)) {
    headers = AxiosHeaders.from(headers || {});
  }

  // Force preflight requests by adding custom headers
  headers.set('X-Requested-With', 'XMLHttpRequest');

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

// Add response interceptor to handle token refresh on 401 errors and convert timestamps
ENDPOINT.interceptors.response.use(
  response => {
    // Convert Unix timestamps to Date objects in the response data
    if (response.data) {
      response.data = convertTimestampsToDates(response.data);
    }
    return response;
  },
  async error => {
    if (error.response?.status === 401) {
      // The OIDC library should handle token refresh automatically
      // If this persists, the user will need to re-authenticate
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
    // Provide better error information
    if (error.response?.data) {
      return Promise.reject(error.response.data);
    } else if (error.message) {
      return Promise.reject({ error: error.message });
    } else {
      return Promise.reject({ error: 'Unknown error occurred' });
    }
  };

  return ENDPOINT(options).then(onSuccess).catch(onError);
};
