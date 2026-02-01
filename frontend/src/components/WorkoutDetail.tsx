import { ExpandMore as ExpandMoreIcon, ExpandLess as ExpandLessIcon } from '@mui/icons-material';
import {
  Box,
  Alert,
  IconButton,
  useTheme,
  Grid,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Autocomplete,
} from '@mui/material';
import { useForm } from '@tanstack/react-form';
import {
  useReactTable,
  getCoreRowModel,
  flexRender,
  createColumnHelper,
} from '@tanstack/react-table';
import { motion } from 'framer-motion';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo, useCallback } from 'react';

import { ChordChart } from './ChordChart';
import { ExerciseName } from './ExerciseName';
import {
  GameCard,
  GameText,
  GameTextField,
  GameFormControl,
  GameInputLabel,
  GameSelect,
  GameMenuItem,
  GAME_CLASSES,
} from './GameTheme';
import { LoadingSpinner } from './LoadingSpinner';
import { RichTextDisplay } from './RichTextDisplay';
import { RichTextEditor } from './RichTextEditor';
import { SetSchemeEditor } from './SetSchemeEditor';
import { SetSchemeForm } from './SetSchemeForm';
import { SunburstChart } from './SunburstChart';
import { WorkoutHeader } from './WorkoutHeader';
import type {
  Exercise,
  ProgramWithWorkouts,
  ProgrammedWorkoutWithStages,
  ProgrammedExerciseWithSetSchemes,
} from '../api/types';
import {
  replaceUnderscoresWithSpaces,
  formatWeightWithUnit,
  convertDisplayWeightToKg,
} from '../common/utils';
import { useData } from '../contexts/DataContext';
import { exportWorkoutToPDF } from '../utils/exportUtils';
import { calculateWorkoutProgress } from '../utils/progressUtils';

interface WorkoutDetailProps {
  workoutId: number;
  onBack: () => void;
  onWorkoutDetailsUpdate?: (workoutDetails: {
    name: string;
    day_number: number;
    stages: number;
  }) => void;
}

// Table row data type
interface TableRow {
  id: string;
  type: 'stage' | 'exercise';
  stageName?: string;
  exerciseName?: string;
  sets?: number;
  reps?: number;
  tempo?: string;
  weight?: string;
  rest?: string;
  notes?: string;
  exerciseNotes?: string;
  stageId?: number;
  exerciseId?: number;
  setSchemes?: Record<string, unknown>[];
  exerciseData?: ProgrammedExerciseWithSetSchemes;
}

const columnHelper = createColumnHelper<TableRow>();

/**
 * Detailed workout display component.
 *
 * Shows all stages, exercises, and set schemes for a specific workout
 * with sticky section headers that pin under the table header when scrolling.
 * Uses optimized data loading to avoid N+1 query problems.
 *
 * @param workoutId The ID of the workout to display
 * @param onBack Callback to go back to the workout list
 * @returns WorkoutDetail component
 */
