import { Search as SearchIcon } from '@mui/icons-material';
import { Box, Grid, InputAdornment, CardContent } from '@mui/material';
import {
  createColumnHelper,
  getCoreRowModel,
  getFilteredRowModel,
  useReactTable,
  flexRender,
} from '@tanstack/react-table';
import React, { useMemo, useState, useEffect } from 'react';

import { ExerciseName } from './ExerciseName';
import { GameText, GameCard, GameTextField, GAME_CLASSES } from './GameTheme';
import { LineChart } from './LineChart';
import { LoadingSpinner } from './LoadingSpinner';
import { PieChart } from './PieChart';
import type { Exercise, UserOneRepMax } from '../api/types';
import { formatWeightWithUnit, KG_TO_LBS } from '../common/utils';
import { useData } from '../contexts/DataContext';

// eslint-disable-next-line @typescript-eslint/no-empty-object-type
interface ConjugateProgressionProps {}

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
export const ConjugateProgression: React.FC<ConjugateProgressionProps> = () => {
  const {
    userData,
    weightUnitPreferences,
    userOneRepMaxes,
    isLoading: isDataLoading,
    getExercise,
  } = useData();

  // State for loaded data
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [globalFilter, setGlobalFilter] = useState('');

  const oneRepMaxes = useMemo(() => {
    if (userOneRepMaxes.length > 0) {
      return userOneRepMaxes;
    }
    return (userData?.user_one_rep_max as unknown as UserOneRepMax[]) ?? [];
  }, [userOneRepMaxes, userData]);

  // Load additional data that's not in DataContext
  useEffect(() => {
    const loadAdditionalData = async () => {
      if (!userData) return;

      setIsLoading(true);
      setError(null);
      try {
        // Extract unique exercises from userData and fetch exercise details
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

        // Fetch exercise details for all unique exercises using DataContext
        const exercisePromises = Array.from(uniqueExercises).map(exerciseName =>
          getExercise(exerciseName).catch(() => null)
        );
        const exerciseResults = await Promise.all(exercisePromises);

        const exerciseMap = new Map<string, Exercise>();
        exerciseResults.forEach(exercise => {
          if (exercise) {
            exerciseMap.set(exercise.name, exercise);
          }
        });
        setExerciseData(exerciseMap);
      } catch {
        setError('Failed to load additional progression data');
      } finally {
        setIsLoading(false);
      }
    };

    loadAdditionalData();
  }, [userData, getExercise]);

  // Table configuration
  const columnHelper = createColumnHelper<{
    exerciseName: string;
    oneRepMax: number;
    displayWeight: string;
  }>();

  const columns = [
    columnHelper.accessor('exerciseName', {
      header: 'Exercise',
      cell: info => <ExerciseName exerciseName={info.getValue()} variant="body2" />,
    }),
    columnHelper.accessor('oneRepMax', {
      header: '1RM',
      cell: info => info.row.original.displayWeight,
    }),
  ];

  // Process 1RM data for table (normalize to kg, then use common formatter)
  const tableData = useMemo(() => {
    return oneRepMaxes.map(oneRepMax => {
      const weightUnitPreference = weightUnitPreferences.find(
        pref => pref.exercise_name === oneRepMax.exercise_name
      );
      const preferredUnit = weightUnitPreference?.preferred_unit as 'KG' | 'LBS' | undefined;
      const weightInKg =
        oneRepMax.unit === 'LBS' ? oneRepMax.one_rep_max / KG_TO_LBS : oneRepMax.one_rep_max;
      const displayWeight = formatWeightWithUnit(weightInKg, preferredUnit);
      const oneRepMaxNumeric =
        parseFloat(formatWeightWithUnit(weightInKg, preferredUnit, false)) || 0;

      return {
        exerciseName: oneRepMax.exercise_name,
        oneRepMax: oneRepMaxNumeric,
        displayWeight,
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

  // Show loading state
  if (isDataLoading || isLoading) {
    return <LoadingSpinner message="Loading progression data..." fullHeight={false} />;
  }

  // Show error state
  if (error) {
    return (
      <GameCard>
        <CardContent>
          <GameText textVariant="error">{error}</GameText>
        </CardContent>
      </GameCard>
    );
  }

  // Show message if no data
  if (!userData || !userData.training_programs || userData.training_programs.length === 0) {
    return (
      <GameCard className={GAME_CLASSES.marginTop3}>
        <CardContent>
          <GameText variant="h5" gutterBottom>
            Conjugate Progress Tracking
          </GameText>
          <GameText>
            Complete your first workout to see progress statistics and correlations.
          </GameText>
        </CardContent>
      </GameCard>
    );
  }

  return (
    <Grid container spacing={3} sx={{ mt: 3 }}>
      {/* Progress Tracking Chart - Shows strength gains over time */}
      <Grid size={{ xs: 12, lg: 6 }}>
        <LineChart
          userDataExport={userData}
          exerciseData={exerciseData}
          chartType="progress"
          title="Strength & Volume Progress"
          description="1RM improvements and volume trends over time"
          xAxisLabel="Workout Date"
          yAxisLabel="Weight (lbs)"
        />
      </Grid>

      {/* Exercise Category Distribution */}
      <Grid size={{ xs: 12, lg: 6 }}>
        <PieChart
          userDataExport={userData}
          exerciseData={exerciseData}
          weightUnitPreferences={weightUnitPreferences}
          title="Exercise Distribution"
          description="Volume by workout stage"
        />
      </Grid>

      {/* 1RM Table and Progress Tracking - Enhanced UX design */}
      <Grid size={{ xs: 12 }}>
        <GameCard
          sx={{
            background:
              'linear-gradient(135deg, rgba(25, 118, 210, 0.05) 0%, rgba(156, 39, 176, 0.05) 100%)',
            border: '1px solid',
            borderColor: 'primary.light',
          }}
        >
          <CardContent>
            <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
              <GameText variant="h6" className={GAME_CLASSES.textMedium}>
                Personal Records (1RM)
              </GameText>
              <Box
                sx={{
                  backgroundColor: 'primary.main',
                  color: 'primary.contrastText',
                  borderRadius: '50%',
                  width: 24,
                  height: 24,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: '0.75rem',
                  fontWeight: 'bold',
                }}
              >
                {oneRepMaxes.length}
              </Box>
            </Box>
            <GameText
              variant="body2"
              textVariant="secondary"
              className={GAME_CLASSES.marginBottom2}
            >
              Track your strength progress and personal records • Click to view details
            </GameText>
            <GameTextField
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
            <Box
              sx={{
                maxHeight: 400,
                overflow: 'auto',
                borderRadius: 2,
                border: '1px solid rgba(0, 188, 212, 0.3)',
                backgroundColor: 'rgba(255, 255, 255, 0.05)',
                backdropFilter: 'blur(20px)',
              }}
            >
              <table
                style={{
                  width: '100%',
                  borderCollapse: 'collapse',
                  fontFamily: 'inherit',
                }}
              >
                <thead
                  style={{
                    position: 'sticky',
                    top: 0,
                    zIndex: 1,
                  }}
                >
                  {table.getHeaderGroups().map(headerGroup => (
                    <tr key={headerGroup.id}>
                      {headerGroup.headers.map((header, index) => (
                        <th
                          key={header.id}
                          style={{
                            textAlign: index === 1 ? 'right' : 'left',
                            padding: '12px 16px',
                            borderBottom: '2px solid rgba(0, 188, 212, 0.3)',
                            fontWeight: '600',
                            backgroundColor: 'rgba(0, 188, 212, 0.1)',
                            color: '#00bcd4',
                            fontSize: '0.875rem',
                            textTransform: 'uppercase',
                            letterSpacing: '0.5px',
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
                      {row.getVisibleCells().map((cell, index) => (
                        <td
                          key={cell.id}
                          style={{
                            padding: '12px 16px',
                            borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
                            fontSize: '0.875rem',
                            color: '#ffffff',
                            transition: 'background-color 0.2s ease',
                            textAlign: index === 1 ? 'right' : 'left',
                          }}
                          onMouseEnter={e => {
                            e.currentTarget.style.backgroundColor = 'rgba(0, 188, 212, 0.15)';
                          }}
                          onMouseLeave={e => {
                            e.currentTarget.style.backgroundColor = 'transparent';
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
        </GameCard>
      </Grid>
    </Grid>
  );
};
