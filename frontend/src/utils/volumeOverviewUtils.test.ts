import {
  computeSetVolume,
  computeProgrammedSetVolume,
  formatCompactVolume,
  resolveVolumeStatus,
  getVolumeStatusLabel,
  getMesocycleWeekSlot,
  getCategoryOnTrackFloor,
  averageSameWeekSlotVolume,
  averageChronicVolume,
  computeAcwr,
  buildBulletScale,
  buildBulletAxisLabels,
  buildWeeklyAcwrSeries,
  buildWeeklyIntensitySeries,
  buildWeekVolumeTotals,
  buildVolumeOverviewModel,
  MESOCYCLE_WEEKS,
  ACWR_HIGH_THRESHOLD,
  OVERSHOOT_RATIO,
} from './volumeOverviewUtils';
import type { WeekVolumeTotals } from './volumeOverviewUtils';
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

  describe('computeProgrammedSetVolume', () => {
    it('computes volume from target weight and reps', () => {
      const result = computeProgrammedSetVolume(
        {
          target_weight: 100,
          target_rep_count: 5,
        },
        'LBS'
      );

      expect(result.hasTarget).toBe(true);
      expect(result.volume).toBeGreaterThan(0);
    });

    it('returns zero when target values are missing', () => {
      const result = computeProgrammedSetVolume(
        {
          performed_weight: 100,
          performed_rep_count: 5,
        },
        'LBS'
      );

      expect(result.hasTarget).toBe(false);
      expect(result.volume).toBe(0);
    });
  });

  describe('formatCompactVolume', () => {
    it('formats thousands with compact notation', () => {
      expect(formatCompactVolume(4001, 'LBS')).toBe('4.0k lbs');
      expect(formatCompactVolume(150, 'KG')).toBe('150 kg');
    });
  });

  describe('getMesocycleWeekSlot', () => {
    it('maps 1-based week numbers into slots 1–4', () => {
      expect(MESOCYCLE_WEEKS).toBe(4);
      expect(getMesocycleWeekSlot(1)).toBe(1);
      expect(getMesocycleWeekSlot(2)).toBe(2);
      expect(getMesocycleWeekSlot(4)).toBe(4);
      expect(getMesocycleWeekSlot(5)).toBe(1);
      expect(getMesocycleWeekSlot(6)).toBe(2);
      expect(getMesocycleWeekSlot(8)).toBe(4);
    });

    it('treats week 0 as slot 1', () => {
      expect(getMesocycleWeekSlot(0)).toBe(1);
    });
  });

  describe('getCategoryOnTrackFloor', () => {
    it('uses tighter ME and wider accessory floors', () => {
      expect(getCategoryOnTrackFloor('Max Effort')).toBe(0.9);
      expect(getCategoryOnTrackFloor('Dynamic Effort')).toBe(0.85);
      expect(getCategoryOnTrackFloor('Accessory')).toBe(0.75);
    });
  });

  describe('resolveVolumeStatus', () => {
    it('returns no_volume when current is zero', () => {
      expect(resolveVolumeStatus(0, 1000)).toBe('no_volume');
    });

    it('returns no_volume when logging is incomplete', () => {
      expect(
        resolveVolumeStatus(0, 1000, { loggingIncomplete: true })
      ).toBe('no_volume');
    });

    it('returns overshoot when flagged', () => {
      expect(
        resolveVolumeStatus(2000, 1000, { isOvershoot: true })
      ).toBe('overshoot');
    });

    it('returns on_track when current is positive but target is zero', () => {
      expect(resolveVolumeStatus(1000, 0)).toBe('on_track');
    });

    it('returns exceeded when current is above target', () => {
      expect(resolveVolumeStatus(2000, 1000)).toBe('exceeded');
    });

    it('returns on_track near target using category floor', () => {
      expect(resolveVolumeStatus(900, 1000)).toBe('on_track');
      expect(resolveVolumeStatus(880, 1000, { onTrackFloor: 0.9 })).toBe('under');
      expect(resolveVolumeStatus(900, 1000, { onTrackFloor: 0.9 })).toBe('on_track');
    });

    it('returns under when meaningfully below target', () => {
      expect(resolveVolumeStatus(500, 1000)).toBe('under');
    });
  });

  describe('status labels', () => {
    it('maps statuses to accessible labels', () => {
      expect(getVolumeStatusLabel('exceeded')).toBe('Exceeded');
      expect(getVolumeStatusLabel('on_track')).toBe('On track');
      expect(getVolumeStatusLabel('under')).toBe('Under');
      expect(getVolumeStatusLabel('no_volume')).toBe('No volume');
      expect(getVolumeStatusLabel('overshoot')).toBe('Over plan');
    });
  });

  describe('buildBulletScale', () => {
    it('builds plan-relative poor, ok, and good/overload ends', () => {
      const scale = buildBulletScale(8000, 10000, 9000, 0.85);
      expect(scale.poorEnd).toBe(8500);
      expect(scale.okEnd).toBe(10000);
      expect(scale.goodEnd).toBe(Math.round(10000 * OVERSHOOT_RATIO));
      expect(scale.scaleMax).toBeGreaterThanOrEqual(scale.goodEnd);
      expect(scale.poorEnd).toBeLessThanOrEqual(scale.okEnd);
      expect(scale.okEnd).toBeLessThanOrEqual(scale.goodEnd);
      expect(scale.goodEnd).toBeLessThanOrEqual(scale.scaleMax);
    });

    it('uses per-card max from done, plan, week avg, and overload bound', () => {
      const scale = buildBulletScale(500, 400, 2000, 0.85);
      expect(scale.scaleMax).toBeGreaterThanOrEqual(Math.round(2000 * 1.18));
      expect(scale.goodEnd).toBe(Math.round(400 * OVERSHOOT_RATIO));
    });
  });

  describe('buildBulletAxisLabels', () => {
    it('labels all five distinct band boundaries without plan text', () => {
      const labels = buildBulletAxisLabels({
        scaleMax: 11800,
        poorEnd: 8500,
        okEnd: 10000,
        goodEnd: 11500,
        hasTarget: true,
        preferredUnit: 'LBS',
      });

      expect(labels.map(label => label.value)).toEqual([0, 8500, 10000, 11500, 11800]);
      expect(labels.every(label => !label.text.includes('Plan'))).toBe(true);
      expect(labels.some(label => /^W\d/.test(label.text))).toBe(false);
    });

    it('dedupes identical bounds and keeps all labels on one line', () => {
      const labels = buildBulletAxisLabels({
        scaleMax: 12000,
        poorEnd: 4000,
        okEnd: 5000,
        goodEnd: 5750,
        hasTarget: true,
        preferredUnit: 'LBS',
      });

      expect(labels.some(label => label.value === 4000)).toBe(true);
      expect(labels.some(label => label.value === 5000)).toBe(true);
      expect(labels.some(label => label.value === 5750)).toBe(true);
      expect(labels.some(label => label.value === 12000)).toBe(true);
      expect(labels).toHaveLength(5);
    });
  });

  describe('computeAcwr', () => {
    it('returns null when chronic average is missing', () => {
      expect(computeAcwr(1000, null)).toEqual({ ratio: null, high: false });
    });

    it('flags high acute load', () => {
      const result = computeAcwr(1300, 1000);
      expect(result.ratio).toBe(1.3);
      expect(result.high).toBe(result.ratio! >= ACWR_HIGH_THRESHOLD);
    });
  });

  describe('same-week slot and chronic averages', () => {
    const emptyWeek = (
      weekNumber: number,
      maxEffortVolume: number
    ): WeekVolumeTotals => ({
      weekNumber,
      maxEffortVolume,
      dynamicEffortVolume: 0,
      accessoryVolume: 0,
      totalVolume: maxEffortVolume,
      maxEffortProgrammedVolume: 0,
      dynamicEffortProgrammedVolume: 0,
      accessoryProgrammedVolume: 0,
      totalProgrammedVolume: 0,
      performedSets: maxEffortVolume > 0 ? 1 : 0,
      targetSets: 1,
      completedWorkouts: 1,
      plannedWorkouts: 1,
      maxEffortPeakWeightLbs: 0,
      maxEffortPeakExerciseName: null,
    });

    const weeks = [
      emptyWeek(1, 1000),
      emptyWeek(2, 2000),
      emptyWeek(3, 3000),
      emptyWeek(4, 4000),
      emptyWeek(5, 1500),
      emptyWeek(6, 2500),
    ];

    it('averages prior same mesocycle slots only', () => {
      const result = averageSameWeekSlotVolume(weeks, 6, 'Max Effort');
      expect(result.slot).toBe(2);
      expect(result.sampleCount).toBe(1);
      expect(result.average).toBe(2000);
    });

    it('averages multiple prior blocks for the same slot', () => {
      const withThirdBlock = [...weeks, emptyWeek(10, 2200)];
      const result = averageSameWeekSlotVolume(withThirdBlock, 10, 'Max Effort');
      expect(result.slot).toBe(2);
      expect(result.sampleCount).toBe(2);
      expect(result.average).toBe(Math.round((2000 + 2500) / 2));
    });

    it('returns null on cold start with no prior same slot', () => {
      const result = averageSameWeekSlotVolume(weeks.slice(0, 1), 1, 'Max Effort');
      expect(result.average).toBeNull();
      expect(result.sampleCount).toBe(0);
      expect(result.slot).toBe(1);
    });

    it('averages chronic window for ACWR', () => {
      const chronic = averageChronicVolume(weeks, 6, 'Max Effort', 4);
      expect(chronic).toBe(Math.round((2000 + 3000 + 4000 + 1500) / 4));
    });

    it('builds weekly ACWR series for prior weeks with chronic history', () => {
      const series = buildWeeklyAcwrSeries(weeks, 'Max Effort');
      expect(series.length).toBeGreaterThan(0);
      expect(series.every(point => point.x.startsWith('W'))).toBe(true);
      expect(series.every(point => typeof point.y === 'number')).toBe(true);
    });

    it('builds weekly intensity series from peak loads and 1RMs', () => {
      const withPeaks = weeks.map(week => ({
        ...week,
        maxEffortPeakWeightLbs: 300,
        maxEffortPeakExerciseName: 'Bench Press',
      }));
      const series = buildWeeklyIntensitySeries(withPeaks, [
        {
          user_id: 'u1',
          exercise_name: 'Bench Press',
          one_rep_max: 400,
          unit: 'LBS',
          created_at: new Date('2024-01-01T00:00:00.000Z'),
          updated_at: new Date('2024-01-01T00:00:00.000Z'),
        },
      ]);
      expect(series).toHaveLength(withPeaks.length);
      expect(series[0].y).toBe(75);
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
      weight: number,
      options?: { performed?: boolean; performedWeight?: number }
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
    });

    const workouts: ProgrammedWorkoutWithStages[] = [
      createWorkout(1, 1, 'ME Upper', 100),
      createWorkout(2, 5, 'ME Upper', 110),
    ];

    it('aggregates zero performed volume for programmed but unlogged sets', () => {
      const programmedOnlyWorkout = createWorkout(3, 1, 'ME Upper', 100, {
        performed: false,
      });

      const weeks = buildWeekVolumeTotals([programmedOnlyWorkout], exerciseData, 4, 'LBS');
      expect(weeks).toHaveLength(1);
      expect(weeks[0].totalVolume).toBe(0);
      expect(weeks[0].performedSets).toBe(0);
      expect(weeks[0].targetSets).toBe(1);
      expect(weeks[0].maxEffortProgrammedVolume).toBeGreaterThan(0);
      expect(weeks[0].totalProgrammedVolume).toBeGreaterThan(0);
    });

    it('aggregates volume by week', () => {
      const weeks = buildWeekVolumeTotals(workouts, exerciseData, 4, 'LBS');
      expect(weeks).toHaveLength(2);
      expect(weeks[0].weekNumber).toBe(1);
      expect(weeks[1].weekNumber).toBe(2);
      expect(weeks[0].maxEffortVolume).toBeGreaterThan(0);
    });

    it('uses programmed week volume as category target', () => {
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
            workouts: [createWorkout(1, 1, 'ME Upper', 100, { performed: false })],
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

      const maxEffort = model?.categories.find(category => category.type === 'Max Effort');
      expect(maxEffort?.hasTarget).toBe(true);
      expect(maxEffort?.current).toBe(0);
      expect(maxEffort?.target).toBeGreaterThan(0);
      expect(maxEffort?.loggingIncomplete).toBe(true);
      expect(maxEffort?.status).toBe('no_volume');
      expect(maxEffort?.sameWeekSlotAverage).toBeNull();
      expect(model?.targetLabel).toContain('programmed week volume');
    });

    it('builds overview model with per-card scale and plan bands', () => {
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
      expect((model as { sharedScaleMax?: number } | null)?.sharedScaleMax).toBeUndefined();
      expect(model?.summary.sessionsPlanned).toBeGreaterThan(0);

      const maxEffort = model?.categories.find(category => category.type === 'Max Effort');
      expect(maxEffort?.hasTarget).toBe(true);
      expect(maxEffort?.scaleMax).toBeGreaterThan(0);
      expect(maxEffort?.poorEnd).toBe(
        Math.round((maxEffort?.target ?? 0) * (maxEffort?.onTrackFloor ?? 0.9))
      );
      expect(maxEffort?.okEnd).toBe(maxEffort?.target);
      expect(maxEffort?.goodEnd).toBe(
        Math.round((maxEffort?.target ?? 0) * OVERSHOOT_RATIO)
      );
      expect(maxEffort?.mesocycleWeekSlot).toBe(2);
    });

    it('computes W{n} avg from prior mesocycle blocks and ACWR', () => {
      const multiBlockWorkouts = [
        createWorkout(1, 1, 'ME Upper', 100),
        createWorkout(2, 5, 'ME Upper', 100),
        createWorkout(3, 9, 'ME Upper', 100),
        createWorkout(4, 13, 'ME Upper', 100),
        createWorkout(5, 17, 'ME Upper', 120),
        createWorkout(6, 21, 'ME Upper', 110),
      ];

      const userDataExport = {
        training_programs: [
          {
            program: {
              id: 1,
              user_id: 'u1',
              name: 'Test',
              is_active: true,
              current_week_number: 6,
              created_at: new Date('2024-01-01T00:00:00.000Z'),
              updated_at: new Date('2024-01-01T00:00:00.000Z'),
            },
            workouts: multiBlockWorkouts,
          },
        ],
        user_one_rep_max: [
          {
            user_id: 'u1',
            exercise_name: 'Bench Press',
            one_rep_max: 150,
            unit: 'KG',
            created_at: new Date('2024-01-01T00:00:00.000Z'),
            updated_at: new Date('2024-01-01T00:00:00.000Z'),
          },
        ],
      } as unknown as UserDataExport;

      const model = buildVolumeOverviewModel(
        userDataExport,
        exerciseData,
        4,
        6,
        'this_week',
        'LBS'
      );

      const maxEffort = model?.categories.find(category => category.type === 'Max Effort');
      expect(maxEffort?.mesocycleWeekSlot).toBe(2);
      expect(maxEffort?.sameWeekSlotSampleCount).toBeGreaterThanOrEqual(1);
      expect(maxEffort?.sameWeekSlotAverage).not.toBeNull();
      expect(maxEffort?.acwr).not.toBeNull();
      expect(maxEffort?.intensityPercent).not.toBeNull();
      expect(maxEffort?.intensityPercent).toBeGreaterThan(0);
      expect(maxEffort?.intensityLabel).toContain('Bench');
    });

    it('flags overshoot on deload-like weeks when done exceeds plan', () => {
      const deloadWorkouts = [
        createWorkout(1, 1, 'ME Upper', 100),
        createWorkout(2, 5, 'ME Upper', 100),
        createWorkout(3, 9, 'ME Upper', 100),
        createWorkout(4, 13, 'ME Upper', 100),
        createWorkout(5, 17, 'ME Upper', 40, { performedWeight: 100 }),
      ];

      const userDataExport = {
        training_programs: [
          {
            program: {
              id: 1,
              user_id: 'u1',
              name: 'Test',
              is_active: true,
              current_week_number: 5,
              created_at: new Date('2024-01-01T00:00:00.000Z'),
              updated_at: new Date('2024-01-01T00:00:00.000Z'),
            },
            workouts: deloadWorkouts,
          },
        ],
        user_one_rep_max: [],
      } as unknown as UserDataExport;

      const model = buildVolumeOverviewModel(
        userDataExport,
        exerciseData,
        4,
        5,
        'this_week',
        'LBS'
      );

      const maxEffort = model?.categories.find(category => category.type === 'Max Effort');
      expect(maxEffort?.mesocycleWeekSlot).toBe(1);
      expect(maxEffort?.isDeloadLike).toBe(true);
      expect(maxEffort?.current).toBeGreaterThan(
        (maxEffort?.target ?? 0) * OVERSHOOT_RATIO
      );
      expect(maxEffort?.isOvershoot).toBe(true);
      expect(maxEffort?.status).toBe('overshoot');
    });
  });
});
