import AxiosMockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import { getExerciseMuscle } from './exerciseMuscle';
import type { ExerciseMuscle } from './types';

const mockAdapter = new AxiosMockAdapter(ENDPOINT);

describe('exerciseMuscle API', () => {
  beforeEach(() => {
    mockAdapter.reset();
  });

  describe('getExerciseMuscle', () => {
    it('should fetch all exercise muscle relationships successfully', async () => {
      const mockExerciseMuscles: ExerciseMuscle[] = [
        {
          exercise_name: 'Bench Press',
          muscle_name: 'Pectoralis Major',
        },
        {
          exercise_name: 'Bench Press',
          muscle_name: 'Triceps Brachii',
        },
        {
          exercise_name: 'Squat',
          muscle_name: 'Quadriceps',
        },
        {
          exercise_name: 'Squat',
          muscle_name: 'Gluteus Maximus',
        },
        {
          exercise_name: 'Dumbbell Curl',
          muscle_name: 'Biceps Brachii',
        },
      ];

      mockAdapter.onGet('/exercise_muscle/').reply(200, mockExerciseMuscles);

      const result = await getExerciseMuscle();

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockExerciseMuscles);
    });

    it('should handle empty response', async () => {
      mockAdapter.onGet('/exercise_muscle/').reply(200, []);

      const result = await getExerciseMuscle();

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual([]);
    });

    it('should handle API errors', async () => {
      const errorData = { error: 'Internal Server Error' };
      mockAdapter.onGet('/exercise_muscle/').reply(500, errorData);

      await expect(getExerciseMuscle()).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle network errors', async () => {
      mockAdapter.onGet('/exercise_muscle/').networkError();

      await expect(getExerciseMuscle()).rejects.toEqual({ error: 'Network Error' });
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle timeout errors', async () => {
      mockAdapter.onGet('/exercise_muscle/').timeout();

      await expect(getExerciseMuscle()).rejects.toEqual({ error: 'timeout of 2500ms exceeded' });
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle 404 errors', async () => {
      const errorData = { error: 'Not found' };
      mockAdapter.onGet('/exercise_muscle/').reply(404, errorData);

      await expect(getExerciseMuscle()).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle 403 errors', async () => {
      const errorData = { error: 'Forbidden' };
      mockAdapter.onGet('/exercise_muscle/').reply(403, errorData);

      await expect(getExerciseMuscle()).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle malformed JSON response', async () => {
      const malformedResponse = 'invalid json';
      mockAdapter.onGet('/exercise_muscle/').reply(200, malformedResponse);

      const result = await getExerciseMuscle();
      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toBe(malformedResponse);
    });

    it('should handle single exercise muscle relationship', async () => {
      const mockExerciseMuscle: ExerciseMuscle[] = [
        {
          exercise_name: 'Bench Press',
          muscle_name: 'Pectoralis Major',
        },
      ];

      mockAdapter.onGet('/exercise_muscle/').reply(200, mockExerciseMuscle);

      const result = await getExerciseMuscle();

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockExerciseMuscle);
      expect(result).toHaveLength(1);
    });
  });
});
