import axios, { AxiosHeaders } from 'axios';

import {
  handleAuthenticationFailure,
  isTokenExpired,
  isTokenExpiringSoon,
  isTokenMalformed,
  sanitizeToken,
} from '../common/authUtils';
import { BACKEND_URL } from '../globals';

import type { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';

/**
 * Converts Unix timestamps (in seconds) to Date objects.
 * Recursively processes objects and arrays to find timestamp fields.
 */
function convertTimestampsToDates(obj: unknown): unknown {
  if (obj === null || obj === undefined) {
    return obj;
  }

  if (Array.isArray(obj)) {
    return obj.map(convertTimestampsToDates);
  }

  if (typeof obj === 'object' && obj !== null) {
    const converted: Record<string, unknown> = {};
    for (const [key, value] of Object.entries(obj)) {
      if (
        key === 'created_at' ||
        key === 'updated_at' ||
        key === 'consent_timestamp' ||
        key === 'export_timestamp' ||
        key === 'last_updated' ||
        key === 'timestamp'
      ) {
        // Convert Unix timestamp in seconds to Date object, or ISO date string to Date object
        if (typeof value === 'number') {
          converted[key] = new Date(value * 1000);
        } else if (
          typeof value === 'string' &&
          (value.includes('T') || value.includes('Z') || /^\d{4}-\d{2}-\d{2}/.test(value))
        ) {
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
  timeout: 10000, // Increased from 2500ms to 10 seconds to handle slow backend responses
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
      // Sanitize token to prevent XSS
      const sanitizedToken = sanitizeToken(token);
      if (!sanitizedToken) {
        handleAuthenticationFailure('Invalid token format');
        return Promise.reject(new Error('Invalid token format'));
      }

      // Check if token is malformed first
      if (isTokenMalformed(sanitizedToken)) {
        // Token is malformed, clear authentication state and redirect to login
        handleAuthenticationFailure('Invalid token');

        // Return a rejected promise to prevent the request
        return Promise.reject(new Error('Invalid token'));
      }

      // Check if token is expired
      if (isTokenExpired(sanitizedToken)) {
        // Token is expired, clear authentication state and redirect to login
        handleAuthenticationFailure('Token expired');

        // Return a rejected promise to prevent the request
        return Promise.reject(new Error('Token expired'));
      } else if (isTokenExpiringSoon(sanitizedToken)) {
        // The OIDC library should handle silent renewal automatically
      }

      headers.set('Authorization', `Bearer ${sanitizedToken}`);
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
      // Authentication failed - clear any stored tokens and redirect to login
      handleAuthenticationFailure('API request returned 401');
    }
    return Promise.reject(error);
  }
);

// Request deduplication cache
const requestCache = new Map<string, Promise<unknown>>();
const CACHE_DURATION = 30000; // 30 seconds
const cacheTimestamps = new Map<string, number>();

/**
 * Generate a cache key for request deduplication
 */
const generateCacheKey = (options: AxiosRequestConfig): string => {
  const { method = 'GET', url, params, data } = options;
  return `${method}:${url}:${JSON.stringify(params)}:${JSON.stringify(data)}`;
};

/**
 * Check if a cached request is still valid
 */
const isCacheValid = (key: string): boolean => {
  const timestamp = cacheTimestamps.get(key);
  if (!timestamp) return false;
  return Date.now() - timestamp < CACHE_DURATION;
};

/**
 * Congen backend request main helper with retry logic, deduplication, and caching.
 */
export const REQUEST = async <T>(
  options: AxiosRequestConfig & { forceRefresh?: boolean },
  retryCount = 0
): Promise<T> => {
  const maxRetries = 3;
  const retryDelay = 1000; // 1 second

  // Only cache GET requests and respect forceRefresh flag
  // Disable caching in test environments
  const isTestEnvironment = process.env.NODE_ENV === 'test' || typeof jest !== 'undefined';
  const shouldCache =
    (options.method === 'GET' || !options.method) && !options.forceRefresh && !isTestEnvironment;
  const cacheKey = shouldCache ? generateCacheKey(options) : null;

  // Check for existing request (deduplication) - skip if forceRefresh is true
  if (cacheKey && requestCache.has(cacheKey) && isCacheValid(cacheKey)) {
    return requestCache.get(cacheKey) as Promise<T>;
  }

  const onSuccess = (response: AxiosResponse<T>): T => {
    return response?.data;
  };

  const onError = async (error: AxiosError<unknown>): Promise<T> => {
    // Check if this is a network error that should be retried
    const isNetworkError =
      error.code === 'ERR_NETWORK' ||
      error.code === 'ECONNREFUSED' ||
      error.message?.includes('CORS') ||
      error.message?.includes('Network Error');

    if (isNetworkError && retryCount < maxRetries) {
      // Wait before retrying
      await new Promise(resolve => setTimeout(resolve, retryDelay * (retryCount + 1)));

      // Retry the request
      return REQUEST<T>(options, retryCount + 1);
    }

    // Provide better error information for non-retryable errors
    if (error.response?.data) {
      return Promise.reject(error.response.data);
    } else if (error.message) {
      return Promise.reject({ error: error.message });
    } else {
      return Promise.reject({ error: 'Unknown error occurred' });
    }
  };

  // Create the request promise
  const requestPromise = ENDPOINT(options).then(onSuccess).catch(onError);

  // Cache the request if it's a GET request
  if (cacheKey && shouldCache) {
    requestCache.set(cacheKey, requestPromise);
    cacheTimestamps.set(cacheKey, Date.now());

    // Clean up cache after request completes
    requestPromise.finally(() => {
      setTimeout(() => {
        requestCache.delete(cacheKey);
        cacheTimestamps.delete(cacheKey);
      }, CACHE_DURATION);
    });
  }

  return requestPromise;
};
