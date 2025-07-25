import {
  InvalidateQueryFilters,
  QueryOptions,
  useQuery,
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';

export const useApiGet = <T>(
  key: string[],
  fn: (...args: any[]) => Promise<T>, // eslint-disable-line @typescript-eslint/no-explicit-any
  options: QueryOptions<T, Error, T, string[]>,
  params: any[] = [] // eslint-disable-line @typescript-eslint/no-explicit-any
) => {
  // Just call the provided fn, which should use REQUEST from endpoint.ts internally,
  // which already handles authentication
  return useQuery<T, Error, T, string[]>({
    queryKey: key,
    queryFn: async (): Promise<T> => fn(...params),
    ...options,
  });
};

export const useApiSend = <T>(
  fn: (...args: any[]) => Promise<T>, // eslint-disable-line @typescript-eslint/no-explicit-any
  success: (arg: T) => void,
  error: (...args: any[]) => void, // eslint-disable-line @typescript-eslint/no-explicit-any
  invalidateKey: InvalidateQueryFilters[],
  options: QueryOptions<T, Error, T, string[]> = {},
  params: any[] = [] // eslint-disable-line @typescript-eslint/no-explicit-any
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
