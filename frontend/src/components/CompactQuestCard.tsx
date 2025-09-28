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
  Stack,
  Alert,
  LinearProgress,
} from '@mui/material';
import { useMutation, useQuery } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import React, { useState } from 'react';

import { CustomSvgIcon } from './CustomSvgIcon';
import { GameCard, GameSubCard, GameText, GameTextSecondary, GAME_CLASSES } from './GameTheme';
import { MetricTrendChart } from './MetricTrendChart';
import { submitPerformanceMetrics, getTestProtocols } from '../api/performanceTracking';
import type { UserPerformanceMetrics, UserTestResult, TestProtocol } from '../api/types';
import { formatDate } from '../common/utils';
import { useData } from '../contexts/DataContext';
import RecoveryIcon from '../resources/recovery-icon.svg';
import DexterityIcon from '../resources/dexterity-icon.svg';
import ExplosivenessIcon from '../resources/explosiveness-icon.svg';
import HealthIcon from '../resources/health-icon.svg';
import StaminaIcon from '../resources/stamina-icon.svg';
import StrainIcon from '../resources/strain-icon.svg';
import ReflexesIcon from '../resources/reflexes-icon.svg';

interface CompactQuestCardProps {
  type: 'daily' | 'weekly';
  currentMetrics?: UserPerformanceMetrics;
  weeklyTests?: UserTestResult[];
  onTestUpdate?: () => void;
}

// Icon mapping for test protocols - using same icons as radar chart
const getIconForProtocol = (testName: string) => {
  switch (testName) {
    case 'vertical_jump':
      return (
        <CustomSvgIcon
          src={ExplosivenessIcon}
          alt="Explosiveness"
          className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`}
        />
      );
    case 'hr_recovery':
      return (
        <CustomSvgIcon src={RecoveryIcon} alt="Recovery" className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`} />
      );
    case 'reflex':
      return (
        <CustomSvgIcon
          src={ReflexesIcon}
          alt="Reflexes"
          className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`}
        />
      );
    case 'mobility':
      return (
        <CustomSvgIcon src={DexterityIcon} alt="Dexterity" className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`} />
      );
    default:
      return (
        <CustomSvgIcon src={ExplosivenessIcon} alt="Test" className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`} />
      );
  }
};

// Daily metrics configuration
const dailyMetricsConfig = [
  {
    key: 'strain',
    label: 'Strain',
    icon: (
      <CustomSvgIcon src={StrainIcon} alt="Strain" className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`} />
    ),
    unit: '',
    description: 'Daily strain score from wearables',
  },
  {
    key: 'recovery',
    label: 'Recovery',
    icon: (
      <CustomSvgIcon src={RecoveryIcon} alt="Recovery" className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`} />
    ),
    unit: '%',
    description: 'Daily recovery score percentage',
  },
  {
    key: 'hrv',
    label: 'HRV',
    icon: <CustomSvgIcon src={HealthIcon} alt="HRV" className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`} />,
    unit: ' ms',
    description: 'Heart rate variability measurement',
  },
  {
    key: 'sleep_score',
    label: 'Sleep Score',
    icon: (
      <CustomSvgIcon
        src={RecoveryIcon}
        alt="Sleep Score"
        className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`}
      />
    ),
    unit: '%',
    description: 'Overall sleep quality score',
  },
  {
    key: 'vo2_max',
    label: 'VO₂ Max',
    icon: (
      <CustomSvgIcon
        src={StaminaIcon}
        alt="VO₂ Max"
        className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`}
      />
    ),
    unit: ' ml/kg/min',
    description: 'Maximum oxygen consumption during exercise',
  },
];

