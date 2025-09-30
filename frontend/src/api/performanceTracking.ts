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
  const params = new URLSearchParams();
  
  // Add only non-null/undefined values as query parameters
  if (metrics.vo2_max !== undefined && metrics.vo2_max !== null) {
    params.append('vo2_max', metrics.vo2_max.toString());
  }
  if (metrics.strain !== undefined && metrics.strain !== null) {
    params.append('strain', metrics.strain.toString());
  }
  if (metrics.recovery !== undefined && metrics.recovery !== null) {
    params.append('recovery', metrics.recovery.toString());
  }
  if (metrics.hrv !== undefined && metrics.hrv !== null) {
    params.append('hrv', metrics.hrv.toString());
  }
  if (metrics.sleep_score !== undefined && metrics.sleep_score !== null) {
    params.append('sleep_score', metrics.sleep_score.toString());
  }
  if (metrics.rem_sleep_minutes !== undefined && metrics.rem_sleep_minutes !== null) {
    params.append('rem_sleep_minutes', metrics.rem_sleep_minutes.toString());
  }
  if (metrics.deep_sleep_minutes !== undefined && metrics.deep_sleep_minutes !== null) {
    params.append('deep_sleep_minutes', metrics.deep_sleep_minutes.toString());
  }
  if (metrics.subjective_tiredness !== undefined && metrics.subjective_tiredness !== null) {
    params.append('subjective_tiredness', metrics.subjective_tiredness.toString());
  }

  return REQUEST<UserPerformanceScores>({
    method: 'PUT',
    url: `/performance/metrics?${params.toString()}`,
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
 * Get historical performance scores for the authenticated user within a date range.
 */
export const getPerformanceScoresHistory = async (
  startDate?: Date,
  endDate?: Date,
  options: { forceRefresh?: boolean } = {}
): Promise<UserPerformanceScores[]> => {
  const params = new URLSearchParams();
  
  if (startDate) {
    params.append('start_date', startDate.toISOString());
  }
  if (endDate) {
    params.append('end_date', endDate.toISOString());
  }

  const queryString = params.toString();
  const url = queryString ? `/performance/scores/history?${queryString}` : '/performance/scores/history';

  return REQUEST<UserPerformanceScores[]>({
    method: 'GET',
    url,
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
  testResult: Omit<UserTestResult, 'id' | 'keycloak_id' | 'created_at' | 'updated_at'>,
  options: { forceRefresh?: boolean } = {}
): Promise<UserTestResult[]> => {
  const params = new URLSearchParams();
  
  params.append('week_start_timestamp', testResult.week_start_timestamp.toISOString());
  params.append('test_name', testResult.test_name);
  params.append('status', testResult.status);
  if (testResult.result_value !== undefined && testResult.result_value !== null) {
    params.append('result_value', testResult.result_value.toString());
  }

  return REQUEST<UserTestResult[]>({
    method: 'PUT',
    url: `/performance/weekly_test?${params.toString()}`,
    ...options,
  });
};

/**
 * Get weekly tests for the authenticated user within a date range.
 */
export const getWeeklyTestsInRange = async (
  startDate?: string,
  endDate?: string,
  options: { forceRefresh?: boolean } = {}
): Promise<UserTestResult[]> => {
  const params: Record<string, string> = {};
  if (startDate) {
    params.startTimestamp = startDate;
  }
  if (endDate) {
    params.endTimestamp = endDate;
  }

  // Only include params in the request if we have any
  const requestConfig: any = {
    method: 'GET',
    url: '/performance/weekly_test',
    ...options,
  };

  if (Object.keys(params).length > 0) {
    requestConfig.params = params;
  }

  return REQUEST<UserTestResult[]>(requestConfig);
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
