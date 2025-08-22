import { useCallback, useRef } from 'react';

/**
 * Custom hook for handling auto-refresh functionality after data mutations.
 * 
 * This hook provides a modular way to refresh data after operations like
 * create, update, or delete. It ensures that components automatically
 * update their data without requiring manual refresh actions.
 * 
 * @param refreshFunction The function to call to refresh data
 * @returns An object with refresh methods and state
 */
export const useAutoRefresh = <T>(
  refreshFunction: () => Promise<T>
) => {
  const isRefreshingRef = useRef(false);

  /**
   * Refreshes data and returns the result.
   * Prevents multiple simultaneous refresh calls.
   */
  const refresh = useCallback(async (): Promise<T> => {
    if (isRefreshingRef.current) {
      // If already refreshing, wait for the current refresh to complete
      return new Promise((resolve, reject) => {
        const checkComplete = () => {
          if (!isRefreshingRef.current) {
            refreshFunction().then(resolve).catch(reject);
          } else {
            setTimeout(checkComplete, 50);
          }
        };
        checkComplete();
      });
    }

    isRefreshingRef.current = true;
    try {
      const result = await refreshFunction();
      return result;
    } finally {
      isRefreshingRef.current = false;
    }
  }, [refreshFunction]);

  /**
   * Executes a mutation function and then automatically refreshes data.
   * This is useful for operations like create, update, or delete that
   * should trigger a data refresh afterward.
   * 
   * @param mutationFunction The function to execute before refreshing
   * @returns Promise that resolves with the refresh result
   */
  const executeAndRefresh = useCallback(async <R>(
    mutationFunction: () => Promise<R>
  ): Promise<T> => {
    await mutationFunction();
    return refresh();
  }, [refresh]);

  return {
    refresh,
    executeAndRefresh,
    isRefreshing: isRefreshingRef.current,
  };
};
