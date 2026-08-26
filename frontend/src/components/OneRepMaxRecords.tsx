import { Box, CardContent, Button, useTheme } from '@mui/material';
import { useField } from '@tanstack/react-form';
import {
  useReactTable,
  getCoreRowModel,
  getFilteredRowModel,
  flexRender,
  createColumnHelper,
} from '@tanstack/react-table';
import { useVirtualizer } from '@tanstack/react-virtual';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo, useRef } from 'react';

import { ExerciseName } from './ExerciseName';
import { FormDialog } from './FormDialog';
import { FormField } from './FormField';
import { GameCard, GameText, GameTextField, GAME_CLASSES } from './GameTheme';
import { LoadingSpinner } from './LoadingSpinner';
import { NumericStepInput } from './NumericStepInput';
import { VolumeTrendSparkline } from './VolumeTrendSparkline';
import type { UserOneRepMax } from '../api/types';
import { formatWeightWithUnit, KG_TO_LBS } from '../common/utils';
import { useActiveProgramContext } from '../hooks/useActiveProgramContext';
import { useData } from '../contexts/DataContext';
import { createCongenNivoTheme } from '../theme/nivoTheme';
import { buildAllExerciseWorkoutTrends } from '../utils/performanceAnalyticsUtils';
import type { VolumeTrendPoint } from '../utils/volumeOverviewUtils';

// eslint-disable-next-line @typescript-eslint/no-empty-object-type
interface OneRepMaxRecordsProps {
  // No props needed for this component
}

/**
 * Component for displaying and managing user 1RM records.
 *
 * Features:
 * - Displays all user 1RM records in a searchable table
 * - Converts weights to user's preferred units
 * - Virtualized table for performance with large datasets
 * - Consistent styling with other workout components
 *
 * @return OneRepMaxRecords component
 */
