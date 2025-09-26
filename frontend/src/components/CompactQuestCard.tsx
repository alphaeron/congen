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
  Grid,
  IconButton,
  Tooltip,
  Stack,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  LinearProgress,
} from '@mui/material';
import { Add as AddIcon, Edit as EditIcon, ExpandMore as ExpandMoreIcon } from '@mui/icons-material';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { GameCard, GameSubCard, GameStatusChip, GameText, GameTextSecondary } from './GameTheme';
import { CustomSvgIcon } from './CustomSvgIcon';
import { useData } from '../contexts/DataContext';
import { submitPerformanceMetrics, getTestProtocols, submitWeeklyTest } from '../api/performanceTracking';
import { formatDate } from '../common/utils';
import type { UserPerformanceMetrics, UserTestResult, TestProtocol } from '../api/types';

// Import icons for weekly tests
import PowerIcon from '../resources/power-icon.svg';
import EnduranceIcon from '../resources/endurance-icon.svg';
import RecoveryIcon from '../resources/recovery-icon.svg';
import SpeedIcon from '../resources/speed-icon.svg';
import MobilityIcon from '../resources/mobility-icon.svg';

interface CompactQuestCardProps {
  type: 'daily' | 'weekly';
  currentMetrics?: UserPerformanceMetrics;
  weeklyTests?: UserTestResult[];
  onTestUpdate?: () => void;
}

// Icon mapping for test protocols - using same icons as radar chart
const getIconForProtocol = (testName: string) => {
  switch (testName) {
    case 'vertical_jump': return <CustomSvgIcon src={PowerIcon} alt="Explosiveness" sx={{ fontSize: 16, color: '#00bcd4' }} />;
    case 'vo2_max': return <CustomSvgIcon src={EnduranceIcon} alt="Stamina" sx={{ fontSize: 16, color: '#00bcd4' }} />;
    case 'hr_recovery': return <CustomSvgIcon src={RecoveryIcon} alt="Recovery" sx={{ fontSize: 16, color: '#00bcd4' }} />;
    case 'reflex': return <CustomSvgIcon src={SpeedIcon} alt="Reflexes" sx={{ fontSize: 16, color: '#00bcd4' }} />;
    case 'mobility': return <CustomSvgIcon src={MobilityIcon} alt="Dexterity" sx={{ fontSize: 16, color: '#00bcd4' }} />;
    default: return <CustomSvgIcon src={PowerIcon} alt="Test" sx={{ fontSize: 16, color: '#00bcd4' }} />;
  }
};

// Daily metrics configuration
const dailyMetricsConfig = [
  { key: 'strain', label: 'Strain', icon: '•', unit: '', description: 'Daily strain score from wearables' },
  { key: 'recovery', label: 'Recovery', icon: '+', unit: '%', description: 'Daily recovery score percentage' },
  { key: 'hrv', label: 'HRV', icon: '♥', unit: ' ms', description: 'Heart rate variability measurement' },
  { key: 'sleep_score', label: 'Sleep Score', icon: '💤', unit: '%', description: 'Overall sleep quality score' },
];

