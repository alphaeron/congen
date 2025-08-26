import MockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import type { UserOneRepMax } from './types';
import {
  getUserOneRepMaxes,
  getUserOneRepMax,
  upsertUserOneRepMax,
  deleteUserOneRepMax,
} from './userOneRepMax';

const mock = new MockAdapter(ENDPOINT);

describe('UserOneRepMax API', () => {
  const mockUserId = 'test-user-id';

  beforeEach(() => {
    mock.reset();
  });

  const mockOneRepMax: UserOneRepMax = {
    user_id: mockUserId,
    exercise_name: 'Bench Press',
    one_rep_max: 225,
    unit: 'KG',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
  };

  describe('getUserOneRepMaxes', () => {
    it('should get all one rep maxes successfully', async () => {
      const oneRepMaxes = [mockOneRepMax];
      mock.onGet(`/user_one_rep_max/user/${mockUserId}`).reply(200, oneRepMaxes);

      const result = await getUserOneRepMaxes(mockUserId);

      expect(result).toEqual(oneRepMaxes);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe(`/user_one_rep_max/user/${mockUserId}`);
    });

    it('should get one rep maxes with unit parameter', async () => {
      const oneRepMaxes = [mockOneRepMax];
      mock
        .onGet(`/user_one_rep_max/user/${mockUserId}`, { params: { unit: 'KG' } })
        .reply(200, oneRepMaxes);

      const result = await getUserOneRepMaxes(mockUserId, 'KG');

      expect(result).toEqual(oneRepMaxes);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe(`/user_one_rep_max/user/${mockUserId}`);
    });

    it('should handle get one rep maxes errors', async () => {
      const errorResponse = { message: 'Internal server error' };
      mock.onGet(`/user_one_rep_max/user/${mockUserId}`).reply(500, errorResponse);

      await expect(getUserOneRepMaxes(mockUserId)).rejects.toEqual(errorResponse);
    });
  });

  describe('getUserOneRepMax', () => {
    it('should get a specific one rep max successfully', async () => {
      const exerciseName = 'Bench Press';
      mock
        .onGet(`/user_one_rep_max/user/${mockUserId}/exercise/${encodeURIComponent(exerciseName)}`)
        .reply(200, mockOneRepMax);

      const result = await getUserOneRepMax(mockUserId, exerciseName);

      expect(result).toEqual(mockOneRepMax);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe(
        `/user_one_rep_max/user/${mockUserId}/exercise/Bench%20Press`
      );
    });

    it('should get one rep max with unit parameter', async () => {
      const exerciseName = 'Bench Press';
      mock
        .onGet(
          `/user_one_rep_max/user/${mockUserId}/exercise/${encodeURIComponent(exerciseName)}`,
          {
            params: { unit: 'KG' },
          }
        )
        .reply(200, mockOneRepMax);

      const result = await getUserOneRepMax(mockUserId, exerciseName, 'KG');

      expect(result).toEqual(mockOneRepMax);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe(
        `/user_one_rep_max/user/${mockUserId}/exercise/Bench%20Press`
      );
    });

    it('should handle get one rep max errors', async () => {
      const exerciseName = 'Bench Press';
      const errorResponse = { message: 'Not found' };
      mock
        .onGet(`/user_one_rep_max/user/${mockUserId}/exercise/${encodeURIComponent(exerciseName)}`)
        .reply(404, errorResponse);

      await expect(getUserOneRepMax(mockUserId, exerciseName)).rejects.toEqual(errorResponse);
    });
  });

  describe('upsertUserOneRepMax', () => {
    it('should create or update a one rep max successfully', async () => {
      mock.onPut('/user_one_rep_max/').reply(200, mockOneRepMax);

      const result = await upsertUserOneRepMax(mockUserId, 'Bench Press', 225, 'KG');

      expect(result).toEqual(mockOneRepMax);
      expect(mock.history.put).toHaveLength(1);
      expect(mock.history.put[0].url).toBe('/user_one_rep_max/');
    });

    it('should handle upsert errors', async () => {
      const errorResponse = { message: 'Bad request' };
      mock.onPut('/user_one_rep_max/').reply(400, errorResponse);

      await expect(upsertUserOneRepMax(mockUserId, 'Bench Press', 225, 'KG')).rejects.toEqual(
        errorResponse
      );
    });
  });

  describe('deleteUserOneRepMax', () => {
    it('should delete a one rep max successfully', async () => {
      const exerciseName = 'Bench Press';
      mock
        .onDelete(
          `/user_one_rep_max/user/${mockUserId}/exercise/${encodeURIComponent(exerciseName)}`
        )
        .reply(200, mockOneRepMax);

      const result = await deleteUserOneRepMax(mockUserId, exerciseName);

      expect(result).toEqual(mockOneRepMax);
      expect(mock.history.delete).toHaveLength(1);
      expect(mock.history.delete[0].url).toBe(
        `/user_one_rep_max/user/${mockUserId}/exercise/Bench%20Press`
      );
    });

    it('should handle delete errors', async () => {
      const exerciseName = 'Bench Press';
      const errorResponse = { message: 'Not found' };
      mock
        .onDelete(
          `/user_one_rep_max/user/${mockUserId}/exercise/${encodeURIComponent(exerciseName)}`
        )
        .reply(404, errorResponse);

      await expect(deleteUserOneRepMax(mockUserId, exerciseName)).rejects.toEqual(errorResponse);
    });
  });
});
