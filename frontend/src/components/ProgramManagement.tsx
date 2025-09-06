import { default as AddIcon } from '@mui/icons-material/Add';
import { default as DeleteIcon } from '@mui/icons-material/Delete';
import { default as EditIcon } from '@mui/icons-material/Edit';
import { default as PauseIcon } from '@mui/icons-material/Pause';
import { default as PlayArrowIcon } from '@mui/icons-material/PlayArrow';
import { Box, Button, Typography, IconButton, Tooltip } from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState } from 'react';

import { ActionCard } from './ActionCard';
import { ConfirmationDialog } from './ConfirmationDialog';
import { EmptyState } from './EmptyState';
import { FormDialog } from './FormDialog';
import { FormField } from './FormField';
import { LoadingBackdrop } from './LoadingBackdrop';
import { LoadingSpinner } from './LoadingSpinner';
import { StatusChip } from './StatusChip';
import { getPrograms, createProgram, updateProgram, deleteProgram } from '../api/program';
import { getProgrammedWorkouts } from '../api/programmedWorkout';
import { getProgramPreferences, updateProgramPreferences } from '../api/programPreferences';
import type { User, Program, ProgrammedWorkout, ProgramPreferences } from '../api/types';
import { formatDate } from '../common/utils';

interface ProgramManagementProps {
  user: User;
}

/**
 * Program management component for creating and managing workout programs.
 *
 * Allows users to create new programs, view current programs,
 * manage workout schedules, and track program progress.
 *
 * @param user The user data
 * @return Program management component
 */
