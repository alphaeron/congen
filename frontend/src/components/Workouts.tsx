import { Box, CardContent } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { GameCard, GameText, GAME_CLASSES } from './GameTheme';
import { HeroCTA } from './HeroCTA';
import { LoadingBackdrop } from './LoadingBackdrop';
import { LoadingSpinner } from './LoadingSpinner';
import { TrainingTimeline } from './TrainingTimeline';
import { VolumeOverviewCards } from './VolumeOverviewCards';
import { WorkoutGenerationWizard } from './WorkoutGenerationWizard';
import { WorkoutHeader } from './WorkoutHeader';
import type { Program, ProgramWithPreferences, Exercise } from '../api/types';
import { useData } from '../contexts/DataContext';
import { exportProgramToPDF } from '../utils/exportUtils';
import { calculateProgramProgress, calculateWorkoutProgress } from '../utils/progressUtils';

interface WorkoutsProps {
  selectedWorkout?: string | null;
}

/**
 * Workouts component for managing and viewing workout programs.
 *
 * Features:
 * - Display active program and its weeks
 * - Generate new workouts for programs
 * - View week details with slide-left animation
 * - Auto-refresh functionality after workout generation
 * - URL query parameters for week and workout selection
 *
 * @param selectedWorkout The selected workout ID (from URL)
 * @returns Workouts component
 */
