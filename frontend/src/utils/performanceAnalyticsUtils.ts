import type { Exercise, UserDataExport, UserOneRepMax } from '../api/types';
import { KG_TO_LBS } from '../common/utils';
import type { VolumeCategory } from './volumeOverviewUtils';
import {
  buildWeekVolumeTotals,
  computePercentChange,
  forEachProgramSetScheme,
  formatCompactVolume,
  getActiveProgramFromExport,
  getVolumeCategoryForPerformedSet,
  type WeekVolumeTotals,
} from './volumeOverviewUtils';

export interface ExercisePerformanceSession {
  weekNumber: number;
  dayNumber: number;
  workoutName: string;
  workoutId: number;
  category: VolumeCategory;
  topWeightKg: number;
  topReps: number;
}

export interface MeTopSetResult {
  weekNumber: number;
  dayInWeek: number;
  workoutName: string;
  workoutId: number;
  exerciseName: string;
  weightKg: number;
  reps: number;
}

export interface WeekVolumeSnapshot {
  maxEffort: number;
  dynamicEffort: number;
  accessory: number;
  total: number;
}

export interface WeekKeyResults {
  weekNumber: number;
  meTopSets: MeTopSetResult[];
  volume: WeekVolumeSnapshot;
  priorWeekVolume: WeekVolumeSnapshot | null;
  volumeDeltaPercent: WeekVolumeSnapshot | null;
}

export interface OneRepMaxWorkoutTrendPoint {
  weekNumber: number;
  weekLabel: string;
  peakWeightKg: number;
}

interface PerformedSetContext {
  exerciseName: string;
  workoutName: string;
  workoutId: number;
  dayNumber: number;
  weekNumber: number;
  dayInWeek: number;
  weightKg: number;
  reps: number;
  category: VolumeCategory;
}

function toVolumeSnapshot(week: WeekVolumeTotals): WeekVolumeSnapshot {
  return {
    maxEffort: week.maxEffortVolume,
    dynamicEffort: week.dynamicEffortVolume,
    accessory: week.accessoryVolume,
    total: week.totalVolume,
  };
}

function comparePerformedSets(
  left: { weightKg: number; reps: number },
  right: { weightKg: number; reps: number }
): number {
  if (left.weightKg !== right.weightKg) {
    return left.weightKg - right.weightKg;
  }
  return left.reps - right.reps;
}

function collectPerformedSets(
  workouts: Parameters<typeof forEachProgramSetScheme>[0],
  exerciseData: Map<string, Exercise>,
  workoutsPerWeek: number,
  preferredUnit: 'KG' | 'LBS',
  filter?: {
    weekNumber?: number;
    exerciseName?: string;
    category?: VolumeCategory;
  }
): PerformedSetContext[] {
  const results: PerformedSetContext[] = [];

  forEachProgramSetScheme(
    workouts,
    exerciseData,
    workoutsPerWeek,
    entry => {
      const category = getVolumeCategoryForPerformedSet(
        entry.exerciseInfo,
        entry.workoutName,
        entry.setScheme,
        preferredUnit
      );
      if (category == null) {
        return;
      }
      if (filter?.category != null && category !== filter.category) {
        return;
      }

      results.push({
        exerciseName: entry.exerciseName,
        workoutName: entry.workoutName,
        workoutId: entry.workoutId,
        dayNumber: entry.dayNumber,
        weekNumber: entry.weekNumber,
        dayInWeek: entry.dayInWeek,
        weightKg: entry.setScheme.performed_weight as number,
        reps: entry.setScheme.performed_rep_count as number,
        category,
      });
    },
    {
      weekNumber: filter?.weekNumber,
      exerciseName: filter?.exerciseName,
    }
  );

  return results;
}

function pickTopSetForSession(
  sets: PerformedSetContext[]
): { weightKg: number; reps: number } | null {
  if (sets.length === 0) {
    return null;
  }
  return sets.reduce(
    (best, current) =>
      comparePerformedSets(current, best) > 0
        ? { weightKg: current.weightKg, reps: current.reps }
        : best,
    { weightKg: sets[0].weightKg, reps: sets[0].reps }
  );
}

