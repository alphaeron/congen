import type {
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises,
  ProgrammedExerciseWithSetSchemes,
  SetScheme,
} from '../api/types';

/**
 * Progress status for workouts, weeks, and programs
 */
export type ProgressStatus = 'not-started' | 'in-progress' | 'completed';

/**
 * Progress metrics for different levels
 */
export interface WorkoutProgress {
  status: ProgressStatus;
  completedExercises: number;
  totalExercises: number;
  completionRate: number;
}

export interface WeekProgress {
  status: ProgressStatus;
  completedWorkouts: number;
  totalWorkouts: number;
  completionRate: number;
}

export interface ProgramProgress {
  status: ProgressStatus;
  completedWeeks: number;
  totalWeeks: number;
  completionRate: number;
}

/**
 * Check if a set scheme has performed data
 */
export const hasPerformedData = (setScheme: SetScheme): boolean => {
  return (
    (setScheme.performed_weight !== undefined && setScheme.performed_weight !== null) ||
    (setScheme.performed_rep_count !== undefined && setScheme.performed_rep_count !== null)
  );
};

/**
 * Check if an exercise has performed data
 */
export const exerciseHasPerformedData = (exercise: ProgrammedExerciseWithSetSchemes): boolean => {
  return exercise.set_schemes.some(setScheme => hasPerformedData(setScheme));
};

/**
 * Check if an exercise is fully completed (all set schemes have performed data)
 */
export const exerciseIsCompleted = (exercise: ProgrammedExerciseWithSetSchemes): boolean => {
  return (
    exercise.set_schemes.length > 0 &&
    exercise.set_schemes.every(setScheme => hasPerformedData(setScheme))
  );
};

/**
 * Calculate workout-level progress
 * Progress is based on the number of exercises with performed fields
 */
export const calculateWorkoutProgress = (workout: ProgrammedWorkoutWithStages): WorkoutProgress => {
  const allExercises: ProgrammedExerciseWithSetSchemes[] = [];

  // Collect all exercises from all stages (handle case where stages might be undefined)
  if (workout.stages && Array.isArray(workout.stages)) {
    workout.stages.forEach((stage: WorkoutStageWithExercises) => {
      if (stage.exercises && Array.isArray(stage.exercises)) {
        allExercises.push(...stage.exercises);
      }
    });
  }

  const totalExercises = allExercises.length;
  const completedExercises = allExercises.filter(exercise => exerciseIsCompleted(exercise)).length;
  const exercisesWithSomeData = allExercises.filter(exercise =>
    exerciseHasPerformedData(exercise)
  ).length;

  let status: ProgressStatus;
  if (completedExercises === totalExercises && totalExercises > 0) {
    status = 'completed';
  } else if (exercisesWithSomeData > 0) {
    status = 'in-progress';
  } else {
    status = 'not-started';
  }

  return {
    status,
    completedExercises,
    totalExercises,
    completionRate: totalExercises > 0 ? (completedExercises / totalExercises) * 100 : 0,
  };
};

/**
 * Calculate week-level progress
 * Progress is based on programmed workouts that have all performed fields included
 */
export const calculateWeekProgress = (workouts: ProgrammedWorkoutWithStages[]): WeekProgress => {
  const totalWorkouts = workouts.length;
  const completedWorkouts = workouts.filter(workout => {
    const workoutProgress = calculateWorkoutProgress(workout);
    return workoutProgress.status === 'completed';
  }).length;

  const workoutsWithSomeData = workouts.filter(workout => {
    const workoutProgress = calculateWorkoutProgress(workout);
    return workoutProgress.status === 'in-progress' || workoutProgress.status === 'completed';
  }).length;

  let status: ProgressStatus;
  if (completedWorkouts === totalWorkouts && totalWorkouts > 0) {
    status = 'completed';
  } else if (workoutsWithSomeData > 0) {
    status = 'in-progress';
  } else {
    status = 'not-started';
  }

  return {
    status,
    completedWorkouts,
    totalWorkouts,
    completionRate: totalWorkouts > 0 ? (completedWorkouts / totalWorkouts) * 100 : 0,
  };
};

/**
 * Calculate program-level progress
 * Progress is the number of weeks where all programmed workouts have all performed fields included
 */
export const calculateProgramProgress = (
  workouts: ProgrammedWorkoutWithStages[],
  workoutsPerWeek: number
): ProgramProgress => {
  // Group workouts by week
  const weeksMap = new Map<number, ProgrammedWorkoutWithStages[]>();

  workouts.forEach(workout => {
    const weekNumber = Math.ceil(workout.workout.day_number / workoutsPerWeek);
    if (!weeksMap.has(weekNumber)) {
      weeksMap.set(weekNumber, []);
    }
    weeksMap.get(weekNumber)!.push(workout);
  });

  const totalWeeks = weeksMap.size;
  const completedWeeks = Array.from(weeksMap.values()).filter(weekWorkouts => {
    const weekProgress = calculateWeekProgress(weekWorkouts);
    return weekProgress.status === 'completed';
  }).length;

  const weeksWithSomeData = Array.from(weeksMap.values()).filter(weekWorkouts => {
    const weekProgress = calculateWeekProgress(weekWorkouts);
    return weekProgress.status === 'in-progress' || weekProgress.status === 'completed';
  }).length;

  let status: ProgressStatus;
  if (completedWeeks === totalWeeks && totalWeeks > 0) {
    status = 'completed';
  } else if (weeksWithSomeData > 0) {
    status = 'in-progress';
  } else {
    status = 'not-started';
  }

  return {
    status,
    completedWeeks,
    totalWeeks,
    completionRate: totalWeeks > 0 ? (completedWeeks / totalWeeks) * 100 : 0,
  };
};

/**
 * Get the appropriate icon for progress status
 */
export const getProgressIcon = (status: ProgressStatus): string => {
  switch (status) {
    case 'completed':
      return 'check_circle'; // Green checkmark
    case 'in-progress':
      return 'schedule'; // In progress symbol
    case 'not-started':
      return 'pause_circle'; // Not started / stop icon
    default:
      return 'pause_circle';
  }
};

/**
 * Get the appropriate color for progress status
 */
export const getProgressColor = (status: ProgressStatus): string => {
  switch (status) {
    case 'completed':
      return 'success.main';
    case 'in-progress':
      return 'warning.main';
    case 'not-started':
      return 'text.secondary';
    default:
      return 'text.secondary';
  }
};
