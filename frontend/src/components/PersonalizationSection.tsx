import Box from '@mui/material/Box';
import { alpha } from '@mui/material/styles';
import * as React from 'react';
import { motion, useInView } from 'framer-motion';

import { GameText, GameCard } from './GameTheme';
import { CycleDiagramReact as CycleDiagram } from './CycleDiagramReact';

// Personalization cycle data
const personalizationNodes = [
  { id: 'generation', label: 'Generate Workouts', details: ['Conjugate Workout Generation'] },
  { id: 'personalization', label: 'Workout Personalization', details: ['One Rep Max Integration'] },
  { id: 'tracking', label: 'Workout Tracking', details: ['Record Performance'] },
  { id: 'performance', label: 'Performance Tracking', details: ['Calculate Scores', 'Leveling Progression'] }
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

export function PersonalizationSection() {
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
          id="personalization"
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
                `radial-gradient(circle at 20% 20%, ${alpha('#10b981', 0.015)} 0%, transparent 50%),
                 radial-gradient(circle at 80% 80%, ${alpha('#3b82f6', 0.012)} 0%, transparent 50%),
                 linear-gradient(45deg, transparent 30%, ${alpha('#8b5cf6', 0.008)} 50%, transparent 70%)`,
              backgroundSize: '400px 400px, 600px 600px, 200px 200px',
              zIndex: 0,
              animation: 'personalizationFlow 25s linear infinite',
              '@keyframes personalizationFlow': {
                '0%': { transform: 'translateX(0) translateY(0)' },
                '100%': { transform: 'translateX(-150px) translateY(-150px)' },
              },
            },
          }}
        >
          <Box
            sx={{
              display: 'flex',
              flexDirection: { xs: 'column', lg: 'row' },
              alignItems: { xs: 'center', lg: 'flex-start' },
              gap: { xs: 2, lg: 3 },
              minHeight: { xs: 'auto', lg: '60vh' },
              width: '100%',
              position: 'relative',
              zIndex: 1,
            }}
          >
            <Box
              sx={{
                flex: { xs: 1, lg: 0.5 },
                textAlign: { xs: 'center', lg: 'left' },
              }}
            >
              <GameText
                variant="h2"
                textVariant="glow"
                sx={{
                  fontWeight: 700,
                  mb: 3,
                  fontSize: { xs: '2rem', sm: '2.5rem', md: '3rem' },
                }}
              >
                Smart Personalization
              </GameText>
              <GameText
                variant="h5"
                textVariant="secondary"
                sx={{
                  fontWeight: 400,
                  mb: 4,
                  lineHeight: 1.6,
                  opacity: 0.9,
                }}
              >
                Every aspect of your program is algorithmically tailored to your equipment, 
                goals, experience level, and physical attributes. Real-time adjustments 
                ensure continuous optimization for maximum results.
              </GameText>
              <Box
                sx={{
                  display: 'flex',
                  flexDirection: { xs: 'column', sm: 'row' },
                  gap: 2,
                  flexWrap: 'wrap',
                }}
              >
                {[
                  'Equipment Matching',
                  'Time Optimization', 
                  'Real-time Updates',
                  'Continuous Learning'
                ].map((feature, index) => (
                  <motion.div
                    key={index}
                    initial="rest"
                    whileHover="hover"
                    variants={cardHoverVariants}
                    style={{
                      transformStyle: 'preserve-3d',
                      perspective: 1000,
                      flex: '1 1 auto',
                      minWidth: '200px',
                    }}
                  >
                    <GameCard
                      sx={{
                        p: 2,
                        background: 'rgba(34, 197, 94, 0.05)',
                        backdropFilter: 'blur(20px)',
                        border: theme => `1px solid ${alpha('#22c55e', 0.2)}`,
                        boxShadow: theme => `0 8px 32px ${alpha('#22c55e', 0.1)}`,
                        transition: 'all 0.3s ease',
                        '&:hover': {
                          boxShadow: theme => `0 20px 40px ${alpha('#22c55e', 0.3)}`,
                          border: theme => `1px solid ${alpha('#22c55e', 0.4)}`,
                        },
                      }}
                    >
                      <GameText
                        variant="body1"
                        textVariant="glow"
                        sx={{ fontWeight: 600 }}
                      >
                        {feature}
                      </GameText>
                    </GameCard>
                  </motion.div>
                ))}
              </Box>
            </Box>
            
            <Box
              sx={{
                flex: { xs: 1, lg: 1.5 },
                minWidth: { xs: 200, sm: 300, md: 400 },
                width: '100%',
                height: '100%',
                aspectRatio: '1 / 1',
                maxHeight: '80vh',
                overflow: 'visible',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
              }}
            >
              <CycleDiagram 
                nodes={personalizationNodes} 
              />
            </Box>
          </Box>
        </Box>
      </motion.div>
    </motion.div>
  );
}