/**
 * Builds chronological performance history for one exercise from logged workouts.
 *
 * @param userDataExport Full user export containing program workouts
 * @param exerciseName Exercise to summarize
 * @param exerciseData Exercise metadata map
 * @param workoutsPerWeek Program days per week
 * @param preferredUnit Display unit for volume categorization
 * @returns One entry per workout session with a logged top set
 */
export function buildExercisePerformanceHistory(
  userDataExport: UserDataExport | null,
  exerciseName: string,
  exerciseData: Map<string, Exercise>,
  workoutsPerWeek: number,
  preferredUnit: 'KG' | 'LBS' = 'LBS'
): ExercisePerformanceSession[] {
  const activeProgram = getActiveProgramFromExport(userDataExport);
  if (!activeProgram?.workouts?.length || workoutsPerWeek <= 0) {
    return [];
  }

  const performedSets = collectPerformedSets(
    activeProgram.workouts,
    exerciseData,
    workoutsPerWeek,
    preferredUnit,
    { exerciseName }
  );

  const sessionMap = new Map<number, PerformedSetContext[]>();
  performedSets.forEach(set => {
    const existing = sessionMap.get(set.workoutId) ?? [];
    existing.push(set);
    sessionMap.set(set.workoutId, existing);
  });

  const sessions: ExercisePerformanceSession[] = [];
  sessionMap.forEach(sets => {
    const topSet = pickTopSetForSession(sets);
    if (!topSet) {
      return;
    }
    const sample = sets[0];
    sessions.push({
      weekNumber: sample.weekNumber,
      dayNumber: sample.dayNumber,
      workoutName: sample.workoutName,
      workoutId: sample.workoutId,
      category: sample.category,
      topWeightKg: topSet.weightKg,
      topReps: topSet.reps,
    });
  });

  return sessions.sort((left, right) => {
    if (left.weekNumber !== right.weekNumber) {
      return left.weekNumber - right.weekNumber;
    }
    return left.dayNumber - right.dayNumber;
  });
}

/**
 * Builds ME top sets and week-over-week volume comparison for a program week.
 *
 * @param userDataExport Full user export containing program workouts
 * @param exerciseData Exercise metadata map
 * @param workoutsPerWeek Program days per week
 * @param weekNumber Target week number
 * @param preferredUnit Display unit for volume totals
 * @returns Week key results or null when program data is unavailable
 */
export function buildWeekKeyResults(
  userDataExport: UserDataExport | null,
  exerciseData: Map<string, Exercise>,
  workoutsPerWeek: number,
  weekNumber: number,
  preferredUnit: 'KG' | 'LBS' = 'LBS'
): WeekKeyResults | null {
  const activeProgram = getActiveProgramFromExport(userDataExport);
  if (!activeProgram?.workouts?.length || workoutsPerWeek <= 0) {
    return null;
  }

  const weekVolumes = buildWeekVolumeTotals(
    activeProgram.workouts,
    exerciseData,
    workoutsPerWeek,
    preferredUnit
  );
  const currentWeek = weekVolumes.find(week => week.weekNumber === weekNumber);
  const priorWeek = weekVolumes.find(week => week.weekNumber === weekNumber - 1);
  if (!currentWeek) {
    return null;
  }

  const meSets = collectPerformedSets(
    activeProgram.workouts,
    exerciseData,
    workoutsPerWeek,
    preferredUnit,
    { weekNumber, category: 'Max Effort' }
  );

  const workoutExerciseMap = new Map<string, PerformedSetContext>();
  meSets.forEach(set => {
    const key = `${set.workoutId}|${set.exerciseName}`;
    const existing = workoutExerciseMap.get(key);
    if (!existing || comparePerformedSets(set, existing) > 0) {
      workoutExerciseMap.set(key, set);
    }
  });

  const meTopSets = Array.from(workoutExerciseMap.values())
    .map(set => ({
      weekNumber: set.weekNumber,
      dayInWeek: set.dayInWeek,
      workoutName: set.workoutName,
      workoutId: set.workoutId,
      exerciseName: set.exerciseName,
      weightKg: set.weightKg,
      reps: set.reps,
    }))
    .sort((left, right) => {
      if (left.dayInWeek !== right.dayInWeek) {
        return left.dayInWeek - right.dayInWeek;
      }
      return left.exerciseName.localeCompare(right.exerciseName);
    });

  const volume = toVolumeSnapshot(currentWeek);
  const priorWeekVolume = priorWeek ? toVolumeSnapshot(priorWeek) : null;
  const volumeDeltaPercent = priorWeekVolume
    ? {
        maxEffort: computePercentChange(volume.maxEffort, priorWeekVolume.maxEffort),
        dynamicEffort: computePercentChange(volume.dynamicEffort, priorWeekVolume.dynamicEffort),
        accessory: computePercentChange(volume.accessory, priorWeekVolume.accessory),
        total: computePercentChange(volume.total, priorWeekVolume.total),
      }
    : null;

  return {
    weekNumber,
    meTopSets,
    volume,
    priorWeekVolume,
    volumeDeltaPercent,
  };
}