export const WorkoutDetail: React.FC<WorkoutDetailProps> = ({
  workoutId,
  onBack,
  onWorkoutDetailsUpdate,
}) => {
  const theme = useTheme();
  const { enqueueSnackbar } = useSnackbar();

  // Use shared data context instead of local state
  const {
    userData,
    exerciseMuscleData,
    weightUnitPreferences,
    isLoading,
    refreshData,
    loadAllExercises,
    createProgrammedExercise,
    updateProgrammedExercise,
  } = useData();

  const [collapsedStages, setCollapsedStages] = useState<Set<number>>(new Set());
  // Remove local exerciseData state - we'll use the data from context directly
  const [isMostRecentWeek, setIsMostRecentWeek] = useState(false);
  const [saving, setSaving] = useState(false);
  const [notesEditorOpen, setNotesEditorOpen] = useState(false);
  const [selectedExerciseForNotes, setSelectedExerciseForNotes] =
    useState<ProgrammedExerciseWithSetSchemes | null>(null);
  const [notesContent, setNotesContent] = useState('');
  const [addExerciseDialogOpen, setAddExerciseDialogOpen] = useState(false);
  const [selectedExercise, setSelectedExercise] = useState<Exercise | null>(null);
  const [selectedStageId, setSelectedStageId] = useState<number | ''>('');
  const [availableExercises, setAvailableExercises] = useState<Exercise[]>([]);
  const [loadingExercises, setLoadingExercises] = useState(false);

  const convertWeightForStorage = (weight: number, exerciseName: string): number => {
    const weightUnitPreference = weightUnitPreferences.find(
      pref => pref.exercise_name === exerciseName
    );
    return convertDisplayWeightToKg(
      weight,
      weightUnitPreference?.preferred_unit as 'KG' | 'LBS' | undefined
    );
  };

  // Form for set scheme details in Add Exercise dialog
  const addExerciseForm = useForm({
    defaultValues: {
      totalSets: 1,
      targetWeight: 1,
      targetReps: 1,
      restSeconds: 60,
      performedWeight: undefined as number | undefined,
      performedReps: undefined as number | undefined,
      useTempo: false,
      eccentricTempo: '',
      isometricTempo: '',
      concentricTempo: '',
      isAmrap: false,
      isEmom: false,
    },
    onSubmit: async ({ value }) => {
      if (!selectedExercise || !selectedStageId) {
        enqueueSnackbar('Please select an exercise and stage', { variant: 'warning' });
        return;
      }

      try {
        setSaving(true);

        // Find the next position in the selected stage
        const selectedStage = workoutData?.stages.find(s => s.stage.id === selectedStageId);
        const nextPosition = selectedStage ? selectedStage.exercises.length + 1 : 1;

        // Create the programmed exercise
        await createProgrammedExercise(
          selectedStageId as number,
          selectedExercise.name,
          nextPosition,
          '', // notes - empty initially
          value.totalSets,
          convertWeightForStorage(value.targetWeight, selectedExercise.name),
          value.targetReps,
          value.restSeconds,
          value.performedWeight
            ? convertWeightForStorage(value.performedWeight, selectedExercise.name)
            : undefined,
          value.performedReps,
          value.useTempo
            ? `${value.eccentricTempo}-${value.isometricTempo}-${value.concentricTempo}`
            : undefined,
          value.isAmrap,
          value.isEmom
        );

        // Refresh data to show the new exercise
        await refreshData();

        handleCloseAddExerciseDialog();
        enqueueSnackbar('Exercise added successfully', { variant: 'success' });
      } catch {
        enqueueSnackbar('Failed to add exercise', { variant: 'error' });
      } finally {
        setSaving(false);
      }
    },
  });

  const handleNotesContentChange = useCallback((newContent: string) => {
    setNotesContent(newContent);
  }, []);

  // Load available exercises when dialog opens
  const loadAvailableExercises = useCallback(async () => {
    setLoadingExercises(true);
    try {
      const exercises = await loadAllExercises();
      setAvailableExercises(exercises);
    } catch {
      enqueueSnackbar('Failed to load exercises', { variant: 'error' });
    } finally {
      setLoadingExercises(false);
    }
  }, [enqueueSnackbar]);

  const handleOpenAddExerciseDialog = useCallback(() => {
    setAddExerciseDialogOpen(true);
    loadAvailableExercises();
    // Reset form state
    setSelectedExercise(null);
    setSelectedStageId('');
  }, [loadAvailableExercises]);

  const handleCloseAddExerciseDialog = useCallback(() => {
    setAddExerciseDialogOpen(false);
    setSelectedExercise(null);
    setSelectedStageId('');
  }, []);

  // Determine if this is the most recent week when userData is available
  useEffect(() => {
    if (userData?.training_programs && userData.training_programs.length > 0) {
      const activeProgram = userData.training_programs.find(
        (program: ProgramWithWorkouts) => program.program.is_active
      );
      if (activeProgram) {
        // Find the current workout's week
        const currentWorkout = activeProgram.workouts.find(
          (workout: ProgrammedWorkoutWithStages) => workout.workout.id === workoutId
        );
        if (currentWorkout) {
          const workoutsPerWeek = activeProgram.program_preferences.program_days_per_week;
          const currentWeek = Math.ceil(currentWorkout.workout.day_number / workoutsPerWeek);

          // Find the highest week number in the program
          const maxWeek = Math.max(
            ...activeProgram.workouts.map((workout: ProgrammedWorkoutWithStages) =>
              Math.ceil(workout.workout.day_number / workoutsPerWeek)
            )
          );

          setIsMostRecentWeek(currentWeek === maxWeek);
        }
      }
    }
  }, [userData, workoutId]);

  // Handle opening notes editor
  const handleOpenNotesEditor = useCallback(
    (exerciseData: ProgrammedExerciseWithSetSchemes) => {
      if (!isMostRecentWeek) {
        enqueueSnackbar('Editing is only available for the most recent week', {
          variant: 'warning',
        });
        return;
      }
      setSelectedExerciseForNotes(exerciseData);
      setNotesContent(exerciseData.exercise.notes || '');
      setNotesEditorOpen(true);
    },
    [isMostRecentWeek, enqueueSnackbar]
  );

  // Handle closing notes editor
  const handleCloseNotesEditor = useCallback(() => {
    setNotesEditorOpen(false);
    setSelectedExerciseForNotes(null);
    setNotesContent('');
  }, []);

  // Handle saving notes from modal
  const handleSaveNotesFromModal = useCallback(async () => {
    if (!selectedExerciseForNotes) return;

    try {
      setSaving(true);

      await updateProgrammedExercise(
        selectedExerciseForNotes.exercise.id,
        selectedExerciseForNotes.exercise.workout_stage_id,
        selectedExerciseForNotes.exercise.exercise_name,
        selectedExerciseForNotes.exercise.position,
        notesContent
      );

      // Refresh data from server to get the updated notes
      await refreshData();

      handleCloseNotesEditor();
      enqueueSnackbar('Exercise notes saved successfully', { variant: 'success' });
    } catch {
      enqueueSnackbar('Failed to save exercise notes', { variant: 'error' });
    } finally {
      setSaving(false);
    }
  }, [selectedExerciseForNotes, notesContent, handleCloseNotesEditor, enqueueSnackbar, workoutId]);

  // Find the specific workout from the exported data
  const workoutData = useMemo(() => {
    if (!userData?.training_programs?.length) return null;

    for (const program of userData.training_programs) {
      const workout = program.workouts.find(w => w.workout.id === workoutId);
      if (workout) {
        return workout;
      }
    }
    return null;
  }, [userData, workoutId]);

  // Calculate day within week and week number
  const dayInWeekData = useMemo(() => {
    if (!workoutData || !userData?.training_programs?.length) return null;

    const activeProgram = userData.training_programs.find(
      (program: ProgramWithWorkouts) => program.program.is_active
    );
    if (!activeProgram) return null;

    const workoutsPerWeek = activeProgram.program_preferences?.program_days_per_week || 3;
    const dayNumber = workoutData.workout.day_number;
    const weekNumber = Math.ceil(dayNumber / workoutsPerWeek);
    const dayInWeek = dayNumber - workoutsPerWeek * (weekNumber - 1);

    return { dayInWeek, weekNumber, totalDayNumber: dayNumber };
  }, [workoutData, userData]);

  // Update parent component with workout details for breadcrumb
  useEffect(() => {
    if (workoutData && onWorkoutDetailsUpdate) {
      onWorkoutDetailsUpdate({
        name: replaceUnderscoresWithSpaces(workoutData.workout.name),
        day_number: workoutData.workout.day_number,
        stages: workoutData.stages.length,
      });
    }
  }, [workoutData, onWorkoutDetailsUpdate]);

  // Toggle stage collapse state
  const toggleStage = (stageId: number) => {
    setCollapsedStages(prev => {
      const newSet = new Set(prev);
      if (newSet.has(stageId)) {
        newSet.delete(stageId);
      } else {
        newSet.add(stageId);
      }
      return newSet;
    });
  };

  // Export handlers
  const handleExportPDF = async () => {
    if (!workoutData) return;
    const workoutName = replaceUnderscoresWithSpaces(workoutData.workout.name);
    await exportWorkoutToPDF(workoutData, weightUnitPreferences, {
      title: workoutName,
      filename: `workout-${workoutName.replace(/\s+/g, '-').toLowerCase()}`,
    });
  };

  // Calculate workout progress metrics
  const getWorkoutProgressMetrics = () => {
    if (!workoutData) return null;

    return calculateWorkoutProgress(workoutData);
  };

  // Render breadcrumbs with integrated progress
  // Get progress metrics for the component
  const progressMetrics = getWorkoutProgressMetrics();

  // Transform data for table
  const tableData = useMemo(() => {
    if (!workoutData) return [];

    const rows: TableRow[] = [];

    workoutData.stages.forEach(stageData => {
      const isCollapsed = collapsedStages.has(stageData.stage.id);

      // Add stage header row
      rows.push({
        id: `stage-${stageData.stage.id}`,
        type: 'stage',
        stageName: stageData.stage.name,
        stageId: stageData.stage.id,
      });

      // Add exercise rows only if stage is not collapsed
      if (!isCollapsed) {
        stageData.exercises.forEach(exerciseData => {
          const setSchemes = exerciseData.set_schemes || [];
          if (setSchemes.length === 0) return;

          // Aggregate set scheme data
          const firstSetScheme = setSchemes[0];
          const totalSets = setSchemes.length;
          const reps = firstSetScheme.target_rep_count;
          const weight = firstSetScheme.target_weight;
          const rest = firstSetScheme.rest_seconds;

          // Get user's weight unit preference for this exercise
          const weightUnitPreference = weightUnitPreferences.find(
            pref => pref.exercise_name === exerciseData.exercise.exercise_name
          );

          // Format tempo if available
          const tempo =
            firstSetScheme.use_tempo &&
            firstSetScheme.eccentric_tempo &&
            firstSetScheme.isometric_tempo &&
            firstSetScheme.concentric_tempo
              ? `${firstSetScheme.eccentric_tempo}-${firstSetScheme.isometric_tempo}-${firstSetScheme.concentric_tempo}`
              : '-';

          rows.push({
            id: `exercise-${exerciseData.exercise.id}`,
            type: 'exercise',
            exerciseName: exerciseData.exercise.exercise_name,
            sets: totalSets,
            reps: reps || undefined,
            tempo: tempo !== '-' ? tempo : undefined,
            weight: weight
              ? formatWeightWithUnit(
                  weight,
                  weightUnitPreference?.preferred_unit as 'KG' | 'LBS' | undefined
                )
              : undefined,
            rest: rest ? `${rest}s` : undefined,
            notes: '-',
            exerciseNotes: exerciseData.exercise.notes,
            stageId: stageData.stage.id,
            exerciseId: exerciseData.exercise.id,
            setSchemes: setSchemes,
            exerciseData: exerciseData,
          });
        });
      }
    });

    return rows;
  }, [workoutData, collapsedStages, weightUnitPreferences]);

  // Define columns
  const columns = useMemo(
    () => [
      columnHelper.accessor('exerciseName', {
        id: 'exercise',
        header: 'Exercise',
        cell: ({ row }) => {
          if (row.original.type === 'exercise') {
            return (
              <Box display="flex" alignItems="center" gap={1} minHeight={40}>
                <SetSchemeEditor
                  exercise={row.original.exerciseData}
                  onExerciseUpdate={async () => {
                    // Refresh data from server to get the updated exercise
                    await refreshData();
                  }}
                  isMostRecentWeek={isMostRecentWeek}
                  weightUnitPreferences={weightUnitPreferences}
                />
                <ExerciseName
                  exerciseName={row.original.exerciseName || ''}
                  variant="body2"
                  sx={{
                    wordWrap: 'break-word',
                    whiteSpace: 'normal',
                    lineHeight: 1.4,
                  }}
                />
              </Box>
            );
          }
          return null;
        },
        size: 300,
        minSize: 200,
        maxSize: 400,
      }),
      columnHelper.accessor('sets', {
        id: 'sets',
        header: 'Sets',
        cell: ({ row }) => {
          if (row.original.type === 'exercise') {
            return <GameText variant="body2">{row.original.sets}</GameText>;
          }
          return null;
        },
        size: 60,
        minSize: 50,
        maxSize: 80,
      }),
      columnHelper.accessor('reps', {
        id: 'reps',
        header: 'Reps',
        cell: ({ row }) => {
          if (row.original.type === 'exercise') {
            return <GameText variant="body2">{row.original.reps || '-'}</GameText>;
          }
          return null;
        },
        size: 60,
        minSize: 50,
        maxSize: 80,
      }),
      columnHelper.accessor('tempo', {
        id: 'tempo',
        header: 'Tempo',
        cell: ({ row }) => {
          if (row.original.type === 'exercise') {
            return <GameText variant="body2">{row.original.tempo || '-'}</GameText>;
          }
          return null;
        },
        size: 80,
        minSize: 70,
        maxSize: 100,
      }),
      columnHelper.accessor('weight', {
        id: 'weight',
        header: 'Weight',
        cell: ({ row }) => {
          if (row.original.type === 'exercise') {
            return <GameText variant="body2">{row.original.weight || '-'}</GameText>;
          }
          return null;
        },
        size: 80,
        minSize: 70,
        maxSize: 100,
      }),
      columnHelper.accessor('rest', {
        id: 'rest',
        header: 'Rest',
        cell: ({ row }) => {
          if (row.original.type === 'exercise') {
            return <GameText variant="body2">{row.original.rest || '-'}</GameText>;
          }
          return null;
        },
        size: 60,
        minSize: 50,
        maxSize: 80,
      }),
      columnHelper.accessor('notes', {
        id: 'notes',
        header: 'Notes',
        cell: ({ row }) => {
          if (row.original.type === 'exercise') {
            const canEdit = isMostRecentWeek && !saving;

            return (
              <Box
                sx={{
                  minHeight: 40,
                  minWidth: 200,
                  maxWidth: 300,
                  cursor: canEdit ? 'pointer' : 'default',
                  p: 1,
                  borderRadius: 1,
                  border: canEdit ? '1px dashed transparent' : 'none',
                  '&:hover': canEdit
                    ? {
                        border: '1px dashed',
                        borderColor: 'primary.main',
                        backgroundColor: 'action.hover',
                      }
                    : {},
                }}
                onClick={() => {
                  if (canEdit) {
                    handleOpenNotesEditor(row.original.exerciseData);
                  }
                }}
              >
                {row.original.exerciseNotes ? (
                  <RichTextDisplay
                    content={row.original.exerciseNotes}
                    sx={{
                      wordWrap: 'break-word',
                      whiteSpace: 'normal',
                      lineHeight: 1.4,
                      '& p': { margin: 0 },
                      '& strong': { fontWeight: 'bold' },
                      '& em': { fontStyle: 'italic' },
                      '& u': { textDecoration: 'underline' },
                    }}
                  />
                ) : (
                  <GameText
                    variant="body2"
                    textVariant="secondary"
                    className={GAME_CLASSES.textItalic}
                  >
                    {canEdit ? 'Click to add notes...' : 'No notes'}
                  </GameText>
                )}
              </Box>
            );
          }
          return null;
        },
        size: 300,
        minSize: 200,
        maxSize: 400,
      }),
    ],
    [isMostRecentWeek, saving, handleOpenNotesEditor]
  );

  // Create table instance
  const table = useReactTable({
    data: tableData,
    columns,
    getCoreRowModel: getCoreRowModel(),
    columnResizeMode: 'onChange',
    enableColumnResizing: true,
  });

  if (isLoading) {
    return <LoadingSpinner message="Loading workout details..." fullHeight={false} />;
  }

  if (!workoutData) {
    return <Alert severity="warning">Workout not found.</Alert>;
  }

  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.8, ease: 'easeOut' }}
      style={{
        marginTop: '24px',
      }}
    >
      <motion.div
        initial={{ opacity: 0, x: -30 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.6, ease: 'easeOut', delay: 0.2 }}
        style={{ position: 'relative', marginBottom: '24px' }}
      >
        {/* Header Section */}
        <WorkoutHeader
          context="day"
          dayNumber={dayInWeekData?.dayInWeek}
          totalDayNumber={dayInWeekData?.totalDayNumber}
          workoutName={workoutData?.workout.workout_name}
          totalExercises={progressMetrics?.totalExercises}
          completedExercises={progressMetrics?.completedExercises}
          onExportPDF={handleExportPDF}
          onBack={onBack}
          onAddExercise={handleOpenAddExerciseDialog}
          disabled={!workoutData}
          saving={saving}
        />
      </motion.div>

      <Grid
        container
        spacing={3}
        sx={{
          px: 3,
        }}
      >
        {/* Table Container - 2/3 width */}
        <Grid size={{ xs: 12, lg: 8 }} sx={{}}>
          <GameCard
            className="glassmorphism-card"
            sx={{ width: '100%', height: '100%', overflow: 'hidden' }}
          >
            <Box
              sx={{
                overflow: 'auto',
              }}
            >
              <table
                style={{
                  width: '100%',
                  borderCollapse: 'collapse',
                  tableLayout: 'fixed',
                }}
              >
                {/* Table Column Headers */}
                <thead
                  style={{
                    position: 'sticky',
                    top: 0,
                    zIndex: 999,
                    backgroundColor: 'var(--game-cyan-light)',
                    borderBottom: '2px solid var(--game-cyan-border)',
                  }}
                >
                  {table.getHeaderGroups().map(headerGroup => (
                    <tr key={headerGroup.id}>
                      {headerGroup.headers.map(header => (
                        <th
                          key={header.id}
                          style={{
                            padding: '12px 8px',
                            textAlign: 'left',
                            fontWeight: 'bold',
                            borderBottom: '1px solid var(--game-cyan-border)',
                            backgroundColor: 'var(--game-cyan-light)',
                            color: 'var(--game-cyan)',
                            width: `${(header.getSize() / 860) * 100}%`,
                            minWidth: `${((header.column.columnDef.minSize || 50) / 860) * 100}%`,
                          }}
                        >
                          {header.isPlaceholder
                            ? null
                            : flexRender(header.column.columnDef.header, header.getContext())}
                        </th>
                      ))}
                    </tr>
                  ))}
                </thead>
                <tbody>
                  {table.getRowModel().rows.map(row => (
                    <tr
                      key={row.id}
                      style={{
                        backgroundColor:
                          row.original.type === 'stage' ? 'var(--game-cyan-light)' : 'transparent',
                        borderBottom: '1px solid var(--game-cyan-light)',
                        color: 'var(--game-white)',
                      }}
                    >
                      {row.original.type === 'stage' ? (
                        // Stage header row - spans all columns with collapse functionality
                        <td
                          colSpan={columns.length}
                          style={{
                            padding: '12px 16px',
                            textAlign: 'left',
                            fontWeight: 'bold',
                            color: 'var(--game-cyan)',
                            backgroundColor: 'var(--game-cyan-light)',
                            borderBottom: '1px solid var(--game-cyan-border)',
                            cursor: 'pointer',
                          }}
                          onClick={() => row.original.stageId && toggleStage(row.original.stageId)}
                        >
                          <Box display="flex" alignItems="center" justifyContent="space-between">
                            <GameText
                              variant="h6"
                              textVariant="accent"
                              className={GAME_CLASSES.textBold}
                            >
                              {row.original.stageName}
                            </GameText>
                            <IconButton
                              size="small"
                              onClick={e => {
                                e.stopPropagation();
                                if (row.original.stageId) {
                                  toggleStage(row.original.stageId);
                                }
                              }}
                              sx={{ color: 'var(--game-cyan)' }}
                            >
                              {row.original.stageId && collapsedStages.has(row.original.stageId) ? (
                                <ExpandMoreIcon />
                              ) : (
                                <ExpandLessIcon />
                              )}
                            </IconButton>
                          </Box>
                        </td>
                      ) : (
                        // Exercise row - normal columns
                        row.getVisibleCells().map(cell => (
                          <td
                            key={cell.id}
                            style={{
                              padding: '8px',
                              borderBottom: `1px solid ${theme.palette.divider}`,
                              width: `${(cell.column.getSize() / 860) * 100}%`,
                              minWidth: `${((cell.column.columnDef.minSize || 50) / 860) * 100}%`,
                              color: theme.palette.text.primary,
                              wordWrap: 'break-word',
                              overflow: 'hidden',
                              textAlign: 'center',
                              verticalAlign: 'middle',
                              minHeight: '40px',
                            }}
                          >
                            {flexRender(cell.column.columnDef.cell, cell.getContext())}
                          </td>
                        ))
                      )}
                    </tr>
                  ))}
                </tbody>
              </table>
            </Box>
          </GameCard>
        </Grid>

        {/* Charts - 1/3 width */}
        <Grid size={{ xs: 12, lg: 4 }} sx={{}}>
          <Box
            sx={{
              display: 'flex',
              flexDirection: 'column',
              gap: 3,
            }}
          >
            {/* Exercise Volume Hierarchy Chart */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, delay: 0.2 }}
              whileHover={{ y: -8 }}
            >
              <SunburstChart
                workoutData={workoutData}
                exerciseMuscleData={exerciseMuscleData}
                weightUnitPreferences={weightUnitPreferences}
                selectedExercise="all"
              />
            </motion.div>
            {/* Exercise Correlations Chord Chart */}
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.4, delay: 0.3 }}
              whileHover={{ y: -8 }}
            >
              <ChordChart
                workoutData={workoutData as unknown as Record<string, unknown>}
                title="Exercise Support Correlation"
                height={300}
              />
            </motion.div>
          </Box>
        </Grid>
      </Grid>

      {/* Notes Editor Modal */}
      <Dialog
        open={notesEditorOpen}
        onClose={handleCloseNotesEditor}
        maxWidth="md"
        fullWidth
        PaperProps={{
          sx: { minHeight: '60vh' },
        }}
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <GameText variant="h6">
              Edit Notes: {selectedExerciseForNotes?.exercise?.exercise_name}
            </GameText>
          </Box>
        </DialogTitle>

        <DialogContent>
          <Alert severity="info" sx={{ mb: 2 }}>
            Add detailed notes for this exercise. Use formatting to organize your thoughts and
            instructions.
          </Alert>

          <RichTextEditor
            value={notesContent}
            onChange={handleNotesContentChange}
            placeholder="Add exercise notes, form cues, or reminders..."
            showToolbar={true}
            minHeight={300}
            maxHeight={400}
            autoSave={false}
          />
        </DialogContent>

        <DialogActions>
          <Button onClick={handleCloseNotesEditor} disabled={saving}>
            Cancel
          </Button>
          <Button onClick={handleSaveNotesFromModal} variant="contained" disabled={saving}>
            Save Notes
          </Button>
        </DialogActions>
      </Dialog>

      {/* Add Exercise Dialog */}
      <Dialog
        open={addExerciseDialogOpen}
        onClose={handleCloseAddExerciseDialog}
        maxWidth="sm"
        fullWidth
        PaperProps={{
          sx: { minHeight: '50vh' },
        }}
      >
        <DialogTitle>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <GameText variant="h6">Add Exercise to Workout</GameText>
          </Box>
        </DialogTitle>

        <DialogContent>
          <Alert severity="info" sx={{ mb: 2 }}>
            Select an exercise and the stage where it should be added. Configure the set scheme
            details below.
          </Alert>

          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            {/* Exercise Selection */}
            <Autocomplete
              id="workout-exercise-autocomplete"
              options={availableExercises}
              value={selectedExercise}
              onChange={(_, newValue) => setSelectedExercise(newValue)}
              getOptionLabel={option => option.name}
              loading={loadingExercises}
              renderInput={params => (
                <GameTextField
                  {...params}
                  label="Exercise"
                  placeholder="Search for an exercise..."
                  required
                  helperText="Start typing to search for exercises"
                />
              )}
              renderOption={(props, option) => (
                <Box component="li" {...props}>
                  <Box>
                    <GameText variant="body1">{option.name}</GameText>
                    <GameText variant="caption" textVariant="secondary">
                      {option.movement_type} • {option.is_upper ? 'Upper' : 'Lower'} Body
                    </GameText>
                  </Box>
                </Box>
              )}
            />

            {/* Stage Selection */}
            <GameFormControl fullWidth required>
              <GameInputLabel>Stage</GameInputLabel>
              <GameSelect
                value={selectedStageId}
                onChange={e => setSelectedStageId(e.target.value as number)}
                label="Stage"
              >
                {workoutData?.stages.map(stageData => (
                  <GameMenuItem key={stageData.stage.id} value={stageData.stage.id}>
                    {stageData.stage.name}
                  </GameMenuItem>
                ))}
              </GameSelect>
            </GameFormControl>

            {/* Set Scheme Details */}
            <SetSchemeForm
              form={addExerciseForm}
              saving={saving}
              exerciseName={selectedExercise?.name}
              weightUnitPreferences={weightUnitPreferences}
              showPerformedFields={false}
              showTempoFields={true}
              showSetTypeFields={true}
            />
          </Box>
        </DialogContent>

        <DialogActions>
          <Button onClick={handleCloseAddExerciseDialog} disabled={saving}>
            Cancel
          </Button>
          <Button
            onClick={() => addExerciseForm.handleSubmit()}
            variant="contained"
            disabled={saving || !selectedExercise || !selectedStageId}
          >
            Add Exercise
          </Button>
        </DialogActions>
      </Dialog>
    </motion.div>
  );
};
