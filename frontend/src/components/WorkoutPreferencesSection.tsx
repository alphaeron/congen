import { Box, List, Tabs, Tab } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect, useMemo } from 'react';
import { motion, AnimatePresence } from 'framer-motion';

import { DeletableChip } from './DeletableChip';
import { DeletableListItem } from './DeletableListItem';
import { FormDialog } from './FormDialog';
import { FormField } from './FormField';
import { LoadingSpinner } from './LoadingSpinner';
import { PreferenceSection } from './PreferenceSection';
import { GAME_CLASSES } from './GameTheme';
import {
  WeightUnit,
  type Exercise,
  type Muscle,
  type Equipment,
  type UserEquipment,
  type UserWeakMuscle,
  type UserExercisePreference,
} from '../api/types';
import { addUserEquipment, removeUserEquipment } from '../api/userEquipment';
import {
  upsertUserExercisePreference,
  removeUserExercisePreference,
} from '../api/userExercisePreference';
import { addUserWeakMuscle, removeUserWeakMuscle } from '../api/userWeakMuscle';
import {
  upsertUserWeightUnitPreference,
  deleteUserWeightUnitPreference,
} from '../api/userWeightUnitPreference';
import { useAuth } from '../contexts/AuthContext';
import { useData } from '../contexts/DataContext';

