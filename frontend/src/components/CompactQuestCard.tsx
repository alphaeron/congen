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
} from '@mui/material';
import { useMutation } from '@tanstack/react-query';
import { useSnackbar } from 'notistack';
import React, { useState, useEffect } from 'react';
import { motion } from 'framer-motion';

import { CustomSvgIcon } from './CustomSvgIcon';
import { GameCard, GameSubCard, GameText, GameTextSecondary, GAME_CLASSES } from './GameTheme';
import { MetricTrendChart } from './MetricTrendChart';
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
  {
    key: 'rem_sleep_minutes',
    label: 'REM Sleep',
    icon: (
      <CustomSvgIcon
        src={RecoveryIcon}
        alt="REM Sleep"
        className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`}
      />
    ),
    unit: ' min',
    description: 'REM sleep duration in minutes',
  },
  {
    key: 'deep_sleep_minutes',
    label: 'Deep Sleep',
    icon: (
      <CustomSvgIcon
        src={RecoveryIcon}
        alt="Deep Sleep"
        className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`}
      />
    ),
    unit: ' min',
    description: 'Deep sleep duration in minutes',
  },
  {
    key: 'subjective_tiredness',
    label: 'Tiredness',
    icon: (
      <CustomSvgIcon
        src={HealthIcon}
        alt="Tiredness"
        className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`}
      />
    ),
    unit: '/5',
    description: 'Subjective tiredness rating (1-5 scale)',
  },
];

export const CompactQuestCard: React.FC<CompactQuestCardProps> = ({
  type,
  currentMetrics,
  weeklyTests = [],
  onTestUpdate,
}) => {
  const {
    submitPerformanceMetrics: submitPerformanceMetricsData,
    submitWeeklyTest: submitWeeklyTestData,
    getCurrentWeekTest,
    loadPerformanceMetricsInRange,
    loadWeeklyTests,
    loadTestProtocols,
    testProtocols,
    refreshData,
  } = useData();
  const { enqueueSnackbar } = useSnackbar();
  const [dialogOpen, setDialogOpen] = useState(false);
  const [confirmClearOpen, setConfirmClearOpen] = useState(false);
  const [historicalMetrics, setHistoricalMetrics] = useState<UserPerformanceMetrics[]>([]);
  const [isLoadingHistorical, setIsLoadingHistorical] = useState(false);
  const [allHistoricalWeeklyTests, setAllHistoricalWeeklyTests] = useState<UserTestResult[]>([]);
  const [isLoadingHistoricalWeekly, setIsLoadingHistoricalWeekly] = useState(false);

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

  // Update form data when currentMetrics changes
  useEffect(() => {
    setFormData({
      strain: currentMetrics?.strain?.toString() || '',
      recovery: currentMetrics?.recovery?.toString() || '',
      hrv: currentMetrics?.hrv?.toString() || '',
      sleep_score: currentMetrics?.sleep_score?.toString() || '',
      vo2_max: currentMetrics?.vo2_max?.toString() || '',
      rem_sleep_minutes: currentMetrics?.rem_sleep_minutes?.toString() || '',
      deep_sleep_minutes: currentMetrics?.deep_sleep_minutes?.toString() || '',
      subjective_tiredness: currentMetrics?.subjective_tiredness?.toString() || '',
    });
  }, [currentMetrics]);

  // Weekly test state
  const [testValue, setTestValue] = useState<string>('');

  // Metric editing state
  const [editingMetric, setEditingMetric] = useState<string | null>(null);
  const [editingProtocol, setEditingProtocol] = useState<TestProtocol | null>(null);

  // Load test protocols for weekly quests
  useEffect(() => {
    if (type === 'weekly' && testProtocols.length === 0) {
      loadTestProtocols();
    }
  }, [type, testProtocols.length, loadTestProtocols]);

  // Get current week test results
  const currentWeekTests = type === 'weekly' ? getCurrentWeekTest() || weeklyTests : [];

  // Daily metrics mutation - uses DataContext function
  const submitMetricsMutation = useMutation({
    mutationFn: (
      metrics: Omit<UserPerformanceMetrics, 'keycloak_id' | 'created_at' | 'updated_at'>
    ) => submitPerformanceMetricsData(metrics),
    onSuccess: () => {
      setDialogOpen(false);
      enqueueSnackbar('Daily metrics updated successfully!', { variant: 'success' });
    },
    onError: () => {
      enqueueSnackbar('Failed to update daily metrics', { variant: 'error' });
    },
  });

  // Weekly test mutation - uses DataContext function
  const submitTestMutation = useMutation({
    mutationFn: (
      testResult: Omit<UserTestResult, 'id' | 'keycloak_id' | 'created_at' | 'updated_at'>
    ) => submitWeeklyTestData(testResult),
    onSuccess: () => {
      onTestUpdate?.();
      setDialogOpen(false);
      enqueueSnackbar('Weekly test updated successfully!', { variant: 'success' });
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

    submitTestMutation.mutate(updatedTestResult, {
      onSuccess: async () => {
        // Refresh the data after successful submission
        await refreshData();
        // Keep dialog open to show updated data
      },
      onError: () => {
        enqueueSnackbar('Failed to submit test. Please try again.', { variant: 'error' });
      }
    });
  };

  const getCurrentWeekStart = () => {
    const now = new Date();
    const monday = new Date(now);
    monday.setDate(now.getDate() - now.getDay() + 1);
    monday.setHours(0, 0, 0, 0);
    return monday.toISOString();
  };

  const getTitle = () => (type === 'daily' ? 'Daily Quests' : 'Weekly Quests');
  const getSubtitle = () => {
    if (type === 'daily') {
      return formatDate(new Date(), {
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
    } else if (type === 'weekly' && editingProtocol) {
      setIsLoadingHistoricalWeekly(true);
      try {
        const endDate = new Date().toISOString();
        const startDate = new Date();
        startDate.setDate(startDate.getDate() - 30);
        const startDateStr = startDate.toISOString();

        const data = await loadWeeklyTests(startDateStr, endDate);
        // Store all historical data, we'll filter dynamically in the chart
        setAllHistoricalWeeklyTests(data);
      } catch {
        setAllHistoricalWeeklyTests([]);
      } finally {
        setIsLoadingHistoricalWeekly(false);
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
    
    // Clear previous historical data
    setAllHistoricalWeeklyTests([]);
    
    // Check if this test already has a recorded value for the current week
    const existingTest = currentWeekTests.find(test => test.test_name === protocol.test_name);
    if (existingTest && existingTest.status === 'COMPLETED' && existingTest.result_value) {
      // Prepopulate with existing value
      setTestValue(existingTest.result_value.toString());
    } else {
      // Clear the value for new entry
      setTestValue('');
    }
    
    setDialogOpen(true);
    loadHistoricalData();
  };

  const handleSubmitDailyMetric = () => {
    if (!editingMetric) return;

    // Create updated metrics with only the metric being edited
    const updatedMetrics: Omit<
      UserPerformanceMetrics,
      'keycloak_id' | 'created_at' | 'updated_at'
    > = {};

    // Only include the metric that was actually edited
    switch (editingMetric) {
      case 'strain':
        if (formData.strain === '') {
          updatedMetrics.strain = undefined;
        } else if (formData.strain) {
          updatedMetrics.strain = parseFloat(formData.strain);
        } else {
          updatedMetrics.strain = undefined;
        }
        break;
      case 'recovery':
        if (formData.recovery === '') {
          updatedMetrics.recovery = undefined;
        } else if (formData.recovery) {
          updatedMetrics.recovery = parseFloat(formData.recovery);
        } else {
          updatedMetrics.recovery = undefined;
        }
        break;
      case 'hrv':
        if (formData.hrv === '') {
          updatedMetrics.hrv = undefined;
        } else if (formData.hrv) {
          updatedMetrics.hrv = parseFloat(formData.hrv);
        } else {
          updatedMetrics.hrv = undefined;
        }
        break;
      case 'sleep_score':
        if (formData.sleep_score === '') {
          updatedMetrics.sleep_score = undefined;
        } else if (formData.sleep_score) {
          updatedMetrics.sleep_score = parseFloat(formData.sleep_score);
        } else {
          updatedMetrics.sleep_score = undefined;
        }
        break;
      case 'vo2_max':
        if (formData.vo2_max === '') {
          updatedMetrics.vo2_max = undefined;
        } else if (formData.vo2_max) {
          updatedMetrics.vo2_max = parseFloat(formData.vo2_max);
        } else {
          updatedMetrics.vo2_max = undefined;
        }
        break;
      case 'rem_sleep_minutes':
        if (formData.rem_sleep_minutes === '') {
          updatedMetrics.rem_sleep_minutes = undefined;
        } else if (formData.rem_sleep_minutes) {
          updatedMetrics.rem_sleep_minutes = parseFloat(formData.rem_sleep_minutes);
        } else {
          updatedMetrics.rem_sleep_minutes = undefined;
        }
        break;
      case 'deep_sleep_minutes':
        if (formData.deep_sleep_minutes === '') {
          updatedMetrics.deep_sleep_minutes = undefined;
        } else if (formData.deep_sleep_minutes) {
          updatedMetrics.deep_sleep_minutes = parseFloat(formData.deep_sleep_minutes);
        } else {
          updatedMetrics.deep_sleep_minutes = undefined;
        }
        break;
      case 'subjective_tiredness':
        if (formData.subjective_tiredness === '') {
          updatedMetrics.subjective_tiredness = undefined;
        } else if (formData.subjective_tiredness) {
          updatedMetrics.subjective_tiredness = parseInt(formData.subjective_tiredness);
        } else {
          updatedMetrics.subjective_tiredness = undefined;
        }
        break;
    }

    submitMetricsMutation.mutate(updatedMetrics, {
      onSuccess: async () => {
        // Refresh the data after successful submission
        await refreshData();
        // Keep dialog open to show updated data
      },
      onError: () => {
        enqueueSnackbar('Failed to submit metric. Please try again.', { variant: 'error' });
      }
    });
  };

  const handleClearMetric = () => {
    if (!editingMetric) return;

    // Create updated metrics with only the metric being cleared
    const updatedMetrics: Omit<
      UserPerformanceMetrics,
      'keycloak_id' | 'created_at' | 'updated_at'
    > = {};

    // Set the metric to undefined to clear it
    switch (editingMetric) {
      case 'strain':
        updatedMetrics.strain = undefined;
        break;
      case 'recovery':
        updatedMetrics.recovery = undefined;
        break;
      case 'hrv':
        updatedMetrics.hrv = undefined;
        break;
      case 'sleep_score':
        updatedMetrics.sleep_score = undefined;
        break;
      case 'vo2_max':
        updatedMetrics.vo2_max = undefined;
        break;
      case 'rem_sleep_minutes':
        updatedMetrics.rem_sleep_minutes = undefined;
        break;
      case 'deep_sleep_minutes':
        updatedMetrics.deep_sleep_minutes = undefined;
        break;
      case 'subjective_tiredness':
        updatedMetrics.subjective_tiredness = undefined;
        break;
    }

    submitMetricsMutation.mutate(updatedMetrics, {
      onSuccess: async () => {
        // Refresh the data after successful clear
        await refreshData();
        setConfirmClearOpen(false);
        setDialogOpen(false);
        setEditingMetric(null);
      },
      onError: (error) => {
        enqueueSnackbar('Failed to clear metric. Please try again.', { variant: 'error' });
        setConfirmClearOpen(false);
      }
    });
  };

  const handleClearTest = () => {
    if (!editingProtocol) return;

    // Create updated test result with PENDING status and no result value
    const updatedTestResult: Omit<
      UserTestResult,
      'id' | 'keycloak_id' | 'created_at' | 'updated_at'
    > = {
      week_start_timestamp: new Date(getCurrentWeekStart()),
      test_name: editingProtocol.test_name,
      status: 'PENDING',
      result_value: undefined,
    };

    submitTestMutation.mutate(updatedTestResult, {
      onSuccess: async () => {
        // Refresh the data after successful clear
        await refreshData();
        setConfirmClearOpen(false);
        // Clear the test value since the test was cleared
        setTestValue('');
        // Keep dialog open to allow re-entry
        // setDialogOpen(false);
        // setEditingProtocol(null);
      },
      onError: (error) => {
        enqueueSnackbar('Failed to clear test. Please try again.', { variant: 'error' });
        setConfirmClearOpen(false);
      }
    });
  };

  const renderCompactGrid = () => {
    if (type === 'daily') {
      return (
        <Grid container spacing={1} sx={{ height: '100%' }}>
          {dailyMetricsConfig.map((metric, index) => {
            return (
              <Grid size={{ xs: 6 }} key={index} sx={{ height: '50%' }}>
                <motion.div
                  whileHover={{ 
                    y: -4, 
                    scale: 1.02,
                    transition: { duration: 0.3, ease: "easeOut" }
                  }}
                  whileTap={{ 
                    y: -2, 
                    scale: 0.98,
                    transition: { duration: 0.1 }
                  }}
                  style={{ height: '100%' }}
                >
                  <GameSubCard
                    className={`${GAME_CLASSES.cursorPointer} ${GAME_CLASSES.hoverOpacity80}`}
                    onClick={() => handleEditDailyMetric(metric.key)}
                    sx={{ 
                      height: '100%',
                      transition: 'box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                      '&:hover': {
                        boxShadow: '0 8px 25px rgba(0, 188, 212, 0.3)',
                        '& .quest-icon': {
                          filter: 'drop-shadow(0 0 8px rgba(0, 188, 212, 0.6))',
                        },
                        '& .quest-text': {
                          color: '#00bcd4',
                          textShadow: '0 0 8px rgba(0, 188, 212, 0.5)',
                        }
                      }
                    }}
                  >
                  <CardContent
                    className={`${GAME_CLASSES.padding2} ${GAME_CLASSES.height100} ${GAME_CLASSES.flex} ${GAME_CLASSES.flexColumn} ${GAME_CLASSES.justifyCenter}`}
                  >
                    <Stack spacing={0.1} alignItems="center" className={GAME_CLASSES.rowGap0}>
                      <motion.div
                        className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan} ${GAME_CLASSES.textBold} quest-icon`}
                        whileHover={{ 
                          scale: 1.1, 
                          rotate: 5,
                          transition: { duration: 0.3, ease: "easeOut" }
                        }}
                      >
                        {metric.icon}
                      </motion.div>
                      <GameText 
                        variant="caption" 
                        className={`${GAME_CLASSES.textBold} ${GAME_CLASSES.textCenter} quest-text`}
                        sx={{ 
                          wordBreak: 'break-word',
                          hyphens: 'auto',
                          lineHeight: 1.2,
                          fontSize: '0.75rem',
                          marginTop: 0,
                          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                        }}
                      >
                        {metric.label}
                      </GameText>
                    </Stack>
                  </CardContent>
                  </GameSubCard>
                </motion.div>
              </Grid>
            );
          })}
        </Grid>
      );
    } else {
      return (
        <Grid container spacing={1} sx={{ height: '100%' }}>
          {testProtocols.slice(0, 4).map((protocol, index) => {
            return (
              <Grid size={{ xs: 6 }} key={index} sx={{ height: '50%' }}>
                <motion.div
                  whileHover={{ 
                    y: -4, 
                    scale: 1.02,
                    transition: { duration: 0.3, ease: "easeOut" }
                  }}
                  whileTap={{ 
                    y: -2, 
                    scale: 0.98,
                    transition: { duration: 0.1 }
                  }}
                  style={{ height: '100%' }}
                >
                  <GameSubCard
                    className={`${GAME_CLASSES.cursorPointer} ${GAME_CLASSES.hoverOpacity80}`}
                    onClick={() => handleEditWeeklyTest(protocol)}
                    sx={{ 
                      height: '100%',
                      transition: 'box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                      '&:hover': {
                        boxShadow: '0 8px 25px rgba(0, 188, 212, 0.3)',
                        '& .quest-icon': {
                          filter: 'drop-shadow(0 0 8px rgba(0, 188, 212, 0.6))',
                        },
                        '& .quest-text': {
                          color: '#00bcd4',
                          textShadow: '0 0 8px rgba(0, 188, 212, 0.5)',
                        }
                      }
                    }}
                  >
                  <CardContent
                    className={`${GAME_CLASSES.padding2} ${GAME_CLASSES.height100} ${GAME_CLASSES.flex} ${GAME_CLASSES.flexColumn} ${GAME_CLASSES.justifyCenter}`}
                  >
                    <Stack spacing={0.1} alignItems="center" className={GAME_CLASSES.rowGap0}>
                      <motion.div
                        className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan} ${GAME_CLASSES.textBold} quest-icon`}
                        whileHover={{ 
                          scale: 1.1, 
                          rotate: -5,
                          transition: { duration: 0.3, ease: "easeOut" }
                        }}
                      >
                        {getIconForProtocol(protocol.test_name)}
                      </motion.div>
                      <GameText 
                        variant="caption" 
                        className={`${GAME_CLASSES.textBold} ${GAME_CLASSES.textCenter} quest-text`}
                        sx={{ 
                          wordBreak: 'break-word',
                          hyphens: 'auto',
                          lineHeight: 1.2,
                          fontSize: '0.75rem',
                          marginTop: 0,
                          transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                        }}
                      >
                        {protocol.display_name}
                      </GameText>
                    </Stack>
                  </CardContent>
                  </GameSubCard>
                </motion.div>
              </Grid>
            );
          })}
        </Grid>
      );
    }
  };

  // Centralized logic for determining if data was already recorded
  const today = new Date();
  const recordedDate = currentMetrics?.updated_at ? 
    new Date(currentMetrics.updated_at.getFullYear(), currentMetrics.updated_at.getMonth(), currentMetrics.updated_at.getDate()) : null;
  const todayDate = new Date(today.getFullYear(), today.getMonth(), today.getDate());
  const isRecordedToday = recordedDate ? recordedDate.getTime() === todayDate.getTime() : false;
  
  // Helper function to check if a specific metric was recorded today
  const isMetricRecordedToday = (metricKey: string): boolean => {
    if (!isRecordedToday) return false;
    
    const metricValue = currentMetrics?.[metricKey as keyof typeof currentMetrics];
    // A metric is considered "recorded" if it has a non-null, non-undefined value
    // If it was cleared (null/undefined), it's not considered recorded and can be re-entered
    return metricValue !== null && metricValue !== undefined;
  };
  
  // Centralized logic for weekly tests
  const testResult = editingProtocol ? currentWeekTests.find(
    test => test.test_name === editingProtocol.test_name
  ) : null;
  const isRecordedThisWeek = testResult?.status === 'COMPLETED' && testResult?.result_value;

  return (
    <React.Fragment>
      <motion.div
        initial={{ opacity: 0, x: type === 'daily' ? 0 : -30 }}
        animate={{ opacity: 1, x: 0 }}
        transition={{ duration: 0.8, ease: 'easeOut' }}
        whileHover={{ y: -2 }}
        whileTap={{ y: -1 }}
        style={{ 
          height: '100%', 
          display: 'flex', 
          flexDirection: 'column',
          transition: 'box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        }}
      >
        <GameCard 
          className={GAME_CLASSES.width100} 
          sx={{ 
            height: '100%', 
            display: 'flex', 
            flexDirection: 'column',
            transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
            '&:hover': {
              boxShadow: '0 12px 40px rgba(0, 188, 212, 0.2)',
            }
          }}
        >
        <CardHeader
          className={GAME_CLASSES.paddingBottom0}
          title={
            <GameText 
              variant="h6" 
              textVariant="glow" 
              className={`${GAME_CLASSES.textBold} ${GAME_CLASSES.textTransformUppercase}`}
              sx={{
                textShadow: '0 0 10px rgba(0, 188, 212, 0.5)',
              }}
            >
              {getTitle()}
            </GameText>
          }
          subheader={
            <GameTextSecondary 
              variant="body2"
              sx={{
              }}
            >
              {getSubtitle()}
            </GameTextSecondary>
          }
        />
        <CardContent className={`${GAME_CLASSES.paddingTop1} ${GAME_CLASSES.flex1}`} sx={{ flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
          {renderCompactGrid()}
        </CardContent>
      </GameCard>
      </motion.div>

      {/* Metric Detail Dialog */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} maxWidth="md" fullWidth sx={{ '& .MuiDialog-paper': { overflow: 'visible' } }}>
        <DialogTitle>
          <span className={`${GAME_CLASSES.textBold} ${GAME_CLASSES.text}`}>
            {type === 'daily'
              ? dailyMetricsConfig.find(m => m.key === editingMetric)?.label
              : editingProtocol?.display_name}
          </span>
        </DialogTitle>
        <DialogContent sx={{ overflow: 'visible' }}>
          <Stack spacing={3} className={GAME_CLASSES.marginTop2} sx={{ overflow: 'visible' }}>
            {/* 30-day Chart */}
            {type === 'daily' && editingMetric ? (
              <MetricTrendChart
                metricLabel={
                  dailyMetricsConfig.find(m => m.key === editingMetric)?.label || editingMetric
                }
                metricUnit={dailyMetricsConfig.find(m => m.key === editingMetric)?.unit || ''}
                data={historicalMetrics
                  .map((metric) => {
                    const value = metric[editingMetric as keyof UserPerformanceMetrics] as number;
                    if (value !== undefined && value !== null) {
                      return {
                        date: metric.created_at,
                        value: value,
                      };
                    }
                    return null;
                  })
                  .filter((item): item is { date: Date; value: number } => item !== null)}
                isLoading={isLoadingHistorical}
                height={200}
              />
            ) : type === 'weekly' && editingProtocol ? (
              <MetricTrendChart
                metricLabel={editingProtocol.display_name}
                metricUnit={editingProtocol.unit}
                data={[
                  // Include current week's data if it exists
                  ...currentWeekTests
                    .filter(test => test.test_name === editingProtocol.test_name && test.status === 'COMPLETED' && test.result_value !== undefined)
                    .map((test) => ({
                      date: test.week_start_timestamp,
                      value: test.result_value!,
                    })),
                  // Include historical data (filter dynamically for the specific test protocol)
                  ...allHistoricalWeeklyTests
                    .filter(test => test.test_name === editingProtocol.test_name && test.status === 'COMPLETED' && test.result_value !== undefined)
                    .map((test) => ({
                      date: test.week_start_timestamp,
                      value: test.result_value!,
                    }))
                ]}
                isLoading={isLoadingHistoricalWeekly}
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
                  
                  // Get the actual database value for this specific metric
                  const databaseValue = currentMetrics?.[editingMetric as keyof UserPerformanceMetrics];
                  
                  // Format the database value for display (only show numeric values, not dates)
                  const displayValue = typeof databaseValue === 'number' ? databaseValue : 
                    (typeof databaseValue === 'string' ? databaseValue : '');
                  
                  return (
                    <Stack spacing={2}>
                      {isMetricRecordedToday(metric.key) ? (
                        <Alert 
                          severity="success"
                          sx={{
                            '& .MuiAlert-icon': {
                            }
                          }}
                        >
                          <GameText variant="body2">
                            ✅ Today&apos;s {metric.label.toLowerCase()} has already been recorded (
                            {displayValue}
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
                                    : metric.key === 'rem_sleep_minutes' || metric.key === 'deep_sleep_minutes'
                                      ? 0
                                      : 0,
                              max:
                                metric.key === 'strain'
                                  ? 21
                                  : metric.key === 'subjective_tiredness'
                                    ? 5
                                    : metric.key === 'rem_sleep_minutes' || metric.key === 'deep_sleep_minutes'
                                      ? 300
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

                  return (
                    <Stack spacing={2}>
                      {isRecordedThisWeek ? (
                        <Alert 
                          severity="success"
                          sx={{
                            '& .MuiAlert-icon': {
                            }
                          }}
                        >
                          <GameText variant="body2">
                            🏆 This week&apos;s {editingProtocol.display_name.toLowerCase()} has
                            already been recorded ({result} {editingProtocol.unit}). Check back next
                            week to record your next result!
                          </GameText>
                        </Alert>
                      ) : (
                        <Stack spacing={2}>
                          <GameText variant="subtitle2" className={GAME_CLASSES.textBold}>
                            Record This Week&apos;s {editingProtocol.display_name}
                          </GameText>

                          <Alert severity="info">{editingProtocol.description}</Alert>

                          <TextField
                            fullWidth
                            label={`Result (${editingProtocol.unit})`}
                            type="number"
                            value={testValue}
                            onChange={e => {
                              setTestValue(e.target.value);
                            }}
                            placeholder={`Enter your ${editingProtocol.test_name.toLowerCase()} result`}
                          />
                        </Stack>
                      )}
                    </Stack>
                  );
                })()}
              </Box>
            )}
          </Stack>
        </DialogContent>
        <DialogActions>
          {(() => {
            // Shared logic for determining if we should show submit/cancel buttons
            const isEditing = (type === 'daily' && editingMetric) || (type === 'weekly' && editingProtocol);
            
            if (!isEditing) {
              // Default: Just close button
              return (
                  <Button
                    onClick={() => {
                      setDialogOpen(false);
                      setEditingMetric(null);
                      setEditingProtocol(null);
                      setTestValue(''); // Clear test value when closing dialog
                    }}
                  >
                    Close
                  </Button>
              );
            }

            // Determine if we can submit - unified logic for both daily and weekly
            let canSubmit = false;
            let isPending = false;
            let onSubmit = () => {};

            if (type === 'daily' && editingMetric) {
              // Always show submit button, but disable if already recorded today
              canSubmit = !isMetricRecordedToday(editingMetric);
              isPending = submitMetricsMutation.isPending;
              onSubmit = handleSubmitDailyMetric;
            } else if (type === 'weekly' && editingProtocol) {
              // Can submit if the test hasn't been completed yet (regardless of testValue)
              canSubmit = !isRecordedThisWeek;
              isPending = submitTestMutation.isPending;
              onSubmit = handleSubmitTest;
            }

            // Show clear button on left, cancel/submit buttons on right
            return (
              <Box sx={{ display: 'flex', justifyContent: 'space-between', width: '100%' }}>
                <Box>
                  {type === 'daily' && editingMetric && isMetricRecordedToday(editingMetric) && (
                    <Button
                      onClick={() => setConfirmClearOpen(true)}
                      variant="contained"
                      color="error"
                      disabled={isPending}
                    >
                      Clear
                    </Button>
                  )}
                  {type === 'weekly' && editingProtocol && isRecordedThisWeek && (
                    <Button
                      onClick={() => setConfirmClearOpen(true)}
                      variant="contained"
                      color="error"
                      disabled={isPending}
                    >
                      Clear
                    </Button>
                  )}
                </Box>
                <Box sx={{ display: 'flex', gap: 1 }}>
                  <Button
                    onClick={() => {
                      setDialogOpen(false);
                      setEditingMetric(null);
                      setEditingProtocol(null);
                      setTestValue(''); // Clear test value when canceling
                    }}
                    variant="outlined"
                    className={GAME_CLASSES.colorCyan}
                  >
                    Cancel
                  </Button>
                  <motion.div
                    whileHover={!isPending && canSubmit ? { 
                      y: -2,
                      transition: { duration: 0.3, ease: "easeOut" }
                    } : {}}
                    whileTap={!isPending && canSubmit ? { 
                      y: 0,
                      transition: { duration: 0.1 }
                    } : {}}
                  >
                    <Button
                      onClick={onSubmit}
                      variant="contained"
                      disabled={!canSubmit || isPending}
                      className={`${GAME_CLASSES.backgroundColorCyan} ${GAME_CLASSES.hoverBackgroundColorCyan}`}
                      sx={{
                        transition: 'box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
                        '&:hover:not(:disabled)': {
                          boxShadow: '0 8px 25px rgba(0, 188, 212, 0.4)',
                        },
                        '&:disabled': {
                          opacity: 0.6,
                        }
                      }}
                    >
                      {isPending ? 'Submitting...' : 'Submit'}
                    </Button>
                  </motion.div>
                </Box>
              </Box>
            );
          })()}
        </DialogActions>
      </Dialog>

      {/* Clear Confirmation Dialog */}
      <Dialog open={confirmClearOpen} onClose={() => setConfirmClearOpen(false)}>
        <DialogTitle>
          <span className={`${GAME_CLASSES.textBold} ${GAME_CLASSES.text}`}>
            Clear {type === 'daily' 
              ? dailyMetricsConfig.find(m => m.key === editingMetric)?.label
              : editingProtocol?.display_name}?
          </span>
        </DialogTitle>
        <DialogContent>
          <GameText variant="body1">
            Are you sure you want to clear the recorded value for{' '}
            {type === 'daily' 
              ? dailyMetricsConfig.find(m => m.key === editingMetric)?.label.toLowerCase()
              : editingProtocol?.display_name.toLowerCase()}?
            This action cannot be undone.
          </GameText>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setConfirmClearOpen(false)}
            variant="outlined"
            className={GAME_CLASSES.colorCyan}
          >
            Cancel
          </Button>
          <Button
            onClick={type === 'daily' ? handleClearMetric : handleClearTest}
            variant="contained"
            color="error"
            disabled={type === 'daily' ? submitMetricsMutation.isPending : submitTestMutation.isPending}
          >
            Clear
          </Button>
        </DialogActions>
      </Dialog>
    </React.Fragment>
  );
};
