import AxiosMockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import { getEquipment, getIndividualEquipment } from './equipment';
import type { Equipment } from './types';

describe('equipment API', () => {
  let mockAdapter: AxiosMockAdapter;

  beforeEach(() => {
    mockAdapter = new AxiosMockAdapter(ENDPOINT);
  });

  afterEach(() => {
    mockAdapter.restore();
  });

  describe('getEquipment', () => {
    it('should fetch all equipment successfully', async () => {
      const mockEquipment: Equipment[] = [
        {
          name: 'Barbell',
          description: 'A long metal bar used for weightlifting',
        },
        {
          name: 'Dumbbell',
          description: 'A handheld weight used for strength training',
        },
      ];

      mockAdapter.onGet('/equipment/').reply(200, mockEquipment);

      const result = await getEquipment();

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockEquipment);
    });

    it('should handle API errors', async () => {
      const errorData = { error: 'Internal Server Error' };
      mockAdapter.onGet('/equipment/').reply(500, errorData);

      await expect(getEquipment()).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle network errors', async () => {
      mockAdapter.onGet('/equipment/').networkError();

      await expect(getEquipment()).rejects.toEqual({ error: 'Network Error' });
      expect(mockAdapter.history.get.length).toBe(4); // 1 initial + 3 retries
    }, 10000);

    it('should handle timeout errors', async () => {
      mockAdapter.onGet('/equipment/').timeout();

      await expect(getEquipment()).rejects.toEqual({ error: 'timeout of 2500ms exceeded' });
      expect(mockAdapter.history.get.length).toBe(1);
    });
  });

  describe('getIndividualEquipment', () => {
    it('should fetch individual equipment successfully', async () => {
      const mockEquipment: Equipment = {
        name: 'Barbell',
        description: 'A long metal bar used for weightlifting',
      };

      mockAdapter.onGet('/equipment/Barbell').reply(200, mockEquipment);

      const result = await getIndividualEquipment('Barbell');

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockEquipment);
    });

    it('should handle equipment not found', async () => {
      const errorData = { error: 'Equipment not found' };
      mockAdapter.onGet('/equipment/Nonexistent').reply(404, errorData);

      await expect(getIndividualEquipment('Nonexistent')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle API errors', async () => {
      const errorData = { error: 'Internal Server Error' };
      mockAdapter.onGet('/equipment/Barbell').reply(500, errorData);

      await expect(getIndividualEquipment('Barbell')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle network errors', async () => {
      mockAdapter.onGet('/equipment/Barbell').networkError();

      await expect(getIndividualEquipment('Barbell')).rejects.toEqual({ error: 'Network Error' });
      expect(mockAdapter.history.get.length).toBe(4); // 1 initial + 3 retries
    }, 10000);

    it('should handle timeout errors', async () => {
      mockAdapter.onGet('/equipment/Barbell').timeout();

      await expect(getIndividualEquipment('Barbell')).rejects.toEqual({
        error: 'timeout of 2500ms exceeded',
      });
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle empty equipment name', async () => {
      const errorData = { error: 'Equipment not found' };
      mockAdapter.onGet('/equipment/').reply(404, errorData);

      await expect(getIndividualEquipment('')).rejects.toEqual(errorData);
      expect(mockAdapter.history.get.length).toBe(1);
    });

    it('should handle special characters in equipment name', async () => {
      const mockEquipment: Equipment = {
        name: 'Smith Machine',
        description: 'A weight machine with guided barbell',
      };

      // Handle URL encoding for spaces
      mockAdapter.onGet('/equipment/Smith%20Machine').reply(200, mockEquipment);

      const result = await getIndividualEquipment('Smith Machine');

      expect(mockAdapter.history.get.length).toBe(1);
      expect(result).toEqual(mockEquipment);
    });
  });
});
