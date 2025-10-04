import Box from '@mui/material/Box';
import { alpha } from '@mui/material/styles';
import * as React from 'react';
import { motion, useInView } from 'framer-motion';

import { GameText, GameCard } from './GameTheme';
import { AdventurerStatusCard } from './AdventurerStatusCard';
import type { UserPerformanceScores, UserPerformanceMetrics, UserTestResult } from '../api/types';

// Sample data for AdventurerStatusCard demonstration
// Calculated using backend logic: average score = 73.3, tanh scaling = level 15
const samplePerformanceScores: UserPerformanceScores = {
  id: 1,
  keycloak_id: 'sample-user',
  explosiveness_score: 75.5,  // 60+ = "Quick Burst" skill
  aerobic_capacity_score: 68.2,  // 60+ = "Endurance Runner" skill  
  recovery_score: 82.1,  // 80+ = "Rapid Recovery" skill
  reaction_time_score: 71.8,  // 60+ = "Quick Response" skill
  mobility_score: 65.4,  // 60+ = "Agile Movement" skill
  strength_score: 78.9,  // 60+ = "Strong Lifter" skill
  wilks_score: 285.6,
  level: 15,  // Calculated: tanh((73.3-50)/15) + 1) * 50 = 15
  level_change_reason: 'weekly_test_completed',
  hp: 85.2,
  hp_loss: 12.3,
  mp: 78.7,
  mp_loss: 8.1,
  fatigue: 45.6,
  fatigue_loss: 15.2,
  skills: ['Quick Burst', 'Endurance Runner', 'Rapid Recovery', 'Quick Response', 'Agile Movement', 'Strong Lifter'],
  created_at: new Date()
};

const samplePerformanceMetrics: UserPerformanceMetrics = {
  keycloak_id: 'sample-user',
  vo2_max: 42.5,
  hrv: 45.2,
  sleep_score: 78.3,
  strain: 12.1,
  recovery: 85.6,
  created_at: new Date(),
  updated_at: new Date()
};

const sampleWeeklyTests: UserTestResult[] = [
  {
    id: 1,
    keycloak_id: 'sample-user',
    week_start_timestamp: new Date(),
    test_name: 'vertical_jump',
    status: 'COMPLETED',
    result_value: 52.3,
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    id: 2,
    keycloak_id: 'sample-user',
    week_start_timestamp: new Date(),
    test_name: 'hr_recovery',
    status: 'COMPLETED',
    result_value: 28,
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    id: 3,
    keycloak_id: 'sample-user',
    week_start_timestamp: new Date(),
    test_name: 'reflex',
    status: 'COMPLETED',
    result_value: 285,
    created_at: new Date(),
    updated_at: new Date()
  },
  {
    id: 4,
    keycloak_id: 'sample-user',
    week_start_timestamp: new Date(),
    test_name: 'mobility',
    status: 'COMPLETED',
    result_value: 78.5,
    created_at: new Date(),
    updated_at: new Date()
  }
];

// Advanced animation variants with 3D effects and micro-interactions
const sectionVariants = {
  hidden: { 
    opacity: 0, 
    y: 60, 
    scale: 0.95,
    rotateX: -10,
    transformPerspective: 1000,
  },
  visible: {
    opacity: 1,
    y: 0,
    scale: 1,
    rotateX: 0,
    transition: {
      duration: 1.0,
      ease: [0.25, 0.46, 0.45, 0.94] as const,
    },
  },
};

const staggerVariants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.25,
      delayChildren: 0.4,
    },
  },
};

const itemVariants = {
  hidden: { 
    y: 30, 
    opacity: 0, 
    scale: 0.9,
    rotateY: -15,
  },
  visible: {
    y: 0,
    opacity: 1,
    scale: 1,
    rotateY: 0,
    transition: {
      duration: 0.8,
      ease: [0.25, 0.46, 0.45, 0.94] as const,
    },
  },
};

// Advanced card hover variants with 3D tilt
const cardHoverVariants = {
  rest: { 
    scale: 1, 
    rotateX: 0, 
    rotateY: 0,
    z: 0,
  },
  hover: { 
    scale: 1.03, 
    rotateX: 3, 
    rotateY: 3,
    z: 15,
    transition: {
      type: "spring" as const,
      stiffness: 300,
      damping: 20,
    },
  },
};

