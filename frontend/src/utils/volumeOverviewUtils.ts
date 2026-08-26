import { calculateWorkoutProgress } from './progressUtils';
import type {
  Exercise,
  ProgramWithWorkouts,
  ProgrammedWorkoutWithStages,
  SetScheme,
  UserDataExport,
  UserOneRepMax,
} from '../api/types';
import { KG_TO_LBS, categorizeExerciseVolume, replaceUnderscoresWithSpaces } from '../common/utils';

export type VolumeStatus = 'under' | 'on_track' | 'exceeded' | 'no_volume' | 'overshoot';

export type VolumeCategory = 'Max Effort' | 'Dynamic Effort' | 'Accessory';

/** Conjugate mesocycle length in weeks. */
export const MESOCYCLE_WEEKS = 4;

/** Default plan-relative on-track floor (DE). */
export const DEFAULT_ON_TRACK_FLOOR = 0.85;

/** Overshoot threshold vs programmed plan. */
export const OVERSHOOT_RATIO = 1.15;

/** Plan vs W{n} avg ratio below which the week looks deload-like. */
export const DELOAD_PLAN_VS_AVG_RATIO = 0.85;

/** ACWR high-risk threshold (acute / chronic). */
export const ACWR_HIGH_THRESHOLD = 1.3;

/** Chronic window length in weeks for ACWR. */
export const ACWR_CHRONIC_WEEKS = 4;

export interface WeekVolumeTotals {
  weekNumber: number;
  maxEffortVolume: number;
  dynamicEffortVolume: number;
  accessoryVolume: number;
  totalVolume: number;
  maxEffortProgrammedVolume: number;
  dynamicEffortProgrammedVolume: number;
  accessoryProgrammedVolume: number;
  totalProgrammedVolume: number;
  performedSets: number;
  targetSets: number;
  completedWorkouts: number;
  plannedWorkouts: number;
  maxEffortPeakWeightLbs: number;
  maxEffortPeakReps: number;
  maxEffortPeakExerciseName: string | null;
}

export interface VolumeCategoryMetrics {
  type: VolumeCategory;
  current: number;
  target: number;
  hasTarget: boolean;
  sameWeekSlotAverage: number | null;
  sameWeekSlotSampleCount: number;
  status: VolumeStatus;
  priorPeriodDeltaPercent: number | null;
  onTrackFloor: number;
  poorEnd: number;
  okEnd: number;
  goodEnd: number;
  scaleMax: number;
  loggingIncomplete: boolean;
  isOvershoot: boolean;
}

export interface VolumeOverviewModel {
  categories: VolumeCategoryMetrics[];
  weekVolumes: WeekVolumeTotals[];
}

type SetSchemeVolumeFields = {
  performed_weight?: number | null;
  performed_rep_count?: number | null;
  target_weight?: number | null;
  target_rep_count?: number | null;
  band_weight_lbs?: number | null;
};

function getBandLbs(setScheme: SetSchemeVolumeFields): number {
  return typeof setScheme.band_weight_lbs === 'number' && setScheme.band_weight_lbs > 0
    ? setScheme.band_weight_lbs
    : 0;
}

function toDisplayVolume(
  weightKg: number,
  reps: number,
  bandLbs: number,
  preferredUnit: 'KG' | 'LBS'
): number {
  if (preferredUnit === 'KG') {
    return (weightKg + bandLbs / KG_TO_LBS) * reps;
  }
  return (weightKg * KG_TO_LBS + bandLbs) * reps;
}

/**
 * Maps a 1-based program week number to mesocycle slot 1–4.
 * Backend/frontend week numbers from day_number use Math.ceil and are 1-based;
 * week 0 (new program) is treated as slot 1.
 *
 * @param weekNumber Program week number (1-based in normal use)
 * @returns Mesocycle week slot in 1..MESOCYCLE_WEEKS
 */
export function getMesocycleWeekSlot(weekNumber: number): number {
  if (weekNumber <= 0) {
    return 1;
  }
  return ((weekNumber - 1) % MESOCYCLE_WEEKS) + 1;
}

