import { Refresh as RefreshIcon, FitnessCenter as FitnessCenterIcon, SportsGymnastics as SportsGymnasticsIcon } from '@mui/icons-material';
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
  Chip,
  Alert,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo, useRef, useCallback } from 'react';

import { FormDialog } from './FormDialog';
import { FormField } from './FormField';
import { LoadingSpinner } from './LoadingSpinner';
import { getExercises } from '../api/exercise';
import { getMuscles } from '../api/muscle';
import { getEquipment } from '../api/equipment';
import { getUserEquipment, addUserEquipment, removeUserEquipment } from '../api/userEquipment';
import { getUserWeakMuscles, addUserWeakMuscle, removeUserWeakMuscle } from '../api/userWeakMuscle';
import { WeightUnit, type Exercise, type UserWeightUnitPreference, type Muscle, type Equipment, type UserEquipment, type UserWeakMuscle } from '../api/types';
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
 * This component allows users to manage their weight unit preferences for exercises,
 * available equipment, and weak muscle groups for targeted training.
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

  // Equipment and weak muscles state
  const [muscles, setMuscles] = useState<Muscle[]>([]);
  const [equipment, setEquipment] = useState<Equipment[]>([]);
  const [userEquipment, setUserEquipment] = useState<UserEquipment[]>([]);
  const [userWeakMuscles, setUserWeakMuscles] = useState<UserWeakMuscle[]>([]);
  const [equipmentDialogOpen, setEquipmentDialogOpen] = useState(false);
  const [weakMuscleDialogOpen, setWeakMuscleDialogOpen] = useState(false);

  // Navigation state
  const [activeSection, setActiveSection] = useState('weight-units');
  const weightUnitsRef = useRef<HTMLDivElement>(null);
  const equipmentRef = useRef<HTMLDivElement>(null);
  const weakMusclesRef = useRef<HTMLDivElement>(null);

  // Form data type for TanStack Form
  interface WeightUnitPreferenceFormData extends Record<string, unknown> {
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
    const [
      unitResponse,
      exercisesResponse,
      musclesResponse,
      equipmentResponse,
      userEquipmentResponse,
      userWeakMusclesResponse,
    ] = await Promise.allSettled([
      getUserWeightUnitPreferences(user!.keycloak_id),
      getExercises(),
      getMuscles(),
      getEquipment(),
      getUserEquipment(user!.keycloak_id),
      getUserWeakMuscles(user!.keycloak_id),
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

    // Handle muscles
    if (musclesResponse.status === 'fulfilled') {
      setMuscles(musclesResponse.value);
    }

    // Handle equipment
    if (equipmentResponse.status === 'fulfilled') {
      setEquipment(equipmentResponse.value);
    }

    // Handle user equipment
    if (userEquipmentResponse.status === 'fulfilled') {
      setUserEquipment(userEquipmentResponse.value);
    }

    // Handle user weak muscles
    if (userWeakMusclesResponse.status === 'fulfilled') {
      setUserWeakMuscles(userWeakMusclesResponse.value);
    }

    setLoading(false);
  };

  // Navigation functions
  const scrollToSection = useCallback((sectionId: string) => {
    const refs = {
      'weight-units': weightUnitsRef,
      'equipment': equipmentRef,
      'weak-muscles': weakMusclesRef,
    };
    
    const ref = refs[sectionId as keyof typeof refs];
    if (ref?.current) {
      ref.current.scrollIntoView({ 
        behavior: 'smooth', 
        block: 'start' 
      });
    }
  }, []);

  // Intersection Observer for scroll detection
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const sectionId = entry.target.getAttribute('data-section');
            if (sectionId) {
              setActiveSection(sectionId);
            }
          }
        });
      },
      {
        rootMargin: '-20% 0px -70% 0px', // Trigger when section is 20% from top
        threshold: 0.1,
      }
    );

    // Observe all section refs
    [weightUnitsRef, equipmentRef, weakMusclesRef].forEach((ref) => {
      if (ref.current) {
        observer.observe(ref.current);
      }
    });

    return () => {
      observer.disconnect();
    };
  }, [loading]); // Re-run when loading changes (data is loaded)

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

  // Equipment handlers
  const handleAddEquipment = async (equipmentName: string) => {
    if (!user?.keycloak_id) return;

    try {
      setSaving(true);
      await addUserEquipment(user.keycloak_id, equipmentName);
      
      // Refresh user equipment
      const userEquipmentResponse = await getUserEquipment(user.keycloak_id);
      setUserEquipment(userEquipmentResponse);
      
      enqueueSnackbar('Equipment added successfully', { variant: 'success' });
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(
        axiosError.response?.data?.message || 'Failed to add equipment',
        { variant: 'error' }
      );
    } finally {
      setSaving(false);
    }
  };

  const handleRemoveEquipment = async (equipmentName: string) => {
    if (!user?.keycloak_id) return;

    try {
      setSaving(true);
      await removeUserEquipment(user.keycloak_id, equipmentName);
      
      // Refresh user equipment
      const userEquipmentResponse = await getUserEquipment(user.keycloak_id);
      setUserEquipment(userEquipmentResponse);
      
      enqueueSnackbar('Equipment removed successfully', { variant: 'success' });
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(
        axiosError.response?.data?.message || 'Failed to remove equipment',
        { variant: 'error' }
      );
    } finally {
      setSaving(false);
    }
  };

  // Weak muscles handlers
  const handleAddWeakMuscle = async (muscleName: string) => {
    if (!user?.keycloak_id) return;

    try {
      setSaving(true);
      await addUserWeakMuscle(user.keycloak_id, muscleName);
      
      // Refresh user weak muscles
      const userWeakMusclesResponse = await getUserWeakMuscles(user.keycloak_id);
      setUserWeakMuscles(userWeakMusclesResponse);
      
      enqueueSnackbar('Weak muscle group added successfully', { variant: 'success' });
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(
        axiosError.response?.data?.message || 'Failed to add weak muscle group',
        { variant: 'error' }
      );
    } finally {
      setSaving(false);
    }
  };

  const handleRemoveWeakMuscle = async (muscleName: string) => {
    if (!user?.keycloak_id) return;

    try {
      setSaving(true);
      await removeUserWeakMuscle(user.keycloak_id, muscleName);
      
      // Refresh user weak muscles
      const userWeakMusclesResponse = await getUserWeakMuscles(user.keycloak_id);
      setUserWeakMuscles(userWeakMusclesResponse);
      
      enqueueSnackbar('Weak muscle group removed successfully', { variant: 'success' });
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(
        axiosError.response?.data?.message || 'Failed to remove weak muscle group',
        { variant: 'error' }
      );
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading workout preferences..." fullHeight={false} />;
  }

  return (
    <Box sx={{ display: 'flex', height: '100vh', overflow: 'hidden' }}>
      {/* Sticky Sidebar Navigation */}
      <Box 
        sx={{ 
          width: 250, 
          minWidth: 250,
          height: '100vh',
          overflow: 'auto',
          position: 'sticky',
          top: 0,
          zIndex: 1,
          p: 2,
          borderRight: 1,
          borderColor: 'divider',
        }}
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1 }}>
          <Typography
            variant="body1"
            onClick={() => scrollToSection('weight-units')}
            sx={{
              cursor: 'pointer',
              color: activeSection === 'weight-units' ? 'primary.main' : 'text.primary',
              textDecoration: activeSection === 'weight-units' ? 'underline' : 'none',
              fontWeight: activeSection === 'weight-units' ? 'bold' : 'normal',
              '&:hover': {
                color: 'primary.main',
                textDecoration: 'underline',
              },
            }}
          >
            Weight Unit Preferences
          </Typography>
          <Typography
            variant="body1"
            onClick={() => scrollToSection('equipment')}
            sx={{
              cursor: 'pointer',
              color: activeSection === 'equipment' ? 'primary.main' : 'text.primary',
              textDecoration: activeSection === 'equipment' ? 'underline' : 'none',
              fontWeight: activeSection === 'equipment' ? 'bold' : 'normal',
              '&:hover': {
                color: 'primary.main',
                textDecoration: 'underline',
              },
            }}
          >
            Available Equipment
          </Typography>
          <Typography
            variant="body1"
            onClick={() => scrollToSection('weak-muscles')}
            sx={{
              cursor: 'pointer',
              color: activeSection === 'weak-muscles' ? 'primary.main' : 'text.primary',
              textDecoration: activeSection === 'weak-muscles' ? 'underline' : 'none',
              fontWeight: activeSection === 'weak-muscles' ? 'bold' : 'normal',
              '&:hover': {
                color: 'primary.main',
                textDecoration: 'underline',
              },
            }}
          >
            Weak Muscle Groups
          </Typography>
        </Box>
      </Box>

      {/* Main Content Area */}
      <Box sx={{ flex: 1, overflow: 'auto', height: '100vh' }}>
        <Box sx={{ p: 3 }}>
          {/* Weight Unit Preferences Section */}
          <div ref={weightUnitsRef} data-section="weight-units">
            <Card sx={{ mb: 3 }}>
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
          </div>

          {/* Available Equipment Section */}
          <div ref={equipmentRef} data-section="equipment">
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                  <Typography variant="h6">Available Equipment</Typography>
                  <Button variant="outlined" size="small" onClick={() => setEquipmentDialogOpen(true)}>
                    Add Equipment
                  </Button>
                </Box>
                <Typography variant="body2" color="text.secondary" paragraph>
                  Manage the equipment you have available for workouts. This affects which exercises are available in your exercise pool.
                </Typography>

                <Divider sx={{ mb: 2 }} />

                {userEquipment.length === 0 ? (
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ textAlign: 'center', py: 2 }}
                  >
                    No equipment added yet. Add equipment to expand your exercise pool.
                  </Typography>
                ) : (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                    {userEquipment.map(equipment => (
                      <Chip
                        key={equipment.equipment_name}
                        label={equipment.equipment_name}
                        onDelete={() => handleRemoveEquipment(equipment.equipment_name)}
                        disabled={saving}
                        color="primary"
                        variant="outlined"
                      />
                    ))}
                  </Box>
                )}
              </CardContent>
            </Card>
          </div>

          {/* Weak Muscle Groups Section */}
          <div ref={weakMusclesRef} data-section="weak-muscles">
            <Card sx={{ mb: 3 }}>
              <CardContent>
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
                  <Typography variant="h6">Weak Muscle Groups</Typography>
                  <Button variant="outlined" size="small" onClick={() => setWeakMuscleDialogOpen(true)}>
                    Add Weak Muscle
                  </Button>
                </Box>
                <Typography variant="body2" color="text.secondary" paragraph>
                  Identify muscle groups you want to target for improvement. This helps prioritize exercises that work these areas.
                </Typography>

                <Divider sx={{ mb: 2 }} />

                {userWeakMuscles.length === 0 ? (
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ textAlign: 'center', py: 2 }}
                  >
                    No weak muscle groups identified yet. Add muscle groups you want to focus on.
                  </Typography>
                ) : (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
                    {userWeakMuscles.map(weakMuscle => (
                      <Chip
                        key={weakMuscle.muscle_name}
                        label={weakMuscle.muscle_name}
                        onDelete={() => handleRemoveWeakMuscle(weakMuscle.muscle_name)}
                        disabled={saving}
                        color="warning"
                        variant="outlined"
                      />
                    ))}
                  </Box>
                )}
              </CardContent>
            </Card>
          </div>
        </Box>
      </Box>

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
        validate={values => {
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
        {form => (
          <Box display="flex" flexDirection="column" gap={2} sx={{ mt: 1 }}>
            <FormField
              type="select"
              label="Exercise"
              name="exerciseName"
              form={form}
              options={
                exercises && exercises.length > 0
                  ? exercises.map(exercise => ({ value: exercise.name, label: exercise.name }))
                  : [{ value: '', label: 'No exercises available' }]
              }
            />

            <FormField
              type="select"
              label="Preferred Unit"
              name="preferredUnit"
              form={form}
              options={[
                { value: WeightUnit.KG, label: 'Kilograms (KG)' },
                { value: WeightUnit.LBS, label: 'Pounds (LBS)' },
              ]}
            />
          </Box>
        )}
      </FormDialog>

      {/* Add Equipment Dialog */}
      <FormDialog<{ equipmentName: string }>
        open={equipmentDialogOpen}
        onClose={() => setEquipmentDialogOpen(false)}
        onSubmit={(data) => {
          handleAddEquipment(data.equipmentName);
          setEquipmentDialogOpen(false);
        }}
        title="Add Equipment"
        submitText="Add Equipment"
        loading={saving}
        useTanStackForm={true}
        defaultValues={{
          equipmentName: '',
        }}
        validate={values => {
          const errors: Record<string, string> = {};
          if (!values.equipmentName) {
            errors.equipmentName = 'Please select equipment';
          }
          return Object.keys(errors).length > 0 ? errors : undefined;
        }}
      >
        {form => (
          <Box display="flex" flexDirection="column" gap={2} sx={{ mt: 1 }}>
            <FormField
              type="select"
              label="Equipment"
              name="equipmentName"
              form={form}
              options={
                equipment && equipment.length > 0
                  ? equipment.map(eq => ({ value: eq.name, label: eq.name }))
                  : [{ value: '', label: 'No equipment available' }]
              }
            />
          </Box>
        )}
      </FormDialog>

      {/* Add Weak Muscle Dialog */}
      <FormDialog<{ muscleName: string }>
        open={weakMuscleDialogOpen}
        onClose={() => setWeakMuscleDialogOpen(false)}
        onSubmit={(data) => {
          handleAddWeakMuscle(data.muscleName);
          setWeakMuscleDialogOpen(false);
        }}
        title="Add Weak Muscle Group"
        submitText="Add Muscle Group"
        loading={saving}
        useTanStackForm={true}
        defaultValues={{
          muscleName: '',
        }}
        validate={values => {
          const errors: Record<string, string> = {};
          if (!values.muscleName) {
            errors.muscleName = 'Please select a muscle group';
          }
          return Object.keys(errors).length > 0 ? errors : undefined;
        }}
      >
        {form => (
          <Box display="flex" flexDirection="column" gap={2} sx={{ mt: 1 }}>
            <FormField
              type="select"
              label="Muscle Group"
              name="muscleName"
              form={form}
              options={
                muscles && muscles.length > 0
                  ? muscles.map(muscle => ({ value: muscle.name, label: muscle.name }))
                  : [{ value: '', label: 'No muscles available' }]
              }
            />
          </Box>
        )}
      </FormDialog>
    </Box>
  );
}
