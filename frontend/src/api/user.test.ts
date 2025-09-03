import AxiosMockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import type { User } from './types';
import { createUserProfile, getCurrentUser } from './user';

const mockAdapter = new AxiosMockAdapter(ENDPOINT);

const mockUser: User = {
  keycloak_id: 'test-id',
  name: 'Test User',
  created_at: new Date('2023-01-01T00:00:00.000Z'),
  updated_at: new Date('2023-01-01T00:00:00.000Z'),
};

describe('user API', () => {
  beforeEach(() => {
    mockAdapter.reset();
  });

  describe('createUserProfile', () => {
    it('should create user profile successfully', async () => {
      mockAdapter.onPost('/user/').reply(200, mockUser);

      const result = await createUserProfile();

      expect(result).toEqual(mockUser);
      expect(mockAdapter.history.post[0].url).toBe('/user/');
    });

    it('should handle 400 error', async () => {
      const errorResponse = { error: 'Bad request' };
      mockAdapter.onPost('/user/').reply(400, errorResponse);

      await expect(createUserProfile()).rejects.toEqual(errorResponse);
    });

    it('should handle 422 error', async () => {
      const errorResponse = { error: 'Validation error' };
      mockAdapter.onPost('/user/').reply(422, errorResponse);

      await expect(createUserProfile()).rejects.toEqual(errorResponse);
    });

    it('should handle 409 error', async () => {
      const errorResponse = { error: 'Conflict' };
      mockAdapter.onPost('/user/').reply(409, errorResponse);

      await expect(createUserProfile()).rejects.toEqual(errorResponse);
    });

    it('should handle 500 error', async () => {
      const errorResponse = { error: 'Internal server error' };
      mockAdapter.onPost('/user/').reply(500, errorResponse);

      await expect(createUserProfile()).rejects.toEqual(errorResponse);
    });

    it('should handle network error', async () => {
      mockAdapter.onPost('/user/').networkError();

      await expect(createUserProfile()).rejects.toEqual({ error: 'Network Error' });
    });

    it('should handle timeout error', async () => {
      mockAdapter.onPost('/user/').timeout();

      await expect(createUserProfile()).rejects.toEqual({ error: 'timeout of 2500ms exceeded' });
    });
  });

  describe('getCurrentUser', () => {
    it('should get current user successfully', async () => {
      mockAdapter.onGet('/user/me').reply(200, mockUser);

      const result = await getCurrentUser();

      expect(result).toEqual(mockUser);
      expect(mockAdapter.history.get[0].url).toBe('/user/me');
    });

    it('should handle 404 error', async () => {
      const errorResponse = { error: 'User not found' };
      mockAdapter.onGet('/user/me').reply(404, errorResponse);

      await expect(getCurrentUser()).rejects.toEqual(errorResponse);
    });

    it('should handle 401 error', async () => {
      const errorResponse = { error: 'Unauthorized' };
      mockAdapter.onGet('/user/me').reply(401, errorResponse);

      await expect(getCurrentUser()).rejects.toEqual(errorResponse);
    });

    it('should handle 500 error', async () => {
      const errorResponse = { error: 'Internal server error' };
      mockAdapter.onGet('/user/me').reply(500, errorResponse);

      await expect(getCurrentUser()).rejects.toEqual(errorResponse);
    });

    it('should handle network error', async () => {
      mockAdapter.onGet('/user/me').networkError();

      await expect(getCurrentUser()).rejects.toEqual({ error: 'Network Error' });
    });

    it('should handle timeout error', async () => {
      mockAdapter.onGet('/user/me').timeout();

      await expect(getCurrentUser()).rejects.toEqual({ error: 'timeout of 2500ms exceeded' });
    });
  });
});
