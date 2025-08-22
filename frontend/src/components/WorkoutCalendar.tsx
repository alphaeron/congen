import { default as CalendarTodayIcon } from '@mui/icons-material/CalendarToday';
import { default as EventIcon } from '@mui/icons-material/Event';
import { default as CheckCircleIcon } from '@mui/icons-material/CheckCircle';
import { default as ScheduleIcon } from '@mui/icons-material/Schedule';
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Alert,
  CircularProgress,
  Chip,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Divider,
  Paper,
} from '@mui/material';
import React, { useEffect, useState } from 'react';

import { getPrograms } from '../api/program';
import { getProgrammedWorkouts } from '../api/programmedWorkout';
import type { User, Program, ProgrammedWorkout } from '../api/types';

interface WorkoutCalendarProps {
  user: User;
}

/**
 * Workout calendar component for scheduling and viewing workouts.
 *
 * Provides calendar-based workout scheduling and shows
 * upcoming and past workouts with program context.
 *
 * @param user The user data
 * @return Workout calendar component
 */
export const WorkoutCalendar: React.FC<WorkoutCalendarProps> = ({ user }) => {
  const [programs, setPrograms] = useState<Program[]>([]);
  const [workouts, setWorkouts] = useState<ProgrammedWorkout[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    loadCalendarData();
  }, []);

  const loadCalendarData = async () => {
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
      console.error('Error loading calendar data:', err);
      setError('Failed to load calendar data. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const getActiveProgram = () => {
    return programs.find(program => program.is_active);
  };

  const getWorkoutsForActiveProgram = () => {
    const activeProgram = getActiveProgram();
    if (!activeProgram) return [];
    return workouts.filter(workout => workout.program_id === activeProgram.id);
  };

  // Mock calendar data - in a real implementation, this would come from backend
  const getUpcomingWorkouts = () => {
    const programWorkouts = getWorkoutsForActiveProgram();
    const today = new Date();
    
    // Mock upcoming workouts for the next 7 days
    return Array.from({ length: 7 }, (_, i) => {
      const date = new Date(today);
      date.setDate(today.getDate() + i);
      
      const workout = programWorkouts[i % programWorkouts.length];
      return {
        date,
        workout,
        isCompleted: i === 0, // Mock: today's workout is completed
        isToday: i === 0,
      };
    });
  };

  const getPastWorkouts = () => {
    const programWorkouts = getWorkoutsForActiveProgram();
    const today = new Date();
    
    // Mock past workouts for the last 7 days
    return Array.from({ length: 7 }, (_, i) => {
      const date = new Date(today);
      date.setDate(today.getDate() - (i + 1));
      
      const workout = programWorkouts[i % programWorkouts.length];
      return {
        date,
        workout,
        isCompleted: Math.random() > 0.3, // Mock: 70% completion rate
      };
    });
  };

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
        <CircularProgress />
      </Box>
    );
  }

  const activeProgram = getActiveProgram();
  const upcomingWorkouts = getUpcomingWorkouts();
  const pastWorkouts = getPastWorkouts();

  return (
    <React.Fragment>
      <Typography variant="h5" gutterBottom>
        Workout Calendar
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {!activeProgram ? (
        <Card>
          <CardContent sx={{ textAlign: 'center', py: 4 }}>
            <Typography variant="h6" gutterBottom>
              No Active Program
            </Typography>
            <Typography variant="body1" color="text.secondary">
              Create or activate a program to view your workout calendar.
            </Typography>
          </CardContent>
        </Card>
      ) : (
        <Grid container spacing={3}>
          {/* Upcoming Workouts */}
          <Grid item xs={12} lg={6}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" gap={1} sx={{ mb: 3 }}>
                  <ScheduleIcon color="primary" />
                  <Typography variant="h6">
                    Upcoming Workouts
                  </Typography>
                </Box>

                {upcomingWorkouts.length > 0 ? (
                  <List>
                    {upcomingWorkouts.map((item, index) => (
                      <React.Fragment key={index}>
                        <ListItem>
                          <ListItemIcon>
                            <EventIcon color={item.isToday ? 'primary' : 'action'} />
                          </ListItemIcon>
                          <ListItemText
                            primary={
                              <Box display="flex" alignItems="center" gap={1}>
                                <Typography variant="body1">
                                  {item.workout ? item.workout.name : 'Rest Day'}
                                </Typography>
                                {item.isToday && (
                                  <Chip label="Today" color="primary" size="small" />
                                )}
                                {item.isCompleted && (
                                  <Chip 
                                    icon={<CheckCircleIcon />} 
                                    label="Completed" 
                                    color="success" 
                                    size="small" 
                                  />
                                )}
                              </Box>
                            }
                            secondary={
                              <Box>
                                <Typography variant="body2" color="text.secondary">
                                  {item.date.toLocaleDateString('en-US', { 
                                    weekday: 'long', 
                                    month: 'short', 
                                    day: 'numeric' 
                                  })}
                                </Typography>
                                {item.workout && (
                                  <Typography variant="caption" color="text.secondary">
                                    Day {item.workout.day_number} • {activeProgram.name}
                                  </Typography>
                                )}
                              </Box>
                            }
                          />
                        </ListItem>
                        {index < upcomingWorkouts.length - 1 && <Divider />}
                      </React.Fragment>
                    ))}
                  </List>
                ) : (
                  <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 2 }}>
                    No upcoming workouts scheduled.
                  </Typography>
                )}
              </CardContent>
            </Card>
          </Grid>

          {/* Past Workouts */}
          <Grid item xs={12} lg={6}>
            <Card>
              <CardContent>
                <Box display="flex" alignItems="center" gap={1} sx={{ mb: 3 }}>
                  <CalendarTodayIcon color="primary" />
                  <Typography variant="h6">
                    Past Workouts
                  </Typography>
                </Box>

                {pastWorkouts.length > 0 ? (
                  <List>
                    {pastWorkouts.map((item, index) => (
                      <React.Fragment key={index}>
                        <ListItem>
                          <ListItemIcon>
                            <EventIcon color={item.isCompleted ? 'success' : 'disabled'} />
                          </ListItemIcon>
                          <ListItemText
                            primary={
                              <Box display="flex" alignItems="center" gap={1}>
                                <Typography variant="body1">
                                  {item.workout ? item.workout.name : 'Rest Day'}
                                </Typography>
                                {item.isCompleted && (
                                  <Chip 
                                    icon={<CheckCircleIcon />} 
                                    label="Completed" 
                                    color="success" 
                                    size="small" 
                                  />
                                )}
                              </Box>
                            }
                            secondary={
                              <Box>
                                <Typography variant="body2" color="text.secondary">
                                  {item.date.toLocaleDateString('en-US', { 
                                    weekday: 'long', 
                                    month: 'short', 
                                    day: 'numeric' 
                                  })}
                                </Typography>
                                {item.workout && (
                                  <Typography variant="caption" color="text.secondary">
                                    Day {item.workout.day_number} • {activeProgram.name}
                                  </Typography>
                                )}
                              </Box>
                            }
                          />
                        </ListItem>
                        {index < pastWorkouts.length - 1 && <Divider />}
                      </React.Fragment>
                    ))}
                  </List>
                ) : (
                  <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', py: 2 }}>
                    No past workouts recorded.
                  </Typography>
                )}
              </CardContent>
            </Card>
          </Grid>

          {/* Calendar View */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Monthly Calendar View
                </Typography>
                
                <Paper sx={{ p: 3, textAlign: 'center' }}>
                  <Typography variant="body1" color="text.secondary">
                    Calendar view will be implemented with a proper calendar component.
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
                    This will show workout scheduling, completion status, and allow rescheduling.
                  </Typography>
                </Paper>
              </CardContent>
            </Card>
          </Grid>

          {/* Program Summary */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Program Summary
                </Typography>
                
                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6} md={3}>
                    <Box textAlign="center">
                      <Typography variant="h4" color="primary">
                        {activeProgram.current_week_number}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Current Week
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <Box textAlign="center">
                      <Typography variant="h4" color="primary">
                        {getWorkoutsForActiveProgram().length}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Total Workouts
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <Box textAlign="center">
                      <Typography variant="h4" color="success.main">
                        {pastWorkouts.filter(w => w.isCompleted).length}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Completed
                      </Typography>
                    </Box>
                  </Grid>
                  <Grid item xs={12} sm={6} md={3}>
                    <Box textAlign="center">
                      <Typography variant="h4" color="primary">
                        {activeProgram.name}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        Active Program
                      </Typography>
                    </Box>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}
    </React.Fragment>
  );
};
