import AxiosMockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import type { UserWeakMuscle } from './types';
import { getUserWeakMuscles, addUserWeakMuscle, removeUserWeakMuscle } from './userWeakMuscle';

const mockAdapter = new AxiosMockAdapter(ENDPOINT);

const mockUserWeakMuscles: UserWeakMuscle[] = [
  {
    user_id: 'test-user-id',
    muscle_name: 'Biceps',
  },
  {
    user_id: 'test-user-id',
    muscle_name: 'Triceps',
  },
];

const mockSingleUserWeakMuscle: UserWeakMuscle = {
  user_id: 'test-user-id',
  muscle_name: 'Deltoids',
};

describe('userWeakMuscle API', () => {
  beforeEach(() => {
    mockAdapter.reset();
  });

  describe('getUserWeakMuscles', () => {
    it('should get user weak muscles successfully', async () => {
      mockAdapter.onGet('/user_weak_muscle/test-user-id').reply(200, mockUserWeakMuscles);

      const result = await getUserWeakMuscles('test-user-id');

      expect(result).toEqual(mockUserWeakMuscles);
      expect(mockAdapter.history.get[0].url).toBe('/user_weak_muscle/test-user-id');
    });

    it('should handle 404 error', async () => {
      const errorResponse = { error: 'User not found' };
      mockAdapter.onGet('/user_weak_muscle/test-user-id').reply(404, errorResponse);

      await expect(getUserWeakMuscles('test-user-id')).rejects.toEqual(errorResponse);
    });

    it('should handle 401 error', async () => {
      const errorResponse = { error: 'Unauthorized' };
      mockAdapter.onGet('/user_weak_muscle/test-user-id').reply(401, errorResponse);

      await expect(getUserWeakMuscles('test-user-id')).rejects.toEqual(errorResponse);
    });

    it('should handle 500 error', async () => {
      const errorResponse = { error: 'Internal server error' };
      mockAdapter.onGet('/user_weak_muscle/test-user-id').reply(500, errorResponse);

      await expect(getUserWeakMuscles('test-user-id')).rejects.toEqual(errorResponse);
    });

    it('should handle network error', async () => {
      mockAdapter.onGet('/user_weak_muscle/test-user-id').networkError();

      await expect(getUserWeakMuscles('test-user-id')).rejects.toEqual({ error: 'Network Error' });
    }, 10000);

    it('should handle timeout error', async () => {
      mockAdapter.onGet('/user_weak_muscle/test-user-id').timeout();

      await expect(getUserWeakMuscles('test-user-id')).rejects.toEqual({
        error: 'timeout of 10000ms exceeded',
      });
    });

    it('should encode user ID in URL', async () => {
      mockAdapter.onGet('/user_weak_muscle/user%20with%20spaces').reply(200, mockUserWeakMuscles);

      await getUserWeakMuscles('user with spaces');

      expect(mockAdapter.history.get[0].url).toBe('/user_weak_muscle/user%20with%20spaces');
    });
  });

  describe('addUserWeakMuscle', () => {
    it('should add user weak muscle successfully', async () => {
      mockAdapter.onPost('/user_weak_muscle/').reply(200, mockSingleUserWeakMuscle);

      const result = await addUserWeakMuscle('test-user-id', 'Deltoids');

      expect(result).toEqual(mockSingleUserWeakMuscle);
      expect(mockAdapter.history.post[0].url).toBe('/user_weak_muscle/');
      expect(mockAdapter.history.post[0].params).toEqual({
        user_id: 'test-user-id',
        muscle_name: 'Deltoids',
      });
    });

    it('should handle 400 error', async () => {
      const errorResponse = { error: 'Bad request' };
      mockAdapter.onPost('/user_weak_muscle/').reply(400, errorResponse);

      await expect(addUserWeakMuscle('test-user-id', 'Deltoids')).rejects.toEqual(errorResponse);
    });

    it('should handle 422 error', async () => {
      const errorResponse = { error: 'Validation error' };
      mockAdapter.onPost('/user_weak_muscle/').reply(422, errorResponse);

      await expect(addUserWeakMuscle('test-user-id', 'Deltoids')).rejects.toEqual(errorResponse);
    });

    it('should handle 409 error', async () => {
      const errorResponse = { error: 'Conflict' };
      mockAdapter.onPost('/user_weak_muscle/').reply(409, errorResponse);

      await expect(addUserWeakMuscle('test-user-id', 'Deltoids')).rejects.toEqual(errorResponse);
    });

    it('should handle 500 error', async () => {
      const errorResponse = { error: 'Internal server error' };
      mockAdapter.onPost('/user_weak_muscle/').reply(500, errorResponse);

      await expect(addUserWeakMuscle('test-user-id', 'Deltoids')).rejects.toEqual(errorResponse);
    });

    it('should handle network error', async () => {
      mockAdapter.onPost('/user_weak_muscle/').networkError();

      await expect(addUserWeakMuscle('test-user-id', 'Deltoids')).rejects.toEqual({
        error: 'Network Error',
      });
    }, 10000);

    it('should handle timeout error', async () => {
      mockAdapter.onPost('/user_weak_muscle/').timeout();

      await expect(addUserWeakMuscle('test-user-id', 'Deltoids')).rejects.toEqual({
        error: 'timeout of 10000ms exceeded',
      });
    });
  });

  describe('removeUserWeakMuscle', () => {
    it('should remove user weak muscle successfully', async () => {
      mockAdapter.onDelete('/user_weak_muscle/').reply(200);

      await removeUserWeakMuscle('test-user-id', 'Deltoids');

      expect(mockAdapter.history.delete[0].url).toBe('/user_weak_muscle/');
      expect(mockAdapter.history.delete[0].params).toEqual({
        user_id: 'test-user-id',
        muscle_name: 'Deltoids',
      });
    });

    it('should handle 404 error', async () => {
      const errorResponse = { error: 'User weak muscle not found' };
      mockAdapter.onDelete('/user_weak_muscle/').reply(404, errorResponse);

      await expect(removeUserWeakMuscle('test-user-id', 'Deltoids')).rejects.toEqual(errorResponse);
    });

    it('should handle 401 error', async () => {
      const errorResponse = { error: 'Unauthorized' };
      mockAdapter.onDelete('/user_weak_muscle/').reply(401, errorResponse);

      await expect(removeUserWeakMuscle('test-user-id', 'Deltoids')).rejects.toEqual(errorResponse);
    });

    it('should handle 500 error', async () => {
      const errorResponse = { error: 'Internal server error' };
      mockAdapter.onDelete('/user_weak_muscle/').reply(500, errorResponse);

      await expect(removeUserWeakMuscle('test-user-id', 'Deltoids')).rejects.toEqual(errorResponse);
    });

    it('should handle network error', async () => {
      mockAdapter.onDelete('/user_weak_muscle/').networkError();

      await expect(removeUserWeakMuscle('test-user-id', 'Deltoids')).rejects.toEqual({
        error: 'Network Error',
      });
    }, 10000);

    it('should handle timeout error', async () => {
      mockAdapter.onDelete('/user_weak_muscle/').timeout();

      await expect(removeUserWeakMuscle('test-user-id', 'Deltoids')).rejects.toEqual({
        error: 'timeout of 10000ms exceeded',
      });
    });
  });
});
