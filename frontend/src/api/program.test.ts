import MockAdapter from 'axios-mock-adapter';
import { ENDPOINT } from './endpoint';
import { createProgram, getPrograms, getProgram, updateProgram, deleteProgram } from './program';
import type { Program } from './types';

const mock = new MockAdapter(ENDPOINT);

describe('Program API', () => {
  beforeEach(() => {
    mock.reset();
  });

  const mockProgram: Program = {
    id: 1,
    user_id: 'test-user-id',
    name: 'Test Program',
    current_week_number: 1,
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
    is_active: true,
  };

  describe('createProgram', () => {
    it('should create a program successfully', async () => {
      mock.onPost('/program/').reply(200, mockProgram);

      const result = await createProgram('Test Program', true, 'test-user-id');

      expect(result).toEqual(mockProgram);
      expect(mock.history.post).toHaveLength(1);
      expect(mock.history.post[0].url).toBe('/program/');
      expect(mock.history.post[0].params).toEqual({
        user_id: 'test-user-id',
        name: 'Test Program',
        is_active: true,
      });
    });

    it('should handle creation errors', async () => {
      const errorResponse = { message: 'Bad request' };
      mock.onPost('/program/').reply(400, errorResponse);

      await expect(createProgram('Test Program', true, 'test-user-id')).rejects.toEqual(errorResponse);
    });
  });

  describe('getPrograms', () => {
    it('should get all programs successfully', async () => {
      const programs = [mockProgram];
      mock.onGet('/program/').reply(200, programs);

      const result = await getPrograms();

      expect(result).toEqual(programs);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/program/');
    });

    it('should handle get programs errors', async () => {
      const errorResponse = { message: 'Internal server error' };
      mock.onGet('/program/').reply(500, errorResponse);

      await expect(getPrograms()).rejects.toEqual(errorResponse);
    });
  });

  describe('getProgram', () => {
    it('should get a specific program successfully', async () => {
      mock.onGet('/program/1').reply(200, mockProgram);

      const result = await getProgram(1);

      expect(result).toEqual(mockProgram);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/program/1');
    });

    it('should handle get program errors', async () => {
      const errorResponse = { message: 'Program not found' };
      mock.onGet('/program/1').reply(404, errorResponse);

      await expect(getProgram(1)).rejects.toEqual(errorResponse);
    });
  });

  describe('updateProgram', () => {
    it('should update a program successfully', async () => {
      const updatedProgram = { ...mockProgram, name: 'Updated Program' };
      mock.onPatch('/program/1').reply(200, updatedProgram);

      const result = await updateProgram(1, 'Updated Program', 2, false);

      expect(result).toEqual(updatedProgram);
      expect(mock.history.patch).toHaveLength(1);
      expect(mock.history.patch[0].url).toBe('/program/1');
    });

    it('should handle update errors', async () => {
      const errorResponse = { message: 'Bad request' };
      mock.onPatch('/program/1').reply(400, errorResponse);

      await expect(updateProgram(1, 'Updated Program', 2, false)).rejects.toEqual(errorResponse);
    });
  });

  describe('deleteProgram', () => {
    it('should delete a program successfully', async () => {
      mock.onDelete('/program/1').reply(200, mockProgram);

      const result = await deleteProgram(1);

      expect(result).toEqual(mockProgram);
      expect(mock.history.delete).toHaveLength(1);
      expect(mock.history.delete[0].url).toBe('/program/1');
    });

    it('should handle delete errors', async () => {
      const errorResponse = { message: 'Program not found' };
      mock.onDelete('/program/1').reply(404, errorResponse);

      await expect(deleteProgram(1)).rejects.toEqual(errorResponse);
    });
  });
});
