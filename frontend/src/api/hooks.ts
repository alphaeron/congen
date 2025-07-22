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
  options: QueryOptions,
  params: any[] = [] // eslint-disable-line @typescript-eslint/no-explicit-any
) =>
  useQuery<T>({
    queryKey: key, // eslint-disable-line @tanstack/query/exhaustive-deps
    queryFn: () => fn(...params),
    ...options,
  });

export const useApiSend = <T>(
  fn: (...args: any[]) => Promise<T>, // eslint-disable-line @typescript-eslint/no-explicit-any
  success: (arg: T) => void,
  error: (...args: any[]) => void, // eslint-disable-line @typescript-eslint/no-explicit-any
  invalidateKey: InvalidateQueryFilters[],
  options: QueryOptions,
  params: any[] = [] // eslint-disable-line @typescript-eslint/no-explicit-any
) => {
  const queryClient = useQueryClient();

  return useMutation<T>({
    mutationFn: () => fn(...params),
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
