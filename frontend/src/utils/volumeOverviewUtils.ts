import { calculateWorkoutProgress } from './progressUtils';
import type {
  Exercise,
  ProgramWithWorkouts,
  ProgrammedWorkoutWithStages,
  UserDataExport,
} from '../api/types';
import { KG_TO_LBS, categorizeExerciseVolume, replaceUnderscoresWithSpaces } from '../common/utils';

export type VolumePeriod = 'this_week' | 'last_4_weeks' | 'program';

export type VolumeStatus = 'under' | 'on_track' | 'exceeded' | 'no_volume';

export type VolumeCategory = 'Max Effort' | 'Dynamic Effort' | 'Accessory';

export interface WeekVolumeTotals {
  weekNumber: number;
  maxEffortVolume: number;
  dynamicEffortVolume: number;
  accessoryVolume: number;
  totalVolume: number;
  performedSets: number;
  targetSets: number;
  completedWorkouts: number;
  plannedWorkouts: number;
}

export interface VolumeCategoryMetrics {
  type: VolumeCategory;
  current: number;
  target: number;
  hasBaseline: boolean;
  status: VolumeStatus;
  deltaAbsolute: number;
  deltaPercent: number;
  priorPeriodDeltaPercent: number | null;
  emptyMessage: string | null;
}

export interface VolumePeriodSummary {
  sessionsCompleted: number;
  sessionsPlanned: number;
  totalVolume: number;
  setsCompleted: number;
  setsPlanned: number;
  volumeDeltaPercent: number | null;
  recentPrCount: number;
  recentPrLabel: string | null;
}

export interface VolumeOverviewModel {
  period: VolumePeriod;
  targetLabel: string;
  periodLabel: string;
  categories: VolumeCategoryMetrics[];
  summary: VolumePeriodSummary;
  sharedScaleMax: number;
  weekVolumes: WeekVolumeTotals[];
}

const TARGET_BUFFER = 1.1;
const ON_TRACK_FLOOR = 0.85;
const LOOKBACK_WEEKS = 4;

/**
 * Computes performed volume for a single set. Unlogged sets return zero volume.
 *
 * @param setScheme The set scheme with performed and target fields
 * @param preferredUnit Display unit preference for converting stored kg weights
 * @returns Object with volume in display units and whether performed data was used
 */
