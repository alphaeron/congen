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
  Stack,
} from '@mui/material';
import { CustomSvgIcon } from './CustomSvgIcon';
import PowerIcon from '../resources/power-icon.svg';
import EnduranceIcon from '../resources/endurance-icon.svg';
import RecoveryIcon from '../resources/recovery-icon.svg';
import SpeedIcon from '../resources/speed-icon.svg';
import MobilityIcon from '../resources/mobility-icon.svg';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import type { UserTestResult, TestProtocol } from '../api/types';
import { GameCard, GameSubCard, GameStatusChip, GameText, GameTextSecondary } from './GameTheme';
import { useData } from '../contexts/DataContext';
import { getTestProtocols } from '../api/performanceTracking';
import { formatDate } from '../common/utils';

interface WeeklyTestTrackerProps {
  weeklyTests: UserTestResult[];
  onTestUpdate?: () => void;
}

// Icon mapping for test protocols - using same icons as radar chart
const getIconForProtocol = (testName: string) => {
  switch (testName) {
    case 'vertical_jump': return <CustomSvgIcon src={PowerIcon} alt="Explosiveness" sx={{ fontSize: 20, color: '#00bcd4' }} />;
    case 'vo2_max': return <CustomSvgIcon src={EnduranceIcon} alt="Stamina" sx={{ fontSize: 20, color: '#00bcd4' }} />;
    case 'hr_recovery': return <CustomSvgIcon src={RecoveryIcon} alt="Recovery" sx={{ fontSize: 20, color: '#00bcd4' }} />;
    case 'reflex': return <CustomSvgIcon src={SpeedIcon} alt="Reflexes" sx={{ fontSize: 20, color: '#00bcd4' }} />;
    case 'mobility': return <CustomSvgIcon src={MobilityIcon} alt="Dexterity" sx={{ fontSize: 20, color: '#00bcd4' }} />;
    default: return <CustomSvgIcon src={PowerIcon} alt="Test" sx={{ fontSize: 20, color: '#00bcd4' }} />;
  }
};

