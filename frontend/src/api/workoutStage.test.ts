import MockAdapter from 'axios-mock-adapter';

import { expectRequestError } from './apiRequestErrorTestUtils';
import { ENDPOINT } from './endpoint';
import type { WorkoutStage } from './types';
import { getWorkoutStagesByWorkout, getWorkoutStage } from './workoutStage';

const mock = new MockAdapter(ENDPOINT);

describe('WorkoutStage API', () => {
  beforeEach(() => {
    mock.reset();
  });

  const mockWorkoutStage: WorkoutStage = {
    id: 1,
    programmed_workout_id: 1,
    stage_type_id: 1,
    position: 1,
    name: 'Warm-up',
    created_at: new Date('2024-01-01T00:00:00.000Z'),
    updated_at: new Date('2024-01-01T00:00:00.000Z'),
  };

  describe('getWorkoutStagesByWorkout', () => {
    it('should get all workout stages for a workout successfully', async () => {
      const stages = [mockWorkoutStage];
      mock.onGet('/workout_stage/workout/1').reply(200, stages);

      const result = await getWorkoutStagesByWorkout(1);

      expect(result).toEqual(stages);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/workout_stage/workout/1');
    });

    it('should handle get stages by workout errors', async () => {
      const errorResponse = { message: 'Workout not found' };
      mock.onGet('/workout_stage/workout/1').reply(404, errorResponse);

      await expectRequestError(getWorkoutStagesByWorkout(1), errorResponse);
    });

    it('should handle empty response when no stages exist', async () => {
      mock.onGet('/workout_stage/workout/1').reply(200, []);

      const result = await getWorkoutStagesByWorkout(1);

      expect(result).toEqual([]);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/workout_stage/workout/1');
    });

    it('should handle multiple stages for a workout', async () => {
      const stages = [
        mockWorkoutStage,
        { ...mockWorkoutStage, id: 2, position: 2, name: 'Main Work', stage_type_id: 2 },
        { ...mockWorkoutStage, id: 3, position: 3, name: 'Cool-down', stage_type_id: 3 },
      ];
      mock.onGet('/workout_stage/workout/1').reply(200, stages);

      const result = await getWorkoutStagesByWorkout(1);

      expect(result).toEqual(stages);
      expect(result).toHaveLength(3);
      expect(result[0].position).toBe(1);
      expect(result[1].position).toBe(2);
      expect(result[2].position).toBe(3);
      expect(result[0].name).toBe('Warm-up');
      expect(result[1].name).toBe('Main Work');
      expect(result[2].name).toBe('Cool-down');
    });

    it('should handle stages with different stage types', async () => {
      const stages = [
        { ...mockWorkoutStage, stage_type_id: 1, name: 'Warm-up' },
        { ...mockWorkoutStage, id: 2, stage_type_id: 2, name: 'Strength' },
        { ...mockWorkoutStage, id: 3, stage_type_id: 3, name: 'Accessory' },
      ];
      mock.onGet('/workout_stage/workout/1').reply(200, stages);

      const result = await getWorkoutStagesByWorkout(1);

      expect(result).toEqual(stages);
      expect(result[0].stage_type_id).toBe(1);
      expect(result[1].stage_type_id).toBe(2);
      expect(result[2].stage_type_id).toBe(3);
    });
  });

  describe('getWorkoutStage', () => {
    it('should get a specific workout stage successfully', async () => {
      mock.onGet('/workout_stage/1').reply(200, mockWorkoutStage);

      const result = await getWorkoutStage(1);

      expect(result).toEqual(mockWorkoutStage);
      expect(mock.history.get).toHaveLength(1);
      expect(mock.history.get[0].url).toBe('/workout_stage/1');
    });

    it('should handle get stage errors', async () => {
      const errorResponse = { message: 'Stage not found' };
      mock.onGet('/workout_stage/1').reply(404, errorResponse);

      await expectRequestError(getWorkoutStage(1), errorResponse);
    });

    it('should handle stage with different stage type', async () => {
      const strengthStage = { ...mockWorkoutStage, stage_type_id: 2, name: 'Strength Work' };
      mock.onGet('/workout_stage/1').reply(200, strengthStage);

      const result = await getWorkoutStage(1);

      expect(result).toEqual(strengthStage);
      expect(result.stage_type_id).toBe(2);
      expect(result.name).toBe('Strength Work');
    });

    it('should handle stage with high position number', async () => {
      const highPositionStage = { ...mockWorkoutStage, position: 10, name: 'Final Exercise' };
      mock.onGet('/workout_stage/1').reply(200, highPositionStage);

      const result = await getWorkoutStage(1);

      expect(result).toEqual(highPositionStage);
      expect(result.position).toBe(10);
      expect(result.name).toBe('Final Exercise');
    });

    it('should handle stage with zero position', async () => {
      const zeroPositionStage = { ...mockWorkoutStage, position: 0, name: 'Pre-workout' };
      mock.onGet('/workout_stage/1').reply(200, zeroPositionStage);

      const result = await getWorkoutStage(1);

      expect(result).toEqual(zeroPositionStage);
      expect(result.position).toBe(0);
      expect(result.name).toBe('Pre-workout');
    });
  });
});
