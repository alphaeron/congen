import { Stack } from '@mui/material';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import Link from '@mui/material/Link';
import { alpha } from '@mui/material/styles';
import * as React from 'react';
import { motion, useScroll, useTransform } from 'framer-motion';

import { GameText, GameCard } from './GameTheme';

export function OpenSource() {
  const { scrollY } = useScroll();
  const y = useTransform(scrollY, [0, 300], [0, 0]);
  const opacity = useTransform(scrollY, [0, 300], [1, 0.8]);

  return (
    <motion.div
      style={{ y, opacity }}
    >
      <Box
        sx={{
          position: 'relative',
          width: '100vw',
          height: '100vh',
          marginLeft: 'calc(-50vw + 50%)',
          overflow: 'hidden',
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
              `linear-gradient(90deg, transparent 48%, ${alpha('#0ea5e9', 0.03)} 49%, ${alpha('#0ea5e9', 0.03)} 51%, transparent 52%),
               linear-gradient(0deg, transparent 48%, ${alpha('#f97316', 0.02)} 49%, ${alpha('#f97316', 0.02)} 51%, transparent 52%),
               radial-gradient(circle at 20% 20%, ${alpha('#22c55e', 0.04)} 1px, transparent 1px),
               radial-gradient(circle at 80% 80%, ${alpha('#8b5cf6', 0.03)} 1px, transparent 1px)`,
            backgroundSize: '80px 80px, 80px 80px, 40px 40px, 40px 40px',
            zIndex: 0,
            animation: 'codeFlow 18s linear infinite',
            '@keyframes codeFlow': {
              '0%': { transform: 'translateX(0) translateY(0)' },
              '100%': { transform: 'translateX(80px) translateY(80px)' },
            },
          },
          '&::after': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: theme =>
              `linear-gradient(45deg, transparent 30%, ${alpha('#0ea5e9', 0.05)} 50%, transparent 70%),
               linear-gradient(-45deg, transparent 30%, ${alpha('#f97316', 0.03)} 50%, transparent 70%)`,
            backgroundSize: '200px 200px, 300px 300px',
            zIndex: 0,
            animation: 'commitFlow 25s linear infinite',
            '@keyframes commitFlow': {
              '0%': { transform: 'translateX(0) translateY(0)' },
              '100%': { transform: 'translateX(-200px) translateY(-200px)' },
            },
          },
        }}
      >
        <Container
          sx={{
            py: { xs: 8, sm: 12 },
            position: 'relative',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            gap: { xs: 4, sm: 6 },
            zIndex: 1,
          }}
        >
          <GameCard
        sx={{
          width: { sm: '100%', md: '70%' },
          textAlign: 'center',
          p: 4,
        }}
      >
        <GameText
          variant="h3"
          textVariant="glow"
          sx={{
            fontWeight: 700,
            mb: 3,
          }}
        >
          ConGen is open source!
        </GameText>

        <GameText
          variant="h6"
          textVariant="secondary"
          sx={{
            mb: 2,
            fontWeight: 400,
            lineHeight: 1.6,
            opacity: 0.9,
          }}
        >
          Built with transparency and community in mind - ConGen is completely open source!
        </GameText>

        <GameText
          variant="body1"
          textVariant="secondary"
          sx={{
            mb: 3,
            lineHeight: 1.6,
            opacity: 0.8,
          }}
        >
          No subscriptions, no hidden costs, no vendor lock-in. Use it, modify it, and contribute to 
          the future of fitness technology. Your data stays yours, and the code is yours to explore.
        </GameText>

        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} justifyContent="center">
          <Button
            variant="contained"
            component={Link}
            href="https://opensource.org/license/mit"
            sx={{
              borderRadius: 2,
              px: 4,
              py: 1.5,
              fontWeight: 600,
              textTransform: 'none',
              boxShadow: theme => `0 4px 14px ${alpha(theme.palette.primary.main, 0.3)}`,
              '&:hover': {
                boxShadow: theme => `0 6px 20px ${alpha(theme.palette.primary.main, 0.4)}`,
                transform: 'translateY(-1px)',
              },
            }}
          >
            View License Details
          </Button>
        </Stack>
          </GameCard>
        </Container>
      </Box>
    </motion.div>
  );
}
