import React, { useState } from 'react';
import {
  CardContent,
  CardHeader,
  Box,
  Button,
  TextField,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  LinearProgress,
  Tooltip,
  IconButton,
} from '@mui/material';
import {
  CheckCircle as CheckCircleIcon,
  RadioButtonUnchecked as RadioButtonUncheckedIcon,
  SkipNext as SkipNextIcon,
  Edit as EditIcon,
  CalendarToday as CalendarTodayIcon,
  FitnessCenter as FitnessCenterIcon,
  Favorite as FavoriteIcon,
  Psychology as PsychologyIcon,
} from '@mui/icons-material';
import { useMutation } from '@tanstack/react-query';
import type { UserWeeklyTest } from '../api/types';
import { GameCard, GameSubCard, GameStatusChip, GameText, GameTextSecondary } from './GameTheme';
import { useData } from '../contexts/DataContext';

interface WeeklyTestTrackerProps {
  weeklyTests: UserWeeklyTest[];
  onTestUpdate?: () => void;
}

const testSchedule = [
  {
    day: 'Monday',
    test: 'Vertical Jump',
    icon: <FitnessCenterIcon />,
    description: 'Measure explosive power using MyJump2 app',
    unit: 'cm',
    field: 'vertical_jump_result' as keyof UserWeeklyTest,
    statusField: 'vertical_jump_status' as keyof UserWeeklyTest,
  },
  {
    day: 'Any Day',
    test: 'HR Recovery',
    icon: <FavoriteIcon />,
    description: '1-minute heart rate drop after exercise',
    unit: 'bpm drop',
    field: 'hr_recovery_result' as keyof UserWeeklyTest,
    statusField: 'hr_recovery_status' as keyof UserWeeklyTest,
  },
  {
    day: 'Any Day',
    test: 'Reflex Speed',
    icon: <PsychologyIcon />,
    description: 'Reaction time using Human Benchmark',
    unit: 'ms',
    field: 'reflex_result' as keyof UserWeeklyTest,
    statusField: 'reflex_status' as keyof UserWeeklyTest,
  },
];