/**
 * Category-specific on-track floor: tighter for ME, wider for accessory.
 *
 * @param category Volume category
 * @returns Floor multiplier against programmed plan
 */
export function getCategoryOnTrackFloor(category: VolumeCategory): number {
  switch (category) {
    case 'Max Effort':
      return 0.9;
    case 'Dynamic Effort':
      return DEFAULT_ON_TRACK_FLOOR;
    case 'Accessory':
      return 0.75;
  }
}

/**
 * Computes performed volume for a single set. Unlogged sets return zero volume.
 *
 * @param setScheme The set scheme with performed and target fields
 * @param preferredUnit Display unit preference for converting stored kg weights
 * @returns Object with volume in display units and whether performed data was used
 */
export function computeSetVolume(
  setScheme: SetSchemeVolumeFields,
  preferredUnit: 'KG' | 'LBS' = 'LBS'
): { volume: number; usedPerformed: boolean; hasPerformed: boolean; hasTarget: boolean } {
  const hasPerformedWeight =
    typeof setScheme.performed_weight === 'number' && setScheme.performed_weight > 0;
  const hasPerformedReps =
    typeof setScheme.performed_rep_count === 'number' && setScheme.performed_rep_count > 0;
  const hasPerformed = hasPerformedWeight && hasPerformedReps;

  const hasTargetWeight =
    typeof setScheme.target_weight === 'number' && setScheme.target_weight > 0;
  const hasTargetReps =
    typeof setScheme.target_rep_count === 'number' && setScheme.target_rep_count > 0;
  const hasTarget = hasTargetWeight && hasTargetReps;

  if (!hasPerformed) {
    return {
      volume: 0,
      usedPerformed: false,
      hasPerformed: false,
      hasTarget,
    };
  }

  return {
    volume: toDisplayVolume(
      setScheme.performed_weight as number,
      setScheme.performed_rep_count as number,
      getBandLbs(setScheme),
      preferredUnit
    ),
    usedPerformed: true,
    hasPerformed: true,
    hasTarget,
  };
}

/**
 * Computes programmed (target) volume for a single set.
 *
 * @param setScheme The set scheme with target fields
 * @param preferredUnit Display unit preference for converting stored kg weights
 * @returns Programmed volume in display units and whether target data exists
 */
export function computeProgrammedSetVolume(
  setScheme: SetSchemeVolumeFields,
  preferredUnit: 'KG' | 'LBS' = 'LBS'
): { volume: number; hasTarget: boolean } {
  const hasTargetWeight =
    typeof setScheme.target_weight === 'number' && setScheme.target_weight > 0;
  const hasTargetReps =
    typeof setScheme.target_rep_count === 'number' && setScheme.target_rep_count > 0;
  const hasTarget = hasTargetWeight && hasTargetReps;

  if (!hasTarget) {
    return { volume: 0, hasTarget: false };
  }

  return {
    volume: toDisplayVolume(
      setScheme.target_weight as number,
      setScheme.target_rep_count as number,
      getBandLbs(setScheme),
      preferredUnit
    ),
    hasTarget: true,
  };
}

/**
 * Formats a volume value with compact notation for dense KPI displays.
 *
 * @param volume Volume in the user's display unit
 * @param preferredUnit Unit label to append
 * @returns Compact formatted string such as "4.0k lbs"
 */
export function formatCompactVolume(volume: number, preferredUnit: 'KG' | 'LBS' = 'LBS'): string {
  const unit = preferredUnit === 'KG' ? 'kg' : 'lbs';
  const absolute = Math.abs(volume);

  if (absolute >= 1000) {
    const compacted = Math.round((volume / 1000) * 10) / 10;
    return `${compacted.toFixed(1)}k ${unit}`;
  }

  return `${Math.round(volume).toLocaleString()} ${unit}`;
}

