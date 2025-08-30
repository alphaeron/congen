import { default as InfoIcon } from '@mui/icons-material/Info';
import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import { default as FitnessCenterIcon } from '@mui/icons-material/FitnessCenter';
import { default as TrendingUpIcon } from '@mui/icons-material/TrendingUp';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  CircularProgress,
  Chip,
  Tooltip,
  Tabs,
  Tab,
  Autocomplete,
  TextField,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState, useMemo } from 'react';
import { useSearchParams } from 'react-router';

import { RadialBarChart } from './RadialBarChart';
import { SunburstChart } from './SunburstChart';
import { getExercises } from '../api/exercise';
import { getUserDataExport } from '../api/gdpr';
import { getExerciseMuscle } from '../api/exerciseMuscle';
import { getUserWeightUnitPreferences, WeightUnit } from '../api/userWeightUnitPreference';
import type { User, UserOneRepMax, Exercise, ExerciseMuscle } from '../api/types';
import type { UserWeightUnitPreference } from '../api/userWeightUnitPreference';
import { getUserOneRepMaxes } from '../api/userOneRepMax';

interface ExerciseHistoryProps {
  user: User;
}

type TabName = 'onerepmax' | 'radial' | 'sunburst' | 'icicle';

/**
 * Exercise History page component for exercise history and trends.
 *
 * Tracks exercise history per lift and visualizes set/rep schemes,
 * and estimated 1RM with explanatory tooltips.
 *
 * @param user The user data
 * @return Exercise History page component
 */
