import { Refresh as RefreshIcon } from '@mui/icons-material';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Button,
  Grid,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Divider,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo } from 'react';

import { LoadingSpinner } from './LoadingSpinner';
import { FormDialog } from './FormDialog';
import { FormField } from './FormField';
import { getExercises } from '../api/exercise';
import { WeightUnit,
  type Exercise,
  type UserWeightUnitPreference,
 } from '../api/types';
import {
  getUserWeightUnitPreferences,
  upsertUserWeightUnitPreference,
  deleteUserWeightUnitPreference,
} from '../api/userWeightUnitPreference';
import { useAuth } from '../contexts/AuthContext';

import type { AxiosError } from 'axios';

/**
 * Workout preferences section component for user profile.
 *
 * This component allows users to manage their weight unit preferences for exercises.
 * Program preferences (workout frequency, duration) are now managed at the program level
 * when creating or editing programs.
 *
 * @return Workout preferences section component
 */
export function WorkoutPreferencesSection(): React.ReactElement {
  const { user } = useAuth();
  const { enqueueSnackbar } = useSnackbar();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  // Weight unit preferences state
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>(
    []
  );
  const [exercises, setExercises] = useState<Exercise[]>([]);
  const [unitDialogOpen, setUnitDialogOpen] = useState(false);

  // Form data type for TanStack Form
  interface WeightUnitPreferenceFormData {
    exerciseName: string;
    preferredUnit: WeightUnit;
  }

  // Load initial data
  useEffect(() => {
    if (user?.keycloak_id) {
      loadData();
    }
  }, [user?.keycloak_id]);

  const loadData = async () => {
    setLoading(true);

    // Load all data in parallel for better performance
    const [unitResponse, exercisesResponse] = await Promise.allSettled([
      getUserWeightUnitPreferences(user!.keycloak_id),
      getExercises(),
    ]);

    // Handle weight unit preferences
    if (unitResponse.status === 'fulfilled') {
      setWeightUnitPreferences(unitResponse.value);
    }
    // If rejected, no weight unit preferences yet

    // Handle exercises
    if (exercisesResponse.status === 'fulfilled') {
      setExercises(exercisesResponse.value);
    } else {
      enqueueSnackbar('Failed to load exercises. Please try again.', { variant: 'error' });
      setExercises([]);
    }

    setLoading(false);
  };

  const handleAddWeightUnitPreference = async (data: WeightUnitPreferenceFormData) => {
    if (!user?.keycloak_id) return;

    try {
      setSaving(true);

      await upsertUserWeightUnitPreference(user.keycloak_id, data.exerciseName, data.preferredUnit);

      // Refresh weight unit preferences
      const unitResponse = await getUserWeightUnitPreferences(user.keycloak_id);
      setWeightUnitPreferences(unitResponse);

      setUnitDialogOpen(false);
      enqueueSnackbar('Weight unit preference added successfully', { variant: 'success' });
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(
        axiosError.response?.data?.message || 'Failed to add weight unit preference',
        { variant: 'error' }
      );
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteWeightUnitPreference = async (exerciseName: string) => {
    if (!user?.keycloak_id) return;

    try {
      setSaving(true);

      await deleteUserWeightUnitPreference(user.keycloak_id, exerciseName);

      // Refresh weight unit preferences
      const unitResponse = await getUserWeightUnitPreferences(user.keycloak_id);
      setWeightUnitPreferences(unitResponse);

      enqueueSnackbar('Weight unit preference deleted successfully', { variant: 'success' });
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(
        axiosError.response?.data?.message || 'Failed to delete weight unit preference',
        { variant: 'error' }
      );
    } finally {
      setSaving(false);
    }
  };

  const exerciseNameMap = useMemo(() => {
    const map = new Map<string, string>();
    exercises.forEach(exercise => {
      map.set(exercise.name, exercise.name);
    });
    return map;
  }, [exercises]);

  const getExerciseName = (exerciseName: string) => {
    return exerciseNameMap.get(exerciseName) || exerciseName;
  };

  if (loading) {
    return <LoadingSpinner message="Loading workout preferences..." fullHeight={false} />;
  }

  return (
    <Box>
      <Grid container spacing={3}>
        {/* Weight Unit Preferences */}
        <Grid size={{ xs: 12 }}>
          <Card>
            <CardContent>
              <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                <Typography variant="h6">Weight Unit Preferences</Typography>
                <Button variant="outlined" size="small" onClick={() => setUnitDialogOpen(true)}>
                  Add Preference
                </Button>
              </Box>
              <Typography variant="body2" color="text.secondary" paragraph>
                Set your preferred weight units for specific exercises.
              </Typography>

              <Divider sx={{ mb: 2 }} />

              {weightUnitPreferences.length === 0 ? (
                <Typography
                  variant="body2"
                  color="text.secondary"
                  sx={{ textAlign: 'center', py: 2 }}
                >
                  No weight unit preferences set yet.
                </Typography>
              ) : (
                <List dense>
                  {weightUnitPreferences.map(pref => (
                    <ListItem key={`${pref.user_id}-${pref.exercise_name}`}>
                      <ListItemText
                        primary={getExerciseName(pref.exercise_name)}
                        secondary={`Preferred unit: ${pref.preferred_unit}`}
                      />
                      <ListItemSecondaryAction>
                        <IconButton
                          edge="end"
                          aria-label="delete"
                          onClick={() => handleDeleteWeightUnitPreference(pref.exercise_name)}
                          disabled={saving}
                        >
                          <RefreshIcon />
                        </IconButton>
                      </ListItemSecondaryAction>
                    </ListItem>
                  ))}
                </List>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Add Weight Unit Preference Dialog */}
      <FormDialog<WeightUnitPreferenceFormData>
        open={unitDialogOpen}
        onClose={() => setUnitDialogOpen(false)}
        onSubmit={handleAddWeightUnitPreference}
        title="Add Weight Unit Preference"
        submitText="Add Preference"
        loading={saving}
        useTanStackForm={true}
        defaultValues={{
          exerciseName: '',
          preferredUnit: WeightUnit.LBS,
        }}
        validate={(values) => {
          const errors: Record<string, string> = {};
          if (!values.exerciseName) {
            errors.exerciseName = 'Please select an exercise';
          }
          if (!values.preferredUnit) {
            errors.preferredUnit = 'Please select a preferred unit';
          }
          return Object.keys(errors).length > 0 ? errors : undefined;
        }}
      >
        {(form) => (
          <Box display="flex" flexDirection="column" gap={2} sx={{ mt: 1 }}>
            <FormField
              type="select"
              label="Exercise"
              name="exerciseName"
              form={form}
              options={exercises && exercises.length > 0 ? exercises.map(exercise => ({ value: exercise.name, label: exercise.name })) : [{ value: '', label: 'No exercises available' }]}
            />

            <FormField
              type="select"
              label="Preferred Unit"
              name="preferredUnit"
              form={form}
              options={[
                { value: WeightUnit.KG, label: 'Kilograms (KG)' },
                { value: WeightUnit.LBS, label: 'Pounds (LBS)' }
              ]}
            />
          </Box>
        )}
      </FormDialog>
    </Box>
  );
}
