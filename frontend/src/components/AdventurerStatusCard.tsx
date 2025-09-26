import { CardContent, CardHeader, Box, Stack, Grid } from '@mui/material';
import React from 'react';

import { CustomSvgIcon } from './CustomSvgIcon';
import { GameProgressBar, GameCircularProgressBar } from './GameProgressBar';
import { GameCard, GameSkillChip, GameText, GameTextSecondary } from './GameTheme';
import { PerformanceRadarChart } from './PerformanceRadarChart';
import type { UserPerformanceScores, UserPerformanceMetrics, UserTestResult } from '../api/types';
import AdventurerProfileIcon from '../resources/adventurer-profile-icon.svg';
import HeartIcon from '../resources/heart-icon.svg';
import PotionBottleIcon from '../resources/potion-bottle-icon.svg';
import SkillsIcon from '../resources/skills-icon.svg';

interface AdventurerStatusCardProps {
  scores: UserPerformanceScores;
  metrics?: UserPerformanceMetrics | null;
  weeklyTests?: UserTestResult[] | null;
  wilksScore?: number | null;
  userName?: string;
}

export const AdventurerStatusCard: React.FC<AdventurerStatusCardProps> = ({
  scores,
  metrics,
  weeklyTests,
  wilksScore,
  userName = 'Raven Thornfield',
}) => {
  return (
    <GameCard sx={{ overflow: 'visible' }}>
      <CardHeader
        title={
          <Box sx={{ textAlign: 'center' }}>
            <GameText
              variant="h3"
              sx={{ fontWeight: 'bold', color: '#ffffff', textShadow: '0 0 3px #00bcd4' }}
            >
              STATUS
            </GameText>
          </Box>
        }
        sx={{ textAlign: 'center' }}
      />

      <CardContent sx={{ pt: 0 }}>
        {/* Profile Information - Centered under STATUS */}
        <Grid container spacing={1} mb={1} overflow="visible">
          <Grid size={12}>
            <Stack direction="column" alignItems="center">
              <Stack direction="row" alignItems="flex-start" spacing={2}>
                <Stack direction="column" alignItems="center" sx={{ rowGap: 0 }}>
                  <CustomSvgIcon
                    src={AdventurerProfileIcon}
                    alt="Profile"
                    sx={{ fontSize: 80, color: 'white' }}
                  />
                  <GameText
                    variant="h6"
                    sx={{
                      mt: '-16px',
                      color: '#ffffff',
                      textShadow: '0 0 5px #00bcd4',
                      textTransform: 'uppercase',
                    }}
                  >
                    {userName}
                  </GameText>
                </Stack>
                <Stack direction="column" alignItems="center" sx={{ rowGap: 0 }}>
                  <GameText
                    variant="h2"
                    sx={{
                      fontSize: '3rem',
                      fontWeight: 'bold',
                      color: '#00bcd4',
                      textShadow: '0 0 10px #00bcd4',
                      lineHeight: 1,
                      mt: '16px',
                    }}
                  >
                    {scores.level}
                  </GameText>
                  <GameText
                    variant="h6"
                    sx={{ color: '#e0e0e0', textShadow: '0 0 5px #00bcd4', mt: '-10px' }}
                  >
                    LEVEL
                  </GameText>
                </Stack>
              </Stack>
            </Stack>
          </Grid>
        </Grid>

        {/* Status Bars - Centered under profile */}
        <Grid container spacing={6} justifyContent="center" alignItems="center" mb={3}>
          <Grid size="auto">
            <GameProgressBar
              icon={
                <CustomSvgIcon src={HeartIcon} alt="HP" sx={{ fontSize: 40, color: '#00bcd4' }} />
              }
              label="HP"
              current={Math.max(0, scores.hp - scores.hp_loss)}
              max={scores.hp}
              color="#00bcd4"
              tooltip="HP (Health Points) - Long-term physical durability and structural integrity. Represents tissue wear and tear that accumulates over time. Recovers slowly (days to weeks)."
            />
          </Grid>
          <Grid size="auto">
            <GameProgressBar
              icon={
                <CustomSvgIcon
                  src={PotionBottleIcon}
                  alt="MP"
                  sx={{ fontSize: 40, color: '#00bcd4' }}
                />
              }
              label="MP"
              current={Math.max(0, scores.mp - scores.mp_loss)}
              max={scores.mp}
              color="#00bcd4"
              tooltip="MP (Magic Points) - Short-term neurological and cognitive energy. Represents CNS readiness and mental sharpness. Recovers overnight to 2-3 days."
            />
          </Grid>
          <Grid size="auto">
            <GameCircularProgressBar
              label="Fatigue"
              current={Math.max(0, scores.fatigue - scores.fatigue_loss)}
              max={scores.fatigue}
              color="#00bcd4"
              tooltip="Fatigue - Immediate performance inhibition and session-to-session exhaustion. Represents how drained you feel right now. Recovers in hours to 1-2 days."
            />
          </Grid>
        </Grid>

        {/* Skills and Radar Chart - Side by Side */}
        <Grid container spacing={3} overflow="visible" justifyContent="center">
          {/* Skills Section - Left */}
          <Grid size="auto">
            <Stack direction="row" alignItems="center" spacing={1} mb={1}>
              <CustomSvgIcon
                src={SkillsIcon}
                alt="Skills"
                sx={{ fontSize: 32, color: '#00bcd4' }}
              />
              <GameText
                variant="h6"
                sx={{ fontWeight: 'bold', color: '#ffffff', textShadow: '0 0 3px #00bcd4' }}
              >
                Skills
              </GameText>
            </Stack>
            {scores.skills.length > 0 ? (
              <Grid container spacing={1} justifyContent="center">
                {scores.skills.map((skill, index) => (
                  <Grid key={index} size="auto">
                    <GameSkillChip
                      label={skill}
                      size="small"
                      icon={
                        <CustomSvgIcon
                          src={SkillsIcon}
                          alt="Skill"
                          sx={{ fontSize: 16, color: '#00bcd4' }}
                        />
                      }
                    />
                  </Grid>
                ))}
              </Grid>
            ) : (
              <GameTextSecondary variant="body2" sx={{ fontStyle: 'italic', textAlign: 'center' }}>
                No skills unlocked yet. Complete quests to unlock skills!
              </GameTextSecondary>
            )}
          </Grid>

          {/* Performance Radar Chart - Right */}
          <Grid size="auto">
            <PerformanceRadarChart
              scores={scores}
              metrics={metrics}
              weeklyTests={weeklyTests}
              wilksScore={wilksScore}
              title=""
              height={300}
            />
          </Grid>
        </Grid>
      </CardContent>
    </GameCard>
  );
};
