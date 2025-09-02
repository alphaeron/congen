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
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  Divider,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo } from 'react';

import { LoadingSpinner } from './LoadingSpinner';
import { getExercises } from '../api/exercise';
import type { Exercise } from '../api/types';
import {
  getUserWeightUnitPreferences,
  upsertUserWeightUnitPreference,
  deleteUserWeightUnitPreference,
  type UserWeightUnitPreference,
  WeightUnit,
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
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Weight unit preferences state
  const [weightUnitPreferences, setWeightUnitPreferences] = useState<UserWeightUnitPreference[]>(
    []
  );
  const [exercises, setExercises] = useState<Exercise[]>([]);
  const [selectedExercise, setSelectedExercise] = useState('');
  const [selectedUnit, setSelectedUnit] = useState<WeightUnit>(WeightUnit.LBS);
  const [unitDialogOpen, setUnitDialogOpen] = useState(false);

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

  const handleAddWeightUnitPreference = async () => {
    if (!user?.keycloak_id || !selectedExercise) return;

    try {
      setSaving(true);

      await upsertUserWeightUnitPreference(user.keycloak_id, selectedExercise, selectedUnit);

      // Refresh weight unit preferences
      const unitResponse = await getUserWeightUnitPreferences(user.keycloak_id);
      setWeightUnitPreferences(unitResponse);

      setUnitDialogOpen(false);
      setSelectedExercise('');
      setSelectedUnit(WeightUnit.LBS);
      setSuccessMessage('Weight unit preference added successfully');
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

      setSuccessMessage('Weight unit preference deleted successfully');
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
      <Typography variant="h5" gutterBottom>
        Weight Unit Preferences
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        Manage your weight unit preferences for exercises.
      </Typography>

      {successMessage && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMessage(null)}>
          {successMessage}
        </Alert>
      )}

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
      <Dialog
        open={unitDialogOpen}
        onClose={() => setUnitDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Add Weight Unit Preference</DialogTitle>
        <DialogContent>
          <Box display="flex" flexDirection="column" gap={2} sx={{ mt: 1 }}>
            <FormControl fullWidth>
              <InputLabel>Exercise</InputLabel>
              <Select
                value={selectedExercise}
                label="Exercise"
                onChange={e => setSelectedExercise(e.target.value)}
              >
                {exercises && exercises.length > 0 ? (
                  exercises.map(exercise => (
                    <MenuItem key={exercise.name} value={exercise.name}>
                      {exercise.name}
                    </MenuItem>
                  ))
                ) : (
                  <MenuItem disabled>No exercises available</MenuItem>
                )}
              </Select>
            </FormControl>

            <FormControl fullWidth>
              <InputLabel>Preferred Unit</InputLabel>
              <Select
                value={selectedUnit}
                label="Preferred Unit"
                onChange={e => setSelectedUnit(e.target.value as WeightUnit)}
              >
                <MenuItem value={WeightUnit.KG}>Kilograms (KG)</MenuItem>
                <MenuItem value={WeightUnit.LBS}>Pounds (LBS)</MenuItem>
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setUnitDialogOpen(false)} disabled={saving}>
            Cancel
          </Button>
          <Button
            onClick={handleAddWeightUnitPreference}
            variant="contained"
            disabled={saving || !selectedExercise}
          >
            {saving ? 'Adding...' : 'Add Preference'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
