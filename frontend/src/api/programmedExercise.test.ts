import MockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import { getProgrammedExercisesByStage, getProgrammedExercise } from './programmedExercise';
import type { ProgrammedExercise } from './types';

const mock = new MockAdapter(ENDPOINT);

describe('ProgrammedExercise API', () => {
  beforeEach(() => {
    mock.reset();
  });

  const mockProgrammedExercise: ProgrammedExercise = {
    id: 1,
    workout_stage_id: 1,
    exercise_name: 'Bench Press',
    position: 1,
    notes: 'Focus on form',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
  };

  describe('getProgrammedExercisesByStage', () => {
    it('should get all programmed exercises for a stage successfully', async () => {
      const exercises = [mockProgrammedExercise];
      mock.onGet('/programmed_exercise/stage/1').reply(200, exercises);

      const result = await getProgrammedExercisesByStage(1);

      expect(result).toEqual(exercises);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/programmed_exercise/stage/1');
    });

    it('should handle get exercises by stage errors', async () => {
      const errorResponse = { message: 'Stage not found' };
      mock.onGet('/programmed_exercise/stage/1').reply(404, errorResponse);

      await expect(getProgrammedExercisesByStage(1)).rejects.toEqual(errorResponse);
    });

    it('should handle empty response when no exercises exist', async () => {
      mock.onGet('/programmed_exercise/stage/1').reply(200, []);

      const result = await getProgrammedExercisesByStage(1);

      expect(result).toEqual([]);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/programmed_exercise/stage/1');
    });
  });

  describe('getProgrammedExercise', () => {
    it('should get a specific programmed exercise successfully', async () => {
      mock.onGet('/programmed_exercise/1').reply(200, mockProgrammedExercise);

      const result = await getProgrammedExercise(1);

      expect(result).toEqual(mockProgrammedExercise);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/programmed_exercise/1');
    });

    it('should handle get exercise errors', async () => {
      const errorResponse = { message: 'Exercise not found' };
      mock.onGet('/programmed_exercise/1').reply(404, errorResponse);

      await expect(getProgrammedExercise(1)).rejects.toEqual(errorResponse);
    });

    it('should handle exercise without notes', async () => {
      const exerciseWithoutNotes = { ...mockProgrammedExercise, notes: undefined };
      mock.onGet('/programmed_exercise/1').reply(200, exerciseWithoutNotes);

      const result = await getProgrammedExercise(1);

      expect(result).toEqual(exerciseWithoutNotes);
      expect(result.notes).toBeUndefined();
    });
  });
});
