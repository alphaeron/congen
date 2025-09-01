import { default as FitnessCenterIcon } from '@mui/icons-material/FitnessCenter';
import { default as InfoIcon } from '@mui/icons-material/Info';
import { default as ShowChartIcon } from '@mui/icons-material/ShowChart';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Tabs,
  Tab,
  TextField,
  Autocomplete,
  Chip,
  Tooltip,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router';

import { RadialBarChart } from './RadialBarChart';
import { getExercises } from '../api/exercise';
import { getExerciseMuscle } from '../api/exerciseMuscle';
import { getUserDataExport } from '../api/gdpr';
import type {
  User,
  UserOneRepMax,
  Exercise,
  ExerciseMuscle,
} from '../api/types';
import { getUserOneRepMaxes } from '../api/userOneRepMax';
import { getUserWeightUnitPreferences } from '../api/userWeightUnitPreference';
import type { UserWeightUnitPreference } from '../api/userWeightUnitPreference';
import { formatDate } from '../common/utils';
import { LoadingSpinner } from './LoadingSpinner';

interface ExerciseHistoryProps {
  user: User;
}

type TabName = 'onerepmax' | 'radial';

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
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>(
    []
  );
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

      const [
        oneRepMaxesData,
        allExercisesData,
        exerciseMuscleData,
        weightUnitPreferencesData,
        userDataExport,
      ] = await Promise.all([
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

  if (isLoading) {
    return (
      <LoadingSpinner message="Loading exercise history..." fullHeight={false} />
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
                    <Grid size={{ xs: 12, md: 6, lg: 4 }} key={index}>
                      <Card variant="outlined">
                        <CardContent>
                          <Typography variant="h6" gutterBottom>
                            {oneRepMax.exercise_name}
                          </Typography>
                          <Typography variant="h4" color="primary" gutterBottom>
                            {oneRepMax.one_rep_max} {oneRepMax.unit}
                          </Typography>
                          <Typography variant="body2" color="text.secondary">
                            Last Updated: {formatDate(oneRepMax.updated_at)}
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
              <RadialBarChart
                userDataExport={workoutData}
                exerciseData={new Map()}
                oneRepMaxes={oneRepMaxes}
                weightUnitPreferences={weightUnitPreferences}
                selectedExercise={selectedExercise}
              />
            </Box>
          )}

        </Box>
      </Card>
    </React.Fragment>
  );
};