export const WeeklyTestTracker: React.FC<WeeklyTestTrackerProps> = ({ weeklyTests, onTestUpdate }) => {
  const { submitWeeklyTest, getCurrentWeekTest } = useData();
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [editingTest, setEditingTest] = useState<any>(null);
  const [testValue, setTestValue] = useState<string>('');
  const [testStatus, setTestStatus] = useState<'PENDING' | 'COMPLETED' | 'SKIPPED'>('PENDING');

  // Use DataContext function to get current week test, fallback to prop
  const weeklyTest = getCurrentWeekTest() || weeklyTests?.[0];

  const submitTestMutation = useMutation({
    mutationFn: (weeklyTest: Omit<UserWeeklyTest, 'keycloak_id' | 'created_at' | 'updated_at'>) => 
      submitWeeklyTest(weeklyTest),
    onSuccess: () => {
      // DataContext automatically refreshes performance data after submission
      onTestUpdate?.();
      setEditDialogOpen(false);
    },
    onError: (error: any) => {
      console.error('Failed to submit weekly test:', error);
    },
  });

  const handleEditTest = (test: any) => {
    setEditingTest(test);
    const resultValue = weeklyTest?.[test.field as keyof UserWeeklyTest] as number | undefined;
    setTestValue(resultValue?.toString() || '');
    const statusValue = weeklyTest?.[test.statusField as keyof UserWeeklyTest] as 'PENDING' | 'COMPLETED' | 'SKIPPED' | undefined;
    setTestStatus(statusValue || 'PENDING');
    setEditDialogOpen(true);
  };

  const handleSubmitTest = () => {
    if (!weeklyTest || !editingTest) return;

    const updatedTest: UserWeeklyTest = {
      ...weeklyTest,
      [editingTest.statusField]: testStatus,
      [editingTest.field]: testStatus === 'COMPLETED' && testValue ? 
        (editingTest.field.includes('result') ? Number(testValue) : testValue) : 
        undefined,
    };

    submitTestMutation.mutate(updatedTest);
  };

  const getCompletionPercentage = () => {
    if (!weeklyTest) return 0;
    
    const totalTests = testSchedule.length;
    const completedTests = testSchedule.filter(test => 
      weeklyTest[test.statusField] === 'COMPLETED'
    ).length;
    
    return Math.round((completedTests / totalTests) * 100);
  };

  const getStatusIcon = (status: 'PENDING' | 'COMPLETED' | 'SKIPPED') => {
    switch (status) {
      case 'COMPLETED':
        return <CheckCircleIcon sx={{ color: '#4CAF50' }} />;
      case 'SKIPPED':
        return <SkipNextIcon sx={{ color: '#FF9800' }} />;
      default:
        return <RadioButtonUncheckedIcon sx={{ color: 'rgba(255, 255, 255, 0.5)' }} />;
    }
  };

  const completionPercentage = getCompletionPercentage();

  return (
    <>
      <GameCard>
        <CardHeader
          avatar={<CalendarTodayIcon sx={{ color: 'white', fontSize: '2rem' }} />}
          title={
            <GameText variant="h6" sx={{ fontWeight: 'bold' }}>
              📅 Weekly Test Protocol
            </GameText>
          }
          subheader={
            <Box sx={{ mt: 1 }}>
              <GameTextSecondary variant="body2" sx={{ mb: 1 }}>
                Week of {weeklyTest?.week_start_timestamp ? new Date(weeklyTest.week_start_timestamp).toLocaleDateString() : 'N/A'}
              </GameTextSecondary>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <LinearProgress
                  variant="determinate"
                  value={completionPercentage}
                  sx={{
                    flexGrow: 1,
                    height: 8,
                    borderRadius: 4,
                    backgroundColor: 'rgba(255, 255, 255, 0.2)',
                    '& .MuiLinearProgress-bar': {
                      backgroundColor: completionPercentage === 100 ? '#4CAF50' : '#2196F3',
                      borderRadius: 4,
                    },
                  }}
                />
                <GameText variant="body2" sx={{ fontWeight: 'bold', minWidth: '40px' }}>
                  {completionPercentage}%
                </GameText>
              </Box>
            </Box>
          }
        />
        
        <CardContent>
          <Box sx={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: 2 }}>
            {testSchedule.map((test, index) => {
              const status = weeklyTest?.[test.statusField as keyof UserWeeklyTest] as 'PENDING' | 'COMPLETED' | 'SKIPPED' || 'PENDING';
              const result = weeklyTest?.[test.field as keyof UserWeeklyTest] as number | undefined;
              
              return (
                <Box key={index}>
                  <GameSubCard>
                    <CardContent sx={{ p: 2 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 1 }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          {test.icon}
                          <GameText variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                            {test.day}
                          </GameText>
                        </Box>
                        <GameStatusChip
                          label={status}
                          status={status}
                          size="small"
                        />
                      </Box>
                      
                      <GameText variant="h6" sx={{ mb: 1, fontWeight: 'bold' }}>
                        {test.test}
                      </GameText>
                      
                      <GameTextSecondary variant="body2" sx={{ mb: 2 }}>
                        {test.description}
                      </GameTextSecondary>
                      
                      {status === 'COMPLETED' && result && (
                        <Box sx={{ mb: 2, p: 1, backgroundColor: 'rgba(76, 175, 80, 0.2)', borderRadius: 1 }}>
                          <GameText variant="body2" sx={{ fontWeight: 'bold' }}>
                            Result: {result} {test.unit}
                          </GameText>
                        </Box>
                      )}
                      
                      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          {getStatusIcon(status)}
                          <GameTextSecondary variant="caption">
                            {status === 'COMPLETED' ? 'Completed' : 
                             status === 'SKIPPED' ? 'Skipped' : 'Pending'}
                          </GameTextSecondary>
                        </Box>
                        
                        <Tooltip title="Edit test result">
                          <IconButton
                            size="small"
                            onClick={() => handleEditTest(test)}
                            sx={{ color: 'white' }}
                          >
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </CardContent>
                  </GameSubCard>
                </Box>
              );
            })}
          </Box>
        </CardContent>
      </GameCard>

      {/* Edit Test Dialog */}
      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          Edit {editingTest?.test} Result
        </DialogTitle>
        <DialogContent>
          <Box sx={{ mt: 2 }}>
            <FormControl fullWidth sx={{ mb: 2 }}>
              <InputLabel>Status</InputLabel>
              <Select
                value={testStatus}
                onChange={(e) => setTestStatus(e.target.value as any)}
                label="Status"
              >
                <MenuItem value="PENDING">Pending</MenuItem>
                <MenuItem value="COMPLETED">Completed</MenuItem>
                <MenuItem value="SKIPPED">Skipped</MenuItem>
              </Select>
            </FormControl>
            
            {testStatus === 'COMPLETED' && (
              <TextField
                fullWidth
                label={`Result (${editingTest?.unit})`}
                type="number"
                value={testValue}
                onChange={(e) => setTestValue(e.target.value)}
                placeholder={`Enter your ${editingTest?.test.toLowerCase()} result`}
                sx={{ mb: 2 }}
              />
            )}
            
            <Alert severity="info" sx={{ mb: 2 }}>
              {editingTest?.description}
            </Alert>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditDialogOpen(false)}>
            Cancel
          </Button>
          <Button
            onClick={handleSubmitTest}
            variant="contained"
            disabled={submitTestMutation.isPending}
          >
            {submitTestMutation.isPending ? 'Saving...' : 'Save'}
          </Button>
        </DialogActions>
      </Dialog>
    </>
  );
};
