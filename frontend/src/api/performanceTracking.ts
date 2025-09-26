import { REQUEST } from './endpoint';
import type {
  UserPerformanceMetrics,
  UserPerformanceScores,
  UserTestResult,
  TestProtocol,
} from './types';

/**
 * Submit performance metrics for the current user.
 */
export const submitPerformanceMetrics = async (
  metrics: Omit<UserPerformanceMetrics, 'keycloak_id' | 'created_at' | 'updated_at'>,
  options: { forceRefresh?: boolean } = {}
): Promise<UserPerformanceScores> => {
  return REQUEST<UserPerformanceScores>({
    method: 'PUT',
    url: '/performance/metrics',
    data: metrics,
    ...options,
  });
};

/**
 * Get current performance scores for the authenticated user.
 */
export const getCurrentPerformanceScores = async (
  options: { forceRefresh?: boolean } = {}
): Promise<UserPerformanceScores> => {
  return REQUEST<UserPerformanceScores>({
    method: 'GET',
    url: '/performance/scores',
    ...options,
  });
};

/**
 * Get current performance metrics for the authenticated user.
 */
export const getCurrentPerformanceMetrics = async (
  options: { forceRefresh?: boolean } = {}
): Promise<UserPerformanceMetrics> => {
  return REQUEST<UserPerformanceMetrics>({
    method: 'GET',
    url: '/performance/metrics',
    ...options,
  });
};

/**
 * Submit weekly test results for the current user.
 */
export const submitWeeklyTest = async (
  testResults: Omit<UserTestResult, 'id' | 'keycloak_id' | 'created_at' | 'updated_at'>[],
  options: { forceRefresh?: boolean } = {}
): Promise<UserTestResult[]> => {
  return REQUEST<UserTestResult[]>({
    method: 'PUT',
    url: '/performance/weekly_test',
    data: testResults,
    ...options,
  });
};

/**
 * Get weekly tests for the authenticated user within a date range.
 */
export const getWeeklyTestsInRange = async (
  startDate: string,
  endDate: string,
  options: { forceRefresh?: boolean } = {}
): Promise<UserTestResult[]> => {
  return REQUEST<UserTestResult[]>({
    method: 'GET',
    url: '/performance/weekly_test',
    params: {
      startTimestamp: startDate,
      endTimestamp: endDate,
    },
    ...options,
  });
};

/**
 * Get Wilks score for the authenticated user.
 */
export const getWilksScore = async (
  bodyWeightKg: number,
  isMale: boolean,
  options: { forceRefresh?: boolean } = {}
): Promise<number | null> => {
  return REQUEST<number | null>({
    method: 'GET',
    url: '/performance/wilks',
    params: {
      body_weight_kg: bodyWeightKg,
      is_male: isMale,
    },
    ...options,
  });
};

/**
 * Get performance metrics for the authenticated user within a date range.
 */
export const getPerformanceMetricsInRange = async (
  startDate: string,
  endDate: string,
  options: { forceRefresh?: boolean } = {}
): Promise<UserPerformanceMetrics[]> => {
  return REQUEST<UserPerformanceMetrics[]>({
    method: 'GET',
    url: '/performance/metrics/range',
    params: {
      startTimestamp: startDate,
      endTimestamp: endDate,
    },
    ...options,
  });
};

/**
 * Get test protocols configuration.
 */
export const getTestProtocols = async (
  options: { forceRefresh?: boolean } = {}
): Promise<TestProtocol[]> => {
  return REQUEST<TestProtocol[]>({
    method: 'GET',
    url: '/performance/test_protocols',
    ...options,
  });
};
