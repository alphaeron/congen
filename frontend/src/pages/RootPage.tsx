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
      <Features />
      <HowItWorks />
      <OpenSource />
    </motion.div>
  );
} // end component RootPage