export const OneRepMaxRecords: React.FC<OneRepMaxRecordsProps> = () => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  const {
    weightUnitPreferences,
    isLoading: isDataLoading,
    upsertUserOneRepMax,
    allExercises,
    loadAllExercises,
    userOneRepMaxes,
    loadUserOneRepMaxes,
  } = useData();
  const { userData, workoutsPerWeek } = useActiveProgramContext();
  const { enqueueSnackbar } = useSnackbar();

  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [globalFilter, setGlobalFilter] = useState('');

  const [recordDialogOpen, setRecordDialogOpen] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [selectedExercise, setSelectedExercise] = useState<string>('');

  const tableParentRef = useRef<HTMLDivElement>(null);

  const oneRepMaxes = useMemo(() => {
    if (userOneRepMaxes.length > 0) {
      return userOneRepMaxes;
    }

    if (!userData?.user_one_rep_max) {
      return [];
    }

    if (Array.isArray(userData.user_one_rep_max)) {
      return userData.user_one_rep_max as unknown as UserOneRepMax[];
    }

    if (typeof userData.user_one_rep_max === 'object' && userData.user_one_rep_max !== null) {
      return Object.values(userData.user_one_rep_max) as unknown as UserOneRepMax[];
    }

    return [];
  }, [userOneRepMaxes, userData]);

  // Get available exercises for the form
  const availableExercises = useMemo(() => {
    return allExercises.map(exercise => exercise.name).sort();
  }, [allExercises]);

  // Get default weight unit for an exercise
  const getDefaultWeightUnit = (exerciseName: string): string => {
    const preference = weightUnitPreferences.find(pref => pref.exercise_name === exerciseName);
    return preference?.preferred_unit || 'KG';
  };

  // Get existing 1RM value for an exercise
  const getExistingOneRepMax = (exerciseName: string): number | null => {
    const existingRecord = oneRepMaxes.find(record => record.exercise_name === exerciseName);
    return existingRecord ? existingRecord.one_rep_max : null;
  };

  // Handle form submission
  const handleSubmitOneRepMax = async (data: { exercise_name: string; one_rep_max: number }) => {
    if (!userData) return;

    try {
      setIsSaving(true);
      const unit = getDefaultWeightUnit(data.exercise_name);
      await upsertUserOneRepMax(data.exercise_name, data.one_rep_max, unit);
      setRecordDialogOpen(false);
      setSelectedExercise('');
      enqueueSnackbar('1RM record saved successfully', { variant: 'success' });
    } catch {
      enqueueSnackbar('Failed to save 1RM record', { variant: 'error' });
    } finally {
      setIsSaving(false);
    }
  };

  useEffect(() => {
    const loadData = async () => {
      if (!userData) return;

      setIsLoading(true);
      setError(null);
      try {
        if (allExercises.length === 0) {
          await loadAllExercises();
        }

        if (userOneRepMaxes.length === 0) {
          await loadUserOneRepMaxes();
        }
      } catch {
        setError('Failed to load 1RM records data');
      } finally {
        setIsLoading(false);
      }
    };

    loadData();
  }, [
    userData,
    allExercises.length,
    loadAllExercises,
    userOneRepMaxes.length,
    loadUserOneRepMaxes,
  ]);

  const exerciseDataMap = useMemo(() => {
    const map = new Map(allExercises.map(exercise => [exercise.name, exercise]));
    return map;
  }, [allExercises]);

  const workoutTrends = useMemo(() => {
    return buildAllExerciseWorkoutTrends(userData, exerciseDataMap, workoutsPerWeek);
  }, [userData, exerciseDataMap, workoutsPerWeek]);

  type OneRepMaxTableRow = {
    exerciseName: string;
    oneRepMax: number;
    displayWeight: string;
    updatedAt: Date;
    trendData: VolumeTrendPoint[];
  };

  const columnHelper = createColumnHelper<OneRepMaxTableRow>();

  const columns = useMemo(
    () => [
      columnHelper.accessor('exerciseName', {
        header: 'Exercise',
        cell: info => <ExerciseName exerciseName={info.getValue()} variant="body2" />,
      }),
      columnHelper.accessor('oneRepMax', {
        header: '1RM',
        cell: info => (
          <div style={{ textAlign: 'center', width: '100%' }}>{info.row.original.displayWeight}</div>
        ),
      }),
      columnHelper.display({
        id: 'loggedTrend',
        header: 'Logged Trend',
        cell: info => {
          const trendData = info.row.original.trendData;
          if (trendData.length < 2) {
            return (
              <div style={{ textAlign: 'center', width: '100%' }} data-testid="one-rm-no-trend">
                —
              </div>
            );
          }
          return (
            <Box sx={{ display: 'flex', justifyContent: 'center' }}>
              <VolumeTrendSparkline
                data={trendData}
                nivoTheme={nivoTheme}
                ariaLabel={`${info.row.original.exerciseName} weekly peak logged weight trend`}
                interactive={false}
              />
            </Box>
          );
        },
      }),
      columnHelper.accessor('updatedAt', {
        header: 'Last Updated',
        cell: info => (
          <div style={{ textAlign: 'right', width: '100%' }}>
            {new Date(info.getValue()).toLocaleDateString()}
          </div>
        ),
      }),
    ],
    [columnHelper, nivoTheme]
  );

  const tableData = useMemo((): OneRepMaxTableRow[] => {
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
      const trendPoints = workoutTrends.get(oneRepMax.exercise_name) ?? [];
      const trendData: VolumeTrendPoint[] = trendPoints.map(point => {
        const displayWeightValue =
          preferredUnit === 'LBS' ? point.peakWeightKg * KG_TO_LBS : point.peakWeightKg;
        return {
          x: point.weekLabel,
          y: Math.round(displayWeightValue * 10) / 10,
        };
      });

      return {
        exerciseName: oneRepMax.exercise_name,
        oneRepMax: oneRepMaxNumeric,
        displayWeight,
        updatedAt: new Date(oneRepMax.updated_at),
        trendData,
      };
    });
  }, [oneRepMaxes, weightUnitPreferences, workoutTrends]);

  const table = useReactTable({
    data: tableData,
    columns,
    getCoreRowModel: getCoreRowModel(),
    getFilteredRowModel: getFilteredRowModel(),
    state: {
      globalFilter,
    },
    onGlobalFilterChange: setGlobalFilter,
  });

  // Virtualization
  const rowVirtualizer = useVirtualizer({
    count: table.getRowModel().rows.length,
    getScrollElement: () => tableParentRef.current,
    estimateSize: () => 50,
    overscan: 10,
  });

  if (isDataLoading || isLoading) {
    return <LoadingSpinner message="Loading 1RM records..." fullHeight={false} />;
  }

  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <GameCard>
          <CardContent>
            <GameText variant="h6" color="error">
              Error loading 1RM records
            </GameText>
            <GameText variant="body2" className={GAME_CLASSES.marginTop2}>
              {error}
            </GameText>
          </CardContent>
        </GameCard>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      <GameCard
        sx={{
          background:
            'linear-gradient(135deg, rgba(25, 118, 210, 0.05) 0%, rgba(156, 39, 176, 0.05) 100%)',
          border: '1px solid',
          borderColor: 'primary.light',
        }}
      >
        <CardContent>
          <Box display="flex" alignItems="center" justifyContent="space-between" sx={{ mb: 2 }}>
            <Box display="flex" alignItems="center" gap={1}>
              <GameText variant="h6" className={GAME_CLASSES.textMedium}>
                1RM Records
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
            <Button variant="contained" onClick={() => setRecordDialogOpen(true)} sx={{ ml: 2 }}>
              Record 1RM
            </Button>
          </Box>
          <GameText variant="body2" textVariant="secondary" className={GAME_CLASSES.marginBottom2}>
            Track your strength progress and personal records • Search and filter your 1RM data
          </GameText>

          {oneRepMaxes.length === 0 ? (
            <Box sx={{ textAlign: 'center', py: 4 }}>
              <GameText variant="h6" className={GAME_CLASSES.marginBottom2}>
                No 1RM Records Found
              </GameText>
              <GameText variant="body2" textVariant="secondary">
                Start recording your 1RM values to track your strength progress over time.
              </GameText>
            </Box>
          ) : (
            <React.Fragment>
              <GameTextField
                fullWidth
                size="small"
                placeholder="Search exercises..."
                value={globalFilter}
                onChange={e => setGlobalFilter(e.target.value)}
                sx={{ mb: 2 }}
              />
              <Box
                ref={tableParentRef}
                sx={{
                  maxHeight: 500,
                  overflow: 'auto',
                  height: '500px',
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
                    tableLayout: 'fixed',
                  }}
                >
                  <thead>
                    {table.getHeaderGroups().map(headerGroup => (
                      <tr key={headerGroup.id}>
                        {headerGroup.headers.map((header, index) => (
                          <th
                            key={header.id}
                            style={{
                              textAlign:
                                index === 1 || index === 2
                                  ? 'center'
                                  : index === 3
                                    ? 'right'
                                    : 'left',
                              padding: '12px 16px',
                              borderBottom: '2px solid rgba(0, 188, 212, 0.3)',
                              fontWeight: '600',
                              backgroundColor: 'rgba(0, 188, 212, 0.1)',
                              color: '#00bcd4',
                              fontSize: '0.875rem',
                              textTransform: 'uppercase',
                              letterSpacing: '0.5px',
                              width:
                                index === 0
                                  ? '34%'
                                  : index === 1
                                    ? '18%'
                                    : index === 2
                                      ? '24%'
                                      : '24%',
                            }}
                          >
                            {flexRender(header.column.columnDef.header, header.getContext())}
                          </th>
                        ))}
                      </tr>
                    ))}
                  </thead>
                  <tbody>
                    <tr>
                      <td
                        colSpan={columns.length}
                        style={{
                          height: `${rowVirtualizer.getTotalSize() + 50}px`, // Add header height
                          padding: 0,
                        }}
                      />
                    </tr>
                    {rowVirtualizer
                      .getVirtualItems()
                      .map((virtualRow: { index: number; start: number; size: number }) => {
                        const row = table.getRowModel().rows[virtualRow.index];
                        if (!row) return null;
                        return (
                          <tr
                            key={row.id}
                            style={{
                              position: 'absolute',
                              top: 0,
                              left: 0,
                              width: '100%',
                              height: `${virtualRow.size}px`,
                              transform: `translateY(${virtualRow.start + 50}px)`, // Add header height offset
                              display: 'table',
                              tableLayout: 'fixed',
                            }}
                          >
                            {row.getVisibleCells().map((cell, index) => (
                              <td
                                key={cell.id}
                                style={{
                                  padding: '12px 16px',
                                  borderBottom: '1px solid rgba(255, 255, 255, 0.1)',
                                  fontSize: '0.875rem',
                                  color: '#ffffff',
                                  transition: 'background-color 0.2s ease',
                                  textAlign:
                                    index === 1 || index === 2
                                      ? 'center'
                                      : index === 3
                                        ? 'right'
                                        : 'left',
                                  display: 'table-cell',
                                  width:
                                    index === 0
                                      ? '34%'
                                      : index === 1
                                        ? '18%'
                                        : index === 2
                                          ? '24%'
                                          : '24%',
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
                        );
                      })}
                  </tbody>
                </table>
              </Box>
            </React.Fragment>
          )}
        </CardContent>
      </GameCard>

      {/* Record 1RM Dialog */}
      <FormDialog<{ exercise_name: string; one_rep_max: number }>
        open={recordDialogOpen}
        onClose={() => {
          setRecordDialogOpen(false);
          setSelectedExercise('');
        }}
        onSubmit={handleSubmitOneRepMax}
        title="Record 1RM"
        description="Enter your one rep max for an exercise"
        submitText="Save"
        cancelText="Cancel"
        loading={isSaving}
        useTanStackForm={true}
        defaultValues={{
          exercise_name: '',
          one_rep_max: 0,
        }}
      >
        {(form: {
          getFieldValue: (name: string) => unknown;
          setFieldValue: (name: string, value: unknown) => void;
        }) => {
          // Subscribe to exercise_name field changes using TanStack Form's useField
          const exerciseField = useField({
            form,
            name: 'exercise_name',
          });

          // Handle exercise field changes
          React.useEffect(() => {
            const exerciseName = exerciseField.state.value as string;

            if (exerciseName && typeof exerciseName === 'string') {
              setSelectedExercise(exerciseName);
              // Pre-populate with existing 1RM value if available, otherwise set to 0
              const existingValue = getExistingOneRepMax(exerciseName);
              form.setFieldValue('one_rep_max', existingValue !== null ? existingValue : 0);
            } else {
              // Field was cleared, reset both exercise and 1RM
              setSelectedExercise('');
              form.setFieldValue('one_rep_max', 0);
            }
          }, [exerciseField.state.value, form]);

          return (
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <FormField
                type="autocomplete"
                label="Exercise"
                name="exercise_name"
                form={form}
                options={availableExercises}
                required
              />
              <form.Field name="one_rep_max">
                {field => {
                  const unitLabel = (
                    selectedExercise ? getDefaultWeightUnit(selectedExercise) : 'KG'
                  ).toLowerCase();
                  return (
                    <NumericStepInput
                      label="1RM Weight"
                      suffix={unitLabel}
                      value={field.state.value as number}
                      onChange={value => field.handleChange(value ?? 0)}
                      min={0}
                      step={0.5}
                      disabled={isSaving}
                    />
                  );
                }}
              </form.Field>
            </Box>
          );
        }}
      </FormDialog>
    </Box>
  );
};
