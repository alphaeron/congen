import MockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import { getProgramPreferences, updateProgramPreferences } from './programPreferences';
import type { ProgramPreferences } from './types';

const mock = new MockAdapter(ENDPOINT);

describe('programPreferences API', () => {
  beforeEach(() => {
    mock.reset();
  });

  const mockProgramPreferences: ProgramPreferences = {
    program_id: 1,
    program_days_per_week: 3,
    session_time_length_in_minutes: 60,
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
  };

  describe('getProgramPreferences', () => {
    it('should get program preferences successfully', async () => {
      mock.onGet('/program_preferences/1').reply(200, mockProgramPreferences);

      const result = await getProgramPreferences(1);

      expect(result).toEqual(mockProgramPreferences);
      expect(mock.history.get[0].url).toBe('/program_preferences/1');
    });

    it('should handle API errors', async () => {
      mock.onGet('/program_preferences/1').reply(404, { message: 'Not found' });

      await expect(getProgramPreferences(1)).rejects.toEqual({ message: 'Not found' });
    });
  });

  describe('updateProgramPreferences', () => {
    it('should update program preferences session time successfully', async () => {
      const updatedPreferences = { ...mockProgramPreferences, session_time_length_in_minutes: 90 };
      mock.onPatch('/program_preferences/').reply(200, updatedPreferences);

      const result = await updateProgramPreferences(1, 90);

      expect(result).toEqual(updatedPreferences);
      expect(mock.history.patch[0].params).toEqual({
        program_id: 1,
        session_time_length_in_minutes: 90,
      });
    });

    it('should handle API errors', async () => {
      mock.onPatch('/program_preferences/').reply(400, { message: 'Bad request' });

      await expect(updateProgramPreferences(1, 90)).rejects.toEqual({ message: 'Bad request' });
    });
  });
});