/**
 * Resolves qualitative volume status for a current vs target comparison.
 *
 * @param current Current period volume
 * @param target Target volume for the period
 * @param options Logging gate, overshoot, and category floor overrides
 * @returns Status including empty/no-volume and overshoot states
 */
export function resolveVolumeStatus(
  current: number,
  target: number,
  options?: {
    loggingIncomplete?: boolean;
    isOvershoot?: boolean;
    onTrackFloor?: number;
  }
): VolumeStatus {
  if (options?.loggingIncomplete || current <= 0) {
    return 'no_volume';
  }
  if (options?.isOvershoot) {
    return 'overshoot';
  }
  if (target <= 0) {
    return 'on_track';
  }
  if (current > target) {
    return 'exceeded';
  }
  const floor = options?.onTrackFloor ?? DEFAULT_ON_TRACK_FLOOR;
  if (current >= target * floor) {
    return 'on_track';
  }
  return 'under';
}

/**
 * Computes acute:chronic workload ratio for a category.
 *
 * @param acute This week's performed volume
 * @param chronicWeeklyAverage Average performed volume over the chronic window
 * @returns Ratio or null when chronic average is unavailable
 */
export function computeAcwr(
  acute: number,
  chronicWeeklyAverage: number | null
): { ratio: number | null; high: boolean } {
  if (chronicWeeklyAverage == null || chronicWeeklyAverage <= 0) {
    return { ratio: null, high: false };
  }
  const ratio = Math.round((acute / chronicWeeklyAverage) * 100) / 100;
  return { ratio, high: ratio >= ACWR_HIGH_THRESHOLD };
}

export function getCategoryValue(week: WeekVolumeTotals, category: VolumeCategory): number {
  switch (category) {
    case 'Max Effort':
      return week.maxEffortVolume;
    case 'Dynamic Effort':
      return week.dynamicEffortVolume;
    case 'Accessory':
      return week.accessoryVolume;
  }
}

export function getProgrammedCategoryValue(
  week: WeekVolumeTotals,
  category: VolumeCategory
): number {
  switch (category) {
    case 'Max Effort':
      return week.maxEffortProgrammedVolume;
    case 'Dynamic Effort':
      return week.dynamicEffortProgrammedVolume;
    case 'Accessory':
      return week.accessoryProgrammedVolume;
  }
}

function sumCategory(weeks: WeekVolumeTotals[], category: VolumeCategory): number {
  return weeks.reduce((sum, week) => sum + getCategoryValue(week, category), 0);
}

function sumProgrammedCategory(weeks: WeekVolumeTotals[], category: VolumeCategory): number {
  return weeks.reduce((sum, week) => sum + getProgrammedCategoryValue(week, category), 0);
}

/**
 * Computes signed percent change between two values.
 *
 * @param current Current value
 * @param previous Prior value
 * @returns Rounded percent delta
 */
export function computePercentChange(current: number, previous: number): number {
  if (previous <= 0) {
    return current > 0 ? 100 : 0;
  }
  return Math.round(((current - previous) / previous) * 100);
}

/**
 * Resolves the active program from a user export.
 *
 * @param userDataExport Full user export
 * @returns Active program workouts container or null
 */
export function getActiveProgramFromExport(
  userDataExport: UserDataExport | null
): ProgramWithWorkouts | null {
  if (!userDataExport?.training_programs?.length) {
    return null;
  }
  return (
    userDataExport.training_programs.find(program => program.program.is_active) ||
    userDataExport.training_programs[0]
  );
}

export interface ProgramSetSchemeContext {
  workoutWithStages: ProgrammedWorkoutWithStages;
  workoutId: number;
  dayNumber: number;
  weekNumber: number;
  dayInWeek: number;
  workoutName: string;
  exerciseName: string;
  exerciseInfo: Exercise | undefined;
  setScheme: SetScheme;
}

export type ProgramSetSchemeFilter = {
  weekNumber?: number;
  exerciseName?: string;
};

