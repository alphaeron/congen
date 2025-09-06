import MockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import { WeightUnit } from './types';
import {
  upsertUserWeightUnitPreference,
  getUserWeightUnitPreferences,
  getUserWeightUnitPreference,
  deleteUserWeightUnitPreference,
} from './userWeightUnitPreference';

// Create axios mock adapter for the ENDPOINT instance
const mock = new MockAdapter(ENDPOINT);

const mockUserWeightUnitPreference = {
  user_id: 'test-user-id',
  exercise_name: 'Bench Press',
  preferred_unit: WeightUnit.LBS,
  created_at: new Date('2024-01-01T00:00:00.000Z'),
  updated_at: new Date('2024-01-01T00:00:00.000Z'),
};

describe('userWeightUnitPreference API', () => {
  beforeEach(() => {
    mock.reset();
  });

  afterAll(() => {
    mock.restore();
  });

  describe('upsertUserWeightUnitPreference', () => {
    it('should create or update user weight unit preference successfully', async () => {
      mock.onPut('/user_weight_unit_preference/').reply(200, mockUserWeightUnitPreference);

      const result = await upsertUserWeightUnitPreference(
        'test-user-id',
        'Bench Press',
        WeightUnit.LBS
      );

      expect(result).toEqual(mockUserWeightUnitPreference);
      expect(mock.history.put[0].params).toEqual({
        user_id: 'test-user-id',
        exercise_name: 'Bench Press',
        preferred_unit: WeightUnit.LBS,
      });
    });

    it('should handle API errors', async () => {
      mock.onPut('/user_weight_unit_preference/').reply(400, { message: 'Bad request' });

      await expect(
        upsertUserWeightUnitPreference('test-user-id', 'Bench Press', WeightUnit.LBS)
      ).rejects.toEqual({ message: 'Bad request' });
    });
  });

  describe('getUserWeightUnitPreferences', () => {
    it('should get all user weight unit preferences successfully', async () => {
      const mockPreferences = [mockUserWeightUnitPreference];
      mock.onGet('/user_weight_unit_preference/test-user-id').reply(200, mockPreferences);

      const result = await getUserWeightUnitPreferences('test-user-id');

      expect(result).toEqual(mockPreferences);
      expect(mock.history.get[0].url).toBe('/user_weight_unit_preference/test-user-id');
    });

    it('should handle API errors', async () => {
      mock.onGet('/user_weight_unit_preference/test-user-id').reply(404, { message: 'Not found' });

      await expect(getUserWeightUnitPreferences('test-user-id')).rejects.toEqual({
        message: 'Not found',
      });
    });
  });

  describe('getUserWeightUnitPreference', () => {
    it('should get specific user weight unit preference successfully', async () => {
      mock
        .onGet('/user_weight_unit_preference/test-user-id/Bench%20Press')
        .reply(200, mockUserWeightUnitPreference);

      const result = await getUserWeightUnitPreference('test-user-id', 'Bench Press');

      expect(result).toEqual(mockUserWeightUnitPreference);
      expect(mock.history.get[0].url).toBe(
        '/user_weight_unit_preference/test-user-id/Bench%20Press'
      );
    });

    it('should handle API errors', async () => {
      mock
        .onGet('/user_weight_unit_preference/test-user-id/Bench%20Press')
        .reply(404, { message: 'Not found' });

      await expect(getUserWeightUnitPreference('test-user-id', 'Bench Press')).rejects.toEqual({
        message: 'Not found',
      });
    });
  });

  describe('deleteUserWeightUnitPreference', () => {
    it('should delete user weight unit preference successfully', async () => {
      mock
        .onDelete('/user_weight_unit_preference/test-user-id/Bench%20Press')
        .reply(200, mockUserWeightUnitPreference);

      const result = await deleteUserWeightUnitPreference('test-user-id', 'Bench Press');

      expect(result).toEqual(mockUserWeightUnitPreference);
      expect(mock.history.delete[0].url).toBe(
        '/user_weight_unit_preference/test-user-id/Bench%20Press'
      );
    });

    it('should handle API errors', async () => {
      mock
        .onDelete('/user_weight_unit_preference/test-user-id/Bench%20Press')
        .reply(404, { message: 'Not found' });

      await expect(deleteUserWeightUnitPreference('test-user-id', 'Bench Press')).rejects.toEqual({
        message: 'Not found',
      });
    });
  });

  describe('WeightUnit enum', () => {
    it('should have correct values', () => {
      expect(WeightUnit.KG).toBe('KG');
      expect(WeightUnit.LBS).toBe('LBS');
    });
  });
});
