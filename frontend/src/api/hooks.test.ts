import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { renderHook, waitFor, act } from '@testing-library/react';
import * as React from 'react';

import { useApiGet, useApiSend } from './hooks';

// Mock the endpoint module
jest.mock('./endpoint', () => ({
  REQUEST: jest.fn(),
}));

// Create a test wrapper that properly handles React Query
const createTestWrapper = () => {
  const queryClient = new QueryClient({
    defaultOptions: {
      queries: {
        retry: false,
        gcTime: 0,
        staleTime: 0,
      },
      mutations: {
        retry: false,
        gcTime: 0,
      },
    },
  });

  const TestWrapper = ({ children }: { children: React.ReactNode }) =>
    React.createElement(QueryClientProvider, { client: queryClient }, children);

  TestWrapper.displayName = 'TestWrapper';

  return TestWrapper;
};

describe('hooks', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('useApiGet', () => {
    it('should call the provided function with correct parameters', async () => {
      const mockFn = jest.fn().mockResolvedValue({ data: 'test' });
      const queryKey = ['test', 'data'];
      const params = ['param1', 'param2'];

      const hookResult = renderHook(() => useApiGet(queryKey, mockFn, {}, params), {
        wrapper: createTestWrapper(),
      });

      await waitFor(() => {
        expect(hookResult.result.current.isSuccess).toBe(true);
      });

      expect(mockFn).toHaveBeenCalledWith('param1', 'param2');
    });

    it('should handle successful response', async () => {
      const mockData = { id: 1, name: 'test' };
      const mockFn = jest.fn().mockResolvedValue(mockData);
      const queryKey = ['test'];

      const hookResult = renderHook(() => useApiGet(queryKey, mockFn, {}), {
        wrapper: createTestWrapper(),
      });

      await waitFor(() => {
        expect(hookResult.result.current.isSuccess).toBe(true);
      });

      expect(hookResult.result.current.data).toEqual(mockData);
    });

    it('should handle error response', async () => {
      const mockError = new Error('API Error');
      const mockFn = jest.fn().mockRejectedValue(mockError);
      const queryKey = ['test'];

      const hookResult = renderHook(() => useApiGet(queryKey, mockFn, {}), {
        wrapper: createTestWrapper(),
      });

      await waitFor(() => {
        expect(hookResult.result.current.isError).toBe(true);
      });

      expect(hookResult.result.current.error).toEqual(mockError);
    });

    it('should handle loading state', async () => {
      // Create a promise that never resolves to keep the query in loading state
      const mockFn = jest.fn().mockImplementation(() => new Promise(() => {}));
      const queryKey = ['test'];

      const hookResult = renderHook(() => useApiGet(queryKey, mockFn, {}), {
        wrapper: createTestWrapper(),
      });

      // The query should be loading initially
      expect(hookResult.result.current.isLoading).toBe(true);
    });
  });

  describe('useApiSend', () => {
    it('should call the provided function with correct parameters', async () => {
      const mockFn = jest.fn().mockResolvedValue({ data: 'test' });
      const mockSuccess = jest.fn();
      const mockError = jest.fn();
      const invalidateKey = [{ queryKey: ['test'] }];
      const params = ['param1', 'param2'];

      const hookResult = renderHook(
        () => useApiSend(mockFn, mockSuccess, mockError, invalidateKey, {}, params),
        { wrapper: createTestWrapper() }
      );

      // Execute the mutation
      await act(async () => {
        await hookResult.result.current.mutateAsync();
      });

      expect(mockFn).toHaveBeenCalledWith('param1', 'param2');
    });

    it('should call success callback on successful mutation', async () => {
      const mockData = { id: 1, name: 'test' };
      const mockFn = jest.fn().mockResolvedValue(mockData);
      const mockSuccess = jest.fn();
      const mockError = jest.fn();
      const invalidateKey = [{ queryKey: ['test'] }];

      const hookResult = renderHook(
        () => useApiSend(mockFn, mockSuccess, mockError, invalidateKey),
        { wrapper: createTestWrapper() }
      );

      // Execute the mutation
      await act(async () => {
        await hookResult.result.current.mutateAsync();
      });

      expect(mockSuccess).toHaveBeenCalledWith(mockData);
      expect(mockError).not.toHaveBeenCalled();
    });

    it('should call error callback on failed mutation', async () => {
      const mockError = new Error('API Error');
      const mockFn = jest.fn().mockRejectedValue(mockError);
      const mockSuccess = jest.fn();
      const mockErrorCallback = jest.fn();
      const invalidateKey = [{ queryKey: ['test'] }];

      const hookResult = renderHook(
        () => useApiSend(mockFn, mockSuccess, mockErrorCallback, invalidateKey),
        { wrapper: createTestWrapper() }
      );

      // Execute the mutation and expect it to throw
      await act(async () => {
        await expect(hookResult.result.current.mutateAsync()).rejects.toThrow('API Error');
      });

      expect(mockErrorCallback).toHaveBeenCalledWith(mockError, undefined, undefined);
      expect(mockSuccess).not.toHaveBeenCalled();
    });

    it('should handle mutation without invalidate keys', async () => {
      const mockData = { id: 1, name: 'test' };
      const mockFn = jest.fn().mockResolvedValue(mockData);
      const mockSuccess = jest.fn();
      const mockError = jest.fn();

      const hookResult = renderHook(() => useApiSend(mockFn, mockSuccess, mockError, []), {
        wrapper: createTestWrapper(),
      });

      // Execute the mutation
      await act(async () => {
        await hookResult.result.current.mutateAsync();
      });

      expect(mockSuccess).toHaveBeenCalledWith(mockData);
    });

    it('should handle mutation without success callback', async () => {
      const mockData = { id: 1, name: 'test' };
      const mockFn = jest.fn().mockResolvedValue(mockData);
      const mockError = jest.fn();
      const invalidateKey = [{ queryKey: ['test'] }];

      const hookResult = renderHook(() => useApiSend(mockFn, jest.fn(), mockError, invalidateKey), {
        wrapper: createTestWrapper(),
      });

      // Execute the mutation
      await act(async () => {
        await hookResult.result.current.mutateAsync();
      });

      expect(mockError).not.toHaveBeenCalled();
    });
  });
});
