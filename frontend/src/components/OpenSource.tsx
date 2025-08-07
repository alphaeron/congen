
import Box from '@mui/material/Box';
import Container from '@mui/material/Container';
import Link from '@mui/material/Link';
import Typography from '@mui/material/Typography';
import * as React from 'react';

export function OpenSource() {
  return (
    <Container
      id="opensource"
      sx={{
        pt: { xs: 4, sm: 12 },
        pb: { xs: 8, sm: 16 },
        position: 'relative',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        gap: { xs: 3, sm: 6 },
      }}
    >
      <Box
        sx={{
          width: { sm: '100%', md: '60%' },
          textAlign: { sm: 'left', md: 'center' },
        }}
      >
        <Typography component="h2" variant="h4" color="text.primary">
          ConGen is open source!
        </Typography>
        <Typography variant="body1" sx={{ color: 'grey.400' }}>
          ConGen is open source software, meaning you can use and modify it!
        </Typography>
        <Typography variant="body1" sx={{ color: 'grey.400' }}>
          That means no paying for trials, no subscriptions, and no limitations!
        </Typography>
        <Link href="https://opensource.org/license/mit" color="primary">
          See details about license usage.
        </Link>
      </Box>
    </Container>
  );
}