/**
 * Visits every set scheme in a program with workout and exercise context.
 *
 * @param workouts Program workouts to traverse
 * @param exerciseData Exercise metadata map
 * @param workoutsPerWeek Program days per week
 * @param visit Callback invoked for each set scheme
 * @param filter Optional week or exercise filter
 */
export function forEachProgramSetScheme(
  workouts: ProgrammedWorkoutWithStages[],
  exerciseData: Map<string, Exercise>,
  workoutsPerWeek: number,
  visit: (entry: ProgramSetSchemeContext) => void,
  filter?: ProgramSetSchemeFilter
): void {
  if (workoutsPerWeek <= 0) {
    return;
  }

  workouts.forEach(workoutWithStages => {
    const dayNumber = workoutWithStages.workout.day_number;
    const weekNumber = Math.ceil(dayNumber / workoutsPerWeek);
    if (filter?.weekNumber != null && weekNumber !== filter.weekNumber) {
      return;
    }

    const dayInWeek = dayNumber - workoutsPerWeek * (weekNumber - 1);
    const workoutName = replaceUnderscoresWithSpaces(workoutWithStages.workout.name);

    workoutWithStages.stages.forEach(stage => {
      stage.exercises.forEach(exerciseWithSchemes => {
        const exerciseName = exerciseWithSchemes.exercise.exercise_name;
        if (filter?.exerciseName != null && exerciseName !== filter.exerciseName) {
          return;
        }

        const exerciseInfo = exerciseData.get(exerciseName);
        exerciseWithSchemes.set_schemes.forEach(setScheme => {
          visit({
            workoutWithStages,
            workoutId: workoutWithStages.workout.id,
            dayNumber,
            weekNumber,
            dayInWeek,
            workoutName,
            exerciseName,
            exerciseInfo,
            setScheme,
          });
        });
      });
    });
  });
}

/**
 * Maps a performed set to its ME/DE/Accessory category.
 *
 * @param exerciseInfo Exercise metadata
 * @param workoutName Workout display name
 * @param setScheme Set scheme with performed fields
 * @param preferredUnit Display unit
 * @returns Volume category or null when performed data is missing
 */
export function getVolumeCategoryForPerformedSet(
  exerciseInfo: Exercise | undefined,
  workoutName: string,
  setScheme: SetSchemeVolumeFields,
  preferredUnit: 'KG' | 'LBS' = 'LBS'
): VolumeCategory | null {
  const setVolume = computeSetVolume(setScheme, preferredUnit);
  if (!setVolume.hasPerformed) {
    return null;
  }

  const categorized = categorizeExerciseVolume(exerciseInfo, workoutName, setVolume.volume);
  if (categorized.maxEffortVolume > 0) {
    return 'Max Effort';
  }
  if (categorized.dynamicEffortVolume > 0) {
    return 'Dynamic Effort';
  }
  return 'Accessory';
}

/**
 * Average performed volume for the same mesocycle week slot across prior blocks.
 *
 * @param weekVolumes All program week totals (1-based week numbers)
 * @param currentWeek Active program week number
 * @param category Volume category
 * @returns Average and sample count from prior blocks only
 */
export function averageSameWeekSlotVolume(
  weekVolumes: WeekVolumeTotals[],
  currentWeek: number,
  category: VolumeCategory
): { average: number | null; sampleCount: number; slot: number } {
  const activeWeek = currentWeek > 0 ? currentWeek : 1;
  const slot = getMesocycleWeekSlot(activeWeek);
  const priorSameSlot = weekVolumes.filter(
    week => week.weekNumber < activeWeek && getMesocycleWeekSlot(week.weekNumber) === slot
  );

  if (priorSameSlot.length === 0) {
    return { average: null, sampleCount: 0, slot };
  }

  const total = sumCategory(priorSameSlot, category);
  return {
    average: Math.round(total / priorSameSlot.length),
    sampleCount: priorSameSlot.length,
    slot,
  };
}

/**
 * Average performed volume over the prior N weeks (chronic ACWR window).
 *
 * @param weekVolumes All program week totals
 * @param currentWeek Active week
 * @param category Volume category
 * @param windowWeeks Chronic window length
 * @returns Weekly average or null
 */
