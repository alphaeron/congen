import MockAdapter from 'axios-mock-adapter';
import { ENDPOINT } from './endpoint';
import { 
  getProgrammedWorkouts, 
  getProgrammedWorkout, 
  getProgrammedWorkoutsByProgram,
  updateProgrammedWorkout 
} from './programmedWorkout';
import type { ProgrammedWorkout } from './types';

const mock = new MockAdapter(ENDPOINT);

describe('ProgrammedWorkout API', () => {
  beforeEach(() => {
    mock.reset();
  });

  const mockProgrammedWorkout: ProgrammedWorkout = {
    id: 1,
    program_id: 1,
    day_number: 1,
    name: 'Push Day',
    created_at: '2024-01-01T00:00:00Z',
    updated_at: '2024-01-01T00:00:00Z',
  };

  describe('getProgrammedWorkouts', () => {
    it('should get all programmed workouts successfully', async () => {
      const workouts = [mockProgrammedWorkout];
      mock.onGet('/programmed_workout/').reply(200, workouts);

      const result = await getProgrammedWorkouts();

      expect(result).toEqual(workouts);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/programmed_workout/');
    });

    it('should handle get workouts errors', async () => {
      const errorResponse = { message: 'Internal server error' };
      mock.onGet('/programmed_workout/').reply(500, errorResponse);

      await expect(getProgrammedWorkouts()).rejects.toEqual(errorResponse);
    });

    it('should handle empty response when no workouts exist', async () => {
      mock.onGet('/programmed_workout/').reply(200, []);

      const result = await getProgrammedWorkouts();

      expect(result).toEqual([]);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/programmed_workout/');
    });
  });

  describe('getProgrammedWorkout', () => {
    it('should get a specific programmed workout successfully', async () => {
      mock.onGet('/programmed_workout/1').reply(200, mockProgrammedWorkout);

      const result = await getProgrammedWorkout(1);

      expect(result).toEqual(mockProgrammedWorkout);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/programmed_workout/1');
    });

    it('should handle get workout errors', async () => {
      const errorResponse = { message: 'Workout not found' };
      mock.onGet('/programmed_workout/1').reply(404, errorResponse);

      await expect(getProgrammedWorkout(1)).rejects.toEqual(errorResponse);
    });
  });

  describe('getProgrammedWorkoutsByProgram', () => {
    it('should get all programmed workouts for a program successfully', async () => {
      const workouts = [mockProgrammedWorkout];
      mock.onGet('/programmed_workout/program/1').reply(200, workouts);

      const result = await getProgrammedWorkoutsByProgram(1);

      expect(result).toEqual(workouts);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/programmed_workout/program/1');
    });

    it('should handle get workouts by program errors', async () => {
      const errorResponse = { message: 'Program not found' };
      mock.onGet('/programmed_workout/program/1').reply(404, errorResponse);

      await expect(getProgrammedWorkoutsByProgram(1)).rejects.toEqual(errorResponse);
    });

    it('should handle empty response when program has no workouts', async () => {
      mock.onGet('/programmed_workout/program/1').reply(200, []);

      const result = await getProgrammedWorkoutsByProgram(1);

      expect(result).toEqual([]);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/programmed_workout/program/1');
    });
  });

  describe('updateProgrammedWorkout', () => {
    it('should update a programmed workout successfully', async () => {
      const updatedWorkout = { ...mockProgrammedWorkout, name: 'Updated Push Day', day_number: 2 };
      mock.onPatch('/programmed_workout/1').reply(200, updatedWorkout);

      const result = await updateProgrammedWorkout(1, 1, 2, 'Updated Push Day');

      expect(result).toEqual(updatedWorkout);
      expect(mock.history.patch).toHaveLength(1);
      expect(mock.history.patch[0].url).toBe('/programmed_workout/1');
      expect(mock.history.patch[0].params).toEqual({
        program_id: 1,
        day_number: 2,
        name: 'Updated Push Day',
      });
    });

    it('should handle update workout errors', async () => {
      const errorResponse = { message: 'Bad request' };
      mock.onPatch('/programmed_workout/1').reply(400, errorResponse);

      await expect(updateProgrammedWorkout(1, 1, 2, 'Updated Push Day')).rejects.toEqual(errorResponse);
    });

    it('should handle workout not found errors', async () => {
      const errorResponse = { message: 'Workout not found' };
      mock.onPatch('/programmed_workout/999').reply(404, errorResponse);

      await expect(updateProgrammedWorkout(999, 1, 2, 'Updated Push Day')).rejects.toEqual(errorResponse);
    });
  });
});