export function computeSetVolume(
  setScheme: {
    performed_weight?: number | null;
    performed_rep_count?: number | null;
    target_weight?: number | null;
    target_rep_count?: number | null;
    band_weight_lbs?: number | null;
  },
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

  const weightKg = setScheme.performed_weight as number;
  const reps = setScheme.performed_rep_count as number;

  const bandLbs =
    typeof setScheme.band_weight_lbs === 'number' && setScheme.band_weight_lbs > 0
      ? setScheme.band_weight_lbs
      : 0;

  let displayWeight: number;
  if (preferredUnit === 'KG') {
    displayWeight = weightKg + bandLbs / KG_TO_LBS;
  } else {
    displayWeight = weightKg * KG_TO_LBS + bandLbs;
  }

  return {
    volume: displayWeight * reps,
    usedPerformed: true,
    hasPerformed: true,
    hasTarget,
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
 * @returns Status including a dedicated empty/no-volume state
 */
export function resolveVolumeStatus(current: number, target: number): VolumeStatus {
  if (current <= 0) {
    return 'no_volume';
  }
  if (target <= 0) {
    return current > 0 ? 'on_track' : 'no_volume';
  }
  if (current > target) {
    return 'exceeded';
  }
  if (current >= target * ON_TRACK_FLOOR) {
    return 'on_track';
  }
  return 'under';
}

/**
 * Builds a human-readable status label for accessibility and chips.
 *
 * @param status Volume status
 * @returns Display label
 */
export function getVolumeStatusLabel(status: VolumeStatus): string {
  switch (status) {
    case 'exceeded':
      return 'Exceeded';
    case 'on_track':
      return 'On track';
    case 'under':
      return 'Under';
    case 'no_volume':
      return 'No volume';
  }
}

/**
 * Returns an empty-state teaching message when no volume was logged.
 *
 * @param category Volume category
 * @param status Resolved status
 * @returns Message or null when not applicable
 */
export function getEmptyVolumeMessage(
  category: VolumeCategory,
  status: VolumeStatus
): string | null {
  if (status !== 'no_volume') {
    return null;
  }
  switch (category) {
    case 'Max Effort':
      return 'No max-effort volume logged for this period';
    case 'Dynamic Effort':
      return 'No dynamic-effort volume logged for this period';
    case 'Accessory':
      return 'No accessory volume logged for this period';
  }
}

function getCategoryValue(week: WeekVolumeTotals, category: VolumeCategory): number {
  switch (category) {
    case 'Max Effort':
      return week.maxEffortVolume;
    case 'Dynamic Effort':
      return week.dynamicEffortVolume;
    case 'Accessory':
      return week.accessoryVolume;
  }
}

function sumCategory(weeks: WeekVolumeTotals[], category: VolumeCategory): number {
  return weeks.reduce((sum, week) => sum + getCategoryValue(week, category), 0);
}

function averageCategory(weeks: WeekVolumeTotals[], category: VolumeCategory): number {
  if (weeks.length === 0) {
    return 0;
  }
  return sumCategory(weeks, category) / weeks.length;
}

function percentChange(current: number, previous: number): number | null {
  if (previous <= 0) {
    return current > 0 ? 100 : 0;
  }
  return Math.round(((current - previous) / previous) * 100);
}

/**
 * Aggregates ME/DE/Accessory volume totals for each program week.
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

  workouts.forEach(workoutWithStages => {
    const weekNumber = Math.ceil(workoutWithStages.workout.day_number / workoutsPerWeek);
    if (!weekMap.has(weekNumber)) {
      weekMap.set(weekNumber, {
        weekNumber,
        maxEffortVolume: 0,
        dynamicEffortVolume: 0,
        accessoryVolume: 0,
        totalVolume: 0,
        performedSets: 0,
        targetSets: 0,
        completedWorkouts: 0,
        plannedWorkouts: 0,
      });
    }

    const weekTotals = weekMap.get(weekNumber)!;
    weekTotals.plannedWorkouts += 1;

    if (calculateWorkoutProgress(workoutWithStages).status === 'completed') {
      weekTotals.completedWorkouts += 1;
    }

    workoutWithStages.stages.forEach(stage => {
      stage.exercises.forEach(exerciseWithSchemes => {
        exerciseWithSchemes.set_schemes.forEach(setScheme => {
          const setVolume = computeSetVolume(setScheme, preferredUnit);
          if (setVolume.hasTarget) {
            weekTotals.targetSets += 1;
          }
          if (setVolume.hasPerformed) {
            weekTotals.performedSets += 1;

            const exerciseName = exerciseWithSchemes.exercise.exercise_name;
            const exerciseInfo = exerciseData.get(exerciseName);
            const categorized = categorizeExerciseVolume(
              exerciseInfo,
              replaceUnderscoresWithSpaces(workoutWithStages.workout.name),
              setVolume.volume
            );

            weekTotals.maxEffortVolume += categorized.maxEffortVolume;
            weekTotals.dynamicEffortVolume += categorized.dynamicEffortVolume;
            weekTotals.accessoryVolume += categorized.accessoryVolume;
            weekTotals.totalVolume += setVolume.volume;
          }
        });
      });
    });
  });

  return Array.from(weekMap.values())
    .map(week => ({
      ...week,
      maxEffortVolume: Math.round(week.maxEffortVolume),
      dynamicEffortVolume: Math.round(week.dynamicEffortVolume),
      accessoryVolume: Math.round(week.accessoryVolume),
      totalVolume: Math.round(week.totalVolume),
    }))
    .sort((a, b) => a.weekNumber - b.weekNumber);
}

function getPeriodWindow(
  weekVolumes: WeekVolumeTotals[],
  currentWeek: number,
  period: VolumePeriod
): { current: WeekVolumeTotals[]; prior: WeekVolumeTotals[]; targetSource: WeekVolumeTotals[] } {
  const byWeek = new Map(weekVolumes.map(week => [week.weekNumber, week]));
  const maxWeek = weekVolumes.length
    ? Math.max(...weekVolumes.map(week => week.weekNumber))
    : currentWeek;
  const activeWeek = currentWeek || maxWeek;

  if (period === 'this_week') {
    const current = byWeek.has(activeWeek) ? [byWeek.get(activeWeek)!] : [];
    const prior = byWeek.has(activeWeek - 1) ? [byWeek.get(activeWeek - 1)!] : [];
    const targetSource = weekVolumes.filter(
      week => week.weekNumber >= activeWeek - LOOKBACK_WEEKS && week.weekNumber < activeWeek
    );
    return { current, prior, targetSource };
  }

  if (period === 'last_4_weeks') {
    const current = weekVolumes.filter(
      week => week.weekNumber > activeWeek - LOOKBACK_WEEKS && week.weekNumber <= activeWeek
    );
    const prior = weekVolumes.filter(
      week =>
        week.weekNumber > activeWeek - LOOKBACK_WEEKS * 2 &&
        week.weekNumber <= activeWeek - LOOKBACK_WEEKS
    );
    const targetSource = prior.length > 0 ? prior : current;
    return { current, prior, targetSource };
  }

  const midpoint = Math.ceil(weekVolumes.length / 2);
  const current = weekVolumes;
  const prior = weekVolumes.slice(0, midpoint);
  const targetSource = weekVolumes.slice(0, Math.max(LOOKBACK_WEEKS, midpoint));
  return { current, prior, targetSource };
}

function buildCategoryMetrics(
  category: VolumeCategory,
  currentWeeks: WeekVolumeTotals[],
  priorWeeks: WeekVolumeTotals[],
  targetSourceWeeks: WeekVolumeTotals[],
  period: VolumePeriod
): VolumeCategoryMetrics {
  const current = Math.round(sumCategory(currentWeeks, category));
  const weeklyAverage = averageCategory(targetSourceWeeks, category);
  const weekCount = Math.max(currentWeeks.length, 1);
  const target =
    period === 'this_week'
      ? Math.round(weeklyAverage * TARGET_BUFFER)
      : Math.round(weeklyAverage * TARGET_BUFFER * weekCount);
  const hasBaseline = target > 0;

  const status = resolveVolumeStatus(current, target);
  const priorTotal = sumCategory(priorWeeks, category);

  return {
    type: category,
    current,
    target,
    hasBaseline,
    status,
    deltaAbsolute: hasBaseline ? current - target : 0,
    deltaPercent: hasBaseline ? Math.round(((current - target) / target) * 100) : 0,
    priorPeriodDeltaPercent: priorWeeks.length > 0 ? percentChange(current, priorTotal) : null,
    emptyMessage: getEmptyVolumeMessage(category, status),
  };
}

function countRecentPrs(
  userDataExport: UserDataExport | null,
  currentWeeks: WeekVolumeTotals[],
  workouts: ProgrammedWorkoutWithStages[],
  workoutsPerWeek: number
): { count: number; label: string | null } {
  if (!userDataExport?.user_one_rep_max?.length || currentWeeks.length === 0) {
    return { count: 0, label: null };
  }

  const weekNumbers = new Set(currentWeeks.map(week => week.weekNumber));
  const weekDates = workouts
    .filter(workout => weekNumbers.has(Math.ceil(workout.workout.day_number / workoutsPerWeek)))
    .map(workout => new Date(workout.workout.created_at).getTime());

  if (weekDates.length === 0) {
    return { count: 0, label: null };
  }

  const minDate = Math.min(...weekDates);
  const maxDate = Math.max(...weekDates) + 7 * 24 * 60 * 60 * 1000;

  const recent = userDataExport.user_one_rep_max.filter(entry => {
    const updatedAt = (entry as { updated_at?: Date | string }).updated_at;
    if (!updatedAt) {
      return false;
    }
    const timestamp = new Date(updatedAt).getTime();
    return timestamp >= minDate && timestamp <= maxDate;
  });

  if (recent.length === 0) {
    return { count: 0, label: null };
  }

  const first = recent[0] as { exercise_name?: string; one_rep_max?: number };
  const label = first.exercise_name
    ? `${replaceUnderscoresWithSpaces(first.exercise_name)}${
        first.one_rep_max != null ? ` ${Math.round(first.one_rep_max)}` : ''
      }`
    : `${recent.length} PR${recent.length === 1 ? '' : 's'}`;

  return { count: recent.length, label };
}

/**
 * Builds the full volume overview model for KPI cards and the period summary strip.
 *
 * @param userDataExport Full user export containing workouts and 1RMs
 * @param exerciseData Exercise metadata map
 * @param workoutsPerWeek Program days per week
 * @param currentWeek Current program week number
 * @param period Selected reporting period
 * @param preferredUnit Display unit
 * @returns Overview model ready for rendering
 */
export function buildVolumeOverviewModel(
  userDataExport: UserDataExport | null,
  exerciseData: Map<string, Exercise>,
  workoutsPerWeek: number,
  currentWeek: number,
  period: VolumePeriod,
  preferredUnit: 'KG' | 'LBS' = 'LBS'
): VolumeOverviewModel | null {
  const activeProgram: ProgramWithWorkouts | undefined =
    userDataExport?.training_programs?.find(program => program.program.is_active) ||
    userDataExport?.training_programs?.[0];

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

  const { current, prior, targetSource } = getPeriodWindow(weekVolumes, currentWeek, period);
  const categories: VolumeCategoryMetrics[] = (
    ['Max Effort', 'Dynamic Effort', 'Accessory'] as VolumeCategory[]
  ).map(category => buildCategoryMetrics(category, current, prior, targetSource, period));

  const sharedScaleMax = Math.max(
    ...categories.map(category => Math.max(category.current, category.target, 1)),
    1
  );

  const totalVolume = current.reduce((sum, week) => sum + week.totalVolume, 0);
  const priorTotalVolume = prior.reduce((sum, week) => sum + week.totalVolume, 0);
  const prs = countRecentPrs(userDataExport, current, activeProgram.workouts, workoutsPerWeek);

  const periodLabel =
    period === 'this_week' ? 'This week' : period === 'last_4_weeks' ? 'Last 4 weeks' : 'Program';

  const targetLabel =
    period === 'this_week'
      ? 'Target: avg of last 4 weeks +10%'
      : period === 'last_4_weeks'
        ? 'Target: prior 4-week avg +10%'
        : 'Target: program weekly avg +10%';

  return {
    period,
    targetLabel,
    periodLabel,
    categories,
    sharedScaleMax,
    weekVolumes,
    summary: {
      sessionsCompleted: current.reduce((sum, week) => sum + week.completedWorkouts, 0),
      sessionsPlanned: current.reduce((sum, week) => sum + week.plannedWorkouts, 0),
      totalVolume: Math.round(totalVolume),
      setsCompleted: current.reduce((sum, week) => sum + week.performedSets, 0),
      setsPlanned: current.reduce((sum, week) => sum + week.targetSets, 0),
      volumeDeltaPercent: prior.length > 0 ? percentChange(totalVolume, priorTotalVolume) : null,
      recentPrCount: prs.count,
      recentPrLabel: prs.label,
    },
  };
}