export const Workouts: React.FC<WorkoutsProps> = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { enqueueSnackbar } = useSnackbar();
  const {
    userData,
    weightUnitPreferences,
    refreshData,
    isLoading: isDataLoading,
    loadProgramPreferences,
    getExercise,
  } = useData();

  const [programsWithPreferences, setProgramsWithPreferences] = useState<
    Array<ProgramWithPreferences>
  >([]);
  const [isLoading, setIsLoading] = useState(true);
  const [wizardOpen, setWizardOpen] = useState(false);
  const [selectedProgram, setSelectedProgram] = useState<Program | null>(null);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [isGenerating, setIsGenerating] = useState(false);

  // Load additional data that's not in DataContext
  useEffect(() => {
    const loadAdditionalData = async () => {
      if (!userData) return;

      setIsLoading(true);
      try {
        const programsData = await loadProgramPreferences();
        setProgramsWithPreferences(programsData);

        // Extract unique exercises from the userData and fetch exercise details
        const uniqueExercises = new Set<string>();
        userData.training_programs?.forEach(program => {
          program.workouts.forEach(workoutWithStages => {
            workoutWithStages.stages.forEach(stageWithExercises => {
              stageWithExercises.exercises.forEach(exerciseWithSetSchemes => {
                uniqueExercises.add(exerciseWithSetSchemes.exercise.exercise_name);
              });
            });
          });
        });

        // Fetch exercise data for all unique exercises using DataContext
        const exerciseMap = new Map<string, Exercise>();
        for (const exerciseName of Array.from(uniqueExercises)) {
          try {
            const exercise = await getExercise(exerciseName);
            if (exercise) {
              exerciseMap.set(exerciseName, exercise);
            }
          } catch {
            enqueueSnackbar(`Error fetching exercise data for ${exerciseName}`, {
              variant: 'error',
            });
          }
        }

        setExerciseData(exerciseMap);
      } catch {
        enqueueSnackbar('Failed to load additional workout data. Please try again.', {
          variant: 'error',
        });
      } finally {
        setIsLoading(false);
      }
    };

    loadAdditionalData();
  }, [userData, enqueueSnackbar, loadProgramPreferences, getExercise]);

  // Get active program data consistently from userData
  const activeProgramData = useMemo(() => {
    if (!userData?.training_programs) return null;
    return userData.training_programs.find(p => p.program.is_active) || null;
  }, [userData]);

  // Get active program preferences
  const activeProgramPreferences = useMemo(() => {
    if (!activeProgramData) return null;
    return programsWithPreferences.find(p => p.program.id === activeProgramData.program.id) || null;
  }, [activeProgramData, programsWithPreferences]);

  // Legacy activeProgram for backward compatibility in render
  const activeProgram = activeProgramPreferences;

  // Group workouts by week
  const weeks = useMemo(() => {
    if (
      !activeProgramData ||
      !activeProgramPreferences ||
      activeProgramData.workouts.length === 0
    ) {
      return [];
    }

    // Use program preferences
    const workoutsPerWeek = activeProgramPreferences.program_preferences.program_days_per_week;

    const weekMap = new Map<number, typeof activeProgramData.workouts>();

    activeProgramData.workouts.forEach(workoutWithStages => {
      const weekNum = Math.ceil(workoutWithStages.workout.day_number / workoutsPerWeek);
      if (!weekMap.has(weekNum)) {
        weekMap.set(weekNum, []);
      }
      weekMap.get(weekNum)!.push(workoutWithStages);
    });

    return Array.from(weekMap.entries())
      .map(([weekNumber, weekWorkoutsWithStages]) => {
        // Calculate completion for each workout
        const workoutProgresses = weekWorkoutsWithStages.map(workoutWithStages =>
          calculateWorkoutProgress(workoutWithStages)
        );

        // A week is completed if all workouts in the week are completed
        const isCompleted = workoutProgresses.every(progress => progress.status === 'completed');

        // Count completed workouts
        const completedWorkouts = workoutProgresses.filter(
          progress => progress.status === 'completed'
        ).length;

        return {
          weekNumber,
          workoutCount: weekWorkoutsWithStages.length,
          workouts: weekWorkoutsWithStages
            .map(w => w.workout)
            .sort((a, b) => a.day_number - b.day_number),
          isCompleted,
          completedWorkouts,
        };
      })
      .sort((a, b) => a.weekNumber - b.weekNumber);
  }, [activeProgramData, activeProgramPreferences]);

  const openWizard = (program: Program) => {
    setSelectedProgram(program);
    setWizardOpen(true);
  };

  const handleWeekClick = (weekNumber: number) => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('section', 'workouts');
    newSearchParams.set('week', weekNumber.toString());
    navigate(`/dashboard?${newSearchParams.toString()}`);
  };

  const handleWizardComplete = async () => {
    // Close the wizard first
    setWizardOpen(false);
    setSelectedProgram(null);

    setIsGenerating(true);
    try {
      // Refresh all data after generation
      await refreshData();
      enqueueSnackbar('Workouts generated successfully!', { variant: 'success' });
    } catch {
      enqueueSnackbar('Failed to refresh workout data', { variant: 'error' });
    } finally {
      setIsGenerating(false);
    }
  };

  const handleWizardClose = () => {
    setWizardOpen(false);
    setSelectedProgram(null);
  };

  // Calculate progress metrics
  const getProgressMetrics = () => {
    if (!activeProgramData || !activeProgramPreferences) return null;

    const workoutsPerWeek =
      activeProgramPreferences.program_preferences?.program_days_per_week || 3;
    const programProgress = calculateProgramProgress(activeProgramData.workouts, workoutsPerWeek);
    const currentWeek = Math.max(activeProgramData.program.current_week_number, 1);

    return {
      ...programProgress,
      currentWeek,
    };
  };

  // Get progress metrics for the component
  const progressMetrics = getProgressMetrics();

  // Export handlers
  const handleExportPDF = async () => {
    if (!activeProgramData) return;

    await exportProgramToPDF(activeProgramData, weightUnitPreferences, {
      title: activeProgramData.program.name,
      filename: `program-${activeProgramData.program.name.replace(/\s+/g, '-').toLowerCase()}`,
    });
  };

  // Show loading state while data is being fetched
  if (isDataLoading || isLoading) {
    return <LoadingSpinner message="Loading workout data..." fullHeight={false} />;
  }

  // Show workout calendar
  return (
    <React.Fragment>
      <Box sx={{ px: 3, pt: 3 }}>
        {!activeProgram ? (
          <GameCard className={`${GAME_CLASSES.marginBottom3} ${GAME_CLASSES.marginTop3}`}>
            <CardContent>
              <GameText variant="h6" gutterBottom={true}>
                No Active Program
              </GameText>
              <GameText variant="body2" className={GAME_CLASSES.opacity80} paragraph>
                You need to create a program first before you can generate and view workouts. Please
                go to the Programs section to create a program.
              </GameText>
            </CardContent>
          </GameCard>
        ) : (
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3, overflow: 'visible' }}>
            {/* Header Section */}
            {progressMetrics && (
              <WorkoutHeader
                context="program"
                currentWeek={Math.max(activeProgram.program.current_week_number, 1)}
                progressValue={progressMetrics.completedWeeks}
                progressMax={progressMetrics.totalWeeks}
                onExportPDF={handleExportPDF}
                disabled={weeks.length === 0}
                currentWeekWorkouts={
                  weeks.find(
                    w => w.weekNumber === Math.max(activeProgram.program.current_week_number, 1)
                  )?.workoutCount || 0
                }
                completedWorkouts={
                  weeks.find(
                    w => w.weekNumber === Math.max(activeProgram.program.current_week_number, 1)
                  )?.completedWorkouts || 0
                }
              />
            )}

            {/* Hero CTA Section */}
            <HeroCTA
              onClick={() => openWizard(activeProgram.program)}
              disabled={isGenerating}
              loading={isGenerating}
              title="Generate Next Week"
              subtitle="Ready to level up your training? Create your next workout week and keep progressing!"
              icon="🎯"
              variant="primary"
            />

            {/* Stats Dashboard */}
            {userData?.training_programs &&
              userData.training_programs.length > 0 &&
              userData.training_programs.some(program => program.workouts.length > 0) && (
                <VolumeOverviewCards
                  userDataExport={userData}
                  exerciseData={exerciseData}
                  weightUnitPreferences={weightUnitPreferences}
                  height={200}
                />
              )}

            {/* Training Timeline */}
            <TrainingTimeline
              weeks={weeks.map(week => ({
                weekNumber: week.weekNumber,
                workouts: week.workouts,
                isCompleted: week.isCompleted,
                completedWorkouts: week.completedWorkouts,
              }))}
              onWeekClick={handleWeekClick}
              currentWeek={Math.max(activeProgram.program.current_week_number, 1)}
            />
          </Box>
        )}
      </Box>

      {/* Workout Generation Wizard */}
      {selectedProgram && (
        <WorkoutGenerationWizard
          open={wizardOpen}
          onClose={handleWizardClose}
          onComplete={handleWizardComplete}
          program={selectedProgram}
        />
      )}

      {/* Full-screen loading overlay during workout generation */}
      <LoadingBackdrop
        open={isGenerating}
        message="Generating workouts..."
        subMessage="This may take a few moments"
      />
    </React.Fragment>
  );
};
