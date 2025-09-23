import { REQUEST } from './endpoint';
import type { UserPerformanceMetrics, UserPerformanceScores, UserWeeklyTest } from './types';

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
  weeklyTest: Omit<UserWeeklyTest, 'keycloak_id' | 'created_at' | 'updated_at'>,
  options: { forceRefresh?: boolean } = {}
): Promise<UserWeeklyTest> => {
  return REQUEST<UserWeeklyTest>({
    method: 'PUT',
    url: '/performance/weekly_test',
    data: weeklyTest,
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
): Promise<UserWeeklyTest[]> => {
  return REQUEST<UserWeeklyTest[]>({
    method: 'GET',
    url: '/performance/weekly_test',
    params: {
      startDate,
      endDate,
    },
    ...options,
  });
};
