import MockAdapter from 'axios-mock-adapter';

import { expectRequestError } from './apiRequestErrorTestUtils';
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
    is_amrap: false,
    is_emom: false,
    use_tempo: false,
    target_weight: 135,
    performed_weight: 135,
    target_rep_count: 8,
    performed_rep_count: 8,
    rest_seconds: 90,
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
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

      await expectRequestError(getSetSchemesByExercise(1), errorResponse);
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
        { ...mockSetScheme, id: 2, set_number: 2, target_rep_count: 6, target_weight: 145 },
        { ...mockSetScheme, id: 3, set_number: 3, target_rep_count: 4, target_weight: 155 },
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

      await expectRequestError(getSetScheme(1), errorResponse);
    });

    it('should handle set scheme without target rep count', async () => {
      const setSchemeWithoutReps = { ...mockSetScheme, target_rep_count: undefined };
      mock.onGet('/set_scheme/1').reply(200, setSchemeWithoutReps);

      const result = await getSetScheme(1);

      expect(result).toEqual(setSchemeWithoutReps);
      expect(result.target_rep_count).toBeUndefined();
    });

    it('should handle set scheme without target weight', async () => {
      const setSchemeWithoutWeight = { ...mockSetScheme, target_weight: undefined };
      mock.onGet('/set_scheme/1').reply(200, setSchemeWithoutWeight);

      const result = await getSetScheme(1);

      expect(result).toEqual(setSchemeWithoutWeight);
      expect(result.target_weight).toBeUndefined();
    });

    it('should handle set scheme with zero values', async () => {
      const setSchemeWithZeros = {
        ...mockSetScheme,
        target_weight: 0,
        rest_seconds: 0,
        target_rep_count: 0,
      };
      mock.onGet('/set_scheme/1').reply(200, setSchemeWithZeros);

      const result = await getSetScheme(1);

      expect(result).toEqual(setSchemeWithZeros);
      expect(result.target_weight).toBe(0);
      expect(result.rest_seconds).toBe(0);
      expect(result.target_rep_count).toBe(0);
    });
  });
});
