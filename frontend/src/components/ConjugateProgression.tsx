import { Search as SearchIcon } from '@mui/icons-material';
import { Box, Card, CardContent, Grid, Typography, TextField, InputAdornment } from '@mui/material';
import {
  createColumnHelper,
  getCoreRowModel,
  getFilteredRowModel,
  useReactTable,
  flexRender,
} from '@tanstack/react-table';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';

import { LineChart } from './LineChart';
import { LoadingSpinner } from './LoadingSpinner';
import { PieChart } from './PieChart';
import { getIndividualExercise } from '../api/exercise';
import { getUserDataExport } from '../api/gdpr';
import type {
  User,
  Exercise,
  UserOneRepMax,
  UserDataExport,
  ProgramWithWorkouts,
} from '../api/types';
import { getUserOneRepMaxes } from '../api/userOneRepMax';
import { getUserWeightUnitPreferences } from '../api/userWeightUnitPreference';
import type { UserWeightUnitPreference } from '../api/userWeightUnitPreference';

interface ConjugateProgressionProps {
  user: User;
}

/**
 * Enhanced Conjugate Progression component displaying actual user statistics and progress.
 *
 * Based on Westside Barbell conjugate method principles, shows:
 * - Volume tracking (total weight lifted including bands)
 * - Exercise volume by workout stage analysis
 * - Progress tracking (1RM improvements over time)
 * - Training intensity distribution
 *
 * @param user The user data
 * @return Enhanced conjugate progression component
 */