export const CompactQuestCard: React.FC<CompactQuestCardProps> = ({ 
  type, 
  currentMetrics, 
  weeklyTests = [], 
  onTestUpdate 
}) => {
  const { refreshPerformanceData, submitWeeklyTest: submitWeeklyTestData, getCurrentWeekTest } = useData();
  const { enqueueSnackbar } = useSnackbar();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [expanded, setExpanded] = useState(false);
  
  // Daily metrics state
  const [formData, setFormData] = useState({
    strain: currentMetrics?.strain?.toString() || '',
    recovery: currentMetrics?.recovery?.toString() || '',
    hrv: currentMetrics?.hrv?.toString() || '',
    sleep_score: currentMetrics?.sleep_score?.toString() || '',
    rem_sleep_minutes: currentMetrics?.rem_sleep_minutes?.toString() || '',
    deep_sleep_minutes: currentMetrics?.deep_sleep_minutes?.toString() || '',
    subjective_tiredness: currentMetrics?.subjective_tiredness?.toString() || '',
  });

  // Weekly test state
  const [editingTest, setEditingTest] = useState<TestProtocol | null>(null);
  const [testValue, setTestValue] = useState<string>('');
  const [testStatus, setTestStatus] = useState<'PENDING' | 'COMPLETED' | 'SKIPPED'>('PENDING');

  // Fetch test protocols from backend
  const { data: testProtocols = [], isLoading: protocolsLoading } = useQuery({
    queryKey: ['testProtocols'],
    queryFn: () => getTestProtocols(),
    staleTime: 5 * 60 * 1000, // 5 minutes
    enabled: type === 'weekly',
  });

  // Get current week test results
  const currentWeekTests = type === 'weekly' ? (getCurrentWeekTest() || weeklyTests) : [];

  // Daily metrics mutation
  const submitMetricsMutation = useMutation({
    mutationFn: (metrics: Omit<UserPerformanceMetrics, 'keycloak_id' | 'created_at' | 'updated_at'>) =>
      submitPerformanceMetrics(metrics),
    onSuccess: async () => {
      await refreshPerformanceData();
      setDialogOpen(false);
      enqueueSnackbar('Daily metrics updated successfully!', { variant: 'success' });
    },
    onError: () => {
      enqueueSnackbar('Failed to update daily metrics', { variant: 'error' });
    },
  });

  // Weekly test mutation
  const submitTestMutation = useMutation({
    mutationFn: (testResults: Omit<UserTestResult, 'id' | 'keycloak_id' | 'created_at' | 'updated_at'>[]) =>
      submitWeeklyTestData(testResults),
    onSuccess: async () => {
      await refreshPerformanceData();
      onTestUpdate?.();
      setDialogOpen(false);
    },
    onError: () => {
      enqueueSnackbar('Failed to submit weekly test', { variant: 'error' });
    },
  });

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
  };

  const handleSubmitMetrics = () => {
    const metrics: Omit<UserPerformanceMetrics, 'keycloak_id' | 'created_at' | 'updated_at'> = {
      strain: formData.strain ? parseFloat(formData.strain) : undefined,
      recovery: formData.recovery ? parseFloat(formData.recovery) : undefined,
      hrv: formData.hrv ? parseFloat(formData.hrv) : undefined,
      sleep_score: formData.sleep_score ? parseFloat(formData.sleep_score) : undefined,
      rem_sleep_minutes: formData.rem_sleep_minutes ? parseFloat(formData.rem_sleep_minutes) : undefined,
      deep_sleep_minutes: formData.deep_sleep_minutes ? parseFloat(formData.deep_sleep_minutes) : undefined,
      subjective_tiredness: formData.subjective_tiredness ? parseInt(formData.subjective_tiredness) : undefined,
    };

    submitMetricsMutation.mutate(metrics);
  };

  const handleEditTest = (protocol: TestProtocol) => {
    setEditingTest(protocol);
    const existingResult = currentWeekTests.find(test => test.test_name === protocol.test_name);
    setTestValue(existingResult?.result_value?.toString() || '');
    setTestStatus(existingResult?.status || 'PENDING');
    setDialogOpen(true);
  };

  const handleSubmitTest = () => {
    if (!editingTest) return;

    const updatedTestResult: Omit<UserTestResult, 'id' | 'keycloak_id' | 'created_at' | 'updated_at'> = {
      week_start_timestamp: currentWeekTests[0]?.week_start_timestamp || new Date().toISOString(),
      test_name: editingTest.test_name,
      status: testStatus,
      result_value: testStatus === 'COMPLETED' && testValue ? Number(testValue) : undefined,
    };

    const updatedTestResults = testProtocols.map(protocol => {
      if (protocol.test_name === editingTest.test_name) {
        return updatedTestResult;
      }
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

  // Calculate completion stats
  const getCompletionStats = () => {
    if (type === 'daily') {
      const completed = dailyMetricsConfig.filter(metric => formData[metric.key as keyof typeof formData]).length;
      return { completed, total: dailyMetricsConfig.length };
    } else {
      const completed = currentWeekTests.filter(test => test.status === 'COMPLETED').length;
      return { completed, total: testProtocols.length };
    }
  };

  const { completed, total } = getCompletionStats();
  const hasAnyData = type === 'daily' 
    ? Object.values(formData).some(value => value !== '')
    : currentWeekTests.length > 0;

  const getTitle = () => type === 'daily' ? 'Daily Quests' : 'Weekly Quests';
  const getSubtitle = () => {
    if (type === 'daily') {
      return new Date().toLocaleDateString('en-US', { 
        weekday: 'long', 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
      });
    } else {
      return currentWeekTests[0]?.week_start_timestamp ? 
        `Week of ${formatDate(currentWeekTests[0].week_start_timestamp)}` : 
        'N/A';
    }
  };

  const renderCompactGrid = () => {
    if (type === 'daily') {
      return (
        <Grid container spacing={1}>
          {dailyMetricsConfig.map((metric, index) => {
            const value = formData[metric.key as keyof typeof formData];
            const hasValue = value && value !== '';
            
            return (
              <Grid item xs={6} key={index}>
                <GameSubCard 
                  sx={{ 
                    height: '80px', 
                    cursor: 'pointer',
                    '&:hover': { opacity: 0.8 }
                  }}
                  onClick={() => setDialogOpen(true)}
                >
                  <CardContent sx={{ p: 1, height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                    <Stack spacing={0.5} alignItems="center">
                      <Box sx={{ fontSize: 16, color: '#00bcd4', fontWeight: 'bold' }}>
                        {metric.icon}
                      </Box>
                      <GameText variant="caption" sx={{ fontWeight: 'bold', textAlign: 'center' }}>
                        {metric.label}
                      </GameText>
                      {hasValue ? (
                        <GameText variant="caption" sx={{ color: '#4caf50', fontWeight: 'bold' }}>
                          {value}{metric.unit}
                        </GameText>
                      ) : (
                        <GameTextSecondary variant="caption" sx={{ fontStyle: 'italic' }}>
                          Pending
                        </GameTextSecondary>
                      )}
                    </Stack>
                  </CardContent>
                </GameSubCard>
              </Grid>
            );
          })}
        </Grid>
      );
    } else {
      if (protocolsLoading) {
        return (
          <Box sx={{ display: 'flex', justifyContent: 'center', p: 2 }}>
            <LinearProgress sx={{ width: '100%' }} />
          </Box>
        );
      }

      return (
        <Grid container spacing={1}>
          {testProtocols.slice(0, 4).map((protocol, index) => {
            const testResult = currentWeekTests.find(test => test.test_name === protocol.test_name);
            const status = testResult?.status || 'PENDING';
            const result = testResult?.result_value;
            
            return (
              <Grid item xs={6} key={index}>
                <GameSubCard 
                  sx={{ 
                    height: '80px', 
                    cursor: 'pointer',
                    '&:hover': { opacity: 0.8 }
                  }}
                  onClick={() => handleEditTest(protocol)}
                >
                  <CardContent sx={{ p: 1, height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                    <Stack spacing={0.5} alignItems="center">
                      {getIconForProtocol(protocol.test_name)}
                      <GameText variant="caption" sx={{ fontWeight: 'bold', textAlign: 'center' }}>
                        {protocol.display_name}
                      </GameText>
                      {status === 'COMPLETED' && result ? (
                        <GameText variant="caption" sx={{ color: '#4caf50', fontWeight: 'bold' }}>
                          {result} {protocol.unit}
                        </GameText>
                      ) : (
                        <GameStatusChip
                          label={status}
                          status={status}
                          size="small"
                          sx={{ fontSize: '0.6rem', height: '16px' }}
                        />
                      )}
                    </Stack>
                  </CardContent>
                </GameSubCard>
              </Grid>
            );
          })}
        </Grid>
      );
    }
  };

  const renderExpandedView = () => {
    if (type === 'daily') {
      return (
        <Stack spacing={0.5} sx={{ alignItems: 'center' }}>
          {dailyMetricsConfig.map((metric, index) => {
            const value = formData[metric.key as keyof typeof formData];
            if (!value || value === '') return null;
            
            return (
              <GameSubCard key={index} sx={{ width: '100%', maxWidth: '400px' }}>
                <CardContent sx={{ p: 2 }}>
                  <Stack spacing={0.5}>
                    <Stack direction="row" alignItems="center" spacing={1}>
                      <Box sx={{ fontSize: 20, color: '#00bcd4', display: 'flex', alignItems: 'center', justifyContent: 'center', fontWeight: 'bold' }}>
                        {metric.icon}
                      </Box>
                      <GameText variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                        {metric.label}
                      </GameText>
                    </Stack>
                    <GameTextSecondary variant="body2" sx={{ wordBreak: 'break-word' }}>
                      {metric.description}
                    </GameTextSecondary>
                    <Box sx={{ p: 1, backgroundColor: 'rgba(76, 175, 80, 0.2)', borderRadius: 1 }}>
                      <GameText variant="body2" sx={{ fontWeight: 'bold' }}>
                        Result: {value}{metric.unit}
                      </GameText>
                    </Box>
                  </Stack>
                </CardContent>
              </GameSubCard>
            );
          })}
        </Stack>
      );
    } else {
      return (
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
      );
    }
  };

  return (
    <>
      <GameCard sx={{ width: '100%' }}>
        <CardHeader
          sx={{ paddingBottom: 0 }}
          title={
            <Stack direction="row" alignItems="center" justifyContent="space-between">
              <GameText variant="h6" sx={{ fontWeight: 'bold' }}>
                {getTitle()}
              </GameText>
              <Stack direction="row" alignItems="center" spacing={1}>
                <GameText variant="caption" sx={{ color: '#00bcd4' }}>
                  {completed}/{total}
                </GameText>
                <Tooltip title={hasAnyData ? "Edit metrics" : "Record metrics"}>
                  <IconButton
                    onClick={() => setDialogOpen(true)}
                    sx={{ color: '#00bcd4' }}
                    size="small"
                  >
                    {hasAnyData ? <EditIcon /> : <AddIcon />}
                  </IconButton>
                </Tooltip>
                {hasAnyData && (
                  <Tooltip title={expanded ? "Collapse" : "Expand"}>
                    <IconButton
                      onClick={() => setExpanded(!expanded)}
                      sx={{ color: '#00bcd4' }}
                      size="small"
                    >
                      <ExpandMoreIcon sx={{ transform: expanded ? 'rotate(180deg)' : 'rotate(0deg)' }} />
                    </IconButton>
                  </Tooltip>
                )}
              </Stack>
            </Stack>
          }
          subheader={
            <GameTextSecondary variant="body2">
              {getSubtitle()}
            </GameTextSecondary>
          }
        />
        <CardContent sx={{ pt: 1 }}>
          {expanded ? renderExpandedView() : renderCompactGrid()}
        </CardContent>
      </GameCard>

      {/* Daily Metrics Dialog */}
      {type === 'daily' && (
        <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
          <DialogTitle>
            <GameText variant="h6" sx={{ fontWeight: 'bold' }}>
              Daily Metrics
            </GameText>
          </DialogTitle>
          <DialogContent>
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Strain"
                  type="number"
                  value={formData.strain}
                  onChange={(e) => handleInputChange('strain', e.target.value)}
                  fullWidth
                  inputProps={{ min: 0, max: 21, step: 0.1 }}
                  helperText="0-21 (from wearables)"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Recovery"
                  type="number"
                  value={formData.recovery}
                  onChange={(e) => handleInputChange('recovery', e.target.value)}
                  fullWidth
                  inputProps={{ min: 0, max: 100, step: 1 }}
                  helperText="0-100%"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="HRV"
                  type="number"
                  value={formData.hrv}
                  onChange={(e) => handleInputChange('hrv', e.target.value)}
                  fullWidth
                  inputProps={{ min: 0, step: 0.1 }}
                  helperText="Heart rate variability (ms)"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Sleep Score"
                  type="number"
                  value={formData.sleep_score}
                  onChange={(e) => handleInputChange('sleep_score', e.target.value)}
                  fullWidth
                  inputProps={{ min: 0, max: 100, step: 1 }}
                  helperText="0-100%"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="REM Sleep"
                  type="number"
                  value={formData.rem_sleep_minutes}
                  onChange={(e) => handleInputChange('rem_sleep_minutes', e.target.value)}
                  fullWidth
                  inputProps={{ min: 0, step: 1 }}
                  helperText="Minutes"
                />
              </Grid>
              <Grid item xs={12} sm={6}>
                <TextField
                  label="Deep Sleep"
                  type="number"
                  value={formData.deep_sleep_minutes}
                  onChange={(e) => handleInputChange('deep_sleep_minutes', e.target.value)}
                  fullWidth
                  inputProps={{ min: 0, step: 1 }}
                  helperText="Minutes"
                />
              </Grid>
              <Grid item xs={12}>
                <TextField
                  label="Subjective Tiredness"
                  type="number"
                  value={formData.subjective_tiredness}
                  onChange={(e) => handleInputChange('subjective_tiredness', e.target.value)}
                  fullWidth
                  inputProps={{ min: 1, max: 5, step: 1 }}
                  helperText="1-5 scale (1 = very fresh, 5 = very tired)"
                />
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setDialogOpen(false)}>
              Cancel
            </Button>
            <Button
              onClick={handleSubmitMetrics}
              variant="contained"
              disabled={submitMetricsMutation.isPending}
              sx={{ backgroundColor: '#00bcd4', '&:hover': { backgroundColor: '#00acc1' } }}
            >
              {submitMetricsMutation.isPending ? 'Saving...' : 'Save Metrics'}
            </Button>
          </DialogActions>
        </Dialog>
      )}

      {/* Weekly Test Dialog */}
      {type === 'weekly' && (
        <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="sm" fullWidth>
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
            <Button onClick={() => setDialogOpen(false)}>
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
      )}
    </>
  );
};
