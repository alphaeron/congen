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
  useTheme,
} from '@mui/material';
import { ResponsiveLine } from '@nivo/line';
import { ResponsivePie } from '@nivo/pie';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';

import { getUserDataExport } from '../api/gdpr';
import { getIndividualExercise } from '../api/exercise';
import type { 
  User, 
  UserDataExport,
  ProgrammedWorkoutWithStages,
  Exercise
} from '../api/types';
import { createCongenNivoTheme, congenColorSchemes } from '../theme/nivoTheme';
import { categorizeExerciseVolume } from '../common/utils';

interface ConjugateProgressionProps {
  user: User;
}

interface ExerciseCorrelation {
  exercise: string;
  category: string; // Will be the workout stage name
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
  const theme = useTheme();
  const nivoTheme = createCongenNivoTheme(theme.palette.mode);
  const [userData, setUserData] = useState<UserDataExport | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [exerciseData, setExerciseData] = useState<Map<string, Exercise>>(new Map());

  // Legend selection state for each chart
  const [volumeSelectedItems, setVolumeSelectedItems] = useState<string[]>([]);
  const [pieSelectedItems, setPieSelectedItems] = useState<string[]>([]);
  const [progressSelectedItems, setProgressSelectedItems] = useState<string[]>([]);

  // Load all workout data using optimized single API call
  useEffect(() => {
    const loadWorkoutData = async () => {
      try {
        setIsLoading(true);

        // Load all data in a single optimized call
        const dataExport = await getUserDataExport();
        setUserData(dataExport);

        // Fetch exercise data for all unique exercises
        const uniqueExercises = new Set<string>();
        dataExport.training_programs.forEach(program => {
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
          } catch (err) {
            enqueueSnackbar(`Error fetching exercise data for ${exerciseName}`, { variant: 'error' });
          }
        }

        setExerciseData(exerciseMap);
      } catch (err) {
        enqueueSnackbar('Failed to load workout data. Please try again.', { variant: 'error' });
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
        stage.exercises.forEach((exerciseWithSchemes) => {
          exerciseWithSchemes.set_schemes.forEach((setScheme) => {
            const weight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const bandWeight = setScheme.band_weight_lbs ? 
              (setScheme.band_weight_lbs as { weight_lbs: number })?.weight_lbs || 0 : 0;
            
            const totalWeight = weight + bandWeight;
            const setVolume = totalWeight * reps;

            // Get exercise data and categorize volume using shared helper
            const exerciseName = exerciseWithSchemes.exercise.exercise_name;
            const exerciseInfo = exerciseData.get(exerciseName);
            const categorizedVolume = categorizeExerciseVolume(
              exerciseInfo,
              workoutData.workout.name,
              setVolume
            );
            
            maxEffortVolume += categorizedVolume.maxEffortVolume;
            dynamicEffortVolume += categorizedVolume.dynamicEffortVolume;
            accessoryVolume += categorizedVolume.accessoryVolume;
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
  }, [userData, exerciseData]);

  // Calculate exercise volume by workout stage data
  const exerciseCorrelationData = useMemo(() => {
    if (!userData?.training_programs) return [];

    const stageVolumeMap = new Map<string, number>();
    const allWorkouts: ProgrammedWorkoutWithStages[] = [];
    userData.training_programs.forEach(program => {
      allWorkouts.push(...program.workouts);
    });

    allWorkouts.forEach((workoutData) => {
      workoutData.stages.forEach((stage) => {
        const stageName = stage.stage.name || 'Unknown Stage';
        
        stage.exercises.forEach((exerciseWithSchemes) => {
          // Calculate volume for this exercise in this stage
          let stageVolume = 0;
          exerciseWithSchemes.set_schemes.forEach((setScheme) => {
            const weight = setScheme.performed_weight || setScheme.target_weight || 0;
            const reps = setScheme.performed_rep_count || setScheme.target_rep_count || 0;
            const bandWeight = setScheme.band_weight_lbs ? 
              (setScheme.band_weight_lbs as { weight_lbs: number })?.weight_lbs || 0 : 0;
            
            stageVolume += (weight + bandWeight) * reps;
          });

          // Add to stage volume
          const currentVolume = stageVolumeMap.get(stageName) || 0;
          stageVolumeMap.set(stageName, currentVolume + stageVolume);
        });
      });
    });

    // Convert to the expected format for the donut chart
    return Array.from(stageVolumeMap.entries()).map(([stageName, volume]) => ({
      exercise: stageName, // Using stage name as exercise name for the chart
      category: stageName,
      volume,
      frequency: 1, // Not used for donut chart
      maxWeight: 0, // Not used for donut chart
    }));
  }, [userData, exerciseData]);

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
    // Aggregate volume by workout stage
    const stageVolumeMap = new Map<string, number>();
    
    console.log('exerciseCorrelationData:', exerciseCorrelationData);
    
    exerciseCorrelationData.forEach(ex => {
      const currentVolume = stageVolumeMap.get(ex.category) || 0;
      stageVolumeMap.set(ex.category, currentVolume + ex.volume);
    });
    
    const result = Array.from(stageVolumeMap.entries()).map(([category, volume]) => ({
      category,
      volume,
    }));
    
    console.log('correlationChartData result:', result);
    return result;
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

  // Filter volume data based on legend selection
  const filteredVolumeChartData = useMemo(() => {
    if (volumeSelectedItems.length === 0) {
      return volumeChartData;
    }
    return volumeChartData.filter(item => 
      volumeSelectedItems.includes(item.id)
    );
  }, [volumeChartData, volumeSelectedItems]);

  // Filter pie data based on legend selection
  const filteredCorrelationChartData = useMemo(() => {
    if (pieSelectedItems.length === 0) {
      return correlationChartData;
    }
    return correlationChartData.filter(item => 
      pieSelectedItems.includes(item.category)
    );
  }, [correlationChartData, pieSelectedItems]);

  // Filter progress data based on legend selection
  const filteredProgressChartData = useMemo(() => {
    if (progressSelectedItems.length === 0) {
      return progressChartData;
    }
    return progressChartData.filter(item => 
      progressSelectedItems.includes(item.id)
    );
  }, [progressChartData, progressSelectedItems]);

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
                        data={filteredVolumeChartData}
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
                            theme={nivoTheme}
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
                                  const itemId = data.id || data.label;
                                  setVolumeSelectedItems(prev => {
                                    if (prev.includes(itemId)) {
                                      return prev.filter(id => id !== itemId);
                                    } else {
                                      return [...prev, itemId];
                                    }
                                  });
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
                  Volume by workout stage
                </Typography>
                <Box sx={{ height: 300 }}>
                  <ResponsivePie
                    data={filteredCorrelationChartData.map(d => ({
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
                    theme={nivoTheme}
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
                          const itemId = data.id || data.label;
                          setPieSelectedItems(prev => {
                            if (prev.includes(itemId)) {
                              return prev.filter(id => id !== itemId);
                            } else {
                              return [...prev, itemId];
                            }
                          });
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
                    data={filteredProgressChartData}
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
                    theme={nivoTheme}
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
                          const itemId = data.id || data.label;
                          setProgressSelectedItems(prev => {
                            if (prev.includes(itemId)) {
                              return prev.filter(id => id !== itemId);
                            } else {
                              return [...prev, itemId];
                            }
                          });
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
        </Grid>
      </CardContent>
    </Card>
  );
};
