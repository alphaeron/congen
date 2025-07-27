import { registerUser, getUserById } from './user';
import { ENDPOINT } from './endpoint';
import { User } from './types';
import AxiosMockAdapter from 'axios-mock-adapter';

const mockAdapter = new AxiosMockAdapter(ENDPOINT);

describe('user API', () => {
  beforeEach(() => {
    mockAdapter.reset();
  });

  describe('registerUser', () => {
    const mockUser: User = {
      id: 1,
      name: 'John Doe',
      age: 30,
      height: 175.5,
      weight: 80.0,
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z',
      keycloak_user_id: '123e4567-e89b-12d3-a456-426614174000',
    };

    it('should register a new user successfully', async () => {
      mockAdapter
        .onPost(
          '/user/?name=John+Doe&age=30&height=175.5&weight=80&email=john.doe%40example.com&password=securePassword123'
        )
        .reply(200, mockUser);

      const result = await registerUser(
        'John Doe',
        30,
        175.5,
        80.0,
        'john.doe@example.com',
        'securePassword123'
      );

      expect(mockAdapter.history.post.length).toBe(1);
      expect(result).toEqual(mockUser);
    });

    it('should register a new user with unit parameter', async () => {
      mockAdapter
        .onPost(
          '/user/?name=John+Doe&age=30&height=175.5&weight=80&email=john.doe%40example.com&password=securePassword123&unit=LB'
        )
        .reply(200, mockUser);

      const result = await registerUser(
        'John Doe',
        30,
        175.5,
        80.0,
        'john.doe@example.com',
        'securePassword123',
        'LB'
      );

      expect(mockAdapter.history.post.length).toBe(1);
      expect(result).toEqual(mockUser);
    });

    it('should handle 400 Bad Request errors', async () => {
      const errorResponse = { message: 'Invalid input data' };
      mockAdapter
        .onPost(
          '/user/?name=John+Doe&age=30&height=175.5&weight=80&email=john.doe%40example.com&password=securePassword123'
        )
        .reply(400, errorResponse);

      await expect(
        registerUser('John Doe', 30, 175.5, 80.0, 'john.doe@example.com', 'securePassword123')
      ).rejects.toEqual(errorResponse);

      expect(mockAdapter.history.post.length).toBe(1);
    });

    it('should handle 422 Unprocessable Entity errors', async () => {
      const errorResponse = {
        message: 'Validation error',
        errors: ['Age must be between 1 and 150'],
      };
      mockAdapter
        .onPost(
          '/user/?name=John+Doe&age=30&height=175.5&weight=80&email=john.doe%40example.com&password=securePassword123'
        )
        .reply(422, errorResponse);

      await expect(
        registerUser('John Doe', 30, 175.5, 80.0, 'john.doe@example.com', 'securePassword123')
      ).rejects.toEqual(errorResponse);

      expect(mockAdapter.history.post.length).toBe(1);
    });

    it('should handle 409 Conflict errors (user already exists)', async () => {
      const errorResponse = { message: 'User with this email already exists' };
      mockAdapter
        .onPost(
          '/user/?name=John+Doe&age=30&height=175.5&weight=80&email=john.doe%40example.com&password=securePassword123'
        )
        .reply(409, errorResponse);

      await expect(
        registerUser('John Doe', 30, 175.5, 80.0, 'john.doe@example.com', 'securePassword123')
      ).rejects.toEqual(errorResponse);

      expect(mockAdapter.history.post.length).toBe(1);
    });

    it('should handle 500 Internal Server Error', async () => {
      const errorResponse = { message: 'Internal server error' };
      mockAdapter
        .onPost(
          '/user/?name=John+Doe&age=30&height=175.5&weight=80&email=john.doe%40example.com&password=securePassword123'
        )
        .reply(500, errorResponse);

      await expect(
        registerUser('John Doe', 30, 175.5, 80.0, 'john.doe@example.com', 'securePassword123')
      ).rejects.toEqual(errorResponse);

      expect(mockAdapter.history.post.length).toBe(1);
    });

    it('should handle network errors', async () => {
      mockAdapter
        .onPost(
          '/user/?name=John+Doe&age=30&height=175.5&weight=80&email=john.doe%40example.com&password=securePassword123'
        )
        .networkError();

      await expect(
        registerUser('John Doe', 30, 175.5, 80.0, 'john.doe@example.com', 'securePassword123')
      ).rejects.toBeUndefined();

      expect(mockAdapter.history.post.length).toBe(1);
    });

    it('should handle timeout errors', async () => {
      mockAdapter
        .onPost(
          '/user/?name=John+Doe&age=30&height=175.5&weight=80&email=john.doe%40example.com&password=securePassword123'
        )
        .timeout();

      await expect(
        registerUser('John Doe', 30, 175.5, 80.0, 'john.doe@example.com', 'securePassword123')
      ).rejects.toBeUndefined();

      expect(mockAdapter.history.post.length).toBe(1);
    });

    it('should properly encode special characters in parameters', async () => {
      // Test with special characters and check what URLSearchParams actually produces
      const params = new URLSearchParams({
        name: 'John Doe Jr.',
        age: '30',
        height: '175.5',
        weight: '80',
        email: 'john.doe+test@example.com',
        password: 'secure&Password#123',
      });
      const expectedUrl = `/user/?${params.toString()}`;

      mockAdapter.onPost(expectedUrl).reply(200, mockUser);

      const result = await registerUser(
        'John Doe Jr.',
        30,
        175.5,
        80.0,
        'john.doe+test@example.com',
        'secure&Password#123'
      );

      expect(mockAdapter.history.post.length).toBe(1);
      expect(result).toEqual(mockUser);
    });
  });

  describe('getUserById', () => {
    const mockUser: User = {
      id: 1,
      name: 'John Doe',
      age: 30,
      height: 175.5,
      weight: 80.0,
      created_at: '2024-01-01T00:00:00Z',
      updated_at: '2024-01-01T00:00:00Z',
      keycloak_user_id: '123e4567-e89b-12d3-a456-426614174000',
    };

    it('should get user by ID successfully', async () => {
      mockAdapter.onGet('/user/1').reply(200, mockUser);

      const result = await getUserById(1);

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockUser);
    });

    it('should handle 404 Not Found errors', async () => {
      const errorResponse = { message: 'User not found' };
      mockAdapter.onGet('/user/999').reply(404, errorResponse);

      await expect(getUserById(999)).rejects.toEqual(errorResponse);

      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle 403 Forbidden errors', async () => {
      const errorResponse = { message: 'Access denied' };
      mockAdapter.onGet('/user/1').reply(403, errorResponse);

      await expect(getUserById(1)).rejects.toEqual(errorResponse);

      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle 401 Unauthorized errors', async () => {
      const errorResponse = { message: 'Unauthorized' };
      mockAdapter.onGet('/user/1').reply(401, errorResponse);

      await expect(getUserById(1)).rejects.toEqual(errorResponse);

      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle 500 Internal Server Error', async () => {
      const errorResponse = { message: 'Internal server error' };
      mockAdapter.onGet('/user/1').reply(500, errorResponse);

      await expect(getUserById(1)).rejects.toEqual(errorResponse);

      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle network errors', async () => {
      mockAdapter.onGet('/user/1').networkError();

      await expect(getUserById(1)).rejects.toBeUndefined();

      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle timeout errors', async () => {
      mockAdapter.onGet('/user/1').timeout();

      await expect(getUserById(1)).rejects.toBeUndefined();

      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle different user IDs', async () => {
      const mockUser2: User = {
        id: 2,
        name: 'Jane Smith',
        age: 25,
        height: 165.0,
        weight: 60.0,
        created_at: '2024-01-02T00:00:00Z',
        updated_at: '2024-01-02T00:00:00Z',
        keycloak_user_id: '456e7890-e89b-12d3-a456-426614174000',
      };

      mockAdapter.onGet('/user/2').reply(200, mockUser2);

      const result = await getUserById(2);

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockUser2);
    });
  });

  describe('API request format', () => {
    it('should use correct HTTP methods and URLs', async () => {
      const mockUser: User = {
        id: 1,
        name: 'John Doe',
        age: 30,
        height: 175.5,
        weight: 80.0,
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z',
      };

      // Test registerUser
      mockAdapter
        .onPost(
          '/user/?name=John+Doe&age=30&height=175.5&weight=80&email=john.doe%40example.com&password=securePassword123'
        )
        .reply(200, mockUser);
      await registerUser('John Doe', 30, 175.5, 80.0, 'john.doe@example.com', 'securePassword123');

      expect(mockAdapter.history.post.length).toBe(1);
      expect(mockAdapter.history.post[0].url).toBe(
        '/user/?name=John+Doe&age=30&height=175.5&weight=80&email=john.doe%40example.com&password=securePassword123'
      );

      // Test getUserById
      mockAdapter.onGet('/user/1').reply(200, mockUser);
      await getUserById(1);

      expect(mockAdapter.history.get.length).toBe(1);
      expect(mockAdapter.history.get[0].url).toBe('/user/1');
    });

    it('should verify request headers', async () => {
      const mockUser: User = {
        id: 1,
        name: 'John Doe',
        age: 30,
        height: 175.5,
        weight: 80.0,
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z',
      };

      mockAdapter
        .onPost(
          '/user/?name=John+Doe&age=30&height=175.5&weight=80&email=john.doe%40example.com&password=securePassword123'
        )
        .reply(200, mockUser);
      await registerUser('John Doe', 30, 175.5, 80.0, 'john.doe@example.com', 'securePassword123');

      expect(mockAdapter.history.post[0].headers).toHaveProperty(
        'Content-Type',
        'application/json'
      );
    });
  });
});
