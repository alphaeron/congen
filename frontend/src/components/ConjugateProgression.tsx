import { default as BarChartIcon } from '@mui/icons-material/BarChart';
import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { default as TrendingUpIcon } from '@mui/icons-material/TrendingUp';
import {
  Box,
  Card,
  CardContent,
  Grid,
  Typography,
  CircularProgress,
} from '@mui/material';
import { ResponsiveChord } from '@nivo/chord';
import { ResponsiveLine } from '@nivo/line';
import { ResponsivePie } from '@nivo/pie';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';

import { getUserDataExport } from '../api/gdpr';
import type { 
  User, 
  UserDataExport,
  ProgrammedWorkoutWithStages,
  WorkoutStageWithExercises
} from '../api/types';
import { congenNivoTheme, congenColorSchemes } from '../theme/nivoTheme';

interface ConjugateProgressionProps {
  user: User;
}

interface ExerciseCorrelation {
  exercise: string;
  category: 'Big 3' | 'Big 4' | 'Accessory';
  volume: number;
  frequency: number;
  maxWeight: number;
}

interface ProgressData {
  date: string;
  exercise: string;
  weight: number;
  type: '1RM' | 'Volume';
}

interface ExerciseCorrelationData {
  source: string;
  target: string;
  value: number;
}

