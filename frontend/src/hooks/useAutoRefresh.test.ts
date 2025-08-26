import { renderHook, act } from '@testing-library/react';

import { useAutoRefresh } from './useAutoRefresh';

describe('useAutoRefresh', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('should refresh data successfully', async () => {
    const mockRefreshFunction = jest.fn().mockResolvedValue('refreshed data');

    const { result } = renderHook(() => useAutoRefresh(mockRefreshFunction));

    await act(async () => {
      const data = await result.current.refresh();
      expect(data).toBe('refreshed data');
    });

    expect(mockRefreshFunction).toHaveBeenCalledTimes(1);
  });

  it('should execute mutation and refresh data', async () => {
    const mockRefreshFunction = jest.fn().mockResolvedValue('refreshed data');
    const mockMutationFunction = jest.fn().mockResolvedValue('mutation result');

    const { result } = renderHook(() => useAutoRefresh(mockRefreshFunction));

    await act(async () => {
      const data = await result.current.executeAndRefresh(mockMutationFunction);
      expect(data).toBe('refreshed data');
    });

    expect(mockMutationFunction).toHaveBeenCalledTimes(1);
    expect(mockRefreshFunction).toHaveBeenCalledTimes(1);
  });

  it('should prevent multiple simultaneous refresh calls', async () => {
    let callCount = 0;
    const mockRefreshFunction = jest.fn().mockImplementation(() => {
      callCount++;
      return Promise.resolve(`refreshed data ${callCount}`);
    });

    const { result } = renderHook(() => useAutoRefresh(mockRefreshFunction));

    // Start first refresh
    const firstRefreshPromise = result.current.refresh();

    // Try to start second refresh immediately (this should be queued)
    const secondRefreshPromise = result.current.refresh();

    const [firstResult, secondResult] = await Promise.all([
      firstRefreshPromise,
      secondRefreshPromise,
    ]);

    expect(firstResult).toBe('refreshed data 1');
    expect(secondResult).toBe('refreshed data 2');
    expect(mockRefreshFunction).toHaveBeenCalledTimes(2); // Called once for first refresh, once for second refresh
  });

  it('should handle refresh function errors', async () => {
    const mockError = new Error('Refresh failed');
    const mockRefreshFunction = jest.fn().mockRejectedValue(mockError);

    const { result } = renderHook(() => useAutoRefresh(mockRefreshFunction));

    await act(async () => {
      await expect(result.current.refresh()).rejects.toThrow('Refresh failed');
    });

    expect(mockRefreshFunction).toHaveBeenCalledTimes(1);
  });

  it('should handle mutation function errors', async () => {
    const mockRefreshFunction = jest.fn().mockResolvedValue('refreshed data');
    const mockError = new Error('Mutation failed');
    const mockMutationFunction = jest.fn().mockRejectedValue(mockError);

    const { result } = renderHook(() => useAutoRefresh(mockRefreshFunction));

    await act(async () => {
      await expect(result.current.executeAndRefresh(mockMutationFunction)).rejects.toThrow(
        'Mutation failed'
      );
    });

    expect(mockMutationFunction).toHaveBeenCalledTimes(1);
    expect(mockRefreshFunction).not.toHaveBeenCalled(); // Should not refresh if mutation fails
  });
});
