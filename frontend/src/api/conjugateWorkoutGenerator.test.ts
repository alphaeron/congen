import MockAdapter from 'axios-mock-adapter';

import { generateNextWeek } from './conjugateWorkoutGenerator';
import { ENDPOINT } from './endpoint';
import type { Program } from './types';

const mock = new MockAdapter(ENDPOINT);

describe('conjugateWorkoutGenerator', () => {
  const mockProgram: Program = {
    id: 1,
    user_id: 'test-user-id',
    name: 'Test Program',
    current_week_number: 2,
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
    is_active: true,
  };

  beforeEach(() => {
    mock.reset();
  });

  afterAll(() => {
    mock.restore();
  });

  describe('generateNextWeek', () => {
    it('should generate next week of workouts successfully', async () => {
      mock.onPost('/conjugate_workout_generator/1').reply(200, mockProgram);
      const result = await generateNextWeek(1);
      expect(result).toEqual(mockProgram);
      expect(mock.history.post).toHaveLength(1);
      expect(mock.history.post[0].url).toBe('/conjugate_workout_generator/1');
    });

    it('should handle generation errors', async () => {
      const errorResponse = { message: 'Program not found' };
      mock.onPost('/conjugate_workout_generator/999').reply(404, errorResponse);
      await expect(generateNextWeek(999)).rejects.toEqual(errorResponse);
    });
  });
});