export const ConjugateProgression: React.FC<ConjugateProgressionProps> = ({ user }) => {
  const { enqueueSnackbar } = useSnackbar();
  const [userData, setUserData] = useState<UserDataExport | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [oneRepMaxes, setOneRepMaxes] = useState<UserOneRepMax[]>([]);
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>(
    []
  );
  const [globalFilter, setGlobalFilter] = useState('');

  // Table configuration
  const columnHelper = createColumnHelper<{
    exerciseName: string;
    oneRepMax: number;
    unit: string;
  }>();

  const columns = [
    columnHelper.accessor('exerciseName', {
      header: 'Exercise',
      cell: info => info.getValue(),
    }),
    columnHelper.accessor('oneRepMax', {
      header: '1RM',
      cell: info => `${info.getValue()} ${info.row.original.unit}`,
    }),
  ];

  // Process 1RM data for table
  const tableData = useMemo(() => {
    return oneRepMaxes.map(oneRepMax => {
      // Find user's preferred unit for this exercise
      const weightUnitPreference = weightUnitPreferences.find(
        pref => pref.exercise_name === oneRepMax.exercise_name
      );

      // Convert weight to user's preferred unit if available
      let displayWeight = oneRepMax.one_rep_max;
      let displayUnit = oneRepMax.unit;

      if (weightUnitPreference?.preferred_unit === 'KG' && oneRepMax.unit === 'LBS') {
        displayWeight = Math.round(oneRepMax.one_rep_max / 2.20462);
        displayUnit = 'KG';
      } else if (weightUnitPreference?.preferred_unit === 'LBS' && oneRepMax.unit === 'KG') {
        displayWeight = Math.round(oneRepMax.one_rep_max * 2.20462);
        displayUnit = 'LBS';
      }

      return {
        exerciseName: oneRepMax.exercise_name,
        oneRepMax: displayWeight,
        unit: displayUnit,
      };
    });
  }, [oneRepMaxes, weightUnitPreferences]);

  const table = useReactTable({
    data: tableData,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    state: {
      globalFilter,
    },
    onGlobalFilterChange: setGlobalFilter,
    globalFilterFn: 'includesString',
  });

  // Load all workout data using optimized single API call
  useEffect(() => {
    const loadWorkoutData = async () => {
      try {
        setIsLoading(true);

        // Load all data in parallel
        const [dataExport, oneRepMaxesData, weightUnitData] = await Promise.all([
          getUserDataExport(),
          getUserOneRepMaxes(user.keycloak_id),
          getUserWeightUnitPreferences(user.keycloak_id),
        ]);

        setUserData(dataExport);
        setOneRepMaxes(oneRepMaxesData);
        setWeightUnitPreferences(weightUnitData || []);

        // Fetch exercise data for all unique exercises
        // Handle case where user has no training programs (empty array)
        const uniqueExercises = new Set<string>();
        (dataExport.training_programs as ProgramWithWorkouts[])?.forEach(program => {
          program.workouts.forEach(workout => {
            workout.stages.forEach(stage => {
              stage.exercises.forEach(exercise => {
                uniqueExercises.add(exercise.exercise.exercise_name);
              });
            });
          });
        });

        const exerciseMap = new Map<string, Exercise>();
        for (const exerciseName of Array.from(uniqueExercises)) {
          try {
            const exercise = await getIndividualExercise(exerciseName);
            exerciseMap.set(exerciseName, exercise);
          } catch {
            enqueueSnackbar(`Error fetching exercise data for ${exerciseName}`, {
              variant: 'error',
            });
          }
        }

        setExerciseData(exerciseMap);
      } catch {
        enqueueSnackbar('Failed to load workout data. Please try again.', { variant: 'error' });
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutData();
  }, [user.keycloak_id]);

  if (isLoading) {
    return (
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <LoadingSpinner message="Loading conjugate progression..." fullHeight={false} />
        </CardContent>
      </Card>
    );
  }

  if (!userData?.training_programs || userData.training_programs.length === 0) {
    return (
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Conjugate Progress Tracking
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Complete your first workout to see progress statistics and correlations.
          </Typography>
        </CardContent>
      </Card>
    );
  }

  return (
    <Grid container spacing={3}>
      {/* Volume Tracking Chart */}
      <Grid size={{ xs: 12, lg: 8 }}>
        <LineChart
          userDataExport={userData}
          exerciseData={exerciseData}
          chartType="volume"
          title="Volume Progression"
          description="Total weight lifted over time (including band resistance)"
          xAxisLabel="Workout Date"
          yAxisLabel="Volume (lbs)"
        />
      </Grid>

      {/* Exercise Category Distribution */}
      <Grid size={{ xs: 12, lg: 4 }}>
        <PieChart
          userDataExport={userData}
          exerciseData={exerciseData}
          weightUnitPreferences={weightUnitPreferences}
          title="Exercise Distribution"
          description="Volume by workout stage"
        />
      </Grid>

      {/* 1RM Table and Progress Tracking */}
      <Grid size={{ xs: 12, lg: 4 }}>
        <Card variant="outlined">
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Current 1RM Values
            </Typography>
            <TextField
              fullWidth
              size="small"
              placeholder="Search exercises..."
              value={globalFilter}
              onChange={e => setGlobalFilter(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon />
                  </InputAdornment>
                ),
              }}
              sx={{ mb: 2 }}
            />
            <Box sx={{ maxHeight: 400, overflow: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  {table.getHeaderGroups().map(headerGroup => (
                    <tr key={headerGroup.id}>
                      {headerGroup.headers.map(header => (
                        <th
                          key={header.id}
                          style={{
                            textAlign: 'left',
                            padding: '8px',
                            borderBottom: '1px solid #e0e0e0',
                            fontWeight: 'bold',
                          }}
                        >
                          {flexRender(header.column.columnDef.header, header.getContext())}
                        </th>
                      ))}
                    </tr>
                  ))}
                </thead>
                <tbody>
                  {table.getRowModel().rows.map(row => (
                    <tr key={row.id}>
                      {row.getVisibleCells().map(cell => (
                        <td
                          key={cell.id}
                          style={{
                            padding: '8px',
                            borderBottom: '1px solid #f0f0f0',
                          }}
                        >
                          {flexRender(cell.column.columnDef.cell, cell.getContext())}
                        </td>
                      ))}
                    </tr>
                  ))}
                </tbody>
              </table>
            </Box>
          </CardContent>
        </Card>
      </Grid>

      {/* Progress Tracking Chart */}
      <Grid size={{ xs: 12, lg: 8 }}>
        <LineChart
          userDataExport={userData}
          exerciseData={exerciseData}
          chartType="progress"
          title="Progress Tracking"
          description="1RM improvements and volume progression over time"
          xAxisLabel="Date"
          yAxisLabel="Weight (lbs)"
        />
      </Grid>
    </Grid>
  );
};