export const CompactQuestCard: React.FC<CompactQuestCardProps> = ({
  type,
  currentMetrics,
  weeklyTests = [],
  onTestUpdate,
}) => {
  const {
    refreshPerformanceData,
    submitWeeklyTest: submitWeeklyTestData,
    getCurrentWeekTest,
    loadPerformanceMetricsInRange,
  } = useData();
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
    vo2_max: currentMetrics?.vo2_max?.toString() || '',
    rem_sleep_minutes: currentMetrics?.rem_sleep_minutes?.toString() || '',
    deep_sleep_minutes: currentMetrics?.deep_sleep_minutes?.toString() || '',
    subjective_tiredness: currentMetrics?.subjective_tiredness?.toString() || '',
  });

  // Weekly test state
  const [testValue, setTestValue] = useState<string>('');

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
  const currentWeekTests = type === 'weekly' ? getCurrentWeekTest() || weeklyTests : [];

  // Daily metrics mutation
  const submitMetricsMutation = useMutation({
    mutationFn: (
      metrics: Omit<UserPerformanceMetrics, 'keycloak_id' | 'created_at' | 'updated_at'>
    ) => submitPerformanceMetrics(metrics),
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
    mutationFn: (
      testResults: Omit<UserTestResult, 'id' | 'keycloak_id' | 'created_at' | 'updated_at'>[]
    ) => submitWeeklyTestData(testResults),
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

  const handleSubmitTest = () => {
    if (!editingProtocol || !testValue) return;

    const updatedTestResult: Omit<
      UserTestResult,
      'id' | 'keycloak_id' | 'created_at' | 'updated_at'
    > = {
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
      return (
        existingResult || {
          week_start_timestamp: updatedTestResult.week_start_timestamp,
          test_name: protocol.test_name,
          status: 'PENDING' as const,
          result_value: undefined,
        }
      );
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

  const getTitle = () => (type === 'daily' ? 'Daily Quests' : 'Weekly Quests');
  const getSubtitle = () => {
    if (type === 'daily') {
      return new Date().toLocaleDateString('en-US', {
        weekday: 'long',
        year: 'numeric',
        month: 'long',
        day: 'numeric',
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
      } catch {
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
    const updatedMetrics: Omit<
      UserPerformanceMetrics,
      'keycloak_id' | 'created_at' | 'updated_at'
    > = {
      strain: formData.strain ? parseFloat(formData.strain) : undefined,
      recovery: formData.recovery ? parseFloat(formData.recovery) : undefined,
      hrv: formData.hrv ? parseFloat(formData.hrv) : undefined,
      sleep_score: formData.sleep_score ? parseFloat(formData.sleep_score) : undefined,
      vo2_max: formData.vo2_max ? parseFloat(formData.vo2_max) : undefined,
      rem_sleep_minutes: formData.rem_sleep_minutes
        ? parseFloat(formData.rem_sleep_minutes)
        : undefined,
      deep_sleep_minutes: formData.deep_sleep_minutes
        ? parseFloat(formData.deep_sleep_minutes)
        : undefined,
      subjective_tiredness: formData.subjective_tiredness
        ? parseInt(formData.subjective_tiredness)
        : undefined,
    };

    submitMetricsMutation.mutate(updatedMetrics);
  };

  const renderCompactGrid = () => {
    if (type === 'daily') {
      return (
        <Grid container spacing={1} sx={{ height: '100%' }}>
          {dailyMetricsConfig.map((metric, index) => {
            return (
              <Grid size={{ xs: 6, sm: 4 }} key={index} sx={{ height: '50%' }}>
                <GameSubCard
                  className={`${GAME_CLASSES.cursorPointer} ${GAME_CLASSES.hoverOpacity80}`}
                  onClick={() => handleEditDailyMetric(metric.key)}
                  sx={{ height: '100%' }}
                >
                  <CardContent
                    className={`${GAME_CLASSES.padding2} ${GAME_CLASSES.height100} ${GAME_CLASSES.flex} ${GAME_CLASSES.flexColumn} ${GAME_CLASSES.justifyCenter}`}
                  >
                    <Stack spacing={0.1} alignItems="center" className={GAME_CLASSES.rowGap0}>
                      <Box className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan} ${GAME_CLASSES.textBold}`}>
                        {metric.icon}
                      </Box>
                      <GameText 
                        variant="caption" 
                        className={`${GAME_CLASSES.textBold} ${GAME_CLASSES.textCenter}`}
                        sx={{ 
                          wordBreak: 'break-word',
                          hyphens: 'auto',
                          lineHeight: 1.2,
                          fontSize: '0.75rem',
                          marginTop: 0
                        }}
                      >
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
          <Box className={`${GAME_CLASSES.flex} ${GAME_CLASSES.justifyCenter} ${GAME_CLASSES.padding2}`}>
            <LinearProgress className={GAME_CLASSES.width100} />
          </Box>
        );
      }

      return (
        <Grid container spacing={1} sx={{ height: '100%' }}>
          {testProtocols.slice(0, 4).map((protocol, index) => {
            return (
              <Grid size={{ xs: 6 }} key={index} sx={{ height: '50%' }}>
                <GameSubCard
                  className={`${GAME_CLASSES.cursorPointer} ${GAME_CLASSES.hoverOpacity80}`}
                  onClick={() => handleEditWeeklyTest(protocol)}
                  sx={{ height: '100%' }}
                >
                  <CardContent
                    className={`${GAME_CLASSES.padding2} ${GAME_CLASSES.height100} ${GAME_CLASSES.flex} ${GAME_CLASSES.flexColumn} ${GAME_CLASSES.justifyCenter}`}
                  >
                    <Stack spacing={0.1} alignItems="center" className={GAME_CLASSES.rowGap0}>
                      <Box className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan} ${GAME_CLASSES.textBold}`}>
                        {getIconForProtocol(protocol.test_name)}
                      </Box>
                      <GameText 
                        variant="caption" 
                        className={`${GAME_CLASSES.textBold} ${GAME_CLASSES.textCenter}`}
                        sx={{ 
                          wordBreak: 'break-word',
                          hyphens: 'auto',
                          lineHeight: 1.2,
                          fontSize: '0.75rem',
                          marginTop: 0
                        }}
                      >
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
    <React.Fragment>
      <GameCard className={GAME_CLASSES.width100} sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
        <CardHeader
          className={GAME_CLASSES.paddingBottom0}
          title={
            <GameText variant="h6" textVariant="glow" className={`${GAME_CLASSES.textBold} ${GAME_CLASSES.textTransformUppercase}`}>
              {getTitle()}
            </GameText>
          }
          subheader={<GameTextSecondary variant="body2">{getSubtitle()}</GameTextSecondary>}
        />
        <CardContent className={`${GAME_CLASSES.paddingTop1} ${GAME_CLASSES.flex1}`} sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
          {renderCompactGrid()}
        </CardContent>
      </GameCard>

      {/* Metric Detail Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          <GameText variant="h6" className={GAME_CLASSES.textBold}>
            {type === 'daily'
              ? dailyMetricsConfig.find(m => m.key === editingMetric)?.label
              : editingProtocol?.display_name}
          </GameText>
        </DialogTitle>
        <DialogContent>
          <Stack spacing={3} className={GAME_CLASSES.marginTop2}>
            {/* 30-day Chart */}
            {type === 'daily' && editingMetric ? (
              <MetricTrendChart
                metricKey={editingMetric}
                metricLabel={
                  dailyMetricsConfig.find(m => m.key === editingMetric)?.label || editingMetric
                }
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
                            Today&apos;s {metric.label.toLowerCase()} has already been recorded (
                            {currentValue}
                            {metric.unit}). Check back tomorrow to record your next result!
                          </GameText>
                        </Alert>
                      ) : (
                        <Stack spacing={2}>
                          <GameText variant="subtitle2" className={GAME_CLASSES.textBold}>
                            Record Today&apos;s {metric.label}
                          </GameText>
                          <TextField
                            label={metric.label}
                            type="number"
                            value={currentValue}
                            onChange={e =>
                              editingMetric && handleInputChange(editingMetric, e.target.value)
                            }
                            fullWidth
                            inputProps={{
                              min:
                                metric.key === 'strain'
                                  ? 0
                                  : metric.key === 'subjective_tiredness'
                                    ? 1
                                    : 0,
                              max:
                                metric.key === 'strain'
                                  ? 21
                                  : metric.key === 'subjective_tiredness'
                                    ? 5
                                    : undefined,
                              step: metric.key === 'strain' || metric.key === 'hrv' ? 0.1 : 1,
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

                  const testResult = currentWeekTests.find(
                    test => test.test_name === editingProtocol.test_name
                  );
                  const result = testResult?.result_value;
                  const hasValue = testResult?.status === 'COMPLETED' && result;

                  return (
                    <Stack spacing={2}>
                      <GameText variant="subtitle2" className={GAME_CLASSES.textBold}>
                        Record This Week&apos;s {editingProtocol.display_name}
                      </GameText>

                      <Alert severity="info">{editingProtocol.description}</Alert>

                      {hasValue ? (
                        <Alert severity="info">
                          <GameText variant="body2">
                            This week&apos;s {editingProtocol.display_name.toLowerCase()} has
                            already been recorded ({result} {editingProtocol.unit}). Check back next
                            week to record your next result!
                          </GameText>
                        </Alert>
                      ) : (
                        <TextField
                          fullWidth
                          label={`Result (${editingProtocol.unit})`}
                          type="number"
                          value={testValue}
                          onChange={e => setTestValue(e.target.value)}
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
          <Button
            onClick={() => {
              setDialogOpen(false);
              setEditingMetric(null);
              setEditingProtocol(null);
            }}
          >
            Close
          </Button>
          {((type === 'daily' &&
            editingMetric &&
            !formData[editingMetric as keyof typeof formData]) ||
            (type === 'weekly' && editingProtocol && testValue)) && (
            <Button
              onClick={type === 'daily' ? handleSubmitDailyMetric : handleSubmitTest}
              variant="contained"
              disabled={submitMetricsMutation.isPending || submitTestMutation.isPending}
              className={`${GAME_CLASSES.backgroundColorCyan} ${GAME_CLASSES.hoverBackgroundColorCyan}`}
            >
              {submitMetricsMutation.isPending || submitTestMutation.isPending
                ? 'Saving...'
                : 'Confirm'}
            </Button>
          )}
        </DialogActions>
      </Dialog>
    </React.Fragment>
  );
};