/**
 * Enhanced Conjugate Progression component displaying actual user statistics and progress.
 *
 * Based on Westside Barbell conjugate method principles, shows:
 * - Volume tracking (total weight lifted including bands)
 * - Exercise correlation analysis (Big 3/4 lifts vs accessories)
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

  // Load all workout data using optimized single API call
  useEffect(() => {
    const loadWorkoutData = async () => {
      try {
        setIsLoading(true);

        // Load all data in a single optimized call
        const dataExport = await getUserDataExport();
        setUserData(dataExport);
      } catch (err) {
        enqueueSnackbar('Failed to load workout data. Please try again.', { variant: 'error' });
        console.error('Error loading workout data:', err);
      } finally {
        setIsLoading(false);
      }
    };

    loadWorkoutData();
  }, [user.keycloak_id]);

  // Calculate volume data for charts
  const volumeData = useMemo(() => {
    if (!userData?.training_programs) return [];

    const allWorkouts: ProgrammedWorkoutWithStages[] = [];
    userData.training_programs.forEach(program => {
      allWorkouts.push(...program.workouts);
    });

    return allWorkouts.map((workoutData) => {
      let totalVolume = 0;
      let maxEffortVolume = 0;
      let dynamicEffortVolume = 0;
      let accessoryVolume = 0;

      workoutData.stages.forEach((stage) => {
        stage.exercises.forEach((exerciseData) => {
          exerciseData.set_schemes.forEach((setScheme) => {
            const weight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const bandWeight = setScheme.band_weight_lbs ? 
              (setScheme.band_weight_lbs as { weight_lbs: number })?.weight_lbs || 0 : 0;
            
            const totalWeight = weight + bandWeight;
            const setVolume = totalWeight * reps;

            // Categorize by exercise type
            const exerciseName = exerciseData.exercise.exercise_name.toLowerCase();
            const isBigLift = ['squat', 'bench', 'deadlift', 'overhead press'].some(lift => 
              exerciseName.includes(lift)
            );
            const isAccessory = exerciseName.includes('curl') || exerciseName.includes('extension') || 
                               exerciseName.includes('fly') || exerciseName.includes('raise');

            if (isBigLift) {
              if (weight > 200) { // Assume heavy weight = max effort
                maxEffortVolume += setVolume;
              } else {
                dynamicEffortVolume += setVolume;
              }
            } else if (isAccessory) {
              accessoryVolume += setVolume;
            } else {
              totalVolume += setVolume;
            }
          });
        });
      });

      return {
        date: new Date(workoutData.workout.created_at).toLocaleDateString(),
        totalVolume: Math.round(totalVolume + maxEffortVolume + dynamicEffortVolume + accessoryVolume),
        maxEffortVolume: Math.round(maxEffortVolume),
        dynamicEffortVolume: Math.round(dynamicEffortVolume),
        accessoryVolume: Math.round(accessoryVolume),
      };
    }).slice(-10); // Last 10 workouts
  }, [userData]);

  // Calculate exercise correlation data
  const exerciseCorrelationData = useMemo(() => {
    if (!userData?.training_programs) return [];

    const exerciseStats = new Map<string, ExerciseCorrelation>();
    const allWorkouts: ProgrammedWorkoutWithStages[] = [];
    userData.training_programs.forEach(program => {
      allWorkouts.push(...program.workouts);
    });

    allWorkouts.forEach((workoutData) => {
      workoutData.stages.forEach((stage) => {
        stage.exercises.forEach((exerciseData) => {
          const exerciseName = exerciseData.exercise.exercise_name;
          const existing = exerciseStats.get(exerciseName) || {
            exercise: exerciseName,
            category: 'Accessory' as const,
            volume: 0,
            frequency: 0,
            maxWeight: 0,
          };

          // Determine category
          const lowerName = exerciseName.toLowerCase();
          if (['squat', 'bench', 'deadlift'].some(lift => lowerName.includes(lift))) {
            existing.category = 'Big 3';
          } else if (lowerName.includes('overhead') || lowerName.includes('press')) {
            existing.category = 'Big 4';
          }

          // Calculate stats
          exerciseData.set_schemes.forEach((setScheme) => {
            const weight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const bandWeight = setScheme.band_weight_lbs ? 
              (setScheme.band_weight_lbs as { weight_lbs: number })?.weight_lbs || 0 : 0;
            
            existing.volume += (weight + bandWeight) * reps;
            existing.maxWeight = Math.max(existing.maxWeight, weight + bandWeight);
          });

          existing.frequency += 1;
          exerciseStats.set(exerciseName, existing);
        });
      });
    });

    return Array.from(exerciseStats.values())
      .sort((a, b) => b.volume - a.volume)
      .slice(0, 8); // Top 8 exercises
  }, [userData]);

  // Calculate progress data
  const progressData = useMemo(() => {
    const progress: ProgressData[] = [];

    // Add 1RM data
    if (userData?.user_one_rep_max) {
      userData.user_one_rep_max.forEach((oneRepMax) => {
        const typedOneRepMax = oneRepMax as { updated_at: string; exercise_name: string; one_rep_max: number };
        progress.push({
          date: new Date(typedOneRepMax.updated_at).toLocaleDateString(),
          exercise: typedOneRepMax.exercise_name,
          weight: typedOneRepMax.one_rep_max,
          type: '1RM',
        });
      });
    }

    // Add volume data from recent workouts
    volumeData.forEach((volume) => {
      progress.push({
        date: volume.date,
        exercise: 'Total Volume',
        weight: volume.totalVolume,
        type: 'Volume',
      });
    });

    return progress.sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());
  }, [userData, volumeData]);

  // Prepare chart data
  const volumeChartData = useMemo(() => [
    {
      id: 'Total Volume',
      data: volumeData.map(d => ({ x: d.date, y: d.totalVolume })),
    },
    {
      id: 'Max Effort',
      data: volumeData.map(d => ({ x: d.date, y: d.maxEffortVolume })),
    },
    {
      id: 'Dynamic Effort',
      data: volumeData.map(d => ({ x: d.date, y: d.dynamicEffortVolume })),
    },
    {
      id: 'Accessory',
      data: volumeData.map(d => ({ x: d.date, y: d.accessoryVolume })),
    },
  ], [volumeData]);

  const correlationChartData = useMemo(() => {
    const categories = ['Big 3', 'Big 4', 'Accessory'];
    return categories.map(category => ({
      category,
      volume: exerciseCorrelationData
        .filter(ex => ex.category === category)
        .reduce((sum, ex) => sum + ex.volume, 0),
    }));
  }, [exerciseCorrelationData]);

  const progressChartData = useMemo(() => [
    {
      id: '1RM Progress',
      data: progressData
        .filter(d => d.type === '1RM')
        .map(d => ({ x: d.date, y: d.weight })),
    },
    {
      id: 'Volume Progress',
      data: progressData
        .filter(d => d.type === 'Volume')
        .map(d => ({ x: d.date, y: d.weight / 1000 })), // Scale down for visibility
    },
  ], [progressData]);

  // Calculate exercise correlations for chord diagram
  const exerciseCorrelations = useMemo(() => {
    if (!userData?.training_programs) return [];

    const correlations: ExerciseCorrelationData[] = [];
    const exercisePairs = new Map<string, number>();
    const allWorkouts: ProgrammedWorkoutWithStages[] = [];
    userData.training_programs.forEach(program => {
      allWorkouts.push(...program.workouts);
    });

    allWorkouts.forEach((workoutData) => {
      const workoutExercises = new Set<string>();
      
      workoutData.stages.forEach((stage) => {
        stage.exercises.forEach((exerciseData) => {
          workoutExercises.add(exerciseData.exercise.exercise_name);
        });
      });

      // Count exercise pairs in the same workout
      const exerciseArray = Array.from(workoutExercises);
      for (let i = 0; i < exerciseArray.length; i++) {
        for (let j = i + 1; j < exerciseArray.length; j++) {
          const pair = [exerciseArray[i], exerciseArray[j]].sort().join('|');
          exercisePairs.set(pair, (exercisePairs.get(pair) || 0) + 1);
        }
      }
    });

    // Convert to chord diagram format
    exercisePairs.forEach((value, pair) => {
      const [source, target] = pair.split('|');
      if (value > 1) { // Only show correlations that appear more than once
        correlations.push({ source, target, value });
      }
    });

    return correlations.slice(0, 10); // Top 10 correlations
  }, [userData]);

  const chordData = useMemo(() => {
    const uniqueExercises = new Set<string>();
    exerciseCorrelations.forEach(corr => {
      uniqueExercises.add(corr.source);
      uniqueExercises.add(corr.target);
    });

    return {
      matrix: Array.from(uniqueExercises).map(source => 
        Array.from(uniqueExercises).map(target => {
          const correlation = exerciseCorrelations.find(
            corr => (corr.source === source && corr.target === target) ||
                   (corr.source === target && corr.target === source)
          );
          return correlation?.value || 0;
        })
      ),
      keys: Array.from(uniqueExercises),
    };
  }, [exerciseCorrelations]);

  if (isLoading) {
    return (
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
            <CircularProgress />
          </Box>
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
    <Card sx={{ mb: 4 }}>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Conjugate Progress Tracking
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
          Based on Westside Barbell conjugate method principles - tracking volume, correlations, and progress
        </Typography>

        <Grid container spacing={3}>
          {/* Volume Tracking Chart */}
          <Grid item xs={12} lg={8}>
            <Card variant="outlined">
              <CardContent>
                <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
                  <BarChartIcon color="primary" />
                  <Typography variant="h6">Volume Progression</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Total weight lifted over time (including band resistance)
                </Typography>
                <Box sx={{ height: 300 }}>
                                            <ResponsiveLine
                            data={volumeChartData}
                            margin={{ top: 20, right: 20, bottom: 50, left: 60 }}
                            xScale={{ type: 'point' }}
                            yScale={{ type: 'linear', min: 'auto', max: 'auto' }}
                            axisTop={null}
                            axisRight={null}
                            axisBottom={{
                              tickSize: 5,
                              tickPadding: 5,
                              tickRotation: -45,
                              legend: 'Workout Date',
                              legendOffset: 40,
                              legendPosition: 'middle'
                            }}
                            axisLeft={{
                              tickSize: 5,
                              tickPadding: 5,
                              tickRotation: 0,
                              legend: 'Volume (lbs)',
                              legendOffset: -50,
                              legendPosition: 'middle'
                            }}
                            pointSize={8}
                            pointColor={{ theme: 'background' }}
                            pointBorderWidth={2}
                            pointBorderColor={{ from: 'serieColor' }}
                            pointLabelYOffset={-12}
                            useMesh={true}
                            colors={congenColorSchemes.strength}
                            theme={congenNivoTheme}
                            legends={[
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
                                onClick: (data: any) => {
                                  console.log('Legend clicked:', data);
                                },
                                effects: [
                                  {
                                    on: 'hover',
                                    style: {
                                      itemBackground: 'rgba(0, 0, 0, .03)',
                                      itemOpacity: 1,
                                      itemTextColor: '#000'
                                    }
                                  }
                                ]
                              }
                            ]}
                          />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Exercise Category Distribution */}
          <Grid item xs={12} lg={4}>
            <Card variant="outlined">
              <CardContent>
                <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
                  <ShowChartIcon color="secondary" />
                  <Typography variant="h6">Exercise Distribution</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Volume by exercise category
                </Typography>
                <Box sx={{ height: 300 }}>
                  <ResponsivePie
                    data={correlationChartData.map(d => ({
                      id: d.category,
                      label: d.category,
                      value: d.volume,
                    }))}
                    margin={{ top: 20, right: 20, bottom: 20, left: 20 }}
                    innerRadius={0.5}
                    padAngle={0.7}
                    cornerRadius={3}
                    activeOuterRadiusOffset={8}
                    borderWidth={1}
                    borderColor={{ from: 'color', modifiers: [['darker', 0.2]] }}
                    arcLinkLabelsSkipAngle={10}
                    arcLinkLabelsTextColor="#333333"
                    arcLinkLabelsThickness={2}
                    arcLinkLabelsColor={{ from: 'color' }}
                    arcLabelsSkipAngle={10}
                    arcLabelsTextColor={{ from: 'color', modifiers: [['darker', 2]] }}
                    theme={congenNivoTheme}
                    legends={[
                      {
                        anchor: 'bottom',
                        direction: 'row',
                        justify: false,
                        translateX: 0,
                        translateY: 56,
                        itemsSpacing: 0,
                        itemWidth: 100,
                        itemHeight: 18,
                        itemTextColor: '#333333',
                        itemDirection: 'left-to-right',
                        itemOpacity: 1,
                        symbolSize: 18,
                        symbolShape: 'circle',
                        onClick: (data: any) => {
                          console.log('Legend clicked:', data);
                        },
                        effects: [
                          {
                            on: 'hover',
                            style: {
                              itemTextColor: '#000'
                            }
                          }
                        ]
                      }
                    ]}
                  />
                </Box>
              </CardContent>
            </Card>
          </Grid>

          {/* Progress Tracking */}
          <Grid item xs={12}>
            <Card variant="outlined">
              <CardContent>
                <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
                  <TrendingUpIcon color="success" />
                  <Typography variant="h6">Progress Tracking</Typography>
                </Box>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  1RM improvements and volume progression over time
                </Typography>
                <Box sx={{ height: 300 }}>
                  <ResponsiveLine
                    data={progressChartData}
                    margin={{ top: 20, right: 20, bottom: 50, left: 60 }}
                    xScale={{ type: 'point' }}
                    yScale={{ type: 'linear', min: 'auto', max: 'auto' }}
                    axisTop={null}
                    axisRight={null}
                    axisBottom={{
                      tickSize: 5,
                      tickPadding: 5,
                      tickRotation: -45,
                      legend: 'Date',
                      legendOffset: 40,
                      legendPosition: 'middle'
                    }}
                    axisLeft={{
                      tickSize: 5,
                      tickPadding: 5,
                      tickRotation: 0,
                      legend: 'Weight (lbs)',
                      legendOffset: -50,
                      legendPosition: 'middle'
                    }}
                    pointSize={8}
                    pointColor={{ theme: 'background' }}
                    pointBorderWidth={2}
                    pointBorderColor={{ from: 'serieColor' }}
                    pointLabelYOffset={-12}
                    useMesh={true}
                    theme={congenNivoTheme}
                    legends={[
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
                        onClick: (data: any) => {
                          console.log('Legend clicked:', data);
                        },
                        effects: [
                          {
                            on: 'hover',
                            style: {
                              itemBackground: 'rgba(0, 0, 0, .03)',
                              itemOpacity: 1,
                              itemTextColor: '#000'
                            }
                          }
                        ]
                      }
                    ]}
                  />
                </Box>
              </CardContent>
            </Card>
          </Grid>

                            {/* Key Performance Indicators */}
                  <Grid item xs={12}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          Key Performance Indicators
                        </Typography>
                        <Grid container spacing={2}>
                          <Grid item xs={12} sm={6} md={3}>
                            <Box textAlign="center">
                              <Typography variant="h4" color="primary">
                                {userData?.training_programs?.reduce((total, program) => total + program.workouts.length, 0) || 0}
                              </Typography>
                              <Typography variant="body2" color="text.secondary">
                                Total Workouts
                              </Typography>
                            </Box>
                          </Grid>
                          <Grid item xs={12} sm={6} md={3}>
                            <Box textAlign="center">
                              <Typography variant="h4" color="secondary">
                                {Math.round(volumeData.reduce((sum, d) => sum + d.totalVolume, 0) / 1000)}k
                              </Typography>
                              <Typography variant="body2" color="text.secondary">
                                Total Volume (lbs)
                              </Typography>
                            </Box>
                          </Grid>
                          <Grid item xs={12} sm={6} md={3}>
                            <Box textAlign="center">
                              <Typography variant="h4" color="success">
                                {userData?.user_one_rep_max?.length || 0}
                              </Typography>
                              <Typography variant="body2" color="text.secondary">
                                1RM Records
                              </Typography>
                            </Box>
                          </Grid>
                          <Grid item xs={12} sm={6} md={3}>
                            <Box textAlign="center">
                              <Typography variant="h4" color="info">
                                {Math.round(volumeData[volumeData.length - 1]?.totalVolume || 0)}
                              </Typography>
                              <Typography variant="body2" color="text.secondary">
                                Latest Volume (lbs)
                              </Typography>
                            </Box>
                          </Grid>
                        </Grid>
                      </CardContent>
                    </Card>
                  </Grid>        

                  {/* Chord Diagram - Exercise Correlations */}
                  {chordData.keys.length > 0 && (
                    <Grid item xs={12} lg={6}>
                      <Card variant="outlined">
                        <CardContent>
                          <Box display="flex" alignItems="center" gap={1} sx={{ mb: 2 }}>
                            <TrendingUpIcon color="info" />
                            <Typography variant="h6">Exercise Correlations</Typography>
                          </Box>
                          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                            Exercise pairing patterns in your workouts
                          </Typography>
                          <Box sx={{ height: 300 }}>
                            <ResponsiveChord
                              data={chordData.matrix}
                              keys={chordData.keys}
                              margin={{ top: 60, right: 60, bottom: 90, left: 60 }}
                              valueFormat=".0f"
                              padAngle={0.02}
                              innerRadiusRatio={0.96}
                              innerRadiusOffset={0.02}
                              inactiveArcOpacity={0.25}
                              arcBorderWidth={1}
                              arcBorderColor={{ from: 'color', modifiers: [['darker', 0.4]] }}
                              activeRibbonOpacity={0.75}
                              inactiveRibbonOpacity={0.25}
                              ribbonBorderWidth={1}
                              ribbonBorderColor={{ from: 'color', modifiers: [['darker', 0.4]] }}
                              enableLabel={true}
                              label="id"
                              labelOffset={12}
                              labelRotation={-90}
                              labelTextColor={{
                                from: 'color',
                                modifiers: [['darker', 1]],
                              }}
                              colors={{ scheme: 'nivo' }}
                              theme={congenNivoTheme}
                            />
                          </Box>
                        </CardContent>
                      </Card>
                    </Grid>
                  )}
        </Grid>
      </CardContent>
    </Card>
  );
};
