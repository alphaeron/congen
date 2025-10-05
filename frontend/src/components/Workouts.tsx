import {
  Box,
  Card,
  CardContent,
  Button,
  List,
  ListItem,
  ListItemText,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate, useSearchParams } from 'react-router';

import { ExportButtons } from './ExportButtons';
import { LoadingBackdrop } from './LoadingBackdrop';
import { LoadingSpinner } from './LoadingSpinner';
import { ProgressBar } from './ProgressBar';
import { StreamChart } from './StreamChart';
import { WorkoutGenerationWizard } from './WorkoutGenerationWizard';
import { GameCard, GameText, GAME_CLASSES } from './GameTheme';
import type {
  Program,
  ProgrammedWorkout,
  User,
  ProgramWithPreferences,
  Exercise,
} from '../api/types';
import { replaceUnderscoresWithSpaces } from '../common/utils';
import { useData } from '../contexts/DataContext';
import { exportProgramToPDF } from '../utils/exportUtils';
import { calculateProgramProgress } from '../utils/progressUtils';

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

    const weekMap = new Map<number, ProgrammedWorkout[]>();

    activeProgramData.workouts.forEach(workoutWithStages => {
      const weekNum = Math.ceil(workoutWithStages.workout.day_number / workoutsPerWeek);
      if (!weekMap.has(weekNum)) {
        weekMap.set(weekNum, []);
      }
      weekMap.get(weekNum)!.push(workoutWithStages.workout);
    });

    return Array.from(weekMap.entries())
      .map(([weekNumber, weekWorkouts]) => ({
        weekNumber,
        workoutCount: weekWorkouts.length,
        workouts: weekWorkouts.sort((a, b) => a.day_number - b.day_number),
      }))
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
      <Box sx={{ p: 3 }}>
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
          <Box>
            {/* Top Section: Progress Bar and Export Buttons */}
            <Box sx={{ mb: 3 }}>
              <Box display="flex" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
                {/* Progress Bar on the left */}
                <Box sx={{ flex: 1, mr: 2 }}>
                  {progressMetrics && (
                    <ProgressBar
                      value={progressMetrics.completionRate}
                      status={progressMetrics.status}
                      current={progressMetrics.completedWeeks}
                      total={progressMetrics.totalWeeks}
                      showTooltip={true}
                      showTicks={true}
                      steps={Array.from(
                        { length: progressMetrics.totalWeeks + 1 },
                        (_, i) => (i / progressMetrics.totalWeeks) * 100
                      )}
                      ticks={Array.from(
                        { length: progressMetrics.totalWeeks + 1 },
                        (_, i) => (i / progressMetrics.totalWeeks) * 100
                      )}
                      width="100%"
                      height={8}
                      smooth={true}
                      animationDuration={400}
                    />
                  )}
                </Box>

                {/* Export Buttons on the right */}
                <ExportButtons onExportPDF={handleExportPDF} disabled={weeks.length === 0} />
              </Box>
            </Box>

            {/* Current Week Section */}
            <GameCard className={GAME_CLASSES.marginBottom3}>
              <CardContent>
                <Box
                  display="flex"
                  justifyContent="space-between"
                  alignItems="flex-start"
                  sx={{ mb: 2 }}
                >
                  <Box>
                    <GameText variant="h6" gutterBottom>
                      {`Current Week: Week ${Math.max(activeProgram.program.current_week_number, 1)}`}
                    </GameText>
                    <GameText variant="body2" textVariant="secondary">
                      Track your progress and generate new workout weeks
                    </GameText>
                  </Box>
                  <Box display="flex" gap={1}>
                    <Button
                      variant="contained"
                      onClick={() => openWizard(activeProgram.program)}
                      disabled={isGenerating}
                      size="large"
                    >
                      {isGenerating ? 'Generating...' : 'Generate Next Week'}
                    </Button>
                  </Box>
                </Box>

                {/* Volume Flow Chart */}
                {userData?.training_programs &&
                  userData.training_programs.length > 0 &&
                  userData.training_programs.some(program => program.workouts.length > 0) && (
                    <Box sx={{ mt: 2 }}>
                      <StreamChart
                        userDataExport={userData}
                        exerciseData={exerciseData}
                        weightUnitPreferences={weightUnitPreferences}
                        title="Volume Flow Over Time"
                        description="Training volume distribution across workout types"
                        height={300}
                      />
                    </Box>
                  )}
              </CardContent>
            </GameCard>

            {/* Training Weeks Section */}
            <GameCard className="glassmorphism-card">
              <CardContent>
                <GameText variant="h6" gutterBottom sx={{ color: '#1e293b' }}>
                  Training Weeks
                </GameText>
                <GameText variant="body2" className={`${GAME_CLASSES.opacity80} ${GAME_CLASSES.marginBottom2}`} sx={{ color: '#64748b' }}>
                  Click on any week to view detailed workout information
                </GameText>
                {isLoading ? (
                  <Box display="flex" justifyContent="center" p={3}>
                    <LoadingSpinner message="Loading weeks..." size={40} />
                  </Box>
                ) : weeks.length === 0 ? (
                  <Box sx={{ textAlign: 'center', py: 4 }}>
                    <GameText variant="body2" textVariant="secondary" className={GAME_CLASSES.marginBottom2} sx={{ color: '#64748b' }}>
                      No workouts generated yet. Click &quot;Generate Next Week&quot; to create your
                      first workout week.
                    </GameText>
                  </Box>
                ) : (
                  <List>
                    {weeks.map(week => (
                      <ListItem
                        key={week.weekNumber}
                        disablePadding
                        className="modern-list-item"
                        sx={{
                          cursor: 'pointer',
                          borderRadius: 1,
                          mb: 1,
                          '&:hover': {
                            backgroundColor: 'rgba(0, 188, 212, 0.05)',
                            transform: 'translateX(4px)',
                          },
                        }}
                        onClick={() => handleWeekClick(week.weekNumber)}
                      >
                        <ListItemText
                          primary={
                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                              <GameText variant="subtitle1" className={GAME_CLASSES.textMedium} sx={{ color: '#1e293b' }}>
                                Week {week.weekNumber}
                              </GameText>
                              {week.workoutCount > 0 && (
                                <Box
                                  sx={{
                                    backgroundColor: '#00bcd4',
                                    color: 'white',
                                    borderRadius: '50%',
                                    width: 20,
                                    height: 20,
                                    display: 'flex',
                                    alignItems: 'center',
                                    justifyContent: 'center',
                                    fontSize: '0.75rem',
                                    fontWeight: 'bold',
                                  }}
                                >
                                  {week.workoutCount}
                                </Box>
                              )}
                            </Box>
                          }
                          secondary={
                            <span className={GAME_CLASSES.textSecondary} style={{ color: '#64748b' }}>
                              {week.workouts
                                .map(w =>
                                  replaceUnderscoresWithSpaces(w.name || `Workout ${w.day_number}`)
                                )
                                .join(' • ')}
                            </span>
                          }
                        />
                      </ListItem>
                    ))}
                  </List>
                )}
              </CardContent>
            </GameCard>
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