export const ExerciseHistory: React.FC<ExerciseHistoryProps> = ({ user }) => {
  const [searchParams, setSearchParams] = useSearchParams();
  const { enqueueSnackbar } = useSnackbar();
  const [oneRepMaxes, setOneRepMaxes] = useState<UserOneRepMax[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [selectedExercise, setSelectedExercise] = useState<string>('all');
  const [allExercises, setAllExercises] = useState<Exercise[]>([]);
  const [exerciseMuscleData, setExerciseMuscleData] = useState<Map<string, string[]>>(new Map());
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>([]);
  const [workoutData, setWorkoutData] = useState<any>(null);

  // Get active tab from URL parameters, default to 'onerepmax'
  const activeTab = (searchParams.get('tab') as TabName) || 'onerepmax';

  useEffect(() => {
    loadExerciseHistoryData();
  }, [user.keycloak_id]);

  // Update URL when tab changes
  const handleTabChange = (event: React.SyntheticEvent, newValue: TabName) => {
    const newSearchParams = new URLSearchParams(searchParams);
    newSearchParams.set('tab', newValue);
    setSearchParams(newSearchParams);
  };

  const loadExerciseHistoryData = async () => {
    try {
      setIsLoading(true);

      const [oneRepMaxesData, allExercisesData, exerciseMuscleData, weightUnitPreferencesData, userDataExport] = await Promise.all([
        getUserOneRepMaxes(user.keycloak_id),
        getExercises(),
        getExerciseMuscle(),
        getUserWeightUnitPreferences(user.keycloak_id),
        getUserDataExport(),
      ]);

      setOneRepMaxes(oneRepMaxesData);
      setAllExercises(allExercisesData);
      
      // Convert exercise muscle data to Map
      const muscleMap = new Map<string, string[]>();
      exerciseMuscleData.forEach((item: ExerciseMuscle) => {
        const existing = muscleMap.get(item.exercise_name) || [];
        existing.push(item.muscle_name);
        muscleMap.set(item.exercise_name, existing);
      });
      setExerciseMuscleData(muscleMap);


      
      setWeightUnitPreferences(weightUnitPreferencesData.data || []);
      setWorkoutData(userDataExport);
    } catch {
      enqueueSnackbar('Failed to load exercise history data. Please try again.', {
        variant: 'error',
      });
    } finally {
      setIsLoading(false);
    }
  };

  // Get unique exercise names for filter (from both 1RM and exercises data)
  const uniqueExercises = Array.from(
    new Set([
      ...oneRepMaxes.map(orm => orm.exercise_name),
      ...allExercises.map(exercise => exercise.name),
    ])
  ).sort();

  // Filter data based on selected exercise
  const filteredOneRepMaxes =
    selectedExercise === 'all'
      ? oneRepMaxes
      : oneRepMaxes.filter(orm => orm.exercise_name === selectedExercise);

  // Helper function to convert weight to user's preferred unit
  const convertWeightToUserUnit = (weight: number, exerciseName: string): number => {
    const preference = weightUnitPreferences.find(pref => pref.exercise_name === exerciseName);
    const userUnit = preference?.preferred_unit || WeightUnit.LBS;
    
    if (userUnit === WeightUnit.LBS) {
      return weight;
    } else if (userUnit === WeightUnit.KG) {
      return weight * 0.453592;
    }
    
    return weight;
  };

  // Create exerciseMap from workout data for volume and frequency calculations
  const exerciseMap = useMemo(() => {
    const map = new Map<string, { totalVolume: number; frequency: number; lastPerformed: string | null }>();

    if (!workoutData?.training_programs) return map;

    workoutData.training_programs.forEach((program: any) => {
      program.workouts?.forEach((workout: any) => {
        workout.stages?.forEach((stage: any) => {
          stage.exercises?.forEach((exercise: any) => {
            const exerciseName = exercise.exercise.exercise_name;
            const existing = map.get(exerciseName) || { totalVolume: 0, frequency: 0, lastPerformed: null };
            
            // Calculate volume from set schemes with weight unit conversion
            let exerciseVolume = 0;
            exercise.set_schemes?.forEach((setScheme: any) => {
              // Use performed values if available, otherwise use target values
              const weight = setScheme.performed_weight || setScheme.target_weight;
              const reps = setScheme.performed_rep_count || setScheme.target_rep_count;
              
              if (weight && reps) {
                const convertedWeight = convertWeightToUserUnit(weight, exerciseName);
                exerciseVolume += convertedWeight * reps;
              }
            });
            
            existing.totalVolume += exerciseVolume;
            // Count frequency if there are any set schemes (programmed or performed)
            if (exercise.set_schemes && exercise.set_schemes.length > 0) {
              existing.frequency += 1;
            }
            
            // Update last performed date
            if (!existing.lastPerformed || (workout.workout.created_at && new Date(workout.workout.created_at) > new Date(existing.lastPerformed))) {
              existing.lastPerformed = workout.workout.created_at;
            }
            
            map.set(exerciseName, existing);
          });
        });
      });
    });
    
    return map;
  }, [workoutData, weightUnitPreferences]);

  // Calculate exercise statistics based on filtered data
  const exerciseStats = (selectedExercise === 'all' ? uniqueExercises : [selectedExercise]).map(
    exerciseName => {
      const exerciseOneRepMax = filteredOneRepMaxes.find(orm => orm.exercise_name === exerciseName);
      const exercise = allExercises.find(ex => ex.name === exerciseName);
      const performanceData = exerciseMap.get(exerciseName);

      return {
        name: exerciseName,
        oneRepMax: exerciseOneRepMax,
        isAccessory: exercise?.is_accessory || false,
        totalVolume: performanceData?.totalVolume || 0,
        frequency: performanceData?.frequency || 0,
        lastPerformed: performanceData?.lastPerformed || null,
      };
    }
  );

  // Chart data calculations
  const radialBarData = useMemo(() => {
    if (!exerciseStats.length) return [];

    return exerciseStats.map(exercise => {
      return {
        id: exercise.name,
        data: [
          {
            x: 'Volume',
            y: exercise.totalVolume,
          },
          {
            x: 'Frequency',
            y: exercise.frequency,
          },
          {
            x: '1RM Weight',
            y: exercise.oneRepMax?.one_rep_max || 0,
          },
        ],
      };
    }).slice(0, 10); // Top 10 exercises
  }, [exerciseStats]);

  const sunburstData = useMemo(() => {
    if (!exerciseStats.length) {
      return {
        name: 'Exercise Volume',
        loc: 0,
        children: [],
      };
    }

    // Group by muscle groups
    const muscleGroups = new Map<string, { totalVolume: number; exercises: Map<string, number> }>();
    
    exerciseStats.forEach(exercise => {
      const individualMuscles = exerciseMuscleData.get(exercise.name) || [];
      
      individualMuscles.forEach(muscle => {
        const existing = muscleGroups.get(muscle) || { totalVolume: 0, exercises: new Map() };
        existing.totalVolume += exercise.totalVolume;
        existing.exercises.set(exercise.name, exercise.totalVolume);
        muscleGroups.set(muscle, existing);
      });
    });

    return {
      name: 'Exercise Volume',
      loc: exerciseStats.reduce((sum, stat) => sum + stat.totalVolume, 0),
      children: Array.from(muscleGroups.entries()).map(([muscle, data]) => ({
        name: muscle,
        loc: data.totalVolume,
        children: Array.from(data.exercises.entries()).map(([exerciseName, volume]) => ({
          name: exerciseName,
          loc: volume,
        })),
      })),
    };
  }, [exerciseStats, exerciseMuscleData]);

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <React.Fragment>
      <Typography variant="h5" gutterBottom>
        Exercise History
      </Typography>

      {/* Exercise Filter */}
      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Autocomplete
            options={['all', ...uniqueExercises]}
            value={selectedExercise}
            onChange={(event, newValue) => setSelectedExercise(newValue || 'all')}
            renderInput={params => (
              <TextField
                {...params}
                label="Filter by Exercise"
                placeholder="Select an exercise or 'All Exercises'"
              />
            )}
            renderOption={(props, option) => (
              <Box component="li" {...props}>
                {option === 'all' ? 'All Exercises' : option}
              </Box>
            )}
            getOptionLabel={option => (option === 'all' ? 'All Exercises' : option)}
          />
        </CardContent>
      </Card>

      {/* Visualization Tabs */}
      <Card sx={{ mb: 4 }}>
        <Tabs
          value={activeTab}
          onChange={handleTabChange}
          variant="scrollable"
          scrollButtons="auto"
          sx={{ borderBottom: 1, borderColor: 'divider' }}
        >
          <Tab
            label="1RM Progress"
            value="onerepmax"
            icon={<ShowChartIcon />}
            iconPosition="start"
          />
          <Tab
            label="Exercise Performance Metrics"
            value="radial"
            icon={<FitnessCenterIcon />}
            iconPosition="start"
          />
          <Tab
            label="Exercise Volume Hierarchy"
            value="sunburst"
            icon={<ShowChartIcon />}
            iconPosition="start"
          />
        </Tabs>

        <Box sx={{ p: 3 }}>
          {/* 1RM Progress Tab */}
          {activeTab === 'onerepmax' && (
            <Box>
              <Typography variant="h6" gutterBottom>
                1RM Progress Tracking
              </Typography>

              {filteredOneRepMaxes.length > 0 ? (
                <Grid container spacing={3}>
                  {filteredOneRepMaxes.map((oneRepMax, index) => (
                    <Grid item xs={12} md={6} lg={4} key={index}>
                      <Card variant="outlined">
                        <CardContent>
                          <Typography variant="h6" gutterBottom>
                            {oneRepMax.exercise_name}
                          </Typography>
                          <Typography variant="h4" color="primary" gutterBottom>
                            {oneRepMax.one_rep_max} {oneRepMax.unit}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            Last Updated: {new Date(oneRepMax.updated_at).toLocaleDateString()}
                          </Typography>

                          <Tooltip title="This 1RM value is used for workout generation and progression calculations">
                            <Chip
                              icon={<InfoIcon />}
                              label="Estimated from performance"
                              size="small"
                              variant="outlined"
                              sx={{ mt: 1 }}
                            />
                          </Tooltip>
                        </CardContent>
                      </Card>
                    </Grid>
                  ))}
                </Grid>
              ) : (
                <Box sx={{ textAlign: 'center', py: 4 }}>
                  <Typography variant="body1" color="text.secondary">
                    No 1RM data available for the selected exercise.
                  </Typography>
                </Box>
              )}
            </Box>
          )}

          {/* Exercise Performance Tab */}
          {activeTab === 'radial' && (
            <Box>
              <RadialBarChart data={radialBarData} />
            </Box>
          )}

          {/* Exercise Volume Hierarchy Tab */}
          {activeTab === 'sunburst' && (
            <Box>
              <SunburstChart data={sunburstData} />
            </Box>
          )}
        </Box>
      </Card>
    </React.Fragment>
  );
};
