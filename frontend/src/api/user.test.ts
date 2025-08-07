import AxiosMockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import type { User } from './types';
import { createUserProfile, getUserById } from './user';

const mockAdapter = new AxiosMockAdapter(ENDPOINT);

describe('user API', () => {
  beforeEach(() => {
    mockAdapter.reset();
  });

  describe('createUserProfile', () => {
    const mockUser: User = {
      keycloak_id: '123e4567-e89b-12d3-a456-426614174000',
      name: 'John Doe',
      age: 30,
      height: 175.5,
      weight: 80.0,
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z',
    };

    it('should create a new user profile successfully', async () => {
      mockAdapter.onPost('/user/?name=John+Doe&age=30&height=175.5&weight=80').reply(200, mockUser);

      const result = await createUserProfile('John Doe', 30, 175.5, 80.0);

      expect(mockAdapter.history.post.length).toBe(1);
      expect(result).toEqual(mockUser);
    });

    it('should create a new user profile with unit parameter', async () => {
      mockAdapter
        .onPost('/user/?name=John+Doe&age=30&height=175.5&weight=80&unit=LB')
        .reply(200, mockUser);

      const result = await createUserProfile('John Doe', 30, 175.5, 80.0, 'LB');

      expect(mockAdapter.history.post.length).toBe(1);
      expect(result).toEqual(mockUser);
    });

    it('should handle 400 Bad Request errors', async () => {
      const errorResponse = { message: 'Invalid input data' };
      mockAdapter
        .onPost('/user/?name=John+Doe&age=30&height=175.5&weight=80')
        .reply(400, errorResponse);

      await expect(createUserProfile('John Doe', 30, 175.5, 80.0)).rejects.toEqual(errorResponse);

      expect(mockAdapter.history.post.length).toBe(1);
    });

    it('should handle 422 Unprocessable Entity errors', async () => {
      const errorResponse = {
        message: 'Validation error',
        errors: ['Age must be between 1 and 150'],
      };
      mockAdapter
        .onPost('/user/?name=John+Doe&age=30&height=175.5&weight=80')
        .reply(422, errorResponse);

      await expect(createUserProfile('John Doe', 30, 175.5, 80.0)).rejects.toEqual(errorResponse);

      expect(mockAdapter.history.post.length).toBe(1);
    });

    it('should handle 409 Conflict errors (user already exists)', async () => {
      const errorResponse = { message: 'User already exists' };
      mockAdapter
        .onPost('/user/?name=John+Doe&age=30&height=175.5&weight=80')
        .reply(409, errorResponse);

      await expect(createUserProfile('John Doe', 30, 175.5, 80.0)).rejects.toEqual(errorResponse);

      expect(mockAdapter.history.post.length).toBe(1);
    });

    it('should handle 500 Internal Server Error', async () => {
      const errorResponse = { message: 'Internal server error' };
      mockAdapter
        .onPost('/user/?name=John+Doe&age=30&height=175.5&weight=80')
        .reply(500, errorResponse);

      await expect(createUserProfile('John Doe', 30, 175.5, 80.0)).rejects.toEqual(errorResponse);

      expect(mockAdapter.history.post.length).toBe(1);
    });

    it('should handle network errors', async () => {
      mockAdapter.onPost('/user/?name=John+Doe&age=30&height=175.5&weight=80').networkError();

      await expect(createUserProfile('John Doe', 30, 175.5, 80.0)).rejects.toBeUndefined();

      expect(mockAdapter.history.post.length).toBe(1);
    });

    it('should handle timeout errors', async () => {
      mockAdapter.onPost('/user/?name=John+Doe&age=30&height=175.5&weight=80').timeout();

      await expect(createUserProfile('John Doe', 30, 175.5, 80.0)).rejects.toBeUndefined();

      expect(mockAdapter.history.post.length).toBe(1);
    });

    it('should properly encode special characters in parameters', async () => {
      // Test with special characters and check what URLSearchParams actually produces
      const params = new URLSearchParams({
        name: 'John Doe Jr.',
        age: '30',
        height: '175.5',
        weight: '80',
      });
      const expectedUrl = `/user/?${params.toString()}`;

      mockAdapter.onPost(expectedUrl).reply(200, mockUser);

      const result = await createUserProfile('John Doe Jr.', 30, 175.5, 80.0);

      expect(mockAdapter.history.post.length).toBe(1);
      expect(result).toEqual(mockUser);
    });
  });

  describe('getUserById', () => {
    const mockUser: User = {
      keycloak_id: '123e4567-e89b-12d3-a456-426614174000',
      name: 'John Doe',
      age: 30,
      height: 175.5,
      weight: 80.0,
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z',
    };

    it('should get user by ID successfully', async () => {
      mockAdapter.onGet('/user/123e4567-e89b-12d3-a456-426614174000').reply(200, mockUser);

      const result = await getUserById('123e4567-e89b-12d3-a456-426614174000');

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockUser);
    });

    it('should handle 404 Not Found errors', async () => {
      const errorResponse = { message: 'User not found' };
      mockAdapter.onGet('/user/non-existent-id').reply(404, errorResponse);

      await expect(getUserById('non-existent-id')).rejects.toEqual(errorResponse);

      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle 403 Forbidden errors', async () => {
      const errorResponse = { message: 'Access denied' };
      mockAdapter.onGet('/user/123e4567-e89b-12d3-a456-426614174000').reply(403, errorResponse);

      await expect(getUserById('123e4567-e89b-12d3-a456-426614174000')).rejects.toEqual(
        errorResponse
      );

      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle 401 Unauthorized errors', async () => {
      const errorResponse = { message: 'Unauthorized' };
      mockAdapter.onGet('/user/123e4567-e89b-12d3-a456-426614174000').reply(401, errorResponse);

      await expect(getUserById('123e4567-e89b-12d3-a456-426614174000')).rejects.toEqual(
        errorResponse
      );

      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle 500 Internal Server Error', async () => {
      const errorResponse = { message: 'Internal server error' };
      mockAdapter.onGet('/user/123e4567-e89b-12d3-a456-426614174000').reply(500, errorResponse);

      await expect(getUserById('123e4567-e89b-12d3-a456-426614174000')).rejects.toEqual(
        errorResponse
      );

      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle network errors', async () => {
      mockAdapter.onGet('/user/123e4567-e89b-12d3-a456-426614174000').networkError();

      await expect(getUserById('123e4567-e89b-12d3-a456-426614174000')).rejects.toBeUndefined();

      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle timeout errors', async () => {
      mockAdapter.onGet('/user/123e4567-e89b-12d3-a456-426614174000').timeout();

      await expect(getUserById('123e4567-e89b-12d3-a456-426614174000')).rejects.toBeUndefined();

      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle different user IDs', async () => {
      const mockUser2: User = {
        keycloak_id: '456e7890-e89b-12d3-a456-426614174000',
        name: 'Jane Smith',
        age: 25,
        height: 165.0,
        weight: 60.0,
        created_at: '2024-01-02T00:00:00Z',
        updated_at: '2024-01-02T00:00:00Z',
      };

      mockAdapter.onGet('/user/456e7890-e89b-12d3-a456-426614174000').reply(200, mockUser2);

      const result = await getUserById('456e7890-e89b-12d3-a456-426614174000');

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockUser2);
    });
  });

  describe('API request format', () => {
    it('should use correct HTTP methods and URLs', async () => {
      const mockUser: User = {
        keycloak_id: '123e4567-e89b-12d3-a456-426614174000',
        name: 'John Doe',
        age: 30,
        height: 175.5,
        weight: 80.0,
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z',
      };

      // Test createUserProfile
      mockAdapter.onPost('/user/?name=John+Doe&age=30&height=175.5&weight=80').reply(200, mockUser);
      await createUserProfile('John Doe', 30, 175.5, 80.0);

      expect(mockAdapter.history.post.length).toBe(1);
      expect(mockAdapter.history.post[0].url).toBe(
        '/user/?name=John+Doe&age=30&height=175.5&weight=80'
      );

      // Test getUserById
      mockAdapter.onGet('/user/123e4567-e89b-12d3-a456-426614174000').reply(200, mockUser);
      await getUserById('123e4567-e89b-12d3-a456-426614174000');

      expect(mockAdapter.history.get.length).toBe(1);
      expect(mockAdapter.history.get[0].url).toBe('/user/123e4567-e89b-12d3-a456-426614174000');
    });

    it('should verify request headers', async () => {
      const mockUser: User = {
        keycloak_id: '123e4567-e89b-12d3-a456-426614174000',
        name: 'John Doe',
        age: 30,
        height: 175.5,
        weight: 80.0,
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z',
      };

      mockAdapter.onPost('/user/?name=John+Doe&age=30&height=175.5&weight=80').reply(200, mockUser);
      await createUserProfile('John Doe', 30, 175.5, 80.0);

      expect(mockAdapter.history.post[0].headers).toHaveProperty(
        'Content-Type',
        'application/json'
      );
    });
  });
});