export function GamificationSection() {
  const ref = React.useRef(null);
  const isInView = useInView(ref, { once: true, margin: "-100px" });
  return (
    <motion.div>
      <motion.div
        ref={ref}
        variants={sectionVariants}
        initial="hidden"
        animate={isInView ? "visible" : "hidden"}
      >
        <Box
          id="gamification"
                  sx={{
                    position: 'relative',
                    background: 'rgba(255, 255, 255, 0.02)',
                    backdropFilter: 'blur(20px)',
                    border: theme => `1px solid ${alpha(theme.palette.primary.main, 0.1)}`,
                    width: '100vw',
                    height: '100vh',
                    marginLeft: 'calc(-50vw + 50%)',
                    marginTop: 0,
                    marginBottom: 0,
                    px: { xs: 2, sm: 4, md: 6 },
                    display: 'flex',
                    alignItems: 'center',
            '&::before': {
              content: '""',
              position: 'absolute',
              top: 0,
              left: 0,
              right: 0,
              bottom: 0,
              background: theme =>
                `radial-gradient(circle at 20% 20%, ${alpha('#8b5cf6', 0.025)} 0%, transparent 50%),
                 radial-gradient(circle at 80% 80%, ${alpha('#f97316', 0.020)} 0%, transparent 50%),
                 linear-gradient(45deg, transparent 30%, ${alpha('#22c55e', 0.015)} 50%, transparent 70%)`,
              backgroundSize: '400px 400px, 600px 600px, 200px 200px',
              zIndex: 0,
              animation: 'rpgFlow 20s linear infinite',
              '@keyframes rpgFlow': {
                '0%': { transform: 'translateX(0) translateY(0)' },
                '100%': { transform: 'translateX(100px) translateY(100px)' },
              },
            },
          }}
        >
          <Box
            sx={{
              display: 'flex',
              flexDirection: { xs: 'column', lg: 'row-reverse' },
              alignItems: 'center',
              gap: { xs: 4, lg: 8 },
              width: '100%',
              position: 'relative',
              zIndex: 1,
            }}
          >
            <motion.div
              variants={staggerVariants}
              style={{
                flex: 1,
                textAlign: 'center',
              }}
            >
              <motion.div variants={itemVariants}>
                <GameText
                  variant="h2"
                  textVariant="glow"
                  sx={{
                    fontWeight: 700,
                    mb: 3,
                    fontSize: { xs: '2rem', sm: '2.5rem', md: '3rem' },
                    background: 'linear-gradient(135deg, #8b5cf6, #f97316)',
                    backgroundClip: 'text',
                    WebkitBackgroundClip: 'text',
                    WebkitTextFillColor: 'transparent',
                  }}
                >
                  RPG-Style Gamification
                </GameText>
              </motion.div>
              
              <motion.div variants={itemVariants}>
                <GameText
                  variant="h5"
                  textVariant="secondary"
                  sx={{
                    fontWeight: 400,
                    mb: 4,
                    lineHeight: 1.6,
                    opacity: 0.9,
                    fontSize: { xs: '1.1rem', sm: '1.25rem' },
                  }}
                >
                  Transform your fitness journey into an epic RPG adventure. Level up your 
                  character, unlock skills, and master the HP/MP/Fatigue 
                  system for maximum motivation.
                </GameText>
              </motion.div>
              
              <motion.div variants={staggerVariants}>
                <Box
                  sx={{
                    display: 'flex',
                    flexDirection: { xs: 'column', sm: 'row' },
                    gap: 2,
                    flexWrap: 'wrap',
                    justifyContent: 'center',
                  }}
                >
                  {[
                    'Level Progression',
                    'Performance Tracking', 
                    'Skill System',
                    'HP/MP Mechanics'
                  ].map((feature, index) => (
                    <motion.div 
                      key={index} 
                      variants={itemVariants}
                      initial="rest"
                      whileHover="hover"
                      style={{
                        transformStyle: 'preserve-3d',
                        perspective: 1000,
                      }}
                    >
                      <GameCard
                        sx={{
                          p: 3,
                          background: 'rgba(139, 92, 246, 0.05)',
                          backdropFilter: 'blur(20px)',
                          border: theme => `2px solid ${alpha('#8b5cf6', 0.2)}`,
                          flex: '1 1 auto',
                          minWidth: '200px',
                          transition: 'all 0.3s ease',
                          boxShadow: theme => `0 8px 32px ${alpha('#8b5cf6', 0.1)}`,
                          '&:hover': {
                            boxShadow: theme => `0 20px 40px ${alpha('#8b5cf6', 0.3)}`,
                            border: theme => `2px solid ${alpha('#8b5cf6', 0.4)}`,
                          },
                        }}
                      >
                        <GameText
                          variant="body1"
                          textVariant="glow"
                          sx={{ 
                            fontWeight: 600,
                            color: '#8b5cf6',
                          }}
                        >
                          {feature}
                        </GameText>
                      </GameCard>
                    </motion.div>
                  ))}
                </Box>
              </motion.div>
            </motion.div>
            
            <motion.div
              variants={itemVariants}
              style={{
                flex: 1,
                minHeight: 600,
                maxHeight: 800,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <Box sx={{ maxWidth: 800, width: '100%' }}>
                <motion.div
                  whileHover={{ scale: 1.02 }}
                  transition={{ duration: 0.3 }}
                >
                  <AdventurerStatusCard
                    scores={samplePerformanceScores}
                    metrics={samplePerformanceMetrics}
                    weeklyTests={sampleWeeklyTests}
                    userName="Alex Chen"
                  />
                </motion.div>
              </Box>
            </motion.div>
          </Box>
        </Box>
      </motion.div>
    </motion.div>
  );
}
