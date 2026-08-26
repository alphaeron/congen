import type {
  Exercise,
  ProgrammedWorkoutWithStages,
  UserDataExport,
  UserOneRepMax,
} from '../api/types';

const BENCH_PRESS: Exercise = {
  name: 'Bench Press',
  description: 'Press',
  movement_type: 'horizontal_press',
  is_unilateral: false,
  is_upper: true,
  is_accessory: false,
};

/**
 * Default exercise map for volume overview tests.
 */
export function createVolumeExerciseData(): Map<string, Exercise> {
  return new Map([['Bench Press', BENCH_PRESS]]);
}

/**
 * Builds a programmed workout with a single Bench Press set for volume tests.
 *
 * @param id Workout / stage / exercise id
 * @param dayNumber Program day number
 * @param name Workout name (e.g. ME Upper)
 * @param weight Target weight in kg
 * @param options Performed-volume overrides
 */
export function createVolumeWorkout(
  id: number,
  dayNumber: number,
  name: string,
  weight: number,
  options?: { performed?: boolean; performedWeight?: number }
): ProgrammedWorkoutWithStages {
  return {
    workout: {
      id,
      program_id: 1,
      day_number: dayNumber,
      name,
      created_at: new Date('2024-01-01T00:00:00.000Z'),
      updated_at: new Date('2024-01-01T00:00:00.000Z'),
    },
    stages: [
      {
        stage: {
          id,
          programmed_workout_id: id,
          stage_type_id: 1,
          position: 1,
          name: 'Main',
          created_at: new Date('2024-01-01T00:00:00.000Z'),
          updated_at: new Date('2024-01-01T00:00:00.000Z'),
        },
        exercises: [
          {
            exercise: {
              id,
              workout_stage_id: id,
              exercise_name: 'Bench Press',
              position: 1,
              created_at: new Date('2024-01-01T00:00:00.000Z'),
              updated_at: new Date('2024-01-01T00:00:00.000Z'),
            },
            set_schemes: [
              {
                id,
                programmed_exercise_id: id,
                set_number: 1,
                is_amrap: false,
                is_emom: false,
                use_tempo: false,
                performed_weight:
                  options?.performed === false
                    ? undefined
                    : (options?.performedWeight ?? weight),
                performed_rep_count: options?.performed === false ? undefined : 5,
                target_weight: weight,
                target_rep_count: 5,
                created_at: new Date('2024-01-01T00:00:00.000Z'),
                updated_at: new Date('2024-01-01T00:00:00.000Z'),
              },
            ],
          },
        ],
      },
    ],
  };
}

/**
 * Builds a UserDataExport with an active program for volume overview tests.
 *
 * @param currentWeek Active program week
 * @param workouts Program workouts
 * @param oneRepMaxes Optional 1RM records
 */
export function createVolumeUserDataExport(
  currentWeek: number,
  workouts: ProgrammedWorkoutWithStages[],
  oneRepMaxes: UserOneRepMax[] = []
): UserDataExport {
  return {
    training_programs: [
      {
        program: {
          id: 1,
          user_id: 'u1',
          name: 'Test',
          is_active: true,
          current_week_number: currentWeek,
          created_at: new Date('2024-01-01T00:00:00.000Z'),
          updated_at: new Date('2024-01-01T00:00:00.000Z'),
        },
        workouts,
      },
    ],
    user_one_rep_max: oneRepMaxes,
  } as unknown as UserDataExport;
}