export function averageChronicVolume(
  weekVolumes: WeekVolumeTotals[],
  currentWeek: number,
  category: VolumeCategory,
  windowWeeks: number = ACWR_CHRONIC_WEEKS
): number | null {
  const activeWeek = currentWeek > 0 ? currentWeek : 1;
  const prior = weekVolumes.filter(
    week => week.weekNumber < activeWeek && week.weekNumber >= activeWeek - windowWeeks
  );
  if (prior.length === 0) {
    return null;
  }
  return Math.round(sumCategory(prior, category) / prior.length);
}

/**
 * Builds plan-relative bullet range endpoints and per-card scale max.
 * Four bands: poor → ok → good → overload, with five labeled boundaries.
 *
 * @param current Performed volume
 * @param target Programmed plan volume
 * @param sameWeekSlotAverage Optional W{n} avg
 * @param onTrackFloor Category on-track floor
 * @returns Range ends and scale max
 */
export function buildBulletScale(
  current: number,
  target: number,
  sameWeekSlotAverage: number | null,
  onTrackFloor: number
): { poorEnd: number; okEnd: number; goodEnd: number; scaleMax: number } {
  const hasTarget = target > 0;
  const goodEndCandidate = hasTarget ? Math.round(target * OVERSHOOT_RATIO) : 0;
  const references = [current, target, sameWeekSlotAverage ?? 0, goodEndCandidate, 1];
  const rawMax = Math.max(...references);
  const scaleMax = Math.max(1, Math.round(rawMax * 1.18));
  const poorEnd = hasTarget
    ? Math.round(target * onTrackFloor)
    : Math.round(scaleMax * onTrackFloor);
  const okEnd = hasTarget ? target : Math.round(scaleMax * 0.5);
  const goodEnd = hasTarget ? goodEndCandidate : Math.round(scaleMax * 0.75);

  const clampedPoor = Math.min(Math.max(poorEnd, 0), scaleMax);
  const clampedOk = Math.min(Math.max(okEnd, clampedPoor), scaleMax);
  const clampedGood = Math.min(Math.max(goodEnd, clampedOk), scaleMax);

  return {
    poorEnd: clampedPoor,
    okEnd: clampedOk,
    goodEnd: clampedGood,
    scaleMax,
  };
}

export type BulletAxisLabel = {
  value: number;
  fraction: number;
  text: string;
};

/**
 * Builds numeric axis labels for all distinct color-band boundaries.
 * Boundaries: 0, poor/ok, ok/good, good/overload, scale max.
 *
 * @param params Scale and band boundary values for one category card
 * @returns Labels sorted left-to-right on a single line
 */
export function buildBulletAxisLabels(params: {
  scaleMax: number;
  poorEnd: number;
  okEnd: number;
  goodEnd: number;
  preferredUnit: 'KG' | 'LBS';
}): BulletAxisLabel[] {
  const { scaleMax, poorEnd, okEnd, goodEnd, preferredUnit } = params;

  const formatTick = (volume: number): string =>
    formatCompactVolume(volume, preferredUnit).replace(/ (lbs|kg)$/, '');

  const toFraction = (value: number): number =>
    scaleMax > 0 ? Math.min(1, Math.max(0, value / scaleMax)) : 0;

  const values: number[] = [0];
  if (poorEnd > 0) {
    values.push(poorEnd);
  }
  if (okEnd > 0) {
    values.push(okEnd);
  }
  if (goodEnd > 0) {
    values.push(goodEnd);
  }
  if (scaleMax > 0) {
    values.push(scaleMax);
  }

  const unique: number[] = [];
  values.forEach(value => {
    if (!unique.includes(value)) {
      unique.push(value);
    }
  });
  unique.sort((a, b) => a - b);

  return unique.map(value => ({
    value,
    fraction: toFraction(value),
    text: value === 0 ? '0' : formatTick(value),
  }));
}

