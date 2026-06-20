import { ApiRequestError } from './endpoint';

/**
 * Asserts that a REQUEST-backed call rejects with ApiRequestError matching the backend response body.
 */
export async function expectRequestError(
  promise: Promise<unknown>,
  errorResponse: Record<string, unknown>
): Promise<void> {
  let caught: unknown;
  try {
    await promise;
    throw new Error('Expected promise to reject');
  } catch (error) {
    caught = error;
  }
  expect(caught).toBeInstanceOf(ApiRequestError);
  const apiError = caught as ApiRequestError;
  expect(apiError.responseData).toEqual(errorResponse);
  if (typeof errorResponse.error === 'string') {
    expect(apiError.message).toContain(errorResponse.error);
  } else {
    expect(apiError.message).toContain('Request failed');
  }
}
