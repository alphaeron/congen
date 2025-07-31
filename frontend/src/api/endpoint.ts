import axios, { AxiosHeaders } from 'axios';
import { AxiosError, AxiosRequestConfig, AxiosResponse } from 'axios';

import { BACKEND_URL } from '../globals';

/**
 * Congen backend endpoint.
 */
export const ENDPOINT = axios.create({
  baseURL: `${BACKEND_URL}/api/v1/`,
  timeout: 2500,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Token getter registration
let getToken: (() => string | null) | null = null;
export const setTokenGetter = (getter: () => string | null) => {
  getToken = getter;
};

// Add interceptor to inject JWT if available
ENDPOINT.interceptors.request.use(async config => {
  if (getToken) {
    const token = getToken();
    if (token) {
      let headers = config.headers;
      // Convert to AxiosHeaders if not already
      if (!(headers instanceof AxiosHeaders)) {
        headers = AxiosHeaders.from(headers || {});
      }
      headers.set('Authorization', `Bearer ${token}`);
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
