import { Stack } from '@mui/material';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import Link from '@mui/material/Link';
import { alpha } from '@mui/material/styles';
import * as React from 'react';

import { GameText, GameCard, GameButton, GAME_CLASSES } from './GameTheme';

export function OpenSource() {
  return (
    <Container
      id="open-source"
      sx={{
        py: { xs: 8, sm: 12 },
        position: 'relative',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: { xs: 4, sm: 6 },
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
          component="h2"
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
          ConGen is open source software, meaning you can use and modify it!
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
          That means no paying for trials, no subscriptions, and no limitations!
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
  );
}
