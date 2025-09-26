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
import { Add as AddIcon, Edit as EditIcon } from '@mui/icons-material';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import { GameCard, GameSubCard, GameStatusChip, GameText, GameTextSecondary } from './GameTheme';
import { CustomSvgIcon } from './CustomSvgIcon';
import { MetricTrendChart } from './MetricTrendChart';
import { useData } from '../contexts/DataContext';
import { submitPerformanceMetrics, getTestProtocols, submitWeeklyTest } from '../api/performanceTracking';
import { formatDate } from '../common/utils';
import type { UserPerformanceMetrics, UserTestResult, TestProtocol } from '../api/types';

// Import icons for weekly tests - matching radar chart icons
import FistPowerIcon from '../resources/fist-power-icon.svg';
import HeartIcon from '../resources/heart-icon.svg';
import BrainMindIcon from '../resources/brain-mind-icon.svg';
import RunningShoeIcon from '../resources/running-shoe-icon.svg';
import EarSoundIcon from '../resources/ear-sound-icon.svg';

interface CompactQuestCardProps {
  type: 'daily' | 'weekly';
  currentMetrics?: UserPerformanceMetrics;
  weeklyTests?: UserTestResult[];
  onTestUpdate?: () => void;
}

// Icon mapping for test protocols - using same icons as radar chart
const getIconForProtocol = (testName: string) => {
  switch (testName) {
    case 'vertical_jump': return <CustomSvgIcon src={FistPowerIcon} alt="Explosiveness" sx={{ fontSize: 24, color: '#00bcd4' }} />;
    case 'vo2_max': return <CustomSvgIcon src={HeartIcon} alt="Stamina" sx={{ fontSize: 24, color: '#00bcd4' }} />;
    case 'hr_recovery': return <CustomSvgIcon src={BrainMindIcon} alt="Recovery" sx={{ fontSize: 24, color: '#00bcd4' }} />;
    case 'reflex': return <CustomSvgIcon src={RunningShoeIcon} alt="Reflexes" sx={{ fontSize: 24, color: '#00bcd4' }} />;
    case 'mobility': return <CustomSvgIcon src={EarSoundIcon} alt="Dexterity" sx={{ fontSize: 24, color: '#00bcd4' }} />;
    default: return <CustomSvgIcon src={FistPowerIcon} alt="Test" sx={{ fontSize: 24, color: '#00bcd4' }} />;
  }
};

// Daily metrics configuration
const dailyMetricsConfig = [
  { key: 'strain', label: 'Strain', icon: <CustomSvgIcon src={FistPowerIcon} alt="Strain" sx={{ fontSize: 24, color: '#00bcd4' }} />, unit: '', description: 'Daily strain score from wearables' },
  { key: 'recovery', label: 'Recovery', icon: <CustomSvgIcon src={BrainMindIcon} alt="Recovery" sx={{ fontSize: 24, color: '#00bcd4' }} />, unit: '%', description: 'Daily recovery score percentage' },
  { key: 'hrv', label: 'HRV', icon: <CustomSvgIcon src={HeartIcon} alt="HRV" sx={{ fontSize: 24, color: '#00bcd4' }} />, unit: ' ms', description: 'Heart rate variability measurement' },
  { key: 'sleep_score', label: 'Sleep Score', icon: <CustomSvgIcon src={BrainMindIcon} alt="Sleep Score" sx={{ fontSize: 24, color: '#00bcd4' }} />, unit: '%', description: 'Overall sleep quality score' },
];