/**
 * Aggregates ME/DE/Accessory performed and programmed volume totals for each program week.
 *
 * @param workouts Workout hierarchy for the active program
 * @param exerciseData Map of exercise metadata for categorization
 * @param workoutsPerWeek Program days per week
 * @param preferredUnit Display unit for volume totals
 * @returns Weekly volume totals sorted ascending by week number
 */
export function buildWeekVolumeTotals(
  workouts: ProgrammedWorkoutWithStages[],
  exerciseData: Map<string, Exercise>,
  workoutsPerWeek: number,
  preferredUnit: 'KG' | 'LBS' = 'LBS'
): WeekVolumeTotals[] {
  const weekMap = new Map<number, WeekVolumeTotals>();
  const visitedWorkouts = new Set<number>();

  forEachProgramSetScheme(workouts, exerciseData, workoutsPerWeek, entry => {
    const { workoutWithStages, weekNumber, workoutName, exerciseName, exerciseInfo, setScheme } =
      entry;

    if (!weekMap.has(weekNumber)) {
      weekMap.set(weekNumber, {
        weekNumber,
        maxEffortVolume: 0,
        dynamicEffortVolume: 0,
        accessoryVolume: 0,
        totalVolume: 0,
        maxEffortProgrammedVolume: 0,
        dynamicEffortProgrammedVolume: 0,
        accessoryProgrammedVolume: 0,
        totalProgrammedVolume: 0,
        performedSets: 0,
        targetSets: 0,
        completedWorkouts: 0,
        plannedWorkouts: 0,
        maxEffortPeakWeightLbs: 0,
        maxEffortPeakReps: 0,
        maxEffortPeakExerciseName: null,
      });
    }

    const weekTotals = weekMap.get(weekNumber)!;

    if (!visitedWorkouts.has(workoutWithStages.workout.id)) {
      visitedWorkouts.add(workoutWithStages.workout.id);
      weekTotals.plannedWorkouts += 1;
      if (calculateWorkoutProgress(workoutWithStages).status === 'completed') {
        weekTotals.completedWorkouts += 1;
      }
    }

    const programmed = computeProgrammedSetVolume(setScheme, preferredUnit);
    if (programmed.hasTarget) {
      weekTotals.targetSets += 1;
      const programmedCategorized = categorizeExerciseVolume(
        exerciseInfo,
        workoutName,
        programmed.volume
      );
      weekTotals.maxEffortProgrammedVolume += programmedCategorized.maxEffortVolume;
      weekTotals.dynamicEffortProgrammedVolume += programmedCategorized.dynamicEffortVolume;
      weekTotals.accessoryProgrammedVolume += programmedCategorized.accessoryVolume;
      weekTotals.totalProgrammedVolume += programmed.volume;
    }

    const setVolume = computeSetVolume(setScheme, preferredUnit);
    if (setVolume.hasPerformed) {
      weekTotals.performedSets += 1;
      const categorized = categorizeExerciseVolume(exerciseInfo, workoutName, setVolume.volume);
      weekTotals.maxEffortVolume += categorized.maxEffortVolume;
      weekTotals.dynamicEffortVolume += categorized.dynamicEffortVolume;
      weekTotals.accessoryVolume += categorized.accessoryVolume;
      weekTotals.totalVolume += setVolume.volume;

      if (categorized.maxEffortVolume > 0 && setScheme.performed_weight) {
        const peakWeightLbs = setScheme.performed_weight * KG_TO_LBS;
        const performedReps = setScheme.performed_rep_count ?? 0;
        if (peakWeightLbs > weekTotals.maxEffortPeakWeightLbs) {
          weekTotals.maxEffortPeakWeightLbs = peakWeightLbs;
          weekTotals.maxEffortPeakReps = performedReps;
          weekTotals.maxEffortPeakExerciseName = exerciseName;
        }
      }
    }
  });

  return Array.from(weekMap.values())
    .map(week => ({
      ...week,
      maxEffortVolume: Math.round(week.maxEffortVolume),
      dynamicEffortVolume: Math.round(week.dynamicEffortVolume),
      accessoryVolume: Math.round(week.accessoryVolume),
      totalVolume: Math.round(week.totalVolume),
      maxEffortProgrammedVolume: Math.round(week.maxEffortProgrammedVolume),
      dynamicEffortProgrammedVolume: Math.round(week.dynamicEffortProgrammedVolume),
      accessoryProgrammedVolume: Math.round(week.accessoryProgrammedVolume),
      totalProgrammedVolume: Math.round(week.totalProgrammedVolume),
      maxEffortPeakWeightLbs: Math.round(week.maxEffortPeakWeightLbs),
    }))
    .sort((a, b) => a.weekNumber - b.weekNumber);
}

