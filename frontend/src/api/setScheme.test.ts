import MockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import { getSetSchemesByExercise, getSetScheme } from './setScheme';
import type { SetScheme } from './types';

const mock = new MockAdapter(ENDPOINT);

describe('SetScheme API', () => {
  beforeEach(() => {
    mock.reset();
  });

  const mockSetScheme: SetScheme = {
    id: 1,
    programmed_exercise_id: 1,
    set_number: 1,
    reps: 8,
    weight: 135,
    rest_seconds: 90,
    rpe: 8,
    notes: 'Focus on form',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
  };

  describe('getSetSchemesByExercise', () => {
    it('should get all set schemes for an exercise successfully', async () => {
      const setSchemes = [mockSetScheme];
      mock.onGet('/set_scheme/exercise/1').reply(200, setSchemes);

      const result = await getSetSchemesByExercise(1);

      expect(result).toEqual(setSchemes);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/set_scheme/exercise/1');
    });

    it('should handle get set schemes by exercise errors', async () => {
      const errorResponse = { message: 'Exercise not found' };
      mock.onGet('/set_scheme/exercise/1').reply(404, errorResponse);

      await expect(getSetSchemesByExercise(1)).rejects.toEqual(errorResponse);
    });

    it('should handle empty response when no set schemes exist', async () => {
      mock.onGet('/set_scheme/exercise/1').reply(200, []);

      const result = await getSetSchemesByExercise(1);

      expect(result).toEqual([]);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/set_scheme/exercise/1');
    });

    it('should handle multiple set schemes for an exercise', async () => {
      const setSchemes = [
        mockSetScheme,
        { ...mockSetScheme, id: 2, set_number: 2, reps: 6, weight: 145 },
        { ...mockSetScheme, id: 3, set_number: 3, reps: 4, weight: 155 },
      ];
      mock.onGet('/set_scheme/exercise/1').reply(200, setSchemes);

      const result = await getSetSchemesByExercise(1);

      expect(result).toEqual(setSchemes);
      expect(result).toHaveLength(3);
      expect(result[0].set_number).toBe(1);
      expect(result[1].set_number).toBe(2);
      expect(result[2].set_number).toBe(3);
    });
  });

  describe('getSetScheme', () => {
    it('should get a specific set scheme successfully', async () => {
      mock.onGet('/set_scheme/1').reply(200, mockSetScheme);

      const result = await getSetScheme(1);

      expect(result).toEqual(mockSetScheme);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/set_scheme/1');
    });

    it('should handle get set scheme errors', async () => {
      const errorResponse = { message: 'Set scheme not found' };
      mock.onGet('/set_scheme/1').reply(404, errorResponse);

      await expect(getSetScheme(1)).rejects.toEqual(errorResponse);
    });

    it('should handle set scheme without notes', async () => {
      const setSchemeWithoutNotes = { ...mockSetScheme, notes: undefined };
      mock.onGet('/set_scheme/1').reply(200, setSchemeWithoutNotes);

      const result = await getSetScheme(1);

      expect(result).toEqual(setSchemeWithoutNotes);
      expect(result.notes).toBeUndefined();
    });

    it('should handle set scheme without RPE', async () => {
      const setSchemeWithoutRPE = { ...mockSetScheme, rpe: undefined };
      mock.onGet('/set_scheme/1').reply(200, setSchemeWithoutRPE);

      const result = await getSetScheme(1);

      expect(result).toEqual(setSchemeWithoutRPE);
      expect(result.rpe).toBeUndefined();
    });

    it('should handle set scheme with zero values', async () => {
      const setSchemeWithZeros = {
        ...mockSetScheme,
        weight: 0,
        rest_seconds: 0,
        rpe: 0,
      };
      mock.onGet('/set_scheme/1').reply(200, setSchemeWithZeros);

      const result = await getSetScheme(1);

      expect(result).toEqual(setSchemeWithZeros);
      expect(result.weight).toBe(0);
      expect(result.rest_seconds).toBe(0);
      expect(result.rpe).toBe(0);
    });
  });
});
