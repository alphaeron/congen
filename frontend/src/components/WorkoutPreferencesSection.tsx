import { default as RefreshIcon } from '@mui/icons-material/Refresh';
import { default as SaveIcon } from '@mui/icons-material/Save';
import {
  Box,
  Button,
  Card,
  CardContent,
  Divider,
  FormControl,
  FormHelperText,
  Grid,
  InputLabel,
  MenuItem,
  Select,
  Typography,
  Alert,
  CircularProgress,
  List,
  ListItem,
  ListItemText,
  ListItemSecondaryAction,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import React, { useEffect, useState, useMemo } from 'react';

import { getExercises } from '../api/exercise';
import type { Exercise } from '../api/types';
import {
  getUserProgramPreferences,
  updateUserProgramPreferences,
  createUserProgramPreferences,
  type UserProgramPreferences,
} from '../api/userProgramPreferences';
import {
  getUserWeightUnitPreferences,
  upsertUserWeightUnitPreference,
  deleteUserWeightUnitPreference,
  type UserWeightUnitPreference,
  WeightUnit,
} from '../api/userWeightUnitPreference';
import { useAuth } from '../contexts/AuthContext';

/**
 * Workout preferences section component for user profile.
 *
 * This component allows users to manage their workout program preferences
 * including workout frequency, duration, and weight unit preferences for exercises.
 *
 * @return Workout preferences section component
 */
export function WorkoutPreferencesSection(): React.ReactElement {
  const { user } = useAuth();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Program preferences state
  const [programPreferences, setProgramPreferences] = useState<UserProgramPreferences | null>(null);
  const [programDaysPerWeek, setProgramDaysPerWeek] = useState(3);
  const [sessionTimeLength, setSessionTimeLength] = useState(60);

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
    setError(null);

    // Load all data in parallel for better performance
    const [prefsResponse, unitResponse, exercisesResponse] = await Promise.allSettled([
      getUserProgramPreferences(user!.keycloak_id),
      getUserWeightUnitPreferences(user!.keycloak_id),
      getExercises(),
    ]);

    // Handle program preferences
    if (prefsResponse.status === 'fulfilled') {
      setProgramPreferences(prefsResponse.value.data);
      setProgramDaysPerWeek(prefsResponse.value.data.program_days_per_week);
      setSessionTimeLength(prefsResponse.value.data.session_time_length_in_minutes);
    }
    // If rejected, program preferences don't exist yet, use defaults

    // Handle weight unit preferences
    if (unitResponse.status === 'fulfilled') {
      setWeightUnitPreferences(unitResponse.value.data);
    }
    // If rejected, no weight unit preferences yet

    // Handle exercises
    if (exercisesResponse.status === 'fulfilled') {
      setExercises(exercisesResponse.value);
    } else {
      console.error('Failed to load exercises:', exercisesResponse.reason);
      setExercises([]);
    }

    setLoading(false);
  };

  const handleSaveProgramPreferences = async () => {
    if (!user?.keycloak_id) return;

    try {
      setSaving(true);
      setError(null);

      if (programPreferences) {
        // Update existing preferences
        await updateUserProgramPreferences(user.keycloak_id, programDaysPerWeek, sessionTimeLength);
      } else {
        // Create new preferences
        const response = await createUserProgramPreferences(
          user.keycloak_id,
          programDaysPerWeek,
          sessionTimeLength
        );
        setProgramPreferences(response.data);
      }

      setSuccessMessage('Program preferences saved successfully');
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      setError(axiosError.response?.data?.message || 'Failed to save program preferences');
    } finally {
      setSaving(false);
    }
  };

  const handleAddWeightUnitPreference = async () => {
    if (!user?.keycloak_id || !selectedExercise) return;

    try {
      setSaving(true);
      setError(null);

      await upsertUserWeightUnitPreference(user.keycloak_id, selectedExercise, selectedUnit);

      // Refresh weight unit preferences
      const unitResponse = await getUserWeightUnitPreferences(user.keycloak_id);
      setWeightUnitPreferences(unitResponse.data);

      setUnitDialogOpen(false);
      setSelectedExercise('');
      setSelectedUnit(WeightUnit.LBS);
      setSuccessMessage('Weight unit preference added successfully');
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      setError(axiosError.response?.data?.message || 'Failed to add weight unit preference');
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteWeightUnitPreference = async (exerciseName: string) => {
    if (!user?.keycloak_id) return;

    try {
      setSaving(true);
      setError(null);

      await deleteUserWeightUnitPreference(user.keycloak_id, exerciseName);

      // Refresh weight unit preferences
      const unitResponse = await getUserWeightUnitPreferences(user.keycloak_id);
      setWeightUnitPreferences(unitResponse.data);

      setSuccessMessage('Weight unit preference deleted successfully');
    } catch (err: unknown) {
      const axiosError = err as { response?: { data?: { message?: string } } };
      setError(axiosError.response?.data?.message || 'Failed to delete weight unit preference');
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
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight={200}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h5" gutterBottom>
        Workout Preferences
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        Manage your workout program preferences and settings.
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}

      {successMessage && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMessage(null)}>
          {successMessage}
        </Alert>
      )}

      {/* Current Settings Summary */}
      <Grid item xs={12}>
        <Card>
          <CardContent>
            <Typography variant="h6" gutterBottom>
              Current Settings Summary
            </Typography>
            <Divider sx={{ mb: 2 }} />

            <Grid container spacing={2}>
              <Grid item xs={12} sm={6} md={3}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Program Days
                  </Typography>
                  <Typography variant="h6">{programDaysPerWeek} days/week</Typography>
                </Box>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Session Length
                  </Typography>
                  <Typography variant="h6">{sessionTimeLength} minutes</Typography>
                </Box>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Weight Unit Preferences
                  </Typography>
                  <Typography variant="h6">{weightUnitPreferences.length} exercises</Typography>
                </Box>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Box>
                  <Typography variant="body2" color="text.secondary">
                    Last Updated
                  </Typography>
                  <Typography variant="h6">
                    {programPreferences?.updated_at
                      ? new Date(programPreferences.updated_at).toLocaleDateString()
                      : 'Never'}
                  </Typography>
                </Box>
              </Grid>
            </Grid>
          </CardContent>
        </Card>
      </Grid>

      <Grid container spacing={3}>
        {/* Program Preferences */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Program Settings
              </Typography>
              <Typography variant="body2" color="text.secondary" paragraph>
                Configure your workout frequency and duration.
              </Typography>

              <Divider sx={{ mb: 3 }} />

              <Box display="flex" flexDirection="column" gap={3}>
                <FormControl fullWidth>
                  <InputLabel>Days per Week</InputLabel>
                  <Select
                    value={programDaysPerWeek}
                    label="Days per Week"
                    onChange={e => setProgramDaysPerWeek(e.target.value as number)}
                  >
                    <MenuItem value={2}>2 days</MenuItem>
                    <MenuItem value={3}>3 days</MenuItem>
                    <MenuItem value={4}>4 days</MenuItem>
                    <MenuItem value={5}>5 days</MenuItem>
                    <MenuItem value={6}>6 days</MenuItem>
                  </Select>
                  <FormHelperText>Number of workout days per week for your program</FormHelperText>
                </FormControl>

                <FormControl fullWidth>
                  <InputLabel>Session Length (minutes)</InputLabel>
                  <Select
                    value={sessionTimeLength}
                    label="Session Length (minutes)"
                    onChange={e => setSessionTimeLength(e.target.value as number)}
                  >
                    <MenuItem value={30}>30 minutes</MenuItem>
                    <MenuItem value={45}>45 minutes</MenuItem>
                    <MenuItem value={60}>60 minutes</MenuItem>
                    <MenuItem value={75}>75 minutes</MenuItem>
                    <MenuItem value={90}>90 minutes</MenuItem>
                  </Select>
                  <FormHelperText>Target duration for each workout session</FormHelperText>
                </FormControl>

                <Button
                  variant="contained"
                  startIcon={saving ? <CircularProgress size={20} /> : <SaveIcon />}
                  onClick={handleSaveProgramPreferences}
                  disabled={saving}
                  fullWidth
                >
                  {saving ? 'Saving...' : 'Save Program Preferences'}
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Weight Unit Preferences */}
        <Grid item xs={12} md={6}>
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
