import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

import type { InvalidateQueryFilters, QueryOptions } from '@tanstack/react-query';

export const useApiGet = <T>(
  key: string[],
  fn: (...args: unknown[]) => Promise<T>,
  options: QueryOptions<T, Error, T, string[]>,
  params: unknown[] = []
) => {
  // Just call the provided fn, which should use REQUEST from endpoint.ts internally,
  // which already handles authentication
  return useQuery<T, Error, T, string[]>({
    queryKey: key, // eslint-disable-line @tanstack/query/exhaustive-deps
    queryFn: async (): Promise<T> => fn(...params),
    ...options,
  });
};

export const useApiSend = <T>(
  fn: (...args: unknown[]) => Promise<T>,
  success: (arg: T) => void,
  error: (...args: unknown[]) => void,
  invalidateKey: InvalidateQueryFilters[],
  options: QueryOptions<T, Error, T, string[]> = {},
  params: unknown[] = []
) => {
  const queryClient = useQueryClient();

  return useMutation<T, Error>({
    mutationFn: async (): Promise<T> => fn(...params),
    onSuccess: (data): void => {
      if (invalidateKey) {
        invalidateKey.forEach(key => {
          queryClient.invalidateQueries(key);
        });
      }
      if (success) {
        success(data);
      }
    },
    onError: error,
    retry: 2,
    ...options,
  });
};