export const CompactQuestCard: React.FC<CompactQuestCardProps> = ({ 
  type, 
  currentMetrics, 
  weeklyTests = [], 
  onTestUpdate 
}) => {
  const { refreshPerformanceData, submitWeeklyTest: submitWeeklyTestData, getCurrentWeekTest, loadPerformanceMetricsInRange } = useData();
  const { enqueueSnackbar } = useSnackbar();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [historicalMetrics, setHistoricalMetrics] = useState<UserPerformanceMetrics[]>([]);
  const [isLoadingHistorical, setIsLoadingHistorical] = useState(false);
  
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
  
  // Metric editing state
  const [editingMetric, setEditingMetric] = useState<string | null>(null);
  const [editingProtocol, setEditingProtocol] = useState<TestProtocol | null>(null);

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
    if (!editingProtocol || !testValue) return;

    const updatedTestResult: Omit<UserTestResult, 'id' | 'keycloak_id' | 'created_at' | 'updated_at'> = {
      week_start_timestamp: new Date(getCurrentWeekStart()),
      test_name: editingProtocol.test_name,
      status: 'COMPLETED',
      result_value: Number(testValue),
    };

    const updatedTestResults = testProtocols.map(protocol => {
      if (protocol.test_name === editingProtocol.test_name) {
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


  const getCurrentWeekStart = () => {
    // Calculate current week start (Monday)
    const now = new Date();
    const dayOfWeek = now.getDay();
    const daysToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek; // Sunday = 0, Monday = 1
    const monday = new Date(now);
    monday.setDate(now.getDate() + daysToMonday);
    monday.setHours(0, 0, 0, 0);
    
    return monday.toISOString();
  };

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
      // Calculate current week start (Monday)
      const now = new Date();
      const dayOfWeek = now.getDay();
      const daysToMonday = dayOfWeek === 0 ? -6 : 1 - dayOfWeek; // Sunday = 0, Monday = 1
      const monday = new Date(now);
      monday.setDate(now.getDate() + daysToMonday);
      monday.setHours(0, 0, 0, 0);
      
      return `Week of ${formatDate(monday)}`;
    }
  };

  const loadHistoricalData = async () => {
    if (type === 'daily') {
      setIsLoadingHistorical(true);
      try {
        const endDate = new Date().toISOString();
        const startDate = new Date();
        startDate.setDate(startDate.getDate() - 30);
        const startDateStr = startDate.toISOString();
        
        const data = await loadPerformanceMetricsInRange(startDateStr, endDate);
        setHistoricalMetrics(data);
      } catch (error) {
        console.error('Failed to load historical data:', error);
        setHistoricalMetrics([]);
      } finally {
        setIsLoadingHistorical(false);
      }
    }
  };

  const handleEditDailyMetric = (metricKey: string) => {
    setEditingMetric(metricKey);
    setDialogOpen(true);
    loadHistoricalData();
  };

  const handleEditWeeklyTest = (protocol: TestProtocol) => {
    setEditingProtocol(protocol);
    setDialogOpen(true);
  };

  const handleSubmitDailyMetric = () => {
    if (!editingMetric) return;

    // Create updated metrics with the new value
    const updatedMetrics: Omit<UserPerformanceMetrics, 'keycloak_id' | 'created_at' | 'updated_at'> = {
      strain: formData.strain ? parseFloat(formData.strain) : undefined,
      recovery: formData.recovery ? parseFloat(formData.recovery) : undefined,
      hrv: formData.hrv ? parseFloat(formData.hrv) : undefined,
      sleep_score: formData.sleep_score ? parseFloat(formData.sleep_score) : undefined,
      rem_sleep_minutes: formData.rem_sleep_minutes ? parseFloat(formData.rem_sleep_minutes) : undefined,
      deep_sleep_minutes: formData.deep_sleep_minutes ? parseFloat(formData.deep_sleep_minutes) : undefined,
      subjective_tiredness: formData.subjective_tiredness ? parseInt(formData.subjective_tiredness) : undefined,
    };

    submitMetricsMutation.mutate(updatedMetrics);
  };

  const renderCompactGrid = () => {
    if (type === 'daily') {
      return (
        <Grid container spacing={1}>
          {dailyMetricsConfig.map((metric, index) => {
            const value = formData[metric.key as keyof typeof formData];
            const hasValue = value && value !== '';
            
            return (
              <Grid size={{ xs: 6 }} key={index}>
                <GameSubCard 
                  sx={{ 
                    height: '80px', 
                    cursor: 'pointer',
                    '&:hover': { opacity: 0.8 }
                  }}
                  onClick={() => handleEditDailyMetric(metric.key)}
                >
                  <CardContent sx={{ p: 1, height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                    <Stack spacing={0.2} alignItems="center" sx={{ rowGap: '0px !important' }}>
                      <Box sx={{ fontSize: 24, color: '#00bcd4', fontWeight: 'bold' }}>
                        {metric.icon}
                      </Box>
                      <GameText variant="caption" sx={{ fontWeight: 'bold', textAlign: 'center' }}>
                        {metric.label}
                      </GameText>
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
              <Grid size={{ xs: 6 }} key={index}>
                <GameSubCard 
                  sx={{ 
                    height: '80px', 
                    cursor: 'pointer',
                    '&:hover': { opacity: 0.8 }
                  }}
                  onClick={() => handleEditWeeklyTest(protocol)}
                >
                  <CardContent sx={{ p: 1, height: '100%', display: 'flex', flexDirection: 'column', justifyContent: 'center' }}>
                    <Stack spacing={0.2} alignItems="center" sx={{ rowGap: '0px !important' }}>
                      {getIconForProtocol(protocol.test_name)}
                      <GameText variant="caption" sx={{ fontWeight: 'bold', textAlign: 'center' }}>
                        {protocol.display_name}
                      </GameText>
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


  return (
    <>
      <GameCard sx={{ width: '100%' }}>
        <CardHeader
          sx={{ paddingBottom: 0 }}
          title={
            <GameText variant="h6" sx={{ fontWeight: 'bold' }}>
              {getTitle()}
            </GameText>
          }
          subheader={
            <GameTextSecondary variant="body2">
              {getSubtitle()}
            </GameTextSecondary>
          }
        />
        <CardContent sx={{ pt: 1 }}>
          {renderCompactGrid()}
        </CardContent>
      </GameCard>

      {/* Metric Detail Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          <GameText variant="h6" sx={{ fontWeight: 'bold' }}>
            {type === 'daily' 
              ? dailyMetricsConfig.find(m => m.key === editingMetric)?.label 
              : editingProtocol?.display_name
            }
          </GameText>
        </DialogTitle>
        <DialogContent>
          <Stack spacing={3} sx={{ mt: 2 }}>
            {/* 30-day Chart */}
            {type === 'daily' && editingMetric ? (
              <MetricTrendChart
                metricKey={editingMetric}
                metricLabel={dailyMetricsConfig.find(m => m.key === editingMetric)?.label || editingMetric}
                metricUnit={dailyMetricsConfig.find(m => m.key === editingMetric)?.unit || ''}
                historicalData={historicalMetrics}
                isLoading={isLoadingHistorical}
                height={200}
              />
            ) : type === 'weekly' && editingProtocol ? (
              <MetricTrendChart
                metricKey={editingProtocol.test_name}
                metricLabel={editingProtocol.display_name}
                metricUnit={editingProtocol.unit}
                historicalData={[]} // TODO: Add weekly test historical data
                isLoading={false}
                height={200}
              />
            ) : null}
            
            {/* Input Section */}
            {type === 'daily' ? (
              <Box>
                {(() => {
                  const metric = dailyMetricsConfig.find(m => m.key === editingMetric);
                  if (!metric) return null;
                  
                  const currentValue = formData[editingMetric as keyof typeof formData];
                  const hasValue = currentValue && currentValue !== '';
                  
                  return (
                    <Stack spacing={2}>
                      {hasValue ? (
                        <Alert severity="info">
                          <GameText variant="body2">
                            Today's {metric.label.toLowerCase()} has already been recorded ({currentValue}{metric.unit}). 
                            Check back tomorrow to record your next result!
                          </GameText>
                        </Alert>
                      ) : (
                        <Stack spacing={2}>
                          <GameText variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                            Record Today's {metric.label}
                          </GameText>
                          <TextField
                            label={metric.label}
                            type="number"
                            value={currentValue}
                            onChange={(e) => editingMetric && handleInputChange(editingMetric, e.target.value)}
                            fullWidth
                            inputProps={{ 
                              min: metric.key === 'strain' ? 0 : metric.key === 'subjective_tiredness' ? 1 : 0,
                              max: metric.key === 'strain' ? 21 : metric.key === 'subjective_tiredness' ? 5 : undefined,
                              step: metric.key === 'strain' || metric.key === 'hrv' ? 0.1 : 1
                            }}
                            helperText={metric.description}
                          />
                        </Stack>
                      )}
                    </Stack>
                  );
                })()}
              </Box>
            ) : (
              <Box>
                {(() => {
                  if (!editingProtocol) return null;
                  
                  const testResult = currentWeekTests.find(test => test.test_name === editingProtocol.test_name);
                  const result = testResult?.result_value;
                  const hasValue = testResult?.status === 'COMPLETED' && result;
                  
                  return (
                    <Stack spacing={2}>
                      <GameText variant="subtitle2" sx={{ fontWeight: 'bold' }}>
                        Record This Week's {editingProtocol.display_name}
                      </GameText>
                      
                      <Alert severity="info">
                        {editingProtocol.description}
                      </Alert>
                      
                      {hasValue ? (
                        <Alert severity="info">
                          <GameText variant="body2">
                            This week's {editingProtocol.display_name.toLowerCase()} has already been recorded ({result} {editingProtocol.unit}). 
                            Check back next week to record your next result!
                          </GameText>
                        </Alert>
                      ) : (
                        <TextField
                          fullWidth
                          label={`Result (${editingProtocol.unit})`}
                          type="number"
                          value={testValue}
                          onChange={(e) => setTestValue(e.target.value)}
                          placeholder={`Enter your ${editingProtocol.test_name.toLowerCase()} result`}
                        />
                      )}
                    </Stack>
                  );
                })()}
              </Box>
            )}
          </Stack>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => {
            setDialogOpen(false);
            setEditingMetric(null);
            setEditingProtocol(null);
          }}>
            Close
          </Button>
          {((type === 'daily' && editingMetric && !formData[editingMetric as keyof typeof formData]) ||
            (type === 'weekly' && editingProtocol && testValue)) && (
            <Button
              onClick={type === 'daily' ? handleSubmitDailyMetric : handleSubmitTest}
              variant="contained"
              disabled={submitMetricsMutation.isPending || submitTestMutation.isPending}
              sx={{ backgroundColor: '#00bcd4', '&:hover': { backgroundColor: '#00acc1' } }}
            >
              {(submitMetricsMutation.isPending || submitTestMutation.isPending) ? 'Saving...' : 'Confirm'}
            </Button>
          )}
        </DialogActions>
      </Dialog>
    </>
  );
};