function getThisWeekWindow(
  weekVolumes: WeekVolumeTotals[],
  currentWeek: number
): { current: WeekVolumeTotals[]; prior: WeekVolumeTotals[] } {
  const byWeek = new Map(weekVolumes.map(week => [week.weekNumber, week]));
  const maxWeek = weekVolumes.length
    ? Math.max(...weekVolumes.map(week => week.weekNumber))
    : currentWeek;
  const activeWeek = currentWeek || maxWeek;
  const current = byWeek.has(activeWeek) ? [byWeek.get(activeWeek)!] : [];
  const prior = byWeek.has(activeWeek - 1) ? [byWeek.get(activeWeek - 1)!] : [];
  return { current, prior };
}

function findOneRepMaxLbs(
  userOneRepMaxes: UserOneRepMax[] | undefined,
  exerciseName: string | null
): number | null {
  if (!userOneRepMaxes?.length || !exerciseName) {
    return null;
  }
  const match = userOneRepMaxes.find(entry => entry.exercise_name === exerciseName);
  if (!match) {
    return null;
  }
  if (match.unit === 'LBS') {
    return match.one_rep_max;
  }
  return match.one_rep_max * KG_TO_LBS;
}

export type VolumeTrendPoint = { x: string; y: number };

/**
 * Computes ACWR ratio for one program week, or null when no chronic baseline exists.
 *
 * @param weekVolumes Program week volume totals
 * @param weekNumber Week to evaluate
 * @param category Volume category
 * @returns Acute:chronic ratio or null on cold start
 */
export function getWeeklyAcwrRatio(
  weekVolumes: WeekVolumeTotals[],
  weekNumber: number,
  category: VolumeCategory
): number | null {
  const week = weekVolumes.find(entry => entry.weekNumber === weekNumber);
  if (!week) {
    return null;
  }
  const chronic = averageChronicVolume(weekVolumes, weekNumber, category);
  const acute = getCategoryValue(week, category);
  return computeAcwr(acute, chronic).ratio;
}

/**
 * Builds weekly ACWR points for the expanded trend dialog.
 * Weeks without a chronic baseline plot at 0 so the line connects to the first real ratio.
 *
 * @param weekVolumes Program week volume totals
 * @param category Volume category
 * @returns One point per program week aligned with volume weeks
 */
export function buildWeeklyAcwrSeries(
  weekVolumes: WeekVolumeTotals[],
  category: VolumeCategory
): VolumeTrendPoint[] {
  return weekVolumes.map(week => {
    const ratio = getWeeklyAcwrRatio(weekVolumes, week.weekNumber, category);
    return {
      x: `W${week.weekNumber}`,
      y: ratio ?? 0,
    };
  });
}

/**
 * Builds weekly ME intensity (% of 1RM) points for the expanded trend dialog.
 *
 * @param weekVolumes Program week volume totals
 * @param userOneRepMaxes User 1RM records
 * @returns Points for weeks with a peak ME load and matching 1RM
 */
