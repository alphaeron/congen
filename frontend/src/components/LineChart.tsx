import { Box, Card, CardContent, useTheme } from '@mui/material';
import { ResponsiveLine } from '@nivo/line';
import React, { useState, useMemo } from 'react';

import type {
  UserDataExport,
  Exercise,
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises,
} from '../api/types';
import { formatDate } from '../common/utils';
import { createCongenNivoTheme, congenColorSchemes } from '../theme/nivoTheme';
import { GameText, GameCard, GAME_CLASSES } from './GameTheme';

interface LineChartProps {
  userDataExport: UserDataExport | null;
  exerciseData?: Map<string, Exercise>;
  title?: string;
  description?: string;
  xAxisLabel?: string;
  yAxisLabel?: string;
  height?: number;
  showLegend?: boolean;
  colors?: string[];
  chartType: 'progress';
}

/**
 * Line Chart component for displaying progression data.
 *
 * This component accepts raw workout data and handles all data transformations
 * internally to calculate and display different types of line charts.
 *
 * @param userDataExport The raw user data export containing all workout information
 * @param exerciseData Map of exercise data for categorization (required for volume charts)
 * @param title Optional title for the chart
 * @param description Optional description for the chart
 * @param xAxisLabel Optional x-axis label
 * @param yAxisLabel Optional y-axis label
 * @param height Optional height for the chart container
 * @param showLegend Whether to show the legend
 * @param colors Optional color scheme
 * @param chartType Type of chart to render ('progress')
 * @return Line Chart component
 */
