import {
  hasPerformedData,
  exerciseHasPerformedData,
  exerciseIsCompleted,
  calculateWorkoutProgress,
  calculateWeekProgress,
  calculateProgramProgress,
  getProgressIcon,
  getProgressColor,
} from './progressUtils';
import type { ProgressStatus } from './progressUtils';
import type {
  SetScheme,
  ProgrammedExerciseWithSetSchemes,
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises,
} from '../api/types';

describe('progressUtils', () => {
  describe('hasPerformedData', () => {
    it('should return true when set scheme has performed_weight', () => {
      const setScheme: SetScheme = {
        id: 1,
        target_weight: 100,
        target_rep_count: 10,
        performed_weight: 105,
        performed_rep_count: undefined,
      } as SetScheme;

      expect(hasPerformedData(setScheme)).toBe(true);
    });

    it('should return true when set scheme has performed_rep_count', () => {
      const setScheme: SetScheme = {
        id: 1,
        target_weight: 100,
        target_rep_count: 10,
        performed_weight: undefined,
        performed_rep_count: 12,
      } as SetScheme;

      expect(hasPerformedData(setScheme)).toBe(true);
    });

    it('should return true when set scheme has both performed fields', () => {
      const setScheme: SetScheme = {
        id: 1,
        target_weight: 100,
        target_rep_count: 10,
        performed_weight: 105,
        performed_rep_count: 12,
      } as SetScheme;

      expect(hasPerformedData(setScheme)).toBe(true);
    });

    it('should return false when set scheme has no performed fields', () => {
      const setScheme: SetScheme = {
        id: 1,
        target_weight: 100,
        target_rep_count: 10,
        performed_weight: undefined,
        performed_rep_count: undefined,
      } as SetScheme;

      expect(hasPerformedData(setScheme)).toBe(false);
    });

    it('should return false when set scheme has null performed fields', () => {
      const setScheme: SetScheme = {
        id: 1,
        target_weight: 100,
        target_rep_count: 10,
        performed_weight: null,
        performed_rep_count: null,
      } as SetScheme;

      expect(hasPerformedData(setScheme)).toBe(false);
    });
  });

  describe('exerciseHasPerformedData', () => {
    it('should return true when exercise has at least one set scheme with performed data', () => {
      const exercise: ProgrammedExerciseWithSetSchemes = {
        id: 1,
        exercise: { id: 1, name: 'Bench Press' } as any,
        set_schemes: [
          {
            id: 1,
            target_weight: 100,
            target_rep_count: 10,
            performed_weight: 105,
            performed_rep_count: undefined,
          } as SetScheme,
          {
            id: 2,
            target_weight: 100,
            target_rep_count: 10,
            performed_weight: undefined,
            performed_rep_count: undefined,
          } as SetScheme,
        ],
      };

      expect(exerciseHasPerformedData(exercise)).toBe(true);
    });

    it('should return false when exercise has no set schemes with performed data', () => {
      const exercise: ProgrammedExerciseWithSetSchemes = {
        id: 1,
        exercise: { id: 1, name: 'Bench Press' } as any,
        set_schemes: [
          {
            id: 1,
            target_weight: 100,
            target_rep_count: 10,
            performed_weight: undefined,
            performed_rep_count: undefined,
          } as SetScheme,
          {
            id: 2,
            target_weight: 100,
            target_rep_count: 10,
            performed_weight: undefined,
            performed_rep_count: undefined,
          } as SetScheme,
        ],
      };

      expect(exerciseHasPerformedData(exercise)).toBe(false);
    });

    it('should return false when exercise has no set schemes', () => {
      const exercise: ProgrammedExerciseWithSetSchemes = {
        id: 1,
        exercise: { id: 1, name: 'Bench Press' } as any,
        set_schemes: [],
      };

      expect(exerciseHasPerformedData(exercise)).toBe(false);
    });
  });

  describe('exerciseIsCompleted', () => {
    it('should return true when all set schemes have performed data', () => {
      const exercise: ProgrammedExerciseWithSetSchemes = {
        id: 1,
        exercise: { id: 1, name: 'Bench Press' } as any,
        set_schemes: [
          {
            id: 1,
            target_weight: 100,
            target_rep_count: 10,
            performed_weight: 105,
            performed_rep_count: 12,
          } as SetScheme,
          {
            id: 2,
            target_weight: 100,
            target_rep_count: 10,
            performed_weight: 110,
            performed_rep_count: 8,
          } as SetScheme,
        ],
      };

      expect(exerciseIsCompleted(exercise)).toBe(true);
    });

    it('should return false when some set schemes are missing performed data', () => {
      const exercise: ProgrammedExerciseWithSetSchemes = {
        id: 1,
        exercise: { id: 1, name: 'Bench Press' } as any,
        set_schemes: [
          {
            id: 1,
            target_weight: 100,
            target_rep_count: 10,
            performed_weight: 105,
            performed_rep_count: 12,
          } as SetScheme,
          {
            id: 2,
            target_weight: 100,
            target_rep_count: 10,
            performed_weight: undefined,
            performed_rep_count: undefined,
          } as SetScheme,
        ],
      };

      expect(exerciseIsCompleted(exercise)).toBe(false);
    });

    it('should return false when exercise has no set schemes', () => {
      const exercise: ProgrammedExerciseWithSetSchemes = {
        id: 1,
        exercise: { id: 1, name: 'Bench Press' } as any,
        set_schemes: [],
      };

      expect(exerciseIsCompleted(exercise)).toBe(false);
    });
  });

  describe('calculateWorkoutProgress', () => {
    it('should return completed status when all exercises are completed', () => {
      const workout: ProgrammedWorkoutWithStages = {
        workout: { id: 1, name: 'Test Workout' } as any,
        stages: [
          {
            id: 1,
            stage_type: 'warmup',
            exercises: [
              {
                id: 1,
                exercise: { id: 1, name: 'Bench Press' } as any,
                set_schemes: [
                  {
                    id: 1,
                    target_weight: 100,
                    target_rep_count: 10,
                    performed_weight: 105,
                    performed_rep_count: 12,
                  } as SetScheme,
                ],
              },
            ],
          } as WorkoutStageWithExercises,
        ],
      };

      const result = calculateWorkoutProgress(workout);

      expect(result.status).toBe('completed');
      expect(result.totalExercises).toBe(1);
      expect(result.completedExercises).toBe(1);
      expect(result.completionRate).toBe(100);
    });

    it('should return in-progress status when some exercises have performed data', () => {
      const workout: ProgrammedWorkoutWithStages = {
        workout: { id: 1, name: 'Test Workout' } as any,
        stages: [
          {
            id: 1,
            stage_type: 'warmup',
            exercises: [
              {
                id: 1,
                exercise: { id: 1, name: 'Bench Press' } as any,
                set_schemes: [
                  {
                    id: 1,
                    target_weight: 100,
                    target_rep_count: 10,
                    performed_weight: 105,
                    performed_rep_count: undefined,
                  } as SetScheme,
                ],
              },
              {
                id: 2,
                exercise: { id: 2, name: 'Squat' } as any,
                set_schemes: [
                  {
                    id: 2,
                    target_weight: 100,
                    target_rep_count: 10,
                    performed_weight: undefined,
                    performed_rep_count: undefined,
                  } as SetScheme,
                ],
              },
            ],
          } as WorkoutStageWithExercises,
        ],
      };

      const result = calculateWorkoutProgress(workout);

      expect(result.status).toBe('in-progress');
      expect(result.totalExercises).toBe(2);
      expect(result.completedExercises).toBe(1);
      expect(result.completionRate).toBe(50);
    });

    it('should return not-started status when no exercises have performed data', () => {
      const workout: ProgrammedWorkoutWithStages = {
        workout: { id: 1, name: 'Test Workout' } as any,
        stages: [
          {
            id: 1,
            stage_type: 'warmup',
            exercises: [
              {
                id: 1,
                exercise: { id: 1, name: 'Bench Press' } as any,
                set_schemes: [
                  {
                    id: 1,
                    target_weight: 100,
                    target_rep_count: 10,
                    performed_weight: undefined,
                    performed_rep_count: undefined,
                  } as SetScheme,
                ],
              },
            ],
          } as WorkoutStageWithExercises,
        ],
      };

      const result = calculateWorkoutProgress(workout);

      expect(result.status).toBe('not-started');
      expect(result.totalExercises).toBe(1);
      expect(result.completedExercises).toBe(0);
      expect(result.completionRate).toBe(0);
    });

    it('should handle workout with no stages', () => {
      const workout: ProgrammedWorkoutWithStages = {
        workout: { id: 1, name: 'Test Workout' } as any,
        stages: undefined,
      };

      const result = calculateWorkoutProgress(workout);

      expect(result.status).toBe('not-started');
      expect(result.totalExercises).toBe(0);
      expect(result.completedExercises).toBe(0);
      expect(result.completionRate).toBe(0);
    });

    it('should handle workout with empty stages', () => {
      const workout: ProgrammedWorkoutWithStages = {
        workout: { id: 1, name: 'Test Workout' } as any,
        stages: [],
      };

      const result = calculateWorkoutProgress(workout);

      expect(result.status).toBe('not-started');
      expect(result.totalExercises).toBe(0);
      expect(result.completedExercises).toBe(0);
      expect(result.completionRate).toBe(0);
    });
  });

  describe('calculateWeekProgress', () => {
    it('should return completed status when all workouts are completed', () => {
      const workouts: ProgrammedWorkoutWithStages[] = [
        {
          workout: { id: 1, name: 'Workout 1' } as any,
          stages: [
            {
              id: 1,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 1,
                  exercise: { id: 1, name: 'Bench Press' } as any,
                  set_schemes: [
                    {
                      id: 1,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: 105,
                      performed_rep_count: 12,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
        {
          workout: { id: 2, name: 'Workout 2' } as any,
          stages: [
            {
              id: 2,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 2,
                  exercise: { id: 2, name: 'Squat' } as any,
                  set_schemes: [
                    {
                      id: 2,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: 110,
                      performed_rep_count: 8,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
      ];

      const result = calculateWeekProgress(workouts);

      expect(result.status).toBe('completed');
      expect(result.totalWorkouts).toBe(2);
      expect(result.completedWorkouts).toBe(2);
      expect(result.completionRate).toBe(100);
    });

    it('should return in-progress status when some workouts are completed', () => {
      const workouts: ProgrammedWorkoutWithStages[] = [
        {
          workout: { id: 1, name: 'Workout 1' } as any,
          stages: [
            {
              id: 1,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 1,
                  exercise: { id: 1, name: 'Bench Press' } as any,
                  set_schemes: [
                    {
                      id: 1,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: 105,
                      performed_rep_count: 12,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
        {
          workout: { id: 2, name: 'Workout 2' } as any,
          stages: [
            {
              id: 2,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 2,
                  exercise: { id: 2, name: 'Squat' } as any,
                  set_schemes: [
                    {
                      id: 2,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: undefined,
                      performed_rep_count: undefined,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
      ];

      const result = calculateWeekProgress(workouts);

      expect(result.status).toBe('in-progress');
      expect(result.totalWorkouts).toBe(2);
      expect(result.completedWorkouts).toBe(1);
      expect(result.completionRate).toBe(50);
    });

    it('should return not-started status when no workouts are completed', () => {
      const workouts: ProgrammedWorkoutWithStages[] = [
        {
          workout: { id: 1, name: 'Workout 1' } as any,
          stages: [
            {
              id: 1,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 1,
                  exercise: { id: 1, name: 'Bench Press' } as any,
                  set_schemes: [
                    {
                      id: 1,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: undefined,
                      performed_rep_count: undefined,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
      ];

      const result = calculateWeekProgress(workouts);

      expect(result.status).toBe('not-started');
      expect(result.totalWorkouts).toBe(1);
      expect(result.completedWorkouts).toBe(0);
      expect(result.completionRate).toBe(0);
    });

    it('should handle empty workouts array', () => {
      const result = calculateWeekProgress([]);

      expect(result.status).toBe('not-started');
      expect(result.totalWorkouts).toBe(0);
      expect(result.completedWorkouts).toBe(0);
      expect(result.completionRate).toBe(0);
    });
  });

  describe('calculateProgramProgress', () => {
    it('should return completed status when all weeks are completed', () => {
      const workouts: ProgrammedWorkoutWithStages[] = [
        // Week 1 - 3 workouts
        {
          workout: { id: 1, name: 'Workout 1', day_number: 1 } as any,
          stages: [
            {
              id: 1,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 1,
                  exercise: { id: 1, name: 'Bench Press' } as any,
                  set_schemes: [
                    {
                      id: 1,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: 105,
                      performed_rep_count: 12,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
        {
          workout: { id: 2, name: 'Workout 2', day_number: 2 } as any,
          stages: [
            {
              id: 2,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 2,
                  exercise: { id: 2, name: 'Squat' } as any,
                  set_schemes: [
                    {
                      id: 2,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: 110,
                      performed_rep_count: 8,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
        {
          workout: { id: 3, name: 'Workout 3', day_number: 3 } as any,
          stages: [
            {
              id: 3,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 3,
                  exercise: { id: 3, name: 'Deadlift' } as any,
                  set_schemes: [
                    {
                      id: 3,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: 115,
                      performed_rep_count: 6,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
        // Week 2 - 3 workouts
        {
          workout: { id: 4, name: 'Workout 4', day_number: 4 } as any,
          stages: [
            {
              id: 4,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 4,
                  exercise: { id: 4, name: 'Overhead Press' } as any,
                  set_schemes: [
                    {
                      id: 4,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: 105,
                      performed_rep_count: 10,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
        {
          workout: { id: 5, name: 'Workout 5', day_number: 5 } as any,
          stages: [
            {
              id: 5,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 5,
                  exercise: { id: 5, name: 'Row' } as any,
                  set_schemes: [
                    {
                      id: 5,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: 110,
                      performed_rep_count: 8,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
        {
          workout: { id: 6, name: 'Workout 6', day_number: 6 } as any,
          stages: [
            {
              id: 6,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 6,
                  exercise: { id: 6, name: 'Pull-up' } as any,
                  set_schemes: [
                    {
                      id: 6,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: 115,
                      performed_rep_count: 6,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
      ];

      const result = calculateProgramProgress(workouts, 3);

      expect(result.status).toBe('completed');
      expect(result.totalWeeks).toBe(2);
      expect(result.completedWeeks).toBe(2);
      expect(result.completionRate).toBe(100);
    });

    it('should return in-progress status when some weeks are completed', () => {
      const workouts: ProgrammedWorkoutWithStages[] = [
        // Week 1 - all completed
        {
          workout: { id: 1, name: 'Workout 1', day_number: 1 } as any,
          stages: [
            {
              id: 1,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 1,
                  exercise: { id: 1, name: 'Bench Press' } as any,
                  set_schemes: [
                    {
                      id: 1,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: 105,
                      performed_rep_count: 12,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
        {
          workout: { id: 2, name: 'Workout 2', day_number: 2 } as any,
          stages: [
            {
              id: 2,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 2,
                  exercise: { id: 2, name: 'Squat' } as any,
                  set_schemes: [
                    {
                      id: 2,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: 110,
                      performed_rep_count: 8,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
        // Week 2 - not completed
        {
          workout: { id: 3, name: 'Workout 3', day_number: 3 } as any,
          stages: [
            {
              id: 3,
              stage_type: 'warmup',
              exercises: [
                {
                  id: 3,
                  exercise: { id: 3, name: 'Deadlift' } as any,
                  set_schemes: [
                    {
                      id: 3,
                      target_weight: 100,
                      target_rep_count: 10,
                      performed_weight: undefined,
                      performed_rep_count: undefined,
                    } as SetScheme,
                  ],
                },
              ],
            } as WorkoutStageWithExercises,
          ],
        },
      ];

      const result = calculateProgramProgress(workouts, 2);

      expect(result.status).toBe('in-progress');
      expect(result.totalWeeks).toBe(2);
      expect(result.completedWeeks).toBe(1);
      expect(result.completionRate).toBe(50);
    });

    it('should handle empty workouts array', () => {
      const result = calculateProgramProgress([], 3);

      expect(result.status).toBe('not-started');
      expect(result.totalWeeks).toBe(0);
      expect(result.completedWeeks).toBe(0);
      expect(result.completionRate).toBe(0);
    });
  });

  describe('getProgressIcon', () => {
    it('should return checkmark for completed status', () => {
      expect(getProgressIcon('completed')).toBe('check_circle');
    });

    it('should return hourglass for in-progress status', () => {
      expect(getProgressIcon('in-progress')).toBe('schedule');
    });

    it('should return pause for not-started status', () => {
      expect(getProgressIcon('not-started')).toBe('pause_circle');
    });
  });

  describe('getProgressColor', () => {
    it('should return success color for completed status', () => {
      expect(getProgressColor('completed')).toBe('success.main');
    });

    it('should return warning color for in-progress status', () => {
      expect(getProgressColor('in-progress')).toBe('warning.main');
    });

    it('should return secondary color for not-started status', () => {
      expect(getProgressColor('not-started')).toBe('text.secondary');
    });
  });
});
