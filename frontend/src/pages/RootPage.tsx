import Box from '@mui/material/Box';
import Divider from '@mui/material/Divider';
import * as React from 'react';

import { Features } from '../components/Features';
import { Hero } from '../components/Hero';
import { OpenSource } from '../components/OpenSource';

/**
 * Root page.
 *
 * @return The root page.
 */
export function RootPage(): React.ReactElement {
  return (
    <React.Fragment>
      <Hero />
      <Box sx={{ bgcolor: 'background.default' }}>
        <Features />
        <Divider />
        <OpenSource />
        <Divider />
      </Box>
    </React.Fragment>
  );
} // end component RootPage
