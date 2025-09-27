import { CardContent, CardHeader, Box, Stack, Grid } from '@mui/material';
import React from 'react';

import { CustomSvgIcon } from './CustomSvgIcon';
import { GameProgressBar, GameCircularProgressBar } from './GameProgressBar';
import { GameCard, GameSkillChip, GameText, GameTextSecondary, GAME_CLASSES } from './GameTheme';
import { PerformanceRadarChart } from './PerformanceRadarChart';
import type { UserPerformanceScores, UserPerformanceMetrics, UserTestResult } from '../api/types';
import ProfileIcon from '../resources/profile-icon.svg';
import HealthIcon from '../resources/health-icon.svg';
import MagicIcon from '../resources/magic-icon.svg';
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
    <GameCard className={GAME_CLASSES.overflowVisible}>
      <CardHeader
        title={
          <Box className={GAME_CLASSES.textCenter}>
            <GameText
              variant="h3"
              textVariant="glow"
              className={GAME_CLASSES.textBold}
            >
              STATUS
            </GameText>
          </Box>
        }
        className={GAME_CLASSES.textCenter}
      />

      <CardContent className={GAME_CLASSES.paddingTop0}>
        {/* Profile Information - Centered under STATUS */}
        <Grid container spacing={1} mb={1} overflow="visible">
          <Grid size={12}>
            <Stack direction="column" alignItems="center">
              <Stack direction="row" alignItems="flex-start" spacing={2}>
                <Stack direction="column" alignItems="center" className={GAME_CLASSES.rowGap0}>
                  <CustomSvgIcon
                    src={ProfileIcon}
                    alt="Profile"
                    className={`${GAME_CLASSES.fontSize80} ${GAME_CLASSES.colorWhite}`}
                  />
                  <GameText
                    variant="h6"
                    className={`${GAME_CLASSES.marginTopNegative16} ${GAME_CLASSES.textShadowGlow5} ${GAME_CLASSES.textTransformUppercase}`}
                  >
                    {userName}
                  </GameText>
                </Stack>
                <Stack direction="column" alignItems="center" className={GAME_CLASSES.rowGap0}>
                  <GameText
                    variant="h2"
                    className={`${GAME_CLASSES.fontSize3rem} ${GAME_CLASSES.textBold} ${GAME_CLASSES.colorCyan} ${GAME_CLASSES.lineHeight1} ${GAME_CLASSES.marginTop16}`}
                  >
                    {scores.level}
                  </GameText>
                  <GameText
                    variant="h6"
                    className={`${GAME_CLASSES.colorLightGray} ${GAME_CLASSES.textShadowGlow5} ${GAME_CLASSES.marginTopNegative10}`}
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
                <CustomSvgIcon src={HealthIcon} alt="HP" className={`${GAME_CLASSES.fontSize40} ${GAME_CLASSES.colorCyan}`} />
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
                  src={MagicIcon}
                  alt="MP"
                  className={`${GAME_CLASSES.fontSize40} ${GAME_CLASSES.colorCyan}`}
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
                className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`}
              />
              <GameText
                variant="h6"
                className={`${GAME_CLASSES.textBold} ${GAME_CLASSES.textShadowGlow}`}
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
                          className={`${GAME_CLASSES.fontSize16} ${GAME_CLASSES.colorCyan}`}
                        />
                      }
                    />
                  </Grid>
                ))}
              </Grid>
            ) : (
              <GameTextSecondary variant="body2" className={`${GAME_CLASSES.textItalic} ${GAME_CLASSES.textCenter}`}>
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
