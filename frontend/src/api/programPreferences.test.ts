import MockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import { getProgramPreferences, updateProgramPreferences } from './programPreferences';
import type { ProgramPreferences } from './types';

// Create axios mock adapter for the ENDPOINT instance
const mock = new MockAdapter(ENDPOINT);

const mockProgramPreferences: ProgramPreferences = {
  program_id: 1,
  program_days_per_week: 3,
  session_time_length_in_minutes: 60,
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
};

describe('programPreferences API', () => {
  beforeEach(() => {
    mock.reset();
  });

  afterAll(() => {
    mock.restore();
  });

  describe('getProgramPreferences', () => {
    it('should get program preferences successfully', async () => {
      mock.onGet('/program_preferences/1').reply(200, mockProgramPreferences);

      const result = await getProgramPreferences(1);

      expect(result.data).toEqual(mockProgramPreferences);
      expect(mock.history.get[0].url).toBe('/program_preferences/1');
    });

    it('should handle API errors', async () => {
      mock.onGet('/program_preferences/1').reply(404, { message: 'Not found' });

      await expect(getProgramPreferences(1)).rejects.toThrow();
    });
  });

  describe('updateProgramPreferences', () => {
    it('should update program preferences session time successfully', async () => {
      const updatedPreferences = { ...mockProgramPreferences, session_time_length_in_minutes: 90 };
      mock.onPatch('/program_preferences/').reply(200, updatedPreferences);

      const result = await updateProgramPreferences(1, 90);

      expect(result.data).toEqual(updatedPreferences);
      expect(mock.history.patch[0].params).toEqual({
        program_id: 1,
        session_time_length_in_minutes: 90,
      });
    });

    it('should handle API errors', async () => {
      mock.onPatch('/program_preferences/').reply(400, { message: 'Bad request' });

      await expect(updateProgramPreferences(1, 90)).rejects.toThrow();
    });
  });
});
