import AxiosMockAdapter from 'axios-mock-adapter';

import { expectRequestError } from './apiRequestErrorTestUtils';
import { ENDPOINT } from './endpoint';
import { getExerciseEquipment } from './exerciseEquipment';
import type { ExerciseEquipment } from './types';

const mockAdapter = new AxiosMockAdapter(ENDPOINT);

describe('exerciseEquipment API', () => {
  beforeEach(() => {
    mockAdapter.reset();
  });

  describe('getExerciseEquipment', () => {
    it('should fetch all exercise equipment relationships successfully', async () => {
      const mockExerciseEquipment: ExerciseEquipment[] = [
        {
          exercise_name: 'Bench Press',
          equipment_name: 'Barbell',
        },
        {
          exercise_name: 'Bench Press',
          equipment_name: 'Bench',
        },
        {
          exercise_name: 'Squat',
          equipment_name: 'Barbell',
        },
        {
          exercise_name: 'Dumbbell Curl',
          equipment_name: 'Dumbbell',
        },
      ];

      mockAdapter.onGet('/exercise_equipment/').reply(200, mockExerciseEquipment);

      const result = await getExerciseEquipment();

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockExerciseEquipment);
    });

    it('should handle empty response', async () => {
      mockAdapter.onGet('/exercise_equipment/').reply(200, []);

      const result = await getExerciseEquipment();

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual([]);
    });

    it('should handle API errors', async () => {
      const errorData = { error: 'Internal Server Error' };
      mockAdapter.onGet('/exercise_equipment/').reply(500, errorData);

      await expectRequestError(getExerciseEquipment(), errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle timeout errors', async () => {
      mockAdapter.onGet('/exercise_equipment/').timeout();

      await expect(getExerciseEquipment()).rejects.toThrow('timeout of 10000ms exceeded');
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle 404 errors', async () => {
      const errorData = { error: 'Not found' };
      mockAdapter.onGet('/exercise_equipment/').reply(404, errorData);

      await expectRequestError(getExerciseEquipment(), errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle 403 errors', async () => {
      const errorData = { error: 'Forbidden' };
      mockAdapter.onGet('/exercise_equipment/').reply(403, errorData);

      await expectRequestError(getExerciseEquipment(), errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle malformed JSON response', async () => {
      const malformedResponse = 'invalid json';
      mockAdapter.onGet('/exercise_equipment/').reply(200, malformedResponse);

      const result = await getExerciseEquipment();
      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toBe(malformedResponse);
    });
  });
});