export const WeeklyTestTracker: React.FC<WeeklyTestTrackerProps> = ({ weeklyTests, onTestUpdate }) => {
  const { submitWeeklyTest, getCurrentWeekTest, refreshPerformanceData } = useData();
  const { enqueueSnackbar } = useSnackbar();
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [editingTest, setEditingTest] = useState<TestProtocol | null>(null);
  const [testValue, setTestValue] = useState<string>('');
  const [testStatus, setTestStatus] = useState<'PENDING' | 'COMPLETED' | 'SKIPPED'>('PENDING');

  // Fetch test protocols from backend
  const { data: testProtocols = [], isLoading: protocolsLoading } = useQuery({
    queryKey: ['testProtocols'],
    queryFn: () => getTestProtocols(),
    staleTime: 5 * 60 * 1000, // 5 minutes
  });

  // Get current week test results
  const currentWeekTests = getCurrentWeekTest() || weeklyTests;

  const submitTestMutation = useMutation({
    mutationFn: (testResults: Omit<UserTestResult, 'id' | 'keycloak_id' | 'created_at' | 'updated_at'>[]) =>
      submitWeeklyTest(testResults),
    onSuccess: async () => {
      // Auto-refresh performance data to immediately reflect changes
      await refreshPerformanceData();
      onTestUpdate?.();
      setEditDialogOpen(false);
    },
    onError: () => {
      enqueueSnackbar('Failed to submit weekly test', { variant: 'error' });
    },
  });

  const handleEditTest = (protocol: TestProtocol) => {
    setEditingTest(protocol);
    // Find existing test result for this protocol
    const existingResult = currentWeekTests.find(test => test.test_name === protocol.test_name);
    setTestValue(existingResult?.result_value?.toString() || '');
    setTestStatus(existingResult?.status || 'PENDING');
    setEditDialogOpen(true);
  };

  const handleSubmitTest = () => {
    if (!editingTest) return;

    // Create or update the test result for this protocol
    const updatedTestResult: Omit<UserTestResult, 'id' | 'keycloak_id' | 'created_at' | 'updated_at'> = {
      week_start_timestamp: currentWeekTests[0]?.week_start_timestamp || new Date().toISOString(),
      test_name: editingTest.test_name,
      status: testStatus,
      result_value: testStatus === 'COMPLETED' && testValue ? Number(testValue) : undefined,
    };

    // Create the full array of test results, updating the one we're editing
    const updatedTestResults = testProtocols.map(protocol => {
      if (protocol.test_name === editingTest.test_name) {
        return updatedTestResult;
      }
      // Return existing result or create a new pending one
      const existingResult = currentWeekTests.find(test => test.test_name === protocol.test_name);
      return existingResult || {
        week_start_timestamp: updatedTestResult.week_start_timestamp,
        test_name: protocol.test_name,
        status: 'PENDING' as const,
        result_value: undefined,
      };
    });

    submitTestMutation.mutate(updatedTestResults);
  };

  return (
    <>
      <GameCard>
        <CardHeader
          sx={{ paddingBottom: 0 }}
          title={
            <GameText variant="h6" sx={{ fontWeight: 'bold' }}>
              Weekly Quests
            </GameText>
          }
          subheader={
            <Box sx={{ mt: 1 }}>
                <GameTextSecondary variant="body2">
                  Week of {currentWeekTests[0]?.week_start_timestamp ? 
                    formatDate(currentWeekTests[0].week_start_timestamp) : 
                    'N/A'
                  }
                </GameTextSecondary>
            </Box>
          }
        />
        
        <CardContent>
          {protocolsLoading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
              <LinearProgress sx={{ width: '100%' }} />
            </Box>
          ) : (
            <Stack spacing={0.5} sx={{ alignItems: 'center' }}>
              {testProtocols.map((protocol, index) => {
              const testResult = currentWeekTests.find(test => test.test_name === protocol.test_name);
              const status = testResult?.status || 'PENDING';
              const result = testResult?.result_value;
              
              return (
                <GameSubCard key={index} sx={{ width: '100%', maxWidth: '400px' }}>
                  <CardContent sx={{ p: 2 }}>
                    <Stack spacing={0.5}>
                      <Stack direction="row" alignItems="center" spacing={1} sx={{ flexWrap: 'wrap' }}>
                        {getIconForProtocol(protocol.test_name)}
                        <GameText variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                          {protocol.display_name}
                        </GameText>
                      </Stack>
                      
                      {status === 'COMPLETED' && result ? (
                        <Box sx={{ p: 1, backgroundColor: 'rgba(76, 175, 80, 0.2)', borderRadius: 1 }}>
                          <GameText variant="body2" sx={{ fontWeight: 'bold' }}>
                            Result: {result} {protocol.unit}
                          </GameText>
                        </Box>
                      ) : (
                        <GameTextSecondary variant="body2" sx={{ wordBreak: 'break-word' }}>
                          {protocol.description}
                        </GameTextSecondary>
                      )}
                      
                      {status !== 'COMPLETED' && (
                        <Stack direction="row" alignItems="center" justifyContent="space-between">
                          <Tooltip title="Click to record your results">
                            <Box
                              onClick={() => handleEditTest(protocol)}
                              sx={{ 
                                cursor: 'pointer',
                                display: 'flex',
                                alignItems: 'center',
                                gap: 1,
                                '&:hover': {
                                  opacity: 0.8
                                }
                              }}
                            >
                              <GameStatusChip
                                label={status}
                                status={status}
                                size="small"
                              />
                            </Box>
                          </Tooltip>
                        </Stack>
                      )}
                    </Stack>
                  </CardContent>
                </GameSubCard>
              );
              })}
            </Stack>
          )}
        </CardContent>
      </GameCard>

      {/* Edit Test Dialog */}
      <Dialog open={editDialogOpen} onClose={() => setEditDialogOpen(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          Edit {editingTest?.test_name} Result
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
                placeholder={`Enter your ${editingTest?.test_name.toLowerCase()} result`}
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
