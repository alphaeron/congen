import axios from "axios";
import { AxiosError, AxiosRequestConfig, AxiosResponse } from "axios";

import { DEPLOYMENT_ENVIRONMENT } from "../globals";

/**
 * Mapping of various environments to the correspoinding backend endpoint.
 */
const _ENVIRONMENT_TO_ENDPOINT_MAPPING = {
  loc: "http://localhost:8080/",
};

/**
 * Congen backend endpoint.
 */
export const ENDPOINT = axios.create({
  // @ts-expect-error dynamic property
  baseURL: _ENVIRONMENT_TO_ENDPOINT_MAPPING[DEPLOYMENT_ENVIRONMENT],
  timeout: 2500,
  headers: {
    common: {
      "Content-Type": "application/json",
    },
  },
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