/**
 * Builds weekly peak logged weight points for one exercise across the active program.
 *
 * @param userDataExport Full user export containing program workouts
 * @param exerciseName Exercise to chart
 * @param exerciseData Exercise metadata map
 * @param workoutsPerWeek Program days per week
 * @returns Trend points sorted by week number
 */
export function buildOneRepMaxWorkoutTrend(
  userDataExport: UserDataExport | null,
  exerciseName: string,
  exerciseData: Map<string, Exercise>,
  workoutsPerWeek: number
): OneRepMaxWorkoutTrendPoint[] {
  const activeProgram = getActiveProgramFromExport(userDataExport);
  if (!activeProgram?.workouts?.length || workoutsPerWeek <= 0) {
    return [];
  }

  const performedSets = collectPerformedSets(
    activeProgram.workouts,
    exerciseData,
    workoutsPerWeek,
    'LBS',
    { exerciseName }
  );

  const peakByWeek = new Map<number, number>();
  performedSets.forEach(set => {
    const currentPeak = peakByWeek.get(set.weekNumber) ?? 0;
    if (set.weightKg > currentPeak) {
      peakByWeek.set(set.weekNumber, set.weightKg);
    }
  });

  return Array.from(peakByWeek.entries())
    .sort((left, right) => left[0] - right[0])
    .map(([weekNumber, peakWeightKg]) => ({
      weekNumber,
      weekLabel: `W${weekNumber}`,
      peakWeightKg,
    }));
}

/**
 * Builds weekly peak logged weight trends for all exercises that appear in the active program.
 *
 * @param userDataExport Full user export containing program workouts
 * @param exerciseData Exercise metadata map
 * @param workoutsPerWeek Program days per week
 * @returns Map of exercise name to trend points
 */
export function buildAllExerciseWorkoutTrends(
  userDataExport: UserDataExport | null,
  exerciseData: Map<string, Exercise>,
  workoutsPerWeek: number
): Map<string, OneRepMaxWorkoutTrendPoint[]> {
  const trends = new Map<string, OneRepMaxWorkoutTrendPoint[]>();
  const activeProgram = getActiveProgramFromExport(userDataExport);
  if (!activeProgram?.workouts?.length || workoutsPerWeek <= 0) {
    return trends;
  }

  const exerciseNames = new Set<string>();
  forEachProgramSetScheme(activeProgram.workouts, exerciseData, workoutsPerWeek, entry => {
    exerciseNames.add(entry.exerciseName);
  });

  exerciseNames.forEach(exerciseName => {
    const points = buildOneRepMaxWorkoutTrend(
      userDataExport,
      exerciseName,
      exerciseData,
      workoutsPerWeek
    );
    if (points.length > 0) {
      trends.set(exerciseName, points);
    }
  });

  return trends;
}

/**
 * Week-over-week volume delta display tone.
 */
