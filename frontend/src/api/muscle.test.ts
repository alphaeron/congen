import AxiosMockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import { getMuscles, getIndividualMuscle } from './muscle';
import type { Muscle } from './types';

describe('muscle API', () => {
  let mockAdapter: AxiosMockAdapter;

  beforeEach(() => {
    mockAdapter = new AxiosMockAdapter(ENDPOINT);
  });

  afterEach(() => {
    mockAdapter.restore();
  });

  describe('getMuscles', () => {
    it('should fetch all muscles successfully', async () => {
      const mockMuscles: Muscle[] = [
        {
          name: 'Pectoralis Major',
          description: 'The large chest muscle responsible for arm adduction and flexion',
        },
        {
          name: 'Quadriceps',
          description: 'The group of four muscles on the front of the thigh',
        },
        {
          name: 'Biceps Brachii',
          description: 'The two-headed muscle on the front of the upper arm',
        },
      ];

      mockAdapter.onGet('/muscle/').reply(200, mockMuscles);

      const result = await getMuscles();

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockMuscles);
    });

    it('should handle API errors', async () => {
      const errorData = { error: 'Internal Server Error' };
      mockAdapter.onGet('/muscle/').reply(500, errorData);

      await expect(getMuscles()).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle network errors', async () => {
      mockAdapter.onGet('/muscle/').networkError();

      await expect(getMuscles()).rejects.toEqual({ error: 'Network Error' });
      expect(mockAdapter.history.get.length).toBe(4); // 1 initial + 3 retries
    }, 10000);

    it('should handle timeout errors', async () => {
      mockAdapter.onGet('/muscle/').timeout();

      await expect(getMuscles()).rejects.toEqual({ error: 'timeout of 10000ms exceeded' });
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle empty response', async () => {
      mockAdapter.onGet('/muscle/').reply(200, []);

      const result = await getMuscles();

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual([]);
    });
  });

  describe('getIndividualMuscle', () => {
    it('should fetch individual muscle successfully', async () => {
      const mockMuscle: Muscle = {
        name: 'Pectoralis Major',
        description: 'The large chest muscle responsible for arm adduction and flexion',
      };

      mockAdapter.onGet('/muscle/Pectoralis%20Major').reply(200, mockMuscle);

      const result = await getIndividualMuscle('Pectoralis Major');

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockMuscle);
    });

    it('should handle muscle not found', async () => {
      const errorData = { error: 'Muscle not found' };
      mockAdapter.onGet('/muscle/Nonexistent').reply(404, errorData);

      await expect(getIndividualMuscle('Nonexistent')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle API errors', async () => {
      const errorData = { error: 'Internal Server Error' };
      mockAdapter.onGet('/muscle/Pectoralis%20Major').reply(500, errorData);

      await expect(getIndividualMuscle('Pectoralis Major')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle network errors', async () => {
      mockAdapter.onGet('/muscle/Pectoralis%20Major').networkError();

      await expect(getIndividualMuscle('Pectoralis Major')).rejects.toEqual({
        error: 'Network Error',
      });
      expect(mockAdapter.history.get.length).toBe(4); // 1 initial + 3 retries
    }, 10000);

    it('should handle timeout errors', async () => {
      mockAdapter.onGet('/muscle/Pectoralis%20Major').timeout();

      await expect(getIndividualMuscle('Pectoralis Major')).rejects.toEqual({
        error: 'timeout of 10000ms exceeded',
      });
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle empty muscle name', async () => {
      const errorData = { error: 'Muscle not found' };
      mockAdapter.onGet('/muscle/').reply(404, errorData);

      await expect(getIndividualMuscle('')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle special characters in muscle name', async () => {
      const mockMuscle: Muscle = {
        name: 'Biceps Brachii',
        description: 'The two-headed muscle on the front of the upper arm',
      };

      mockAdapter.onGet('/muscle/Biceps%20Brachii').reply(200, mockMuscle);

      const result = await getIndividualMuscle('Biceps Brachii');

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockMuscle);
    });

    it('should handle muscle with complex name', async () => {
      const mockMuscle: Muscle = {
        name: 'Rectus Abdominis',
        description: 'The abdominal muscle commonly known as the six-pack',
      };

      mockAdapter.onGet('/muscle/Rectus%20Abdominis').reply(200, mockMuscle);

      const result = await getIndividualMuscle('Rectus Abdominis');

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockMuscle);
    });
  });
});
