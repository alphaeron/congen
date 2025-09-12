import AxiosMockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import type { UserExercisePreference } from './types';
import {
  getUserExercisePreferences,
  upsertUserExercisePreference,
  updateUserExercisePreference,
  removeUserExercisePreference,
} from './userExercisePreference';

const mockAdapter = new AxiosMockAdapter(ENDPOINT);

const mockUserExercisePreferences: UserExercisePreference[] = [
  {
    user_id: 'test-user-id',
    exercise_name: 'Bench Press',
    should_avoid: false,
  },
  {
    user_id: 'test-user-id',
    exercise_name: 'Squat',
    should_avoid: true,
  },
];

const mockSingleUserExercisePreference: UserExercisePreference = {
  user_id: 'test-user-id',
  exercise_name: 'Deadlift',
  should_avoid: false,
};

describe('userExercisePreference API', () => {
  beforeEach(() => {
    mockAdapter.reset();
  });

  describe('getUserExercisePreferences', () => {
    it('should get user exercise preferences successfully', async () => {
      mockAdapter
        .onGet('/user_exercise_preference/test-user-id')
        .reply(200, mockUserExercisePreferences);

      const result = await getUserExercisePreferences('test-user-id');

      expect(result).toEqual(mockUserExercisePreferences);
      expect(mockAdapter.history.get[0].url).toBe('/user_exercise_preference/test-user-id');
    });

    it('should handle 404 error', async () => {
      const errorResponse = { error: 'User not found' };
      mockAdapter.onGet('/user_exercise_preference/test-user-id').reply(404, errorResponse);

      await expect(getUserExercisePreferences('test-user-id')).rejects.toEqual(errorResponse);
    });

    it('should handle 401 error', async () => {
      const errorResponse = { error: 'Unauthorized' };
      mockAdapter.onGet('/user_exercise_preference/test-user-id').reply(401, errorResponse);

      await expect(getUserExercisePreferences('test-user-id')).rejects.toEqual(errorResponse);
    });

    it('should handle 500 error', async () => {
      const errorResponse = { error: 'Internal server error' };
      mockAdapter.onGet('/user_exercise_preference/test-user-id').reply(500, errorResponse);

      await expect(getUserExercisePreferences('test-user-id')).rejects.toEqual(errorResponse);
    });

    it('should handle network error', async () => {
      mockAdapter.onGet('/user_exercise_preference/test-user-id').networkError();

      await expect(getUserExercisePreferences('test-user-id')).rejects.toEqual({
        error: 'Network Error',
      });
    }, 10000);

    it('should handle timeout error', async () => {
      mockAdapter.onGet('/user_exercise_preference/test-user-id').timeout();

      await expect(getUserExercisePreferences('test-user-id')).rejects.toEqual({
        error: 'timeout of 2500ms exceeded',
      });
    });

    it('should encode user ID in URL', async () => {
      mockAdapter
        .onGet('/user_exercise_preference/user%20with%20spaces')
        .reply(200, mockUserExercisePreferences);

      await getUserExercisePreferences('user with spaces');

      expect(mockAdapter.history.get[0].url).toBe('/user_exercise_preference/user%20with%20spaces');
    });
  });

  describe('upsertUserExercisePreference', () => {
    it('should upsert user exercise preference successfully', async () => {
      mockAdapter.onPost('/user_exercise_preference/').reply(200, mockSingleUserExercisePreference);

      const result = await upsertUserExercisePreference('test-user-id', 'Deadlift', false);

      expect(result).toEqual(mockSingleUserExercisePreference);
      expect(mockAdapter.history.post[0].url).toBe('/user_exercise_preference/');
      expect(mockAdapter.history.post[0].params).toEqual({
        user_id: 'test-user-id',
        exercise_name: 'Deadlift',
        should_avoid: false,
      });
    });

    it('should handle 400 error', async () => {
      const errorResponse = { error: 'Bad request' };
      mockAdapter.onPost('/user_exercise_preference/').reply(400, errorResponse);

      await expect(upsertUserExercisePreference('test-user-id', 'Deadlift', false)).rejects.toEqual(
        errorResponse
      );
    });

    it('should handle 422 error', async () => {
      const errorResponse = { error: 'Validation error' };
      mockAdapter.onPost('/user_exercise_preference/').reply(422, errorResponse);

      await expect(upsertUserExercisePreference('test-user-id', 'Deadlift', false)).rejects.toEqual(
        errorResponse
      );
    });

    it('should handle 500 error', async () => {
      const errorResponse = { error: 'Internal server error' };
      mockAdapter.onPost('/user_exercise_preference/').reply(500, errorResponse);

      await expect(upsertUserExercisePreference('test-user-id', 'Deadlift', false)).rejects.toEqual(
        errorResponse
      );
    });

    it('should handle network error', async () => {
      mockAdapter.onPost('/user_exercise_preference/').networkError();

      await expect(upsertUserExercisePreference('test-user-id', 'Deadlift', false)).rejects.toEqual(
        { error: 'Network Error' }
      );
    }, 10000);

    it('should handle timeout error', async () => {
      mockAdapter.onPost('/user_exercise_preference/').timeout();

      await expect(upsertUserExercisePreference('test-user-id', 'Deadlift', false)).rejects.toEqual(
        { error: 'timeout of 2500ms exceeded' }
      );
    });
  });

  describe('updateUserExercisePreference', () => {
    it('should update user exercise preference successfully', async () => {
      mockAdapter
        .onPatch('/user_exercise_preference/')
        .reply(200, mockSingleUserExercisePreference);

      const result = await updateUserExercisePreference('test-user-id', 'Deadlift', false);

      expect(result).toEqual(mockSingleUserExercisePreference);
      expect(mockAdapter.history.patch[0].url).toBe('/user_exercise_preference/');
      expect(mockAdapter.history.patch[0].params).toEqual({
        user_id: 'test-user-id',
        exercise_name: 'Deadlift',
        should_avoid: false,
      });
    });

    it('should handle 400 error', async () => {
      const errorResponse = { error: 'Bad request' };
      mockAdapter.onPatch('/user_exercise_preference/').reply(400, errorResponse);

      await expect(updateUserExercisePreference('test-user-id', 'Deadlift', false)).rejects.toEqual(
        errorResponse
      );
    });

    it('should handle 404 error', async () => {
      const errorResponse = { error: 'Exercise preference not found' };
      mockAdapter.onPatch('/user_exercise_preference/').reply(404, errorResponse);

      await expect(updateUserExercisePreference('test-user-id', 'Deadlift', false)).rejects.toEqual(
        errorResponse
      );
    });

    it('should handle 422 error', async () => {
      const errorResponse = { error: 'Validation error' };
      mockAdapter.onPatch('/user_exercise_preference/').reply(422, errorResponse);

      await expect(updateUserExercisePreference('test-user-id', 'Deadlift', false)).rejects.toEqual(
        errorResponse
      );
    });

    it('should handle 500 error', async () => {
      const errorResponse = { error: 'Internal server error' };
      mockAdapter.onPatch('/user_exercise_preference/').reply(500, errorResponse);

      await expect(updateUserExercisePreference('test-user-id', 'Deadlift', false)).rejects.toEqual(
        errorResponse
      );
    });

    it('should handle network error', async () => {
      mockAdapter.onPatch('/user_exercise_preference/').networkError();

      await expect(updateUserExercisePreference('test-user-id', 'Deadlift', false)).rejects.toEqual(
        { error: 'Network Error' }
      );
    }, 10000);

    it('should handle timeout error', async () => {
      mockAdapter.onPatch('/user_exercise_preference/').timeout();

      await expect(updateUserExercisePreference('test-user-id', 'Deadlift', false)).rejects.toEqual(
        { error: 'timeout of 2500ms exceeded' }
      );
    });
  });

  describe('removeUserExercisePreference', () => {
    it('should remove user exercise preference successfully', async () => {
      mockAdapter.onDelete('/user_exercise_preference/').reply(200);

      await removeUserExercisePreference('test-user-id', 'Deadlift');

      expect(mockAdapter.history.delete[0].url).toBe('/user_exercise_preference/');
      expect(mockAdapter.history.delete[0].params).toEqual({
        user_id: 'test-user-id',
        exercise_name: 'Deadlift',
      });
    });

    it('should handle 404 error', async () => {
      const errorResponse = { error: 'Exercise preference not found' };
      mockAdapter.onDelete('/user_exercise_preference/').reply(404, errorResponse);

      await expect(removeUserExercisePreference('test-user-id', 'Deadlift')).rejects.toEqual(
        errorResponse
      );
    });

    it('should handle 401 error', async () => {
      const errorResponse = { error: 'Unauthorized' };
      mockAdapter.onDelete('/user_exercise_preference/').reply(401, errorResponse);

      await expect(removeUserExercisePreference('test-user-id', 'Deadlift')).rejects.toEqual(
        errorResponse
      );
    });

    it('should handle 500 error', async () => {
      const errorResponse = { error: 'Internal server error' };
      mockAdapter.onDelete('/user_exercise_preference/').reply(500, errorResponse);

      await expect(removeUserExercisePreference('test-user-id', 'Deadlift')).rejects.toEqual(
        errorResponse
      );
    });

    it('should handle network error', async () => {
      mockAdapter.onDelete('/user_exercise_preference/').networkError();

      await expect(removeUserExercisePreference('test-user-id', 'Deadlift')).rejects.toEqual({
        error: 'Network Error',
      });
    }, 10000);

    it('should handle timeout error', async () => {
      mockAdapter.onDelete('/user_exercise_preference/').timeout();

      await expect(removeUserExercisePreference('test-user-id', 'Deadlift')).rejects.toEqual({
        error: 'timeout of 2500ms exceeded',
      });
    });
  });
});
