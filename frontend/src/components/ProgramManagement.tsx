import { default as AddIcon } from '@mui/icons-material/Add';
import { default as DeleteIcon } from '@mui/icons-material/Delete';
import { default as EditIcon } from '@mui/icons-material/Edit';
import { default as PauseIcon } from '@mui/icons-material/Pause';
import { default as PlayArrowIcon } from '@mui/icons-material/PlayArrow';
import {
  Box,
  Button,
  Card,
  CardContent,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Typography,
  Grid,
  CircularProgress,
  Chip,
  IconButton,
  Tooltip,
} from '@mui/material';
import { useSnackbar } from 'notistack';
import React, { useEffect, useState } from 'react';

import { getPrograms, createProgram, updateProgram, deleteProgram } from '../api/program';
import { getProgrammedWorkouts } from '../api/programmedWorkout';
import { getProgramPreferences, updateProgramPreferences } from '../api/programPreferences';
import type { User, Program, ProgrammedWorkout } from '../api/types';

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
  const [workouts, setWorkouts] = useState<ProgrammedWorkout[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [stopDialogOpen, setStopDialogOpen] = useState(false);
  const [resumeDialogOpen, setResumeDialogOpen] = useState(false);
  const [selectedProgram, setSelectedProgram] = useState<Program | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    numDaysPerWeek: 4,
    isActive: true,
    sessionTimeLengthInMinutes: 60,
  });

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
    } catch {
      enqueueSnackbar('Failed to load programs. Please try again.', { variant: 'error' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateProgram = async () => {
    try {
      const newProgram = await createProgram(
        formData.name,
        formData.numDaysPerWeek,
        user.keycloak_id
      );
      setPrograms(prev => [...prev, newProgram]);
      setCreateDialogOpen(false);
      setFormData({ name: '', numDaysPerWeek: 4, isActive: true, sessionTimeLengthInMinutes: 60 });
    } catch {
      enqueueSnackbar('Failed to create program. Please try again.', { variant: 'error' });
    }
  };

  const handleUpdateSessionDuration = async () => {
    if (!selectedProgram) return;

    try {
      await updateProgramPreferences(selectedProgram.id, formData.sessionTimeLengthInMinutes);
      setEditDialogOpen(false);
      setSelectedProgram(null);
      setFormData({ name: '', numDaysPerWeek: 4, isActive: true, sessionTimeLengthInMinutes: 60 });
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

      // Refresh the entire programs list since other programs may have been deactivated
      const refreshedPrograms = await getPrograms();
      setPrograms(refreshedPrograms);

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
      setDeleteDialogOpen(false);
      setSelectedProgram(null);
    } catch {
      enqueueSnackbar('Failed to delete program. Please try again.', { variant: 'error' });
    }
  };

  const openEditDialog = async (program: Program) => {
    setSelectedProgram(program);

    try {
      // Load program preferences to get session time
      const preferences = await getProgramPreferences(program.id);
      setFormData({
        name: program.name,
        numDaysPerWeek: 4, // Not editable
        isActive: program.is_active,
        sessionTimeLengthInMinutes: preferences.data.session_time_length_in_minutes,
      });
    } catch {
      // Fallback to default values if preferences can't be loaded
      setFormData({
        name: program.name,
        numDaysPerWeek: 4,
        isActive: program.is_active,
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
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
        <CircularProgress />
      </Box>
    );
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

      {/* Programs Grid */}
      <Grid container spacing={3}>
        {programs.map(program => {
          const programWorkouts = getWorkoutsForProgram(program.id);
          return (
            <Grid item xs={12} md={6} lg={4} key={program.id}>
              <Card>
                <CardContent>
                  <Box
                    display="flex"
                    justifyContent="space-between"
                    alignItems="flex-start"
                    sx={{ mb: 2 }}
                  >
                    <Typography variant="h6" component="h3">
                      {program.name}
                    </Typography>
                    <Box>
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
                    </Box>
                  </Box>

                  <Box display="flex" gap={1} sx={{ mb: 2 }}>
                    <Chip
                      label={program.is_active ? 'Active' : 'Inactive'}
                      color={program.is_active ? 'success' : 'default'}
                      size="small"
                    />
                    <Chip
                      label={`Week ${program.current_week_number}`}
                      color="primary"
                      size="small"
                    />
                    <Chip
                      label={`${programWorkouts.length} workouts`}
                      variant="outlined"
                      size="small"
                    />
                  </Box>

                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Created: {new Date(program.created_at).toLocaleDateString()}
                  </Typography>

                  {programWorkouts.length > 0 && (
                    <Box>
                      <Typography variant="subtitle2" gutterBottom>
                        Recent Workouts:
                      </Typography>
                      <Box display="flex" flexWrap="wrap" gap={0.5}>
                        {programWorkouts.slice(-3).map(workout => (
                          <Chip
                            key={workout.id}
                            label={`Day ${workout.day_number}: ${workout.name}`}
                            size="small"
                            variant="outlined"
                          />
                        ))}
                      </Box>
                    </Box>
                  )}
                </CardContent>
              </Card>
            </Grid>
          );
        })}
      </Grid>

      {/* No Programs State */}
      {programs.length === 0 && (
        <Card>
          <CardContent sx={{ textAlign: 'center', py: 4 }}>
            <Typography variant="h6" gutterBottom>
              No Programs Yet
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Create your first program to get started with structured workouts.
            </Typography>
          </CardContent>
        </Card>
      )}

      {/* Create Program Dialog */}
      <Dialog
        open={createDialogOpen}
        onClose={() => setCreateDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Create New Program</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Your new program will be created and set as your active program. If you have any other
            active programs, they will be marked as inactive.
          </Typography>
          <TextField
            autoFocus
            margin="dense"
            label="Program Name"
            fullWidth
            variant="outlined"
            value={formData.name}
            onChange={e => setFormData(prev => ({ ...prev, name: e.target.value }))}
            sx={{ mb: 2 }}
          />
          <TextField
            margin="dense"
            label="Days per Week"
            type="number"
            fullWidth
            variant="outlined"
            value={formData.numDaysPerWeek}
            onChange={e =>
              setFormData(prev => ({ ...prev, numDaysPerWeek: parseInt(e.target.value) || 4 }))
            }
            helperText="Number of training days per week (2, 3, or 4)"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setCreateDialogOpen(false)}>Cancel</Button>
          <Button
            onClick={handleCreateProgram}
            variant="contained"
            disabled={!formData.name.trim()}
          >
            Create Program
          </Button>
        </DialogActions>
      </Dialog>

      {/* Change Session Duration Dialog */}
      <Dialog
        open={editDialogOpen}
        onClose={() => setEditDialogOpen(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Change Session Duration</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Program: {formData.name}
          </Typography>
          <TextField
            autoFocus
            margin="dense"
            label="Session Duration (minutes)"
            type="number"
            fullWidth
            variant="outlined"
            value={formData.sessionTimeLengthInMinutes}
            onChange={e =>
              setFormData(prev => ({
                ...prev,
                sessionTimeLengthInMinutes: parseInt(e.target.value) || 60,
              }))
            }
            inputProps={{ min: 15, max: 300 }}
            helperText="Session duration in minutes (15-300)"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>Cancel</Button>
          <Button
            onClick={handleUpdateSessionDuration}
            variant="contained"
            disabled={
              !formData.sessionTimeLengthInMinutes ||
              formData.sessionTimeLengthInMinutes < 15 ||
              formData.sessionTimeLengthInMinutes > 300
            }
          >
            Update Session Duration
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Program Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Delete Program</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete this program? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleDeleteProgram} color="error" variant="contained">
            Delete Program
          </Button>
        </DialogActions>
      </Dialog>

      {/* Stop Program Dialog */}
      <Dialog open={stopDialogOpen} onClose={() => setStopDialogOpen(false)}>
        <DialogTitle>Stop Program</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to stop this program? You can resume it later if needed.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStopDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleStopProgram} color="warning" variant="contained">
            Stop Program
          </Button>
        </DialogActions>
      </Dialog>

      {/* Resume Program Dialog */}
      <Dialog open={resumeDialogOpen} onClose={() => setResumeDialogOpen(false)}>
        <DialogTitle>Resume Program</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to resume this program? Any other active programs will be marked
            as inactive.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setResumeDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleResumeProgram} color="primary" variant="contained">
            Resume Program
          </Button>
        </DialogActions>
      </Dialog>
    </React.Fragment>
  );
};
