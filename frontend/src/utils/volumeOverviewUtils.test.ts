import {
  computeSetVolume,
  formatCompactVolume,
  resolveVolumeStatus,
  getVolumeStatusLabel,
  getEmptyVolumeMessage,
  buildWeekVolumeTotals,
  buildVolumeOverviewModel,
} from './volumeOverviewUtils';
import type { Exercise, ProgrammedWorkoutWithStages, UserDataExport } from '../api/types';

describe('volumeOverviewUtils', () => {
  describe('computeSetVolume', () => {
    it('prefers performed values over targets', () => {
      const result = computeSetVolume(
        {
          performed_weight: 100,
          performed_rep_count: 5,
          target_weight: 90,
          target_rep_count: 5,
        },
        'LBS'
      );

      expect(result.usedPerformed).toBe(true);
      expect(result.volume).toBeGreaterThan(0);
    });

    it('returns zero volume when only target values exist', () => {
      const result = computeSetVolume(
        {
          target_weight: 100,
          target_rep_count: 5,
        },
        'LBS'
      );

      expect(result.usedPerformed).toBe(false);
      expect(result.hasTarget).toBe(true);
      expect(result.volume).toBe(0);
    });
  });

  describe('formatCompactVolume', () => {
    it('formats thousands with compact notation', () => {
      expect(formatCompactVolume(4001, 'LBS')).toBe('4.0k lbs');
      expect(formatCompactVolume(150, 'KG')).toBe('150 kg');
    });
  });

  describe('resolveVolumeStatus', () => {
    it('returns no_volume when current is zero', () => {
      expect(resolveVolumeStatus(0, 1000)).toBe('no_volume');
    });

    it('returns on_track when current is positive but target is zero', () => {
      expect(resolveVolumeStatus(1000, 0)).toBe('on_track');
    });

    it('returns exceeded when current is above target', () => {
      expect(resolveVolumeStatus(2000, 1000)).toBe('exceeded');
    });

    it('returns on_track near target', () => {
      expect(resolveVolumeStatus(900, 1000)).toBe('on_track');
    });

    it('returns under when meaningfully below target', () => {
      expect(resolveVolumeStatus(500, 1000)).toBe('under');
    });
  });

  describe('status labels and empty messages', () => {
    it('maps statuses to accessible labels', () => {
      expect(getVolumeStatusLabel('exceeded')).toBe('Exceeded');
      expect(getVolumeStatusLabel('on_track')).toBe('On track');
      expect(getVolumeStatusLabel('under')).toBe('Under');
      expect(getVolumeStatusLabel('no_volume')).toBe('No volume');
    });

    it('returns teaching empty-state messages', () => {
      expect(getEmptyVolumeMessage('Max Effort', 'no_volume')).toContain('max-effort');
      expect(getEmptyVolumeMessage('Max Effort', 'under')).toBeNull();
    });
  });

  describe('buildWeekVolumeTotals and buildVolumeOverviewModel', () => {
    const exerciseData = new Map<string, Exercise>([
      [
        'Bench Press',
        {
          name: 'Bench Press',
          description: 'Press',
          movement_type: 'horizontal_press',
          is_unilateral: false,
          is_upper: true,
          is_accessory: false,
        },
      ],
    ]);

    const createWorkout = (
      id: number,
      dayNumber: number,
      name: string,
      weight: number
    ): ProgrammedWorkoutWithStages => ({
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
                  performed_weight: weight,
                  performed_rep_count: 5,
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
    });

    const workouts: ProgrammedWorkoutWithStages[] = [
      createWorkout(1, 1, 'ME Upper', 100),
      createWorkout(2, 5, 'ME Upper', 110),
    ];

    it('aggregates zero volume for programmed but unlogged sets', () => {
      const programmedOnlyWorkout: ProgrammedWorkoutWithStages = {
        ...createWorkout(3, 1, 'ME Upper', 100),
        stages: [
          {
            ...createWorkout(3, 1, 'ME Upper', 100).stages[0],
            exercises: [
              {
                ...createWorkout(3, 1, 'ME Upper', 100).stages[0].exercises[0],
                set_schemes: [
                  {
                    ...createWorkout(3, 1, 'ME Upper', 100).stages[0].exercises[0].set_schemes[0],
                    performed_weight: undefined,
                    performed_rep_count: undefined,
                    target_weight: 100,
                    target_rep_count: 5,
                  },
                ],
              },
            ],
          },
        ],
      };

      const weeks = buildWeekVolumeTotals([programmedOnlyWorkout], exerciseData, 4, 'LBS');
      expect(weeks).toHaveLength(1);
      expect(weeks[0].totalVolume).toBe(0);
      expect(weeks[0].performedSets).toBe(0);
      expect(weeks[0].targetSets).toBe(1);
    });

    it('aggregates volume by week', () => {
      const weeks = buildWeekVolumeTotals(workouts, exerciseData, 4, 'LBS');
      expect(weeks).toHaveLength(2);
      expect(weeks[0].weekNumber).toBe(1);
      expect(weeks[1].weekNumber).toBe(2);
      expect(weeks[0].maxEffortVolume).toBeGreaterThan(0);
    });

    it('builds overview model with shared scale and summary metrics', () => {
      const userDataExport = {
        training_programs: [
          {
            program: {
              id: 1,
              user_id: 'u1',
              name: 'Test',
              is_active: true,
              current_week_number: 2,
              created_at: new Date('2024-01-01T00:00:00.000Z'),
              updated_at: new Date('2024-01-01T00:00:00.000Z'),
            },
            workouts,
          },
        ],
        user_one_rep_max: [],
      } as unknown as UserDataExport;

      const model = buildVolumeOverviewModel(
        userDataExport,
        exerciseData,
        4,
        2,
        'this_week',
        'LBS'
      );

      expect(model).not.toBeNull();
      expect(model?.categories).toHaveLength(3);
      expect(model?.sharedScaleMax).toBeGreaterThan(0);
      expect(model?.summary.sessionsPlanned).toBeGreaterThan(0);
      expect(model?.categories[0].hasBaseline).toBe(true);
      expect(model?.targetLabel).toContain('avg of last 4 weeks');
    });

    it('marks categories without historical baseline', () => {
      const userDataExport = {
        training_programs: [
          {
            program: {
              id: 1,
              user_id: 'u1',
              name: 'Test',
              is_active: true,
              current_week_number: 1,
              created_at: new Date('2024-01-01T00:00:00.000Z'),
              updated_at: new Date('2024-01-01T00:00:00.000Z'),
            },
            workouts: [createWorkout(1, 1, 'ME Upper', 100)],
          },
        ],
        user_one_rep_max: [],
      } as unknown as UserDataExport;

      const model = buildVolumeOverviewModel(
        userDataExport,
        exerciseData,
        4,
        1,
        'this_week',
        'LBS'
      );

      expect(model?.categories[0].hasBaseline).toBe(false);
      expect(model?.categories[0].deltaPercent).toBe(0);
    });
  });
});