import type { AxiosError } from 'axios';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
  slideDirection?: 'left' | 'right';
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, slideDirection = 'left', ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`preferences-tabpanel-${index}`}
      aria-labelledby={`preferences-tab-${index}`}
      {...other}
    >
      <AnimatePresence mode="wait">
        {value === index && (
          <motion.div
            key={index}
            initial={{ 
              opacity: 0, 
              x: slideDirection === 'left' ? 50 : -50 
            }}
            animate={{ 
              opacity: 1, 
              x: 0 
            }}
            exit={{ 
              opacity: 0, 
              x: slideDirection === 'left' ? -50 : 50 
            }}
            transition={{ 
              duration: 0.3, 
              ease: 'easeInOut' 
            }}
          >
            <Box sx={{ p: 0 }}>{children}</Box>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}

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
  const {
    weightUnitPreferences,
    loadAllExercises,
    loadAllMuscles,
    loadAllEquipment,
    loadUserEquipment,
    loadUserWeakMuscles,
    loadUserExercisePreferences,
    refreshData,
  } = useData();
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [exercises, setExercises] = useState<Exercise[]>([]);
  const [unitDialogOpen, setUnitDialogOpen] = useState(false);

  // Equipment and weak muscles state
  const [muscles, setMuscles] = useState<Muscle[]>([]);
  const [equipment, setEquipment] = useState<Equipment[]>([]);
  const [userEquipment, setUserEquipment] = useState<UserEquipment[]>([]);
  const [userWeakMuscles, setUserWeakMuscles] = useState<UserWeakMuscle[]>([]);
  const [equipmentDialogOpen, setEquipmentDialogOpen] = useState(false);
  const [weakMuscleDialogOpen, setWeakMuscleDialogOpen] = useState(false);

  // Exercise preferences state
  const [userExercisePreferences, setUserExercisePreferences] = useState<UserExercisePreference[]>(
    []
  );
  const [exercisePreferenceDialogOpen, setExercisePreferenceDialogOpen] = useState(false);

  // Tab state
  const [activeTab, setActiveTab] = useState(0);
  const [slideDirection, setSlideDirection] = useState<'left' | 'right'>('left');

  // Form data types for TanStack Form
  interface WeightUnitPreferenceFormData extends Record<string, unknown> {
    exerciseName: string;
    preferredUnit: WeightUnit;
  }

  interface ExercisePreferenceFormData extends Record<string, unknown> {
    exerciseName: string;
    shouldAvoid: boolean;
  }

  // Load initial data
  useEffect(() => {
    if (user?.keycloak_id) {
      loadData();
    }
  }, [
    user?.keycloak_id,
    loadAllExercises,
    loadAllMuscles,
    loadAllEquipment,
    loadUserEquipment,
    loadUserWeakMuscles,
    loadUserExercisePreferences,
  ]);

  const loadData = async () => {
    setLoading(true);

    try {
      // Load all data in parallel using DataContext
      const [
        exercisesData,
        musclesData,
        equipmentData,
        userEquipmentData,
        userWeakMusclesData,
        userExercisePreferencesData,
      ] = await Promise.all([
        loadAllExercises(),
        loadAllMuscles(),
        loadAllEquipment(),
        loadUserEquipment(),
        loadUserWeakMuscles(),
        loadUserExercisePreferences(),
      ]);

      setExercises(exercisesData);
      setMuscles(musclesData);
      setEquipment(equipmentData);
      setUserEquipment(userEquipmentData);
      setUserWeakMuscles(userWeakMusclesData);
      setUserExercisePreferences(userExercisePreferencesData);
    } catch {
      enqueueSnackbar('Failed to load preferences data. Please try again.', { variant: 'error' });
    } finally {
      setLoading(false);
    }
  };

  // Tab change handler
  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    // Set slide direction based on tab navigation
    if (newValue > activeTab) {
      setSlideDirection('left'); // Moving forward to next tab
    } else {
      setSlideDirection('right'); // Moving backward to previous tab
    }
    
    setActiveTab(newValue);
  };

  const handleAddWeightUnitPreference = async (data: WeightUnitPreferenceFormData) => {
    if (!user?.keycloak_id) return;

    try {
      setSaving(true);

      await upsertUserWeightUnitPreference(user.keycloak_id, data.exerciseName, data.preferredUnit);

      // Refresh weight unit preferences from DataContext
      await refreshData();

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

      // Refresh weight unit preferences from DataContext
      await refreshData();

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

      // Refresh user equipment using DataContext
      const userEquipmentResponse = await loadUserEquipment();
      setUserEquipment(userEquipmentResponse);
      // Refresh DataContext to keep it in sync
      await refreshData();

      enqueueSnackbar('Equipment added successfully', { variant: 'success' });
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(axiosError.response?.data?.message || 'Failed to add equipment', {
        variant: 'error',
      });
    } finally {
      setSaving(false);
    }
  };

  const handleRemoveEquipment = async (equipmentName: string) => {
    if (!user?.keycloak_id) return;

    try {
      setSaving(true);
      await removeUserEquipment(user.keycloak_id, equipmentName);

      // Refresh user equipment using DataContext
      const userEquipmentResponse = await loadUserEquipment();
      setUserEquipment(userEquipmentResponse);
      // Refresh DataContext to keep it in sync
      await refreshData();

      enqueueSnackbar('Equipment removed successfully', { variant: 'success' });
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(axiosError.response?.data?.message || 'Failed to remove equipment', {
        variant: 'error',
      });
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

      // Refresh user weak muscles using DataContext
      const userWeakMusclesResponse = await loadUserWeakMuscles();
      setUserWeakMuscles(userWeakMusclesResponse);
      // Refresh DataContext to keep it in sync
      await refreshData();

      enqueueSnackbar('Weak muscle group added successfully', { variant: 'success' });
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(axiosError.response?.data?.message || 'Failed to add weak muscle group', {
        variant: 'error',
      });
    } finally {
      setSaving(false);
    }
  };

  const handleRemoveWeakMuscle = async (muscleName: string) => {
    if (!user?.keycloak_id) return;

    try {
      setSaving(true);
      await removeUserWeakMuscle(user.keycloak_id, muscleName);

      // Refresh user weak muscles using DataContext
      const userWeakMusclesResponse = await loadUserWeakMuscles();
      setUserWeakMuscles(userWeakMusclesResponse);
      // Refresh DataContext to keep it in sync
      await refreshData();

      enqueueSnackbar('Weak muscle group removed successfully', { variant: 'success' });
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(axiosError.response?.data?.message || 'Failed to remove weak muscle group', {
        variant: 'error',
      });
    } finally {
      setSaving(false);
    }
  };

  // Exercise preference handlers
  const handleAddExercisePreference = async (exerciseName: string, shouldAvoid: boolean) => {
    if (!user?.keycloak_id) return;

    try {
      setSaving(true);
      await upsertUserExercisePreference(user.keycloak_id, exerciseName, shouldAvoid);

      // Refresh exercise preferences using DataContext
      const userExercisePreferencesResponse = await loadUserExercisePreferences();
      setUserExercisePreferences(userExercisePreferencesResponse);
      // Refresh DataContext to keep it in sync
      await refreshData();

      enqueueSnackbar('Exercise preference added successfully', { variant: 'success' });
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(axiosError.response?.data?.message || 'Failed to add exercise preference', {
        variant: 'error',
      });
    } finally {
      setSaving(false);
    }
  };

  const handleRemoveExercisePreference = async (exerciseName: string) => {
    if (!user?.keycloak_id) return;

    try {
      setSaving(true);
      await removeUserExercisePreference(user.keycloak_id, exerciseName);

      // Refresh exercise preferences using DataContext
      const userExercisePreferencesResponse = await loadUserExercisePreferences();
      setUserExercisePreferences(userExercisePreferencesResponse);
      // Refresh DataContext to keep it in sync
      await refreshData();

      enqueueSnackbar('Exercise preference removed successfully', { variant: 'success' });
    } catch (err: unknown) {
      const axiosError = err as AxiosError<{ message?: string }>;
      enqueueSnackbar(
        axiosError.response?.data?.message || 'Failed to remove exercise preference',
        {
          variant: 'error',
        }
      );
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <LoadingSpinner message="Loading workout preferences..." fullHeight={false} />;
  }

  return (
    <Box sx={{ height: '100vh', overflow: 'hidden' }}>
      {/* Horizontal Tabs */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.8, ease: 'easeOut' }}
        style={{ 
          borderBottom: '1px solid',
          borderColor: 'var(--mui-palette-divider)',
        }}
      >
        <Tabs
          value={activeTab}
          onChange={handleTabChange}
          aria-label="workout preferences tabs"
          variant="standard"
          scrollButtons={false}
          className={GAME_CLASSES.tabs}
          sx={{
            '& .MuiTabs-flexContainer': {
              flexWrap: 'nowrap',
            },
            '& .MuiTab-root': {
              minWidth: 'auto',
              flexShrink: 0,
              '&:hover': {
                transform: 'translateY(-2px)',
                backgroundColor: 'rgba(0, 188, 212, 0.1)',
                boxShadow: '0 4px 15px rgba(0, 188, 212, 0.2)',
              },
              '&.Mui-selected': {
                color: '#00bcd4',
                textShadow: '0 0 8px rgba(0, 188, 212, 0.5)',
              }
            },
            '& .MuiTabs-indicator': {
              backgroundColor: '#00bcd4',
              boxShadow: '0 0 10px rgba(0, 188, 212, 0.5)',
            }
          }}
        >
          <Tab 
            label="Weight Unit Preferences" 
            id="preferences-tab-0"
            aria-controls="preferences-tabpanel-0"
          />
          <Tab 
            label="Available Equipment" 
            id="preferences-tab-1"
            aria-controls="preferences-tabpanel-1"
          />
          <Tab 
            label="Weak Muscle Groups" 
            id="preferences-tab-2"
            aria-controls="preferences-tabpanel-2"
          />
          <Tab 
            label="Exercise Preferences" 
            id="preferences-tab-3"
            aria-controls="preferences-tabpanel-3"
          />
        </Tabs>
      </motion.div>

      {/* Tab Content */}
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, ease: 'easeOut', delay: 0.6 }}
        style={{ flex: 1, overflow: 'auto', height: 'calc(100vh - 48px)' }}
      >
        <TabPanel value={activeTab} index={0} slideDirection={slideDirection}>
          <PreferenceSection
            title="Weight Unit Preferences"
            description="Set your preferred weight units for specific exercises."
            addButtonText="Add Preference"
            onAddClick={() => setUnitDialogOpen(true)}
            hasItems={weightUnitPreferences.length > 0}
            emptyMessage="No weight unit preferences set yet."
          >
            <List dense>
              {weightUnitPreferences.map(pref => (
                <DeletableListItem
                  key={`${pref.user_id}-${pref.exercise_name}`}
                  primary={getExerciseName(pref.exercise_name)}
                  secondary={`Preferred unit: ${pref.preferred_unit}`}
                  onDelete={() => handleDeleteWeightUnitPreference(pref.exercise_name)}
                  deleteTooltip="Remove weight unit preference"
                  disabled={saving}
                />
              ))}
            </List>
          </PreferenceSection>
        </TabPanel>

        <TabPanel value={activeTab} index={1} slideDirection={slideDirection}>
          <PreferenceSection
            title="Available Equipment"
            description="Manage the equipment you have available for workouts. This affects which exercises are available in your exercise pool."
            addButtonText="Add Equipment"
            onAddClick={() => setEquipmentDialogOpen(true)}
            hasItems={userEquipment.length > 0}
            emptyMessage="No equipment added yet. Add equipment to expand your exercise pool."
          >
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {userEquipment.map(equipment => (
                <DeletableChip
                  key={equipment.equipment_name}
                  label={equipment.equipment_name}
                  onDelete={() => handleRemoveEquipment(equipment.equipment_name)}
                  deleteTooltip="Remove equipment"
                  disabled={saving}
                  color="primary"
                  variant="outlined"
                />
              ))}
            </Box>
          </PreferenceSection>
        </TabPanel>

        <TabPanel value={activeTab} index={2} slideDirection={slideDirection}>
          <PreferenceSection
            title="Weak Muscle Groups"
            description="Identify muscle groups you want to target for improvement. This helps prioritize exercises that work these areas."
            addButtonText="Add Weak Muscle"
            onAddClick={() => setWeakMuscleDialogOpen(true)}
            hasItems={userWeakMuscles.length > 0}
            emptyMessage="No weak muscle groups identified yet. Add muscle groups you want to focus on."
          >
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1 }}>
              {userWeakMuscles.map(weakMuscle => (
                <DeletableChip
                  key={weakMuscle.muscle_name}
                  label={weakMuscle.muscle_name}
                  onDelete={() => handleRemoveWeakMuscle(weakMuscle.muscle_name)}
                  deleteTooltip="Remove weak muscle group"
                  disabled={saving}
                  color="warning"
                  variant="outlined"
                />
              ))}
            </Box>
          </PreferenceSection>
        </TabPanel>

        <TabPanel value={activeTab} index={3} slideDirection={slideDirection}>
          <PreferenceSection
            title="Exercise Preferences"
            description="Set your preferences for specific exercises. You can prefer exercises you enjoy or ignore exercises you want to avoid."
            addButtonText="Add Preference"
            onAddClick={() => setExercisePreferenceDialogOpen(true)}
            hasItems={userExercisePreferences.length > 0}
            emptyMessage="No exercise preferences set yet. Add exercises you prefer or want to avoid."
          >
            <List dense>
              {userExercisePreferences.map(pref => (
                <DeletableListItem
                  key={`${pref.user_id}-${pref.exercise_name}`}
                  primary={pref.exercise_name}
                  secondary={pref.should_avoid ? 'Ignored' : 'Preferred'}
                  onDelete={() => handleRemoveExercisePreference(pref.exercise_name)}
                  deleteTooltip="Remove exercise preference"
                  disabled={saving}
                />
              ))}
            </List>
          </PreferenceSection>
        </TabPanel>
      </motion.div>

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
        onSubmit={data => {
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
        onSubmit={data => {
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

      {/* Add Exercise Preference Dialog */}
      <FormDialog<ExercisePreferenceFormData>
        open={exercisePreferenceDialogOpen}
        onClose={() => setExercisePreferenceDialogOpen(false)}
        onSubmit={data => {
          handleAddExercisePreference(data.exerciseName, data.shouldAvoid);
          setExercisePreferenceDialogOpen(false);
        }}
        title="Add Exercise Preference"
        submitText="Add Preference"
        loading={saving}
        useTanStackForm={true}
        defaultValues={{
          exerciseName: '',
          shouldAvoid: false,
        }}
        validate={values => {
          const errors: Record<string, string> = {};
          if (!values.exerciseName) {
            errors.exerciseName = 'Please select an exercise';
          }
          if (values.shouldAvoid === undefined || values.shouldAvoid === null) {
            errors.shouldAvoid = 'Please select a preference';
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
              label="Preference"
              name="shouldAvoid"
              form={form}
              options={[
                { value: false, label: 'Prefer this exercise' },
                { value: true, label: 'Ignore this exercise' },
              ]}
            />
          </Box>
        )}
      </FormDialog>
    </Box>
  );
}
