import MockAdapter from 'axios-mock-adapter';

import { ENDPOINT } from './endpoint';
import {
  createUserProgramPreferences,
  getUserProgramPreferences,
  updateUserProgramPreferences,
  deleteUserProgramPreferences,
  type UserProgramPreferences,
} from './userProgramPreferences';

// Create axios mock adapter for the ENDPOINT instance
const mock = new MockAdapter(ENDPOINT);

const mockUserProgramPreferences: UserProgramPreferences = {
  user_id: 'test-user-id',
  program_days_per_week: 3,
  session_time_length_in_minutes: 60,
  created_at: '2024-01-01T00:00:00Z',
  updated_at: '2024-01-01T00:00:00Z',
};

describe('userProgramPreferences API', () => {
  beforeEach(() => {
    mock.reset();
  });

  afterAll(() => {
    mock.restore();
  });

  describe('createUserProgramPreferences', () => {
    it('should create user program preferences successfully', async () => {
      mock.onPost('/user_program_preferences/').reply(200, mockUserProgramPreferences);

      const result = await createUserProgramPreferences('test-user-id', 3, 60);

      expect(result.data).toEqual(mockUserProgramPreferences);
      expect(mock.history.post[0].params).toEqual({
        user_id: 'test-user-id',
        program_days_per_week: 3,
        session_time_length_in_minutes: 60,
      });
    });

    it('should handle API errors', async () => {
      mock.onPost('/user_program_preferences/').reply(400, { message: 'Bad request' });

      await expect(createUserProgramPreferences('test-user-id', 3, 60)).rejects.toThrow();
    });
  });

  describe('getUserProgramPreferences', () => {
    it('should get user program preferences successfully', async () => {
      mock.onGet('/user_program_preferences/test-user-id').reply(200, mockUserProgramPreferences);

      const result = await getUserProgramPreferences('test-user-id');

      expect(result.data).toEqual(mockUserProgramPreferences);
      expect(mock.history.get[0].url).toBe('/user_program_preferences/test-user-id');
    });

    it('should handle API errors', async () => {
      mock.onGet('/user_program_preferences/test-user-id').reply(404, { message: 'Not found' });

      await expect(getUserProgramPreferences('test-user-id')).rejects.toThrow();
    });
  });

  describe('updateUserProgramPreferences', () => {
    it('should update user program preferences successfully', async () => {
      const updatedPreferences = { ...mockUserProgramPreferences, program_days_per_week: 4 };
      mock.onPatch('/user_program_preferences/').reply(200, updatedPreferences);

      const result = await updateUserProgramPreferences('test-user-id', 4, 60);

      expect(result.data).toEqual(updatedPreferences);
      expect(mock.history.patch[0].params).toEqual({
        user_id: 'test-user-id',
        program_days_per_week: 4,
        session_time_length_in_minutes: 60,
      });
    });

    it('should handle API errors', async () => {
      mock.onPatch('/user_program_preferences/').reply(400, { message: 'Bad request' });

      await expect(updateUserProgramPreferences('test-user-id', 4, 60)).rejects.toThrow();
    });
  });

  describe('deleteUserProgramPreferences', () => {
    it('should delete user program preferences successfully', async () => {
      mock
        .onDelete('/user_program_preferences/test-user-id')
        .reply(200, mockUserProgramPreferences);

      const result = await deleteUserProgramPreferences('test-user-id');

      expect(result.data).toEqual(mockUserProgramPreferences);
      expect(mock.history.delete[0].url).toBe('/user_program_preferences/test-user-id');
    });

    it('should handle API errors', async () => {
      mock.onDelete('/user_program_preferences/test-user-id').reply(404, { message: 'Not found' });

      await expect(deleteUserProgramPreferences('test-user-id')).rejects.toThrow();
    });
  });
});
