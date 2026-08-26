import {
  computeSetVolume,
  computeProgrammedSetVolume,
  formatCompactVolume,
  resolveVolumeStatus,
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
import {
  createVolumeExerciseData,
  createVolumeUserDataExport,
  createVolumeWorkout,
} from '../testUtils/volumeOverviewFixtures';

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
    it.each([
      [4001, 'LBS', '4.0k lbs'],
      [150, 'KG', '150 kg'],
    ] as const)('formats %s %s as %s', (volume, unit, expected) => {
      expect(formatCompactVolume(volume, unit)).toBe(expected);
    });
  });

  describe('getMesocycleWeekSlot', () => {
    it.each([
      [1, 1],
      [2, 2],
      [4, 4],
      [5, 1],
      [6, 2],
      [8, 4],
      [0, 1],
    ])('maps week %i to slot %i', (week, slot) => {
      expect(MESOCYCLE_WEEKS).toBe(4);
      expect(getMesocycleWeekSlot(week)).toBe(slot);
    });
  });

  describe('getCategoryOnTrackFloor', () => {
    it.each([
      ['Max Effort', 0.9],
      ['Dynamic Effort', 0.85],
      ['Accessory', 0.75],
    ] as const)('%s floor is %s', (category, floor) => {
      expect(getCategoryOnTrackFloor(category)).toBe(floor);
    });
  });

  describe('resolveVolumeStatus', () => {
    it.each([
      [0, 1000, undefined, 'no_volume'],
      [0, 1000, { loggingIncomplete: true }, 'no_volume'],
      [2000, 1000, { isOvershoot: true }, 'overshoot'],
      [1000, 0, undefined, 'on_track'],
      [2000, 1000, undefined, 'exceeded'],
      [900, 1000, undefined, 'on_track'],
      [880, 1000, { onTrackFloor: 0.9 }, 'under'],
      [900, 1000, { onTrackFloor: 0.9 }, 'on_track'],
      [500, 1000, undefined, 'under'],
    ] as const)(
      'current=%s target=%s options=%j → %s',
      (current, target, options, expected) => {
        expect(resolveVolumeStatus(current, target, options)).toBe(expected);
      }
    );
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
        preferredUnit: 'LBS',
      });

      expect(labels.map(label => label.value)).toEqual([0, 4000, 5000, 5750, 12000]);
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
    const emptyWeek = (weekNumber: number, maxEffortVolume: number): WeekVolumeTotals => ({
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
    const exerciseData = createVolumeExerciseData();
    const workouts = [
      createVolumeWorkout(1, 1, 'ME Upper', 100),
      createVolumeWorkout(2, 5, 'ME Upper', 110),
    ];

    it('aggregates zero performed volume for programmed but unlogged sets', () => {
      const programmedOnlyWorkout = createVolumeWorkout(3, 1, 'ME Upper', 100, {
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
      const model = buildVolumeOverviewModel(
        createVolumeUserDataExport(1, [
          createVolumeWorkout(1, 1, 'ME Upper', 100, { performed: false }),
        ]),
        exerciseData,
        4,
        1,
        'LBS'
      );

      const maxEffort = model?.categories.find(category => category.type === 'Max Effort');
      expect(maxEffort?.hasTarget).toBe(true);
      expect(maxEffort?.current).toBe(0);
      expect(maxEffort?.target).toBeGreaterThan(0);
      expect(maxEffort?.loggingIncomplete).toBe(true);
      expect(maxEffort?.status).toBe('no_volume');
      expect(maxEffort?.sameWeekSlotAverage).toBeNull();
    });

    it('builds overview model with per-card scale and plan bands', () => {
      const model = buildVolumeOverviewModel(
        createVolumeUserDataExport(2, workouts),
        exerciseData,
        4,
        2,
        'LBS'
      );

      expect(model).not.toBeNull();
      expect(model?.categories).toHaveLength(3);

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
    });

    it('computes W{n} avg from prior mesocycle blocks', () => {
      const multiBlockWorkouts = [
        createVolumeWorkout(1, 1, 'ME Upper', 100),
        createVolumeWorkout(2, 5, 'ME Upper', 100),
        createVolumeWorkout(3, 9, 'ME Upper', 100),
        createVolumeWorkout(4, 13, 'ME Upper', 100),
        createVolumeWorkout(5, 17, 'ME Upper', 120),
        createVolumeWorkout(6, 21, 'ME Upper', 110),
      ];

      const model = buildVolumeOverviewModel(
        createVolumeUserDataExport(6, multiBlockWorkouts, [
          {
            user_id: 'u1',
            exercise_name: 'Bench Press',
            one_rep_max: 150,
            unit: 'KG',
            created_at: new Date('2024-01-01T00:00:00.000Z'),
            updated_at: new Date('2024-01-01T00:00:00.000Z'),
          },
        ]),
        exerciseData,
        4,
        6,
        'LBS'
      );

      const maxEffort = model?.categories.find(category => category.type === 'Max Effort');
      expect(maxEffort?.sameWeekSlotSampleCount).toBeGreaterThanOrEqual(1);
      expect(maxEffort?.sameWeekSlotAverage).not.toBeNull();
      expect(buildWeeklyIntensitySeries(model!.weekVolumes, [
        {
          user_id: 'u1',
          exercise_name: 'Bench Press',
          one_rep_max: 150,
          unit: 'KG',
          created_at: new Date('2024-01-01T00:00:00.000Z'),
          updated_at: new Date('2024-01-01T00:00:00.000Z'),
        },
      ]).length).toBeGreaterThan(0);
    });

    it('flags overshoot on deload-like weeks when done exceeds plan', () => {
      const deloadWorkouts = [
        createVolumeWorkout(1, 1, 'ME Upper', 100),
        createVolumeWorkout(2, 5, 'ME Upper', 100),
        createVolumeWorkout(3, 9, 'ME Upper', 100),
        createVolumeWorkout(4, 13, 'ME Upper', 100),
        createVolumeWorkout(5, 17, 'ME Upper', 40, { performedWeight: 100 }),
      ];

      const model = buildVolumeOverviewModel(
        createVolumeUserDataExport(5, deloadWorkouts),
        exerciseData,
        4,
        5,
        'LBS'
      );

      const maxEffort = model?.categories.find(category => category.type === 'Max Effort');
      expect(maxEffort?.current).toBeGreaterThan(
        (maxEffort?.target ?? 0) * OVERSHOOT_RATIO
      );
      expect(maxEffort?.isOvershoot).toBe(true);
      expect(maxEffort?.status).toBe('overshoot');
    });
  });
});