export const LineChart: React.FC<LineChartProps> = ({
  userDataExport,
  title = 'Volume Progression',
  description = 'Total weight lifted over time (including band resistance)',
  xAxisLabel = 'Workout Date',
  yAxisLabel = 'Volume (lbs)',
  height = 300,
  showLegend = true,
  colors,
}) => {
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  const [selectedItems, setSelectedItems] = useState<string[]>([]);

  // Extract workouts from the raw data
  const workouts = useMemo(() => {
    if (!userDataExport?.training_programs?.length) return [];

    const allWorkouts: ProgrammedWorkoutWithStages[] = [];
    userDataExport.training_programs.forEach(program => {
      allWorkouts.push(...program.workouts);
    });

    return allWorkouts;
  }, [userDataExport]);

  // Calculate progress data for progress charts
  const progressData = useMemo(() => {
    if (!userDataExport) return [];

    const progress: Array<{
      date: string;
      exercise: string;
      weight: number;
      type: '1RM' | 'Volume';
    }> = [];

    // Add 1RM data
    if (userDataExport.user_one_rep_max) {
      userDataExport.user_one_rep_max.forEach(oneRepMax => {
        const typedOneRepMax = oneRepMax as {
          updated_at: Date;
          exercise_name: string;
          one_rep_max: number;
        };
        progress.push({
          date: formatDate(typedOneRepMax.updated_at),
          exercise: typedOneRepMax.exercise_name,
          weight: typedOneRepMax.one_rep_max,
          type: '1RM',
        });
      });
    }

    // Add volume data from recent workouts (simplified)
    if (workouts.length) {
      workouts.slice(-10).forEach(workoutData => {
        let totalVolume = 0;
        (workoutData.stages as WorkoutStageWithExercises[]).forEach(stage => {
          stage.exercises.forEach(exerciseWithSchemes => {
            exerciseWithSchemes.set_schemes.forEach(setScheme => {
              const weight = setScheme.performed_weight || setScheme.target_weight || 0;
              const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
              const bandWeight = setScheme.band_weight_lbs
                ? (setScheme.band_weight_lbs as { weight_lbs: number })?.weight_lbs || 0
                : 0;
              totalVolume += (weight + bandWeight) * reps;
            });
          });
        });
        progress.push({
          date: formatDate(workoutData.workout.created_at),
          exercise: 'Total Volume',
          weight: totalVolume,
          type: 'Volume',
        });
      });
    }

    return progress.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
  }, [userDataExport, workouts]);

  // Prepare chart data for progress charts
  const chartData = useMemo(() => {
    return [
      {
        id: '1RM Progress',
        data: progressData.filter(d => d.type === '1RM').map(d => ({ x: d.date, y: d.weight })),
      },
      {
        id: 'Volume Progress',
        data: progressData
          .filter(d => d.type === 'Volume')
          .map(d => ({ x: d.date, y: d.weight / 1000 })), // Scale down for visibility
      },
    ];
  }, [progressData]);

  // Filter data based on legend selection
  const filteredData = useMemo(() => {
    if (selectedItems.length === 0) {
      return chartData;
    }
    return chartData.filter(item => selectedItems.includes(item.id));
  }, [chartData, selectedItems]);

  // Don't render if no data
  if (!chartData.length || !chartData[0].data.length) {
    return (
      <GameCard>
        <CardContent>
          <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
            <BarChartIcon color="primary" />
            <GameText variant="h6">{title}</GameText>
          </Box>
          <GameText variant="body2" textVariant="secondary" className={GAME_CLASSES.marginBottom2}>
            {description}
          </GameText>
          <Box
            sx={{ height: 200, display: 'flex', alignItems: 'center', justifyContent: 'center' }}
          >
            <GameText variant="body2" textVariant="secondary">
              No progress data available. Complete workouts and record 1RM values to see progress
              tracking.
            </GameText>
          </Box>
        </CardContent>
      </GameCard>
    );
  }

  return (
    <Card variant="outlined">
      <CardContent>
        <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
          <GameText variant="h6">{title}</GameText>
        </Box>
        <GameText variant="body2" textVariant="secondary" className={GAME_CLASSES.marginBottom2}>
          {description}
        </GameText>
        <Box sx={{ height }}>
          <ResponsiveLine
            data={filteredData}
            margin={{ top: 20, right: 20, bottom: 50, left: 60 }}
            xScale={{ type: 'point' }}
            yScale={{ type: 'linear', min: 'auto', max: 'auto' }}
            axisTop={null}
            axisRight={null}
            axisBottom={{
              tickSize: 5,
              tickPadding: 5,
              tickRotation: -45,
              legend: xAxisLabel,
              legendOffset: 40,
              legendPosition: 'middle',
            }}
            axisLeft={{
              tickSize: 5,
              tickPadding: 5,
              tickRotation: 0,
              legend: yAxisLabel,
              legendOffset: -50,
              legendPosition: 'middle',
            }}
            pointSize={8}
            pointColor={{ theme: 'background' }}
            pointBorderWidth={2}
            pointBorderColor={{ from: 'serieColor' }}
            pointLabelYOffset={-12}
            useMesh={true}
            colors={colors || congenColorSchemes.strength}
            animate={true}
            motionConfig="gentle"
            theme={nivoTheme}
            tooltip={({ point }) => (
              <div
                style={{
                  padding: '8px 12px',
                  color: nivoTheme.tooltip.container.color,
                  background: nivoTheme.tooltip.container.background,
                  borderRadius: nivoTheme.tooltip.container.borderRadius,
                  boxShadow: nivoTheme.tooltip.container.boxShadow,
                  border: nivoTheme.tooltip.container.border,
                  whiteSpace: 'nowrap',
                  fontSize: nivoTheme.tooltip.container.fontSize,
                  fontFamily: '"Inter", "Roboto", "Helvetica", "Arial", sans-serif',
                  lineHeight: '1.4',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '8px',
                }}
              >
                <div
                  style={{
                    width: '12px',
                    height: '12px',
                    backgroundColor: point.seriesColor,
                    borderRadius: '2px',
                    flexShrink: 0,
                  }}
                />
                <div>
                  <div>
                    Date: <span style={{ fontWeight: 'bold' }}>{point.data.x}</span>
                  </div>
                  <div>
                    Weight: <span style={{ fontWeight: 'bold' }}>{point.data.y} lbs</span>
                  </div>
                </div>
              </div>
            )}
            legends={
              showLegend
                ? [
                    {
                      anchor: 'top',
                      direction: 'row',
                      justify: false,
                      translateX: 0,
                      translateY: -20,
                      itemsSpacing: 0,
                      itemDirection: 'left-to-right',
                      itemWidth: 80,
                      itemHeight: 20,
                      itemTextColor: '#333333',
                      itemOpacity: 1,
                      symbolSize: 12,
                      symbolShape: 'circle',
                      symbolBorderColor: 'rgba(0, 0, 0, .5)',
                      onClick: (datum: { id?: string | number; label?: string | number }) => {
                        const itemId =
                          typeof datum.id === 'string' ? datum.id : String(datum.label || '');
                        if (itemId) {
                          setSelectedItems(prev => {
                            if (prev.includes(itemId)) {
                              return prev.filter(id => id !== itemId);
                            } else {
                              return [...prev, itemId];
                            }
                          });
                        }
                      },
                      effects: [
                        {
                          on: 'hover',
                          style: {
                            itemBackground: 'rgba(0, 0, 0, .03)',
                            itemOpacity: 1,
                            itemTextColor: '#000',
                          },
                        },
                      ],
                    },
                  ]
                : []
            }
          />
        </Box>
      </CardContent>
    </Card>
  );
};
