import Box from '@mui/material/Box';
import Divider from '@mui/material/Divider';
import { alpha } from '@mui/material/styles';
import * as React from 'react';
import { motion } from 'framer-motion';

import { Features } from '../components/Features';
import { Hero } from '../components/Hero';
import { HowItWorks } from '../components/HowItWorks';
import { OpenSource } from '../components/OpenSource';

/**
 * Root page.
 *
 * @return The root page.
 */
export function RootPage(): React.ReactElement {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.5 }}
    >
      <Hero />
      <Box 
        sx={{ 
          bgcolor: 'background.default',
          position: 'relative',
          '&::before': {
            content: '""',
            position: 'absolute',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: theme => 
              `linear-gradient(180deg, transparent 0%, ${alpha(theme.palette.primary.main, 0.02)} 50%, transparent 100%)`,
            pointerEvents: 'none',
            zIndex: 0,
          },
        }}
      >
        <Box sx={{ position: 'relative', zIndex: 1 }}>
          <Features />
          <motion.div
            initial={{ opacity: 0, scale: 0.8 }}
            whileInView={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            viewport={{ once: true, margin: "-100px" }}
          >
            <Divider 
              sx={{ 
                borderColor: theme => alpha(theme.palette.primary.main, 0.1),
                borderWidth: 1,
                my: 4,
              }} 
            />
          </motion.div>
          <HowItWorks />
          <motion.div
            initial={{ opacity: 0, scale: 0.8 }}
            whileInView={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            viewport={{ once: true, margin: "-100px" }}
          >
            <Divider 
              sx={{ 
                borderColor: theme => alpha(theme.palette.primary.main, 0.1),
                borderWidth: 1,
                my: 4,
              }} 
            />
          </motion.div>
          <OpenSource />
        </Box>
      </Box>
    </motion.div>
  );
} // end component RootPage
