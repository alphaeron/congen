import { Grid, Stack } from '@mui/material';
import React from 'react';

import { AdventurerStatusCard } from './AdventurerStatusCard';
import { CompactQuestCard } from './CompactQuestCard';
import { LoadingSpinner } from './LoadingSpinner';
import { GAME_CLASSES } from './GameTheme';
import type { User } from '../api/types';
import { useData } from '../contexts/DataContext';

interface DashboardOverviewProps {
  user: User;
}

/**
 * Dashboard overview component displaying user progress and statistics.
 *
 * Shows user progress over time, 1RM graphs, exercise trends,
 * and key statistics like volume, frequency, and PRs.
 *
 * @param user The user data to display
 * @return Dashboard overview component
 */
export const DashboardOverview: React.FC<DashboardOverviewProps> = ({ user }) => {
  const {
    isLoading: isDataLoading,
    performanceScores,
    performanceMetrics,
    weeklyTests,
    refreshPerformanceData,
    isReady,
  } = useData();

  if (!isReady || isDataLoading) {
    return <LoadingSpinner message="Loading dashboard..." fullHeight={false} />;
  }

  return (
    <React.Fragment>
      {/* Main Dashboard Layout: 3/4 Status Card + 1/4 Sidebar */}
      {performanceScores ? (
        <Grid container spacing={3} className={GAME_CLASSES.marginBottom3}>
          <Grid size={{ xs: 12, lg: 9 }}>
          {/* Status Card - 3/4 width */}
            <AdventurerStatusCard
              scores={performanceScores}
              metrics={performanceMetrics}
              weeklyTests={weeklyTests}
              userName={user.name}
            />
          </Grid>

          {/* Sidebar - 1/4 width */}
          <Grid size={{ xs: 12, lg: 3 }}>
            <Stack spacing={1}>
              {/* Daily Quests */}
              <CompactQuestCard type="daily" currentMetrics={performanceMetrics || undefined} />
              {/* Weekly Quests */}
              <CompactQuestCard
                type="weekly"
                weeklyTests={weeklyTests}
                onTestUpdate={refreshPerformanceData}
              />
            </Stack>
          </Grid>
        </Grid>
      ) : null}
    </React.Fragment>
  );
};
