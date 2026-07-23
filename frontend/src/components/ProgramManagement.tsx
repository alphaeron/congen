import { default as DeleteIcon } from '@mui/icons-material/Delete';
import { default as EditIcon } from '@mui/icons-material/Edit';
import { default as PauseIcon } from '@mui/icons-material/Pause';
import { default as PlayArrowIcon } from '@mui/icons-material/PlayArrow';
import { Box, Button, IconButton, Tooltip } from '@mui/material';
import { motion } from 'framer-motion';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState } from 'react';

import { ActionCard } from './ActionCard';
import { HoverCard, HoverScale, ButtonPress, Magnetic } from './AnimatedWrapper';
import { ConfirmationDialog } from './ConfirmationDialog';
import { EmptyState } from './EmptyState';
import { FormDialog } from './FormDialog';
import { FormField } from './FormField';
import { GameText } from './GameTheme';
import { LoadingBackdrop } from './LoadingBackdrop';
import { LoadingSpinner } from './LoadingSpinner';
import { StatusChip } from './StatusChip';
import { createProgram, updateProgram, deleteProgram } from '../api/program';
import { updateProgramPreferences } from '../api/programPreferences';
import type { User, Program } from '../api/types';
import { formatDate } from '../common/utils';
import { useData } from '../contexts/DataContext';

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
  const {
    userData,
    refreshData,
    refreshSpecificData,
    isLoading,
    isReady,
    programPreferences = [],
    loadProgramPreferences,
  } = useData();
  const [isLoadingPrograms, setIsLoadingPrograms] = useState(false);
  const [isCreating, setIsCreating] = useState(false);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [stopDialogOpen, setStopDialogOpen] = useState(false);
  const [resumeDialogOpen, setResumeDialogOpen] = useState(false);
  const [selectedProgram, setSelectedProgram] = useState<Program | null>(null);
  // Form data types for TanStack Form
  interface CreateProgramFormData extends Record<string, unknown> {
    name: string;
    numDaysPerWeek: number;
  }

  interface EditSessionDurationFormData extends Record<string, unknown> {
    sessionTimeLengthInMinutes: number;
  }

  useEffect(() => {
    const loadPrograms = async () => {
      if (!userData?.training_programs?.length || programPreferences.length > 0) {
        return;
      }

      try {
        setIsLoadingPrograms(true);
        await loadProgramPreferences();
      } catch {
        enqueueSnackbar('Failed to load program preferences. Please try again.', {
          variant: 'error',
        });
      } finally {
        setIsLoadingPrograms(false);
      }
    };

    loadPrograms();
  }, [userData, programPreferences.length, loadProgramPreferences, enqueueSnackbar]);

  const handleCreateProgram = async (data: CreateProgramFormData) => {
    // Close dialog immediately and show loading state
    setCreateDialogOpen(false);
    setIsCreating(true);

    try {
      await createProgram(data.name, data.numDaysPerWeek, user.keycloak_id);
      await refreshData();
      await refreshSpecificData('programs');
      enqueueSnackbar('Program created successfully!', { variant: 'success' });
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

      await refreshSpecificData('programs');

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
      await updateProgram(
        selectedProgram.id,
        selectedProgram.name,
        selectedProgram.current_week_number,
        false // Set to inactive
      );
      await refreshData();
      await refreshSpecificData('programs');
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

      await refreshData();
      await refreshSpecificData('programs');

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
      await refreshData();
      await refreshSpecificData('programs');
      setDeleteDialogOpen(false);
      setSelectedProgram(null);
    } catch {
      enqueueSnackbar('Failed to delete program. Please try again.', { variant: 'error' });
    }
  };

  const [editFormData, setEditFormData] = useState<EditSessionDurationFormData>({
    sessionTimeLengthInMinutes: 60,
  });

  const openEditDialog = (program: Program) => {
    setSelectedProgram(program);

    const preferences = programPreferences.find(item => item.program.id === program.id);
    setEditFormData({
      sessionTimeLengthInMinutes:
        preferences?.program_preferences.session_time_length_in_minutes ?? 60,
    });

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
    if (!userData?.training_programs) return [];
    const programData = userData.training_programs.find(p => p.program.id === programId);
    return programData?.workouts || [];
  };

  if (!isReady || isLoading || isLoadingPrograms) {
    return <LoadingSpinner message="Loading programs..." fullHeight={false} />;
  }

  return (
    <React.Fragment>
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.8, ease: 'easeOut' }}
        style={{
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          marginBottom: '24px',
        }}
      >
        <motion.div
          initial={{ opacity: 0, x: -30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.6, ease: 'easeOut', delay: 0.2 }}
        >
          <GameText variant="h5" textVariant="glow">
            Program Management
          </GameText>
        </motion.div>
        <motion.div
          initial={{ opacity: 0, x: 30 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.6, ease: 'easeOut', delay: 0.2 }}
          whileHover={{ y: -2 }}
          whileTap={{ scale: 0.98 }}
        >
          <ButtonPress>
            <Button
              variant="contained"
              onClick={() => setCreateDialogOpen(true)}
              sx={{
                '&:hover': {
                  boxShadow: '0 8px 25px rgba(0, 188, 212, 0.4)',
                },
              }}
            >
              Create Program
            </Button>
          </ButtonPress>
        </motion.div>
      </motion.div>

      {/* Programs Cards */}
      <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
        {userData?.training_programs
          ?.sort(
            (a, b) =>
              new Date(b.program.created_at).getTime() - new Date(a.program.created_at).getTime()
          )
          ?.map((programData, index) => {
            const program = programData.program;
            const programWorkouts = getWorkoutsForProgram(program.id);
            const programWithPreferences = programPreferences.find(
              item => item.program.id === program.id
            );
            const sessionDuration =
              programWithPreferences?.program_preferences.session_time_length_in_minutes || 60;

            return (
              <HoverCard
                key={program.id}
                initial={{ opacity: 0, x: -30 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.6, ease: 'easeOut', delay: index * 0.1 }}
              >
                <ActionCard
                  title={program.name}
                  actions={
                    <React.Fragment>
                      {program.is_active ? (
                        <React.Fragment>
                          <Tooltip title="Change Session Duration">
                            <Magnetic>
                              <IconButton
                                size="small"
                                onClick={() => openEditDialog(program)}
                                sx={{
                                  '&:hover': {
                                    backgroundColor: 'rgba(0, 188, 212, 0.1)',
                                    boxShadow: '0 4px 15px rgba(0, 188, 212, 0.3)',
                                  },
                                }}
                              >
                                <EditIcon />
                              </IconButton>
                            </Magnetic>
                          </Tooltip>
                          <Tooltip title="Stop Program">
                            <Magnetic>
                              <IconButton
                                size="small"
                                color="primary"
                                onClick={() => openStopDialog(program)}
                                sx={{
                                  '&:hover': {
                                    backgroundColor: 'rgba(255, 152, 0, 0.1)',
                                    boxShadow: '0 4px 15px rgba(255, 152, 0, 0.3)',
                                  },
                                }}
                              >
                                <PauseIcon />
                              </IconButton>
                            </Magnetic>
                          </Tooltip>
                        </React.Fragment>
                      ) : (
                        <Tooltip title="Resume Program">
                          <Magnetic>
                            <IconButton
                              size="small"
                              color="primary"
                              onClick={() => openResumeDialog(program)}
                              sx={{
                                '&:hover': {
                                  backgroundColor: 'rgba(76, 175, 80, 0.1)',
                                  boxShadow: '0 4px 15px rgba(76, 175, 80, 0.3)',
                                },
                              }}
                            >
                              <PlayArrowIcon />
                            </IconButton>
                          </Magnetic>
                        </Tooltip>
                      )}
                      <Tooltip title="Delete Program">
                        <Magnetic>
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => openDeleteDialog(program)}
                            sx={{
                              '&:hover': {
                                backgroundColor: 'rgba(244, 67, 54, 0.1)',
                                boxShadow: '0 4px 15px rgba(244, 67, 54, 0.3)',
                              },
                            }}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Magnetic>
                      </Tooltip>
                    </React.Fragment>
                  }
                >
                  <Box
                    display="flex"
                    gap={1}
                    flexWrap="wrap"
                    sx={{
                      mb: 2,
                    }}
                  >
                    <HoverScale>
                      <StatusChip
                        label={program.is_active ? 'Active' : 'Inactive'}
                        status={program.is_active ? 'active' : 'inactive'}
                      />
                    </HoverScale>
                    <HoverScale>
                      <StatusChip
                        label={`Week ${Math.max(program.current_week_number, 1)}`}
                        status="info"
                      />
                    </HoverScale>
                    <HoverScale>
                      <StatusChip
                        label={`${programWorkouts.length} workouts`}
                        status="default"
                        variant="outlined"
                      />
                    </HoverScale>
                    <HoverScale>
                      <StatusChip
                        label={`Session Duration: ${sessionDuration} min`}
                        status="default"
                        variant="outlined"
                      />
                    </HoverScale>
                  </Box>

                  <GameText variant="body2" textVariant="secondary">
                    Created: {formatDate(program.created_at)}
                  </GameText>
                </ActionCard>
              </HoverCard>
            );
          })}
      </Box>

      {/* No Programs State */}
      {(!userData?.training_programs || userData.training_programs.length === 0) && (
        <HoverCard
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 1, ease: 'easeOut', delay: 0.5 }}
        >
          <EmptyState
            title="No Programs Yet"
            message="Create your first program to get started with structured workouts."
          />
        </HoverCard>
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
