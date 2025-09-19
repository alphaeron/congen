import { Search as SearchIcon } from '@mui/icons-material';
import { Box, Card, CardContent, Grid, Typography, TextField, InputAdornment } from '@mui/material';
import {
  createColumnHelper,
  getCoreRowModel,
  getFilteredRowModel,
  useReactTable,
  flexRender,
} from '@tanstack/react-table';
import { useVirtualizer } from '@tanstack/react-virtual';
import React, { useMemo, useState, useRef } from 'react';

import { ExerciseName } from './ExerciseName';
import { LineChart } from './LineChart';
import { PieChart } from './PieChart';
import type {
  User,
  Exercise,
  UserOneRepMax,
  UserDataExport,
  UserWeightUnitPreference,
} from '../api/types';

interface ConjugateProgressionProps {
  user: User;
  userData: UserDataExport | null;
  exerciseData: Map<string, Exercise>;
  oneRepMaxes: UserOneRepMax[];
  weightUnitPreferences: UserWeightUnitPreference[];
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
 * @param userData The user's workout and training data
 * @param exerciseData Map of exercise names to exercise data
 * @param oneRepMaxes User's one rep max records
 * @param weightUnitPreferences User's weight unit preferences
 * @return Enhanced conjugate progression component
 */
export const ConjugateProgression: React.FC<ConjugateProgressionProps> = ({
  userData,
  exerciseData,
  oneRepMaxes,
  weightUnitPreferences,
}) => {
  const [globalFilter, setGlobalFilter] = useState('');

  // Virtualization setup
  const tableParentRef = useRef<HTMLDivElement>(null);

  // Table configuration
  const columnHelper = createColumnHelper<{
    exerciseName: string;
    oneRepMax: number;
    unit: string;
  }>();

  const columns = [
    columnHelper.accessor('exerciseName', {
      header: 'Exercise',
      cell: info => <ExerciseName exerciseName={info.getValue()} variant="body2" />,
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

  // Create virtualizer for table rows
  const rowVirtualizer = useVirtualizer({
    count: table.getRowModel().rows.length,
    getScrollElement: () => tableParentRef.current,
    estimateSize: () => 50, // Approximate row height
    overscan: 5, // Render 5 extra rows above and below viewport
  });

  if (!userData?.training_programs || userData.training_programs.length === 0) {
    return (
      <Card sx={{ mb: 3 }}>
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
        <Card variant="outlined" sx={{ 
          background: 'linear-gradient(135deg, rgba(25, 118, 210, 0.05) 0%, rgba(156, 39, 176, 0.05) 100%)',
          border: '1px solid',
          borderColor: 'primary.light'
        }}>
          <CardContent>
            <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
              <Typography variant="h6" fontWeight="medium">
                Personal Records (1RM)
              </Typography>
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
                  fontWeight: 'bold'
                }}
              >
                {oneRepMaxes.length}
              </Box>
            </Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
              Track your strength progress and personal records • Click to view details
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
              sx={{ 
                mb: 2,
                '& .MuiOutlinedInput-root': {
                  backgroundColor: 'background.paper',
                  '&:hover': {
                    backgroundColor: 'action.hover',
                  }
                }
              }}
            />
            <Box
              ref={tableParentRef}
              sx={{
                maxHeight: 400,
                overflow: 'auto',
                height: '400px',
                borderRadius: 1,
                border: 1,
                borderColor: 'divider',
                backgroundColor: 'background.paper'
              }}
            >
              <table style={{ 
                width: '100%', 
                borderCollapse: 'collapse',
                fontFamily: 'inherit'
              }}>
                <thead>
                  {table.getHeaderGroups().map(headerGroup => (
                    <tr key={headerGroup.id}>
                      {headerGroup.headers.map(header => (
                        <th
                          key={header.id}
                          style={{
                            textAlign: 'left',
                            padding: '12px 16px',
                            borderBottom: '2px solid #e0e0e0',
                            fontWeight: '600',
                            backgroundColor: '#f8f9fa',
                            color: '#495057',
                            fontSize: '0.875rem',
                            textTransform: 'uppercase',
                            letterSpacing: '0.5px'
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
                        height: `${rowVirtualizer.getTotalSize()}px`,
                        padding: 0,
                      }}
                    />
                  </tr>
                  {rowVirtualizer.getVirtualItems().map(virtualRow => {
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
                          transform: `translateY(${virtualRow.start}px)`,
                        }}
                      >
                        {row.getVisibleCells().map(cell => (
                          <td
                            key={cell.id}
                            style={{
                              padding: '12px 16px',
                              borderBottom: '1px solid #f0f0f0',
                              fontSize: '0.875rem',
                              transition: 'background-color 0.2s ease'
                            }}
                            onMouseEnter={(e) => {
                              e.currentTarget.style.backgroundColor = '#f8f9fa';
                            }}
                            onMouseLeave={(e) => {
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
          </CardContent>
        </Card>
      </Grid>

    </Grid>
  );
};
