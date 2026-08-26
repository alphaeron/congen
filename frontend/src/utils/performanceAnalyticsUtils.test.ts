import {
  buildExercisePerformanceHistory,
  buildOneRepMaxWorkoutTrend,
  buildWeekKeyResults,
  computeMeSetIntensityPercent,
  formatVolumeDeltaLabel,
  formatVolumeDeltaPercent,
  getVolumeDeltaColor,
  getVolumeDeltaTone,
} from './performanceAnalyticsUtils';
import {
  createVolumeExerciseData,
  createVolumeUserDataExport,
  createVolumeWorkout,
} from '../testUtils/volumeOverviewFixtures';

describe('performanceAnalyticsUtils', () => {
  const exerciseData = createVolumeExerciseData();

  it('builds exercise performance history sorted by week and day', () => {
    const workouts = [
      createVolumeWorkout(1, 1, 'ME Upper Day', 100, { performedWeight: 100 }),
      createVolumeWorkout(2, 4, 'ME Upper Day', 105, { performedWeight: 105 }),
    ];
    const userData = createVolumeUserDataExport(2, workouts);

    const history = buildExercisePerformanceHistory(
      userData,
      'Bench Press',
      exerciseData,
      3,
      'LBS'
    );

    expect(history).toHaveLength(2);
    expect(history[0].weekNumber).toBe(1);
    expect(history[1].weekNumber).toBe(2);
    expect(history[0].category).toBe('Max Effort');
    expect(history[0].topWeightKg).toBe(100);
    expect(history[1].topReps).toBe(5);
  });

  it('builds week key results with ME top sets and volume deltas', () => {
    const workouts = [
      createVolumeWorkout(1, 1, 'ME Upper Day', 100, { performedWeight: 100 }),
      createVolumeWorkout(2, 4, 'ME Upper Day', 110, { performedWeight: 110 }),
    ];
    const userData = createVolumeUserDataExport(2, workouts);

    const weekTwo = buildWeekKeyResults(userData, exerciseData, 3, 2, 'LBS');
    expect(weekTwo).not.toBeNull();
    expect(weekTwo?.meTopSets).toHaveLength(1);
    expect(weekTwo?.meTopSets[0].exerciseName).toBe('Bench Press');
    expect(weekTwo?.meTopSets[0].weightKg).toBe(110);
    expect(weekTwo?.volume.total).toBeGreaterThan(0);
    expect(weekTwo?.priorWeekVolume?.total).toBeGreaterThan(0);
    expect(weekTwo?.volumeDeltaPercent?.total).toBeGreaterThan(0);
  });

  it('builds weekly peak logged weight trend for an exercise', () => {
    const workouts = [
      createVolumeWorkout(1, 1, 'ME Upper Day', 100, { performedWeight: 100 }),
      createVolumeWorkout(2, 2, 'ME Upper Day', 105, { performedWeight: 105 }),
      createVolumeWorkout(3, 4, 'ME Upper Day', 110, { performedWeight: 110 }),
    ];
    const userData = createVolumeUserDataExport(2, workouts);

    const trend = buildOneRepMaxWorkoutTrend(userData, 'Bench Press', exerciseData, 3);
    expect(trend).toEqual([
      { weekNumber: 1, weekLabel: 'W1', peakWeightKg: 105 },
      { weekNumber: 2, weekLabel: 'W2', peakWeightKg: 110 },
    ]);
  });

  it('formats volume delta strings', () => {
    expect(formatVolumeDeltaPercent(null)).toBe('—');
    expect(formatVolumeDeltaPercent(12)).toBe('+12%');
    expect(formatVolumeDeltaPercent(-8)).toBe('-8%');
  });

  it('classifies volume delta tones and labels', () => {
    expect(getVolumeDeltaTone(1200, 1000, 20)).toBe('positive');
    expect(getVolumeDeltaTone(800, 1000, -20)).toBe('negative');
    expect(getVolumeDeltaTone(1000, 980, 2)).toBe('flat');
    expect(getVolumeDeltaTone(0, 1000, -100)).toBe('not_logged');
    expect(getVolumeDeltaTone(1000, null, null)).toBe('no_prior');
    expect(formatVolumeDeltaLabel('no_prior', null)).toBe('First week — no prior comparison');
    expect(formatVolumeDeltaLabel('not_logged', -100)).toBe('Not logged');
    expect(getVolumeDeltaColor('positive')).toBe('var(--game-success)');
  });

  it('computes ME intensity percent from stored 1RM', () => {
    const intensity = computeMeSetIntensityPercent(110, 'Bench Press', [
      {
        exercise_name: 'Bench Press',
        one_rep_max: 220,
        unit: 'KG',
        created_at: new Date(),
        updated_at: new Date(),
      },
    ]);
    expect(intensity).toBe(50);
  });
});
