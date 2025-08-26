import MockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import {
  createExerciseRotationHistory,
  getExerciseRotationHistory,
  getExerciseRotationHistoryByAccessory,
  getExerciseRotationHistoryById,
  updateExerciseRotationHistory,
  deleteExerciseRotationHistory,
} from './exerciseRotationHistory';
import type { ExerciseRotationHistory } from './types';

const mock = new MockAdapter(ENDPOINT);

describe('exerciseRotationHistory API', () => {
  beforeEach(() => {
    mock.reset();
  });

  describe('createExerciseRotationHistory', () => {
    it('should create a new exercise rotation history record', async () => {
      const mockResponse: ExerciseRotationHistory = {
        id: 1,
        user_id: 'user123',
        exercise_name: 'Bench Press',
        is_accessory: false,
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z',
      };

      mock.onPost('/exercise_rotation_history/').reply(200, mockResponse);

      const result = await createExerciseRotationHistory('Bench Press', false);

      expect(result).toEqual(mockResponse);
      expect(mock.history.post).toHaveLength(1);
      expect(mock.history.post[0].url).toBe('/exercise_rotation_history/');
      expect(mock.history.post[0].params).toEqual({
        user_id: 'current',
        exercise_name: 'Bench Press',
        is_accessory: false,
      });
    });
  });

  describe('getExerciseRotationHistory', () => {
    it('should get all exercise rotation history records for current user', async () => {
      const mockResponse: ExerciseRotationHistory[] = [
        {
          id: 1,
          user_id: 'user123',
          exercise_name: 'Bench Press',
          is_accessory: false,
          created_at: '2024-01-01T00:00:00Z',
          updated_at: '2024-01-01T00:00:00Z',
        },
        {
          id: 2,
          user_id: 'user123',
          exercise_name: 'Squat',
          is_accessory: false,
          created_at: '2024-01-02T00:00:00Z',
          updated_at: '2024-01-02T00:00:00Z',
        },
      ];

      mock.onGet('/exercise_rotation_history/').reply(200, mockResponse);

      const result = await getExerciseRotationHistory();

      expect(result).toEqual(mockResponse);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/exercise_rotation_history/');
    });
  });

  describe('getExerciseRotationHistoryByAccessory', () => {
    it('should get exercise rotation history records filtered by accessory type', async () => {
      const mockResponse: ExerciseRotationHistory[] = [
        {
          id: 1,
          user_id: 'user123',
          exercise_name: 'Lateral Raises',
          is_accessory: true,
          created_at: '2024-01-01T00:00:00Z',
          updated_at: '2024-01-01T00:00:00Z',
        },
      ];

      mock.onGet('/exercise_rotation_history/is_accessory/true').reply(200, mockResponse);

      const result = await getExerciseRotationHistoryByAccessory(true);

      expect(result).toEqual(mockResponse);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/exercise_rotation_history/is_accessory/true');
    });
  });

  describe('getExerciseRotationHistoryById', () => {
    it('should get a specific exercise rotation history record by ID', async () => {
      const mockResponse: ExerciseRotationHistory = {
        id: 1,
        user_id: 'user123',
        exercise_name: 'Bench Press',
        is_accessory: false,
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z',
      };

      mock.onGet('/exercise_rotation_history/1').reply(200, mockResponse);

      const result = await getExerciseRotationHistoryById(1);

      expect(result).toEqual(mockResponse);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/exercise_rotation_history/1');
    });
  });

  describe('updateExerciseRotationHistory', () => {
    it('should update an existing exercise rotation history record', async () => {
      const mockResponse: ExerciseRotationHistory = {
        id: 1,
        user_id: 'user123',
        exercise_name: 'Incline Bench Press',
        is_accessory: true,
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z',
      };

      mock.onPatch('/exercise_rotation_history/1').reply(200, mockResponse);

      const result = await updateExerciseRotationHistory(1, 'Incline Bench Press', true);

      expect(result).toEqual(mockResponse);
      expect(mock.history.patch).toHaveLength(1);
      expect(mock.history.patch[0].url).toBe('/exercise_rotation_history/1');
      expect(mock.history.patch[0].params).toEqual({
        user_id: 'current',
        exercise_name: 'Incline Bench Press',
        is_accessory: true,
      });
    });
  });

  describe('deleteExerciseRotationHistory', () => {
    it('should delete an exercise rotation history record', async () => {
      const mockResponse: ExerciseRotationHistory = {
        id: 1,
        user_id: 'user123',
        exercise_name: 'Bench Press',
        is_accessory: false,
        created_at: '2024-01-01T00:00:00Z',
        updated_at: '2024-01-01T00:00:00Z',
      };

      mock.onDelete('/exercise_rotation_history/1').reply(200, mockResponse);

      const result = await deleteExerciseRotationHistory(1);

      expect(result).toEqual(mockResponse);
      expect(mock.history.delete).toHaveLength(1);
      expect(mock.history.delete[0].url).toBe('/exercise_rotation_history/1');
    });
  });
});
