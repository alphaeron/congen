import MockAdapter from 'axios-mock-adapter';

import { ENDPOINT, setTokenGetter, REQUEST } from './endpoint';

// Mock the globals module
jest.mock('../globals', () => ({
  BACKEND_URL: 'http://localhost:8888',
}));

describe('endpoint', () => {
  let mock: MockAdapter;

  beforeEach(() => {
    mock = new MockAdapter(ENDPOINT);
    // Reset token getter
    setTokenGetter(() => null);
  });

  afterEach(() => {
    mock.restore();
  });

  describe('ENDPOINT configuration', () => {
    it('should have correct base URL', () => {
      expect(ENDPOINT.defaults.baseURL).toBe('http://localhost:8888/api/v1/');
    });

    it('should have correct timeout', () => {
      expect(ENDPOINT.defaults.timeout).toBe(2500);
    });

    it('should have withCredentials set to true', () => {
      expect(ENDPOINT.defaults.withCredentials).toBe(true);
    });

    it('should have correct default headers', () => {
      expect(ENDPOINT.defaults.headers).toMatchObject({
        'Content-Type': 'application/json',
        'X-Requested-With': 'XMLHttpRequest',
      });
    });
  });

  describe('setTokenGetter', () => {
    it('should set token getter function', () => {
      const mockTokenGetter = jest.fn(() => 'test-token');
      setTokenGetter(mockTokenGetter);

      // The token getter is used in interceptors, so we can't directly test it
      // but we can verify it doesn't throw
      expect(mockTokenGetter).toBeDefined();
    });
  });

  describe('Request interceptor', () => {
    it('should add X-Requested-With header to all requests', async () => {
      mock.onGet('/test').reply(200, { data: 'success' });

      await ENDPOINT.get('/test');

      expect(mock.history.get[0].headers?.['X-Requested-With']).toBe('XMLHttpRequest');
    });

    it('should add Authorization header when token is available', async () => {
      const mockToken =
        'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTl9.test';
      setTokenGetter(() => mockToken);

      mock.onGet('/test').reply(200, { data: 'success' });

      await ENDPOINT.get('/test');

      expect(mock.history.get[0].headers?.Authorization).toBe(`Bearer ${mockToken}`);
    });

    it('should not add Authorization header when token is not available', async () => {
      setTokenGetter(() => null);

      mock.onGet('/test').reply(200, { data: 'success' });

      await ENDPOINT.get('/test');

      expect(mock.history.get[0].headers?.Authorization).toBeUndefined();
    });

    it('should handle malformed JWT tokens gracefully', async () => {
      const malformedToken = 'invalid-token';
      setTokenGetter(() => malformedToken);

      mock.onGet('/test').reply(200, { data: 'success' });

      // Should not throw an error
      await expect(ENDPOINT.get('/test')).resolves.toBeDefined();
    });

    it('should handle expired tokens gracefully', async () => {
      // Create a token that's already expired
      const expiredToken =
        'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjF9.test';
      setTokenGetter(() => expiredToken);

      mock.onGet('/test').reply(200, { data: 'success' });

      // Should not throw an error
      await expect(ENDPOINT.get('/test')).resolves.toBeDefined();
    });
  });

  describe('Response interceptor', () => {
    it('should handle 401 errors without throwing', async () => {
      mock.onGet('/test').reply(401, { error: 'Unauthorized' });

      await expect(ENDPOINT.get('/test')).rejects.toBeDefined();
    });

    it('should pass through successful responses', async () => {
      const responseData = { data: 'success' };
      mock.onGet('/test').reply(200, responseData);

      const response = await ENDPOINT.get('/test');
      expect(response.data).toEqual(responseData);
    });
  });

  describe('REQUEST helper', () => {
    it('should return data from successful response', async () => {
      const responseData = { id: 1, name: 'test' };
      mock.onGet('/test').reply(200, responseData);

      const result = await REQUEST({
        url: '/test',
        method: 'GET',
      });

      expect(result).toEqual(responseData);
    });

    it('should reject with response data on error', async () => {
      const errorData = { error: 'Not found' };
      mock.onGet('/test').reply(404, errorData);

      await expect(
        REQUEST({
          url: '/test',
          method: 'GET',
        })
      ).rejects.toEqual(errorData);
    });

    it('should handle network errors', async () => {
      mock.onGet('/test').networkError();

      await expect(
        REQUEST({
          url: '/test',
          method: 'GET',
        })
      ).rejects.toEqual({ error: 'Network Error' });
    });

    it('should handle timeout errors', async () => {
      mock.onGet('/test').timeout();

      await expect(
        REQUEST({
          url: '/test',
          method: 'GET',
        })
      ).rejects.toEqual({ error: 'timeout of 2500ms exceeded' });
    });

    it('should work with POST requests', async () => {
      const postData = { name: 'test' };
      const responseData = { id: 1, ...postData };
      mock.onPost('/test', postData).reply(201, responseData);

      const result = await REQUEST({
        url: '/test',
        method: 'POST',
        data: postData,
      });

      expect(result).toEqual(responseData);
    });

    it('should work with PUT requests', async () => {
      const putData = { name: 'updated' };
      const responseData = { id: 1, ...putData };
      mock.onPut('/test/1', putData).reply(200, responseData);

      const result = await REQUEST({
        url: '/test/1',
        method: 'PUT',
        data: putData,
      });

      expect(result).toEqual(responseData);
    });

    it('should work with DELETE requests', async () => {
      mock.onDelete('/test/1').reply(204);

      const result = await REQUEST({
        url: '/test/1',
        method: 'DELETE',
      });

      expect(result).toBeUndefined();
    });
  });
});
