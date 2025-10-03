import { CardContent, CardHeader, Box, Stack, Grid } from '@mui/material';
import React, { useEffect, useRef, useState } from 'react';
import { motion, useMotionValue, useTransform, useAnimation } from 'framer-motion';

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
  userName: string;
}

export const AdventurerStatusCard: React.FC<AdventurerStatusCardProps> = ({
  scores,
  metrics,
  weeklyTests,
  userName,
}) => {
  // State for skills section viewport detection
  const [isSkillsInView, setIsSkillsInView] = useState(false);
  const skillsRef = useRef<HTMLDivElement>(null);

  // State for level slot machine animation
  const [displayedLevel, setDisplayedLevel] = useState(1);
  const [isLevelAnimating, setIsLevelAnimating] = useState(false);
  const [isLevelInView, setIsLevelInView] = useState(false);
  const levelRef = useRef<HTMLDivElement>(null);

  // State for profile section viewport detection
  const [isProfileInView, setIsProfileInView] = useState(false);
  const profileRef = useRef<HTMLDivElement>(null);

  // Intersection Observer for skills section
  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsSkillsInView(true);
        }
      },
      { threshold: 0.9 }
    );

    if (skillsRef.current) {
      observer.observe(skillsRef.current);
    }

    return () => {
      if (skillsRef.current) {
        observer.unobserve(skillsRef.current);
      }
    };
  }, []);

  // Intersection Observer for level section
  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsLevelInView(true);
        }
      },
      { threshold: 0.9 }
    );

    if (levelRef.current) {
      observer.observe(levelRef.current);
    }

    return () => {
      if (levelRef.current) {
        observer.unobserve(levelRef.current);
      }
    };
  }, []);

  // Intersection Observer for profile section
  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsProfileInView(true);
        }
      },
      { threshold: 0.9 }
    );

    if (profileRef.current) {
      observer.observe(profileRef.current);
    }

    return () => {
      if (profileRef.current) {
        observer.unobserve(profileRef.current);
      }
    };
  }, []);

  // Level slot machine animation
  useEffect(() => {
    if (isLevelInView && scores.level > 1 && !isLevelAnimating) {
      setIsLevelAnimating(true);
      
      const animateLevel = () => {
        let currentLevel = 1;
        const targetLevel = scores.level;
        const increment = Math.max(1, Math.ceil((targetLevel - 1) / 20)); // Adjust speed based on level
        const delay = Math.max(50, 200 - (targetLevel * 5)); // Faster for higher levels
        
        // Set initial level to 1
        setDisplayedLevel(1);
        
        const timer = setInterval(() => {
          currentLevel += increment;
          if (currentLevel >= targetLevel) {
            currentLevel = targetLevel;
            setDisplayedLevel(currentLevel);
            clearInterval(timer);
            setIsLevelAnimating(false);
          } else {
            setDisplayedLevel(currentLevel);
          }
        }, delay);
      };

      // Start animation after the fade-in completes (0.5s delay for fade-in + 0.3s for entrance)
      setTimeout(animateLevel, 800);
    } else if (isLevelInView && scores.level === 1) {
      setDisplayedLevel(1);
    }
  }, [isLevelInView, scores.level]);

  return (
    <GameCard 
      className={GAME_CLASSES.overflowVisible}
      interactive={true}
      sx={{
        opacity: 0,
        transform: 'translateY(20px) scale(0.98)',
        animation: 'cardEntrance 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94) forwards',
        '@keyframes cardEntrance': {
          '0%': {
            opacity: 0,
            transform: 'translateY(20px) scale(0.98)',
          },
          '100%': {
            opacity: 1,
            transform: 'translateY(0) scale(1)',
          },
        },
      }}
    >
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
        <motion.div
          ref={profileRef}
          initial={{ opacity: 0, y: 15 }}
          animate={isProfileInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 15 }}
          transition={{ duration: 0.3, delay: 0.1, ease: "easeOut" }}
        >
          <Grid container spacing={1} mb={1} overflow="visible">
            <Grid size={12}>
              <Stack direction="column" alignItems="center">
                <Stack direction="row" alignItems="flex-start" spacing={2}>
                  <motion.div
                    initial={{ opacity: 0, scale: 0.95 }}
                    animate={isProfileInView ? { opacity: 1, scale: 1 } : { opacity: 0, scale: 0.95 }}
                    transition={{ duration: 0.25, delay: 0.2, ease: "easeOut" }}
                  >
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
                  </motion.div>
                  <motion.div
                    ref={levelRef}
                    initial={{ opacity: 0, scale: 0.95 }}
                    animate={isLevelInView ? { opacity: 1, scale: 1 } : { opacity: 0, scale: 0.95 }}
                    transition={{ duration: 0.5, delay: 0.3, ease: "easeOut" }}
                  >
                    <Stack direction="column" alignItems="center" className={GAME_CLASSES.rowGap0}>
                      <motion.div
                        key={displayedLevel}
                        initial={{ y: 20, opacity: 0.7 }}
                        animate={isLevelInView ? { y: 0, opacity: 1 } : { y: 20, opacity: 0.7 }}
                        transition={{ 
                          duration: 0.1, 
                          ease: "easeOut",
                          type: "spring",
                          stiffness: 300,
                          damping: 20
                        }}
                      >
                        <GameText
                          variant="h2"
                          className={`${GAME_CLASSES.fontSize3rem} ${GAME_CLASSES.textBold} ${GAME_CLASSES.colorCyan} ${GAME_CLASSES.lineHeight1} ${GAME_CLASSES.marginTop16}`}
                        >
                          {displayedLevel}
                        </GameText>
                      </motion.div>
                      <GameText
                        variant="h6"
                        className={`${GAME_CLASSES.colorLightGray} ${GAME_CLASSES.textShadowGlow5} ${GAME_CLASSES.marginTopNegative10}`}
                      >
                        LEVEL
                      </GameText>
                    </Stack>
                  </motion.div>
                </Stack>
              </Stack>
            </Grid>
          </Grid>
        </motion.div>

        {/* Status Bars - Centered under profile */}
        <Box
          sx={{
            opacity: 0,
            transform: 'translateY(15px)',
            animation: 'statusBarsEntrance 0.3s ease-out 0.4s forwards',
            '@keyframes statusBarsEntrance': {
              '0%': {
                opacity: 0,
                transform: 'translateY(15px)',
              },
              '100%': {
                opacity: 1,
                transform: 'translateY(0)',
              },
            },
          }}
        >
          <Grid container spacing={6} justifyContent="center" alignItems="center" mb={3}>
            <Grid size="auto">
              <GameProgressBar
                icon={
                  <CustomSvgIcon src={HealthIcon} alt="HP" className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`} />
                }
                label="HP"
                current={Math.max(0, scores.hp - scores.hp_loss)}
                max={scores.hp}
                color="#00bcd4"
                tooltip="HP (Health Points) - Long-term physical durability and structural integrity. Represents tissue wear and tear that accumulates over time. Recovers slowly (days to weeks)."
                animated={true}
                delay={500}
              />
            </Grid>
            <Grid size="auto">
              <GameProgressBar
                icon={
                  <CustomSvgIcon
                    src={MagicIcon}
                    alt="MP"
                    className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`}
                  />
                }
                label="MP"
                current={Math.max(0, scores.mp - scores.mp_loss)}
                max={scores.mp}
                color="#00bcd4"
                tooltip="MP (Magic Points) - Short-term neurological and cognitive energy. Represents CNS readiness and mental sharpness. Recovers overnight to 2-3 days."
                animated={true}
                delay={600}
              />
            </Grid>
            <Grid size="auto">
              <GameCircularProgressBar
                label="Fatigue"
                current={Math.max(0, scores.fatigue - scores.fatigue_loss)}
                max={scores.fatigue}
                color="#00bcd4"
                tooltip="Fatigue - Immediate performance inhibition and session-to-session exhaustion. Represents how drained you feel right now. Recovers in hours to 1-2 days."
                animated={true}
                delay={700}
              />
            </Grid>
          </Grid>
        </Box>

        {/* Skills and Radar Chart - Side by Side */}
        <motion.div
          ref={skillsRef}
          initial={{ opacity: 0, y: 15 }}
          animate={isSkillsInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 15 }}
          transition={{ duration: 0.3, ease: "easeOut" }}
        >
          <Grid container spacing={3} overflow="visible" justifyContent="center">
            {/* Skills Section - Left */}
            <Grid size={{ xs: 12, sm: 6, md: 5 }}>
              <Box>
                {/* Skills Header - Fade in first */}
                <motion.div
                  initial={{ opacity: 0, x: -15 }}
                  animate={isSkillsInView ? { opacity: 1, x: 0 } : { opacity: 0, x: -15 }}
                  transition={{ duration: 0.3, delay: 0.1, ease: "easeOut" }}
                >
                  <Stack 
                    direction="row" 
                    alignItems="center" 
                    spacing={1} 
                    mb={1}
                  >
                  <CustomSvgIcon
                    src={SkillsIcon}
                    alt="Skills"
                    className={`${GAME_CLASSES.fontSize32} ${GAME_CLASSES.colorCyan}`}
                    sx={{ fontSize: '32px' }}
                  />
                  <GameText
                    variant="h6"
                    className={`${GAME_CLASSES.textBold} ${GAME_CLASSES.textShadowGlow}`}
                  >
                    Skills
                  </GameText>
                  </Stack>
                </motion.div>
                 {scores.skills.length > 0 ? (
                   <Stack 
                     direction="row" 
                     spacing={1} 
                     justifyContent="center" 
                     flexWrap="wrap"
                     useFlexGap={true}
                   >
                     {scores.skills.map((skill, index) => (
                       <motion.div
                         key={index}
                         initial={{ opacity: 0, y: 10, scale: 0.95 }}
                         animate={isSkillsInView ? { opacity: 1, y: 0, scale: 1 } : { opacity: 0, y: 10, scale: 0.95 }}
                         transition={{ 
                           duration: 0.25, 
                           delay: 0.4 + (index * 0.1), 
                           ease: "easeOut" 
                         }}
                       >
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
                       </motion.div>
                     ))}
                   </Stack>
                ) : (
                  <motion.div
                    initial={{ opacity: 0, y: 10 }}
                    animate={isSkillsInView ? { opacity: 1, y: 0 } : { opacity: 0, y: 10 }}
                    transition={{ duration: 0.25, delay: 0.4, ease: "easeOut" }}
                  >
                    <GameTextSecondary 
                      variant="body2" 
                      className={`${GAME_CLASSES.textItalic} ${GAME_CLASSES.textCenter}`}
                    >
                      No skills unlocked yet. Complete quests to unlock skills!
                    </GameTextSecondary>
                  </motion.div>
                )}
              </Box>
            </Grid>

            {/* Performance Radar Chart - Right */}
            <Grid size={{ xs: 12, sm: 6, md: 7 }}>
              <Box
                sx={{
                  opacity: 0,
                  transform: 'translateX(15px)',
                  animation: 'radarChartEntrance 0.25s ease-out 1.0s forwards',
                  '@keyframes radarChartEntrance': {
                    '0%': {
                      opacity: 0,
                      transform: 'translateX(15px)',
                    },
                    '100%': {
                      opacity: 1,
                      transform: 'translateX(0)',
                    },
                  },
                }}
              >
                <PerformanceRadarChart
                  scores={scores}
                  metrics={metrics}
                  weeklyTests={weeklyTests}
                  title=""
                  height={300}
                />
              </Box>
            </Grid>
          </Grid>
        </motion.div>
      </CardContent>
    </GameCard>
  );
};
