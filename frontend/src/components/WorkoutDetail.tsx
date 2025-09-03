import {
  Notes as NotesIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
} from '@mui/icons-material';
import { Box, Typography, Alert, IconButton, Tooltip, Paper, useTheme, Grid } from '@mui/material';
import {
  useReactTable,
  getCoreRowModel,
  flexRender,
  createColumnHelper,
} from '@tanstack/react-table';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';

import { ChordChart } from './ChordChart';
import { LoadingSpinner } from './LoadingSpinner';
import { SunburstChart } from './SunburstChart';
import { getExerciseMuscle } from '../api/exerciseMuscle';
import { getUserDataExport } from '../api/gdpr';
import type { UserDataExport, ExerciseMuscle, UserWeightUnitPreference } from '../api/types';
import { getUserWeightUnitPreferences } from '../api/userWeightUnitPreference';
import { replaceUnderscoresWithSpaces, formatWeightWithUnit } from '../common/utils';
import { useAuth } from '../contexts/AuthContext';

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
  onWorkoutDetailsUpdate,
}) => {
  const theme = useTheme();
  const { enqueueSnackbar } = useSnackbar();
  const { user } = useAuth();
  const [userData, setUserData] = useState<UserDataExport | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [collapsedStages, setCollapsedStages] = useState<Set<number>>(new Set());

  const [exerciseMuscleData, setExerciseMuscleData] = useState<Map<string, string[]>>(new Map());
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>(
    []
  );

  useEffect(() => {
    const loadWorkoutDetails = async () => {
      try {
        setIsLoading(true);

        // Load user data first to get the workout data, then load chart data
        const dataExport = await getUserDataExport();
        setUserData(dataExport);

        // Load data needed for the chart
        const [exerciseMuscleData, weightUnitPreferencesData] = await Promise.all([
          getExerciseMuscle(),
          getUserWeightUnitPreferences(user?.keycloak_id || ''),
        ]);

        // Convert exercise muscle data to Map
        const muscleMap = new Map<string, string[]>();
        exerciseMuscleData.forEach((item: ExerciseMuscle) => {
          const existing = muscleMap.get(item.exercise_name) || [];
          existing.push(item.muscle_name);
          muscleMap.set(item.exercise_name, existing);
        });
        setExerciseMuscleData(muscleMap);

        setWeightUnitPreferences(weightUnitPreferencesData || []);
      } catch {
        enqueueSnackbar('Failed to load workout details. Please try again.', { variant: 'error' });
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutDetails();
  }, [workoutId, user?.keycloak_id]);

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
              ? formatWeightWithUnit(weight, weightUnitPreference?.preferred_unit)
              : undefined,
            rest: rest ? `${rest}s` : undefined,
            notes: '-',
            exerciseNotes: exerciseData.exercise.notes,
            stageId: stageData.stage.id,
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
              <Box display="flex" alignItems="center" gap={1}>
                <Typography
                  variant="body2"
                  sx={{
                    wordWrap: 'break-word',
                    whiteSpace: 'normal',
                    lineHeight: 1.4,
                  }}
                >
                  {row.original.exerciseName}
                </Typography>
                {row.original.exerciseNotes && (
                  <Tooltip title={row.original.exerciseNotes} arrow>
                    <IconButton size="small">
                      <NotesIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
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
      columnHelper.accessor('sets', {
        id: 'sets',
        header: 'Sets',
        cell: ({ row }) => {
          if (row.original.type === 'exercise') {
            return <Typography variant="body2">{row.original.sets}</Typography>;
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
            return <Typography variant="body2">{row.original.reps || '-'}</Typography>;
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
            return <Typography variant="body2">{row.original.tempo || '-'}</Typography>;
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
            return <Typography variant="body2">{row.original.weight || '-'}</Typography>;
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
            return <Typography variant="body2">{row.original.rest || '-'}</Typography>;
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
            return (
              <Typography
                variant="body2"
                sx={{
                  wordWrap: 'break-word',
                  whiteSpace: 'normal',
                  lineHeight: 1.4,
                }}
              >
                {row.original.notes}
              </Typography>
            );
          }
          return null;
        },
        size: 200,
        minSize: 150,
        maxSize: 300,
      }),
    ],
    []
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
    <Box sx={{ height: 'calc(100vh - 48px)', overflow: 'auto' }}>
      <Grid container spacing={3} sx={{ height: '100%' }}>
        {/* Table Container - 2/3 width */}
        <Grid size={{ xs: 12, lg: 8 }}>
          <Paper sx={{ width: '100%', overflow: 'hidden', height: '100%' }}>
            <Box sx={{ overflow: 'auto', maxHeight: 'calc(100vh - 48px)' }}>
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
                    backgroundColor: theme.palette.background.paper,
                    borderBottom: 'none',
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
                            borderBottom: `1px solid ${theme.palette.divider}`,
                            backgroundColor: theme.palette.background.paper,
                            color: theme.palette.text.primary,
                            width: `${(header.getSize() / 860) * 100}%`, // Total approximate width of all columns
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
                          row.original.type === 'stage'
                            ? theme.palette.mode === 'dark'
                              ? theme.palette.grey[800]
                              : theme.palette.grey[100]
                            : theme.palette.background.paper,
                        borderBottom: `1px solid ${theme.palette.divider}`,
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
                            color: theme.palette.primary.main,
                            backgroundColor:
                              row.original.type === 'stage'
                                ? theme.palette.mode === 'dark'
                                  ? theme.palette.grey[800]
                                  : theme.palette.grey[100]
                                : theme.palette.background.paper,
                            borderBottom: `1px solid ${theme.palette.divider}`,
                            cursor: 'pointer',
                          }}
                          onClick={() => row.original.stageId && toggleStage(row.original.stageId)}
                        >
                          <Box display="flex" alignItems="center" justifyContent="space-between">
                            <Typography variant="h6" color="primary" sx={{ fontWeight: 'bold' }}>
                              {row.original.stageName}
                            </Typography>
                            <IconButton
                              size="small"
                              onClick={e => {
                                e.stopPropagation();
                                if (row.original.stageId) {
                                  toggleStage(row.original.stageId);
                                }
                              }}
                              sx={{ color: theme.palette.primary.main }}
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
                              width: `${(cell.column.getSize() / 860) * 100}%`, // Total approximate width of all columns
                              minWidth: `${((cell.column.columnDef.minSize || 50) / 860) * 100}%`,
                              color: theme.palette.text.primary,
                              wordWrap: 'break-word',
                              overflow: 'hidden',
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
          </Paper>
        </Grid>

        {/* Charts - 1/3 width */}
        <Grid size={{ xs: 12, lg: 4 }}>
          <Box sx={{ mt: 3, display: 'flex', flexDirection: 'column', gap: 3 }}>
            {/* Exercise Volume Hierarchy Chart */}
            <SunburstChart
              workoutData={workoutData}
              exerciseMuscleData={exerciseMuscleData}
              weightUnitPreferences={weightUnitPreferences}
              selectedExercise="all"
            />
            {/* Exercise Correlations Chord Chart */}
            <ChordChart
              workoutData={workoutData as unknown as Record<string, unknown>}
              title="Exercise Support Correlation"
              height={300}
            />
          </Box>
        </Grid>
      </Grid>
    </Box>
  );
};