export type VolumeDeltaTone = 'positive' | 'negative' | 'flat' | 'no_prior' | 'not_logged';

/**
 * Classifies a week-over-week volume change for display styling.
 *
 * @param currentVolume Logged volume for the current week
 * @param priorVolume Logged volume for the prior week, if any
 * @param deltaPercent Signed percent change, if comparable
 * @returns Semantic tone for labels and colors
 */
export function getVolumeDeltaTone(
  currentVolume: number,
  priorVolume: number | null | undefined,
  deltaPercent: number | null | undefined
): VolumeDeltaTone {
  if (priorVolume == null || deltaPercent == null) {
    return 'no_prior';
  }
  if (currentVolume <= 0) {
    return 'not_logged';
  }
  if (deltaPercent >= -5 && deltaPercent <= 5) {
    return 'flat';
  }
  if (deltaPercent > 5) {
    return 'positive';
  }
  return 'negative';
}

/**
 * Maps a volume delta tone to a Congen theme color token.
 *
 * @param tone Volume delta tone
 * @returns CSS color variable
 */
export function getVolumeDeltaColor(tone: VolumeDeltaTone): string {
  switch (tone) {
    case 'positive':
      return 'var(--game-success)';
    case 'negative':
      return 'var(--game-warning)';
    case 'flat':
      return 'var(--game-cyan)';
    case 'no_prior':
    case 'not_logged':
      return 'var(--game-white-muted)';
  }
}

/**
 * Formats a volume delta label with first-week and not-logged copy.
 *
 * @param tone Volume delta tone
 * @param deltaPercent Signed percent change
 * @returns Display label
 */
export function formatVolumeDeltaLabel(
  tone: VolumeDeltaTone,
  deltaPercent: number | null | undefined
): string {
  if (tone === 'no_prior') {
    return 'First week — no prior comparison';
  }
  if (tone === 'not_logged') {
    return 'Not logged';
  }
  return formatVolumeDeltaPercent(deltaPercent);
}

/**
 * Formats prior-week volume context for a category row.
 *
 * @param priorVolume Prior week volume
 * @param preferredUnit Display unit
 * @returns Caption such as "vs 3.6k lbs prior week"
 */
export function formatVolumePriorComparison(
  priorVolume: number | null | undefined,
  preferredUnit: 'KG' | 'LBS'
): string | null {
  if (priorVolume == null) {
    return null;
  }
  return `vs ${formatCategoryVolume(priorVolume, preferredUnit)} prior week`;
}

/**
 * Computes ME set intensity as a percent of stored 1RM when available.
 *
 * @param weightKg Logged top-set weight in kilograms
 * @param exerciseName Exercise name
 * @param userOneRepMaxes User 1RM records
 * @returns Rounded intensity percent or null
 */
export function computeMeSetIntensityPercent(
  weightKg: number,
  exerciseName: string,
  userOneRepMaxes: UserOneRepMax[] | undefined
): number | null {
  if (!userOneRepMaxes?.length || weightKg <= 0) {
    return null;
  }
  const match = userOneRepMaxes.find(entry => entry.exercise_name === exerciseName);
  if (!match || match.one_rep_max <= 0) {
    return null;
  }
  const oneRepMaxKg =
    match.unit === 'KG' ? match.one_rep_max : match.one_rep_max / KG_TO_LBS;
  return Math.round((weightKg / oneRepMaxKg) * 100);
}

export function formatVolumeDeltaPercent(deltaPercent: number | null | undefined): string {
  if (deltaPercent == null) {
    return '—';
  }
  if (deltaPercent > 0) {
    return `+${deltaPercent}%`;
  }
  return `${deltaPercent}%`;
}

/**
 * Formats a category volume total with unit label.
 *
 * @param volume Volume amount
 * @param preferredUnit Display unit
 * @returns Compact formatted volume
 */
export function formatCategoryVolume(volume: number, preferredUnit: 'KG' | 'LBS'): string {
  return formatCompactVolume(volume, preferredUnit);
}