export function buildWeeklyIntensitySeries(
  weekVolumes: WeekVolumeTotals[],
  userOneRepMaxes: UserOneRepMax[] | undefined
): VolumeTrendPoint[] {
  const points: VolumeTrendPoint[] = [];
  weekVolumes.forEach(week => {
    if (week.maxEffortPeakWeightLbs <= 0 || !week.maxEffortPeakExerciseName) {
      return;
    }
    const oneRepMaxLbs = findOneRepMaxLbs(userOneRepMaxes, week.maxEffortPeakExerciseName);
    if (oneRepMaxLbs == null || oneRepMaxLbs <= 0) {
      return;
    }
    points.push({
      x: `W${week.weekNumber}`,
      y: Math.round((week.maxEffortPeakWeightLbs / oneRepMaxLbs) * 100),
    });
  });
  return points;
}

function buildCategoryMetrics(
  category: VolumeCategory,
  currentWeeks: WeekVolumeTotals[],
  priorWeeks: WeekVolumeTotals[],
  weekVolumes: WeekVolumeTotals[],
  currentWeek: number
): VolumeCategoryMetrics {
  const current = Math.round(sumCategory(currentWeeks, category));
  const target = Math.round(sumProgrammedCategory(currentWeeks, category));
  const hasTarget = target > 0;
  const slotAverage = averageSameWeekSlotVolume(weekVolumes, currentWeek, category);
  const onTrackFloor = getCategoryOnTrackFloor(category);
  const sameWeekSlotAverage = slotAverage.average;
  const isDeloadLike =
    hasTarget &&
    sameWeekSlotAverage != null &&
    sameWeekSlotAverage > 0 &&
    target < sameWeekSlotAverage * DELOAD_PLAN_VS_AVG_RATIO;
  const isOvershoot =
    hasTarget && current > target * OVERSHOOT_RATIO && (isDeloadLike || current > target * 1.2);

  const performedSets = currentWeeks.reduce((sum, week) => sum + week.performedSets, 0);
  const targetSets = currentWeeks.reduce((sum, week) => sum + week.targetSets, 0);
  const completedWorkouts = currentWeeks.reduce((sum, week) => sum + week.completedWorkouts, 0);
  const loggingIncomplete =
    current <= 0 && targetSets > 0 && performedSets === 0 && completedWorkouts === 0;

  const { poorEnd, okEnd, goodEnd, scaleMax } = buildBulletScale(
    current,
    target,
    sameWeekSlotAverage,
    onTrackFloor
  );

  const priorTotal = sumCategory(priorWeeks, category);

  const status = resolveVolumeStatus(current, target, {
    loggingIncomplete,
    isOvershoot,
    onTrackFloor,
  });

  return {
    type: category,
    current,
    target,
    hasTarget,
    sameWeekSlotAverage,
    sameWeekSlotSampleCount: slotAverage.sampleCount,
    status,
    priorPeriodDeltaPercent:
      priorWeeks.length > 0 ? computePercentChange(current, priorTotal) : null,
    onTrackFloor,
    poorEnd,
    okEnd,
    goodEnd,
    scaleMax,
    loggingIncomplete,
    isOvershoot,
  };
}

/**
 * Builds the full volume overview model for KPI cards (this-week window).
 *
 * @param userDataExport Full user export containing workouts and 1RMs
 * @param exerciseData Exercise metadata map
 * @param workoutsPerWeek Program days per week
 * @param currentWeek Current program week number
 * @param preferredUnit Display unit
 * @returns Overview model ready for rendering
 */
export function buildVolumeOverviewModel(
  userDataExport: UserDataExport | null,
  exerciseData: Map<string, Exercise>,
  workoutsPerWeek: number,
  currentWeek: number,
  preferredUnit: 'KG' | 'LBS' = 'LBS'
): VolumeOverviewModel | null {
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

  if (weekVolumes.length === 0) {
    return null;
  }

  const { current, prior } = getThisWeekWindow(weekVolumes, currentWeek);
  const categories: VolumeCategoryMetrics[] = (
    ['Max Effort', 'Dynamic Effort', 'Accessory'] as VolumeCategory[]
  ).map(category => buildCategoryMetrics(category, current, prior, weekVolumes, currentWeek));

  return {
    categories,
    weekVolumes,
  };
}