export const ProgramManagement: React.FC<ProgramManagementProps> = ({ user }) => {
  const { enqueueSnackbar } = useSnackbar();
  const [programs, setPrograms] = useState<Program[]>([]);
  const [programPreferences, setProgramPreferences] = useState<Map<number, ProgramPreferences>>(
    new Map()
  );
  const [workouts, setWorkouts] = useState<ProgrammedWorkout[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isCreating, setIsCreating] = useState(false);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [stopDialogOpen, setStopDialogOpen] = useState(false);
  const [resumeDialogOpen, setResumeDialogOpen] = useState(false);
  const [selectedProgram, setSelectedProgram] = useState<Program | null>(null);
  // Form data types for TanStack Form
  interface CreateProgramFormData {
    name: string;
    numDaysPerWeek: number;
  }

  interface EditSessionDurationFormData {
    sessionTimeLengthInMinutes: number;
  }

  useEffect(() => {
    loadPrograms();
  }, []);

  const loadPrograms = async () => {
    try {
      setIsLoading(true);

      const [programsData, workoutsData] = await Promise.all([
        getPrograms(),
        getProgrammedWorkouts(),
      ]);

      setPrograms(programsData);
      setWorkouts(workoutsData);

      // Load program preferences for each program
      const preferencesMap = new Map<number, ProgramPreferences>();
      for (const program of programsData) {
        try {
          const preferences = await getProgramPreferences(program.id);
          preferencesMap.set(program.id, preferences);
        } catch {
          // Use default preferences if loading fails
          preferencesMap.set(program.id, {
            program_id: program.id,
            program_days_per_week: 4,
            session_time_length_in_minutes: 60,
            created_at: program.created_at,
            updated_at: program.updated_at,
          });
        }
      }
      setProgramPreferences(preferencesMap);
    } catch {
      enqueueSnackbar('Failed to load programs. Please try again.', { variant: 'error' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateProgram = async (data: CreateProgramFormData) => {
    // Close dialog immediately and show loading state
    setCreateDialogOpen(false);
    setIsCreating(true);

    try {
      await createProgram(data.name, data.numDaysPerWeek, user.keycloak_id);
      // Reload programs to get the updated data with preferences
      loadPrograms();
    } catch {
      enqueueSnackbar('Failed to create program. Please try again.', { variant: 'error' });
    } finally {
      setIsCreating(false);
    }
  };

  const handleUpdateSessionDuration = async (data: EditSessionDurationFormData) => {
    if (!selectedProgram) return;

    try {
      await updateProgramPreferences(selectedProgram.id, data.sessionTimeLengthInMinutes);

      // Update the local program preferences state
      setProgramPreferences(prev => {
        const newMap = new Map(prev);
        const existingPreferences = prev.get(selectedProgram.id);
        if (existingPreferences) {
          const updatedPreferences = {
            ...existingPreferences,
            session_time_length_in_minutes: data.sessionTimeLengthInMinutes,
            updated_at: new Date(),
          };
          newMap.set(selectedProgram.id, updatedPreferences);
        }
        return newMap;
      });

      setEditDialogOpen(false);
      setSelectedProgram(null);
      enqueueSnackbar('Session duration updated successfully.', { variant: 'success' });
    } catch {
      enqueueSnackbar('Failed to update session duration. Please try again.', { variant: 'error' });
    }
  };

  const handleStopProgram = async () => {
    if (!selectedProgram) return;

    try {
      const updatedProgram = await updateProgram(
        selectedProgram.id,
        selectedProgram.name,
        selectedProgram.current_week_number,
        false // Set to inactive
      );
      setPrograms(prev => prev.map(p => (p.id === selectedProgram.id ? updatedProgram : p)));
      setStopDialogOpen(false);
      setSelectedProgram(null);
      enqueueSnackbar('Program stopped successfully.', { variant: 'success' });
    } catch {
      enqueueSnackbar('Failed to stop program. Please try again.', { variant: 'error' });
    }
  };

  const handleResumeProgram = async () => {
    if (!selectedProgram) return;

    try {
      await updateProgram(
        selectedProgram.id,
        selectedProgram.name,
        selectedProgram.current_week_number,
        true // Set to active
      );

      // Reload programs to get the updated data
      loadPrograms();

      setResumeDialogOpen(false);
      setSelectedProgram(null);
      enqueueSnackbar('Program resumed successfully.', { variant: 'success' });
    } catch {
      enqueueSnackbar('Failed to resume program. Please try again.', { variant: 'error' });
    }
  };

  const handleDeleteProgram = async () => {
    if (!selectedProgram) return;

    try {
      await deleteProgram(selectedProgram.id);
      setPrograms(prev => prev.filter(p => p.id !== selectedProgram.id));
      setProgramPreferences(prev => {
        const newMap = new Map(prev);
        newMap.delete(selectedProgram.id);
        return newMap;
      });
      setDeleteDialogOpen(false);
      setSelectedProgram(null);
    } catch {
      enqueueSnackbar('Failed to delete program. Please try again.', { variant: 'error' });
    }
  };

  const [editFormData, setEditFormData] = useState<EditSessionDurationFormData>({
    sessionTimeLengthInMinutes: 60,
  });

  const openEditDialog = async (program: Program) => {
    setSelectedProgram(program);

    try {
      // Load program preferences to get session time
      const preferences = await getProgramPreferences(program.id);
      setEditFormData({
        sessionTimeLengthInMinutes: preferences.session_time_length_in_minutes,
      });
    } catch {
      // Fallback to default values if preferences can't be loaded
      setEditFormData({
        sessionTimeLengthInMinutes: 60,
      });
    }

    setEditDialogOpen(true);
  };

  const openDeleteDialog = (program: Program) => {
    setSelectedProgram(program);
    setDeleteDialogOpen(true);
  };

  const openStopDialog = (program: Program) => {
    setSelectedProgram(program);
    setStopDialogOpen(true);
  };

  const openResumeDialog = (program: Program) => {
    setSelectedProgram(program);
    setResumeDialogOpen(true);
  };

  const getWorkoutsForProgram = (programId: number) => {
    return workouts.filter(workout => workout.program_id === programId);
  };

  if (isLoading) {
    return <LoadingSpinner message="Loading programs..." fullHeight={false} />;
  }

  return (
    <React.Fragment>
      <Box display="flex" justifyContent="space-between" alignItems="center" sx={{ mb: 3 }}>
        <Typography variant="h5">Program Management</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setCreateDialogOpen(true)}
        >
          Create Program
        </Button>
      </Box>

      {/* Programs Cards */}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        {programs.map(program => {
          const programWorkouts = getWorkoutsForProgram(program.id);
          const preferences = programPreferences.get(program.id);
          const sessionDuration = preferences?.session_time_length_in_minutes || 60;

          return (
            <ActionCard
              key={program.id}
              title={program.name}
              actions={
                <React.Fragment>
                  {program.is_active ? (
                    <React.Fragment>
                      <Tooltip title="Change Session Duration">
                        <IconButton size="small" onClick={() => openEditDialog(program)}>
                          <EditIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Stop Program">
                        <IconButton
                          size="small"
                          color="primary"
                          onClick={() => openStopDialog(program)}
                        >
                          <PauseIcon />
                        </IconButton>
                      </Tooltip>
                    </React.Fragment>
                  ) : (
                    <Tooltip title="Resume Program">
                      <IconButton
                        size="small"
                        color="primary"
                        onClick={() => openResumeDialog(program)}
                      >
                        <PlayArrowIcon />
                      </IconButton>
                    </Tooltip>
                  )}
                  <Tooltip title="Delete Program">
                    <IconButton
                      size="small"
                      color="error"
                      onClick={() => openDeleteDialog(program)}
                    >
                      <DeleteIcon />
                    </IconButton>
                  </Tooltip>
                </React.Fragment>
              }
            >
              <Box display="flex" gap={1} flexWrap="wrap" sx={{ mb: 2 }}>
                <StatusChip
                  label={program.is_active ? 'Active' : 'Inactive'}
                  status={program.is_active ? 'active' : 'inactive'}
                />
                <StatusChip
                  label={`Week ${Math.max(program.current_week_number, 1)}`}
                  status="info"
                />
                <StatusChip
                  label={`${programWorkouts.length} workouts`}
                  status="default"
                  variant="outlined"
                />
                <StatusChip
                  label={`Session Duration: ${sessionDuration} min`}
                  status="default"
                  variant="outlined"
                />
              </Box>

              <Typography variant="body2" color="text.secondary">
                Created: {formatDate(program.created_at)}
              </Typography>
            </ActionCard>
          );
        })}
      </Box>

      {/* No Programs State */}
      {programs.length === 0 && (
        <EmptyState
          title="No Programs Yet"
          message="Create your first program to get started with structured workouts."
        />
      )}

      {/* Create Program Dialog */}
      <FormDialog<CreateProgramFormData>
        open={createDialogOpen}
        onClose={() => setCreateDialogOpen(false)}
        onSubmit={handleCreateProgram}
        title="Create New Program"
        description="Your new program will be created and set as your active program. If you have any other active programs, they will be marked as inactive."
        submitText="Create Program"
        useTanStackForm={true}
        defaultValues={{
          name: '',
          numDaysPerWeek: 4,
        }}
        validate={values => {
          const errors: Record<string, string> = {};
          if (!values.name?.trim()) {
            errors.name = 'Program name is required';
          }
          if (values.numDaysPerWeek < 2 || values.numDaysPerWeek > 4) {
            errors.numDaysPerWeek = 'Days per week must be between 2 and 4';
          }
          return Object.keys(errors).length > 0 ? errors : undefined;
        }}
      >
        {form => (
          <React.Fragment>
            <FormField
              type="text"
              label="Program Name"
              name="name"
              form={form}
              required
              sx={{ mb: 2 }}
            />
            <FormField
              type="number"
              label="Days per Week"
              name="numDaysPerWeek"
              form={form}
              inputProps={{ min: 2, max: 4 }}
              helperText="Number of training days per week (2, 3, or 4)"
            />
          </React.Fragment>
        )}
      </FormDialog>

      {/* Change Session Duration Dialog */}
      <FormDialog<EditSessionDurationFormData>
        open={editDialogOpen}
        onClose={() => setEditDialogOpen(false)}
        onSubmit={handleUpdateSessionDuration}
        title="Change Session Duration"
        description={`Program: ${selectedProgram?.name || ''}`}
        submitText="Update Session Duration"
        useTanStackForm={true}
        defaultValues={editFormData}
        validate={values => {
          const errors: Record<string, string> = {};
          if (
            !values.sessionTimeLengthInMinutes ||
            values.sessionTimeLengthInMinutes < 15 ||
            values.sessionTimeLengthInMinutes > 300
          ) {
            errors.sessionTimeLengthInMinutes =
              'Session duration must be between 15 and 300 minutes';
          }
          return Object.keys(errors).length > 0 ? errors : undefined;
        }}
      >
        {form => (
          <FormField
            type="number"
            label="Session Duration (minutes)"
            name="sessionTimeLengthInMinutes"
            form={form}
            inputProps={{ min: 15, max: 300 }}
            helperText="Session duration in minutes (15-300)"
          />
        )}
      </FormDialog>

      {/* Delete Program Dialog */}
      <ConfirmationDialog
        open={deleteDialogOpen}
        onClose={() => setDeleteDialogOpen(false)}
        onConfirm={handleDeleteProgram}
        title="Delete Program"
        message="Are you sure you want to delete this program? This action cannot be undone."
        confirmText="Delete Program"
        confirmColor="error"
      />

      {/* Stop Program Dialog */}
      <ConfirmationDialog
        open={stopDialogOpen}
        onClose={() => setStopDialogOpen(false)}
        onConfirm={handleStopProgram}
        title="Stop Program"
        message="Are you sure you want to stop this program? You can resume it later if needed."
        confirmText="Stop Program"
        confirmColor="warning"
      />

      {/* Resume Program Dialog */}
      <ConfirmationDialog
        open={resumeDialogOpen}
        onClose={() => setResumeDialogOpen(false)}
        onConfirm={handleResumeProgram}
        title="Resume Program"
        message="Are you sure you want to resume this program? Any other active programs will be marked as inactive."
        confirmText="Resume Program"
        confirmColor="primary"
      />

      {/* Full-screen loading overlay during program creation */}
      <LoadingBackdrop
        open={isCreating}
        message="Creating program..."
        subMessage="This may take a few moments"
      />
    </React.Fragment>
  );
};
