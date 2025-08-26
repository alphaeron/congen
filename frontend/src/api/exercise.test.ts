import AxiosMockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import {
  getExercises,
  getIndividualExercise,
  getExerciseMuscles,
  getExerciseEquipment,
} from './exercise';
import type { Exercise, ExerciseMuscle, ExerciseEquipment } from './types';

const mockAdapter = new AxiosMockAdapter(ENDPOINT);

describe('exercise API', () => {
  beforeEach(() => {
    mockAdapter.reset();
  });

  describe('getExercises', () => {
    it('should fetch all exercises successfully', async () => {
      const mockExercises: Exercise[] = [
        {
          name: 'Bench Press',
          description: 'A compound exercise for chest development',
          movement_type: 'push',
          is_unilateral: false,
          is_upper: true,
          is_accessory: false,
        },
        {
          name: 'Squat',
          description: 'A compound exercise for leg development',
          movement_type: 'push',
          is_unilateral: false,
          is_upper: false,
          is_accessory: false,
        },
      ];

      mockAdapter.onGet('/exercise/').reply(200, mockExercises);

      const result = await getExercises();

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockExercises);
    });

    it('should handle API errors', async () => {
      const errorData = { error: 'Internal Server Error' };
      mockAdapter.onGet('/exercise/').reply(500, errorData);

      await expect(getExercises()).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle network errors', async () => {
      mockAdapter.onGet('/exercise/').networkError();

      await expect(getExercises()).rejects.toEqual({ error: 'Network Error' });
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle timeout errors', async () => {
      mockAdapter.onGet('/exercise/').timeout();

      await expect(getExercises()).rejects.toEqual({ error: 'timeout of 2500ms exceeded' });
      expect(mockAdapter.history.get.length).toBe(1);
    });
  });

  describe('getIndividualExercise', () => {
    it('should fetch individual exercise successfully', async () => {
      const mockExercise: Exercise = {
        name: 'Bench Press',
        description: 'A compound exercise for chest development',
        movement_type: 'push',
        is_unilateral: false,
        is_upper: true,
        is_accessory: false,
      };

      mockAdapter.onGet('/exercise/Bench Press').reply(200, mockExercise);

      const result = await getIndividualExercise('Bench Press');

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockExercise);
    });

    it('should handle exercise not found', async () => {
      const errorData = { error: 'Exercise not found' };
      mockAdapter.onGet('/exercise/Nonexistent').reply(404, errorData);

      await expect(getIndividualExercise('Nonexistent')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle API errors', async () => {
      const errorData = { error: 'Internal Server Error' };
      mockAdapter.onGet('/exercise/Bench Press').reply(500, errorData);

      await expect(getIndividualExercise('Bench Press')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle network errors', async () => {
      mockAdapter.onGet('/exercise/Bench Press').networkError();

      await expect(getIndividualExercise('Bench Press')).rejects.toEqual({ error: 'Network Error' });
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle timeout errors', async () => {
      mockAdapter.onGet('/exercise/Bench Press').timeout();

      await expect(getIndividualExercise('Bench Press')).rejects.toEqual({ error: 'timeout of 2500ms exceeded' });
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle empty exercise name', async () => {
      const errorData = { error: 'Exercise not found' };
      mockAdapter.onGet('/exercise/').reply(404, errorData);

      await expect(getIndividualExercise('')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });
  });

  describe('getExerciseMuscles', () => {
    it('should fetch exercise muscles successfully', async () => {
      const mockExerciseMuscles: ExerciseMuscle[] = [
        {
          exercise_name: 'Bench Press',
          muscle_name: 'Pectoralis Major',
        },
        {
          exercise_name: 'Bench Press',
          muscle_name: 'Triceps Brachii',
        },
      ];

      mockAdapter.onGet('/exercise/Bench Press/muscle').reply(200, mockExerciseMuscles);

      const result = await getExerciseMuscles('Bench Press');

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockExerciseMuscles);
    });

    it('should handle exercise not found', async () => {
      const errorData = { error: 'Exercise not found' };
      mockAdapter.onGet('/exercise/Nonexistent/muscle').reply(404, errorData);

      await expect(getExerciseMuscles('Nonexistent')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle API errors', async () => {
      const errorData = { error: 'Internal Server Error' };
      mockAdapter.onGet('/exercise/Bench Press/muscle').reply(500, errorData);

      await expect(getExerciseMuscles('Bench Press')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle network errors', async () => {
      mockAdapter.onGet('/exercise/Bench Press/muscle').networkError();

      await expect(getExerciseMuscles('Bench Press')).rejects.toEqual({ error: 'Network Error' });
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle timeout errors', async () => {
      mockAdapter.onGet('/exercise/Bench Press/muscle').timeout();

      await expect(getExerciseMuscles('Bench Press')).rejects.toEqual({ error: 'timeout of 2500ms exceeded' });
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle empty exercise name', async () => {
      const errorData = { error: 'Exercise not found' };
      mockAdapter.onGet('/exercise//muscle').reply(404, errorData);

      await expect(getExerciseMuscles('')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });
  });

  describe('getExerciseEquipment', () => {
    it('should fetch exercise equipment successfully', async () => {
      const mockExerciseEquipment: ExerciseEquipment[] = [
        {
          exercise_name: 'Bench Press',
          equipment_name: 'Barbell',
        },
        {
          exercise_name: 'Bench Press',
          equipment_name: 'Bench',
        },
      ];

      mockAdapter.onGet('/exercise/Bench Press/equipment').reply(200, mockExerciseEquipment);

      const result = await getExerciseEquipment('Bench Press');

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockExerciseEquipment);
    });

    it('should handle exercise not found', async () => {
      const errorData = { error: 'Exercise not found' };
      mockAdapter.onGet('/exercise/Nonexistent/equipment').reply(404, errorData);

      await expect(getExerciseEquipment('Nonexistent')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle API errors', async () => {
      const errorData = { error: 'Internal Server Error' };
      mockAdapter.onGet('/exercise/Bench Press/equipment').reply(500, errorData);

      await expect(getExerciseEquipment('Bench Press')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle network errors', async () => {
      mockAdapter.onGet('/exercise/Bench Press/equipment').networkError();

      await expect(getExerciseEquipment('Bench Press')).rejects.toEqual({ error: 'Network Error' });
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle timeout errors', async () => {
      mockAdapter.onGet('/exercise/Bench Press/equipment').timeout();

      await expect(getExerciseEquipment('Bench Press')).rejects.toEqual({ error: 'timeout of 2500ms exceeded' });
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle empty exercise name', async () => {
      const errorData = { error: 'Exercise not found' };
      mockAdapter.onGet('/exercise//equipment').reply(404, errorData);

      await expect(getExerciseEquipment('')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });
  });
});
