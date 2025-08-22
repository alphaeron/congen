import { default as AddIcon } from '@mui/icons-material/Add';
import { default as EditIcon } from '@mui/icons-material/Edit';
import { default as DeleteIcon } from '@mui/icons-material/Delete';
import { default as PlayArrowIcon } from '@mui/icons-material/PlayArrow';
import { default as PauseIcon } from '@mui/icons-material/Pause';
import {
  Box,
  Button,
  Card,
  CardContent,
  CardActions,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Typography,
  Grid,
  Alert,
  CircularProgress,
  Chip,
  IconButton,
  Tooltip,
  FormControlLabel,
  Switch,
} from '@mui/material';
import React, { useEffect, useState } from 'react';

import { getPrograms, createProgram, updateProgram, deleteProgram } from '../api/program';
import { getProgrammedWorkouts } from '../api/programmedWorkout';
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
  const [programs, setPrograms] = useState<Program[]>([]);
  const [workouts, setWorkouts] = useState<ProgrammedWorkout[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [selectedProgram, setSelectedProgram] = useState<Program | null>(null);
  const [formData, setFormData] = useState({
    name: '',
    isActive: true,
  });

  useEffect(() => {
    loadPrograms();
  }, []);

  const loadPrograms = async () => {
    try {
      setIsLoading(true);
      setError(null);

      const [programsData, workoutsData] = await Promise.all([
        getPrograms(),
        getProgrammedWorkouts(),
      ]);

      setPrograms(programsData);
      setWorkouts(workoutsData);
    } catch (err) {
      console.error('Error loading programs:', err);
      setError('Failed to load programs. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateProgram = async () => {
    try {
      const newProgram = await createProgram(formData.name, formData.isActive);
      setPrograms(prev => [...prev, newProgram]);
      setCreateDialogOpen(false);
      setFormData({ name: '', isActive: true });
    } catch (err) {
      console.error('Error creating program:', err);
      setError('Failed to create program. Please try again.');
    }
  };

  const handleUpdateProgram = async () => {
    if (!selectedProgram) return;

    try {
      const updatedProgram = await updateProgram(
        selectedProgram.id,
        formData.name,
        selectedProgram.current_week_number,
        formData.isActive
      );
      setPrograms(prev => prev.map(p => p.id === selectedProgram.id ? updatedProgram : p));
      setEditDialogOpen(false);
      setSelectedProgram(null);
      setFormData({ name: '', isActive: true });
    } catch (err) {
      console.error('Error updating program:', err);
      setError('Failed to update program. Please try again.');
    }
  };

  const handleDeleteProgram = async () => {
    if (!selectedProgram) return;

    try {
      await deleteProgram(selectedProgram.id);
      setPrograms(prev => prev.filter(p => p.id !== selectedProgram.id));
      setDeleteDialogOpen(false);
      setSelectedProgram(null);
    } catch (err) {
      console.error('Error deleting program:', err);
      setError('Failed to delete program. Please try again.');
    }
  };

  const openEditDialog = (program: Program) => {
    setSelectedProgram(program);
    setFormData({
      name: program.name,
      isActive: program.is_active,
    });
    setEditDialogOpen(true);
  };

  const openDeleteDialog = (program: Program) => {
    setSelectedProgram(program);
    setDeleteDialogOpen(true);
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
        <Typography variant="h5">
          Program Management
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setCreateDialogOpen(true)}
        >
          Create Program
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Programs Grid */}
      <Grid container spacing={3}>
        {programs.map((program) => {
          const programWorkouts = getWorkoutsForProgram(program.id);
          return (
            <Grid item xs={12} md={6} lg={4} key={program.id}>
              <Card>
                <CardContent>
                  <Box display="flex" justifyContent="space-between" alignItems="flex-start" sx={{ mb: 2 }}>
                    <Typography variant="h6" component="h3">
                      {program.name}
                    </Typography>
                    <Box>
                      <Tooltip title="Edit Program">
                        <IconButton
                          size="small"
                          onClick={() => openEditDialog(program)}
                        >
                          <EditIcon />
                        </IconButton>
                      </Tooltip>
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
                        {programWorkouts.slice(-3).map((workout) => (
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
                <CardActions>
                  <Button
                    size="small"
                    startIcon={program.is_active ? <PauseIcon /> : <PlayArrowIcon />}
                    onClick={() => openEditDialog(program)}
                  >
                    {program.is_active ? 'Pause' : 'Activate'}
                  </Button>
                </CardActions>
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
      <Dialog open={createDialogOpen} onClose={() => setCreateDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create New Program</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Program Name"
            fullWidth
            variant="outlined"
            value={formData.name}
            onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
            sx={{ mb: 2 }}
          />
          <FormControlLabel
            control={
              <Switch
                checked={formData.isActive}
                onChange={(e) => setFormData(prev => ({ ...prev, isActive: e.target.checked }))}
              />
            }
            label="Set as active program"
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

      {/* Edit Program Dialog */}
      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Edit Program</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Program Name"
            fullWidth
            variant="outlined"
            value={formData.name}
            onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
            sx={{ mb: 2 }}
          />
          <FormControlLabel
            control={
              <Switch
                checked={formData.isActive}
                onChange={(e) => setFormData(prev => ({ ...prev, isActive: e.target.checked }))}
              />
            }
            label="Set as active program"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>Cancel</Button>
          <Button
            onClick={handleUpdateProgram}
            variant="contained"
            disabled={!formData.name.trim()}
          >
            Update Program
          </Button>
        </DialogActions>
      </Dialog>

      {/* Delete Program Dialog */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Delete Program</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete "{selectedProgram?.name}"? This action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleDeleteProgram} color="error" variant="contained">
            Delete Program
          </Button>
        </DialogActions>
      </Dialog>
    </React.Fragment>
  );
};
