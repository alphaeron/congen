import { Stack } from '@mui/material';
import Box from '@mui/material/Box';
import Button from '@mui/material/Button';
import Container from '@mui/material/Container';
import Link from '@mui/material/Link';
import { alpha } from '@mui/material/styles';
import Typography from '@mui/material/Typography';
import * as React from 'react';

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
      <Box
        sx={{
          width: { sm: '100%', md: '70%' },
          textAlign: 'center',
          p: 4,
          borderRadius: 4,
          background: theme =>
            `linear-gradient(135deg, ${alpha(theme.palette.background.paper, 0.8)}, ${alpha(theme.palette.background.paper, 0.6)})`,
          border: theme => `1px solid ${alpha(theme.palette.divider, 0.3)}`,
          backdropFilter: 'blur(20px)',
          boxShadow: theme => `0 8px 32px ${alpha(theme.palette.primary.main, 0.1)}`,
        }}
      >
        <Typography
          component="h2"
          variant="h3"
          color="text.primary"
          sx={{
            fontWeight: 700,
            mb: 3,
            background: 'linear-gradient(135deg, #0ea5e9, #f97316)',
            backgroundClip: 'text',
            WebkitBackgroundClip: 'text',
            WebkitTextFillColor: 'transparent',
          }}
        >
          ConGen is open source!
        </Typography>

        <Typography
          variant="h6"
          color="text.secondary"
          sx={{
            mb: 2,
            fontWeight: 400,
            lineHeight: 1.6,
            opacity: 0.9,
          }}
        >
          ConGen is open source software, meaning you can use and modify it!
        </Typography>

        <Typography
          variant="body1"
          color="text.secondary"
          sx={{
            mb: 3,
            lineHeight: 1.6,
            opacity: 0.8,
          }}
        >
          That means no paying for trials, no subscriptions, and no limitations!
        </Typography>

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
      </Box>
    </Container>
  );
}
