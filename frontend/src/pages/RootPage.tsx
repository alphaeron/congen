import * as React from 'react';
import { motion } from 'framer-motion';

import AlgorithmInfographic from '../components/AlgorithmInfographic';
import { Features } from '../components/Features';
import { GamificationSection } from '../components/GamificationSection';
import { Hero } from '../components/Hero';
import { OpenSource } from '../components/OpenSource';
import { PersonalizationSection } from '../components/PersonalizationSection';

/**
 * Root page with viewport-locked fullscreen sections.
 *
 * @return The root page.
 */
export function RootPage(): React.ReactElement {
  return (
    <motion.div
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      transition={{ duration: 0.5 }}
      style={{
        scrollSnapType: 'y mandatory',
        height: '600vh', // 6 sections × 100vh each (Hero, Features, Algorithm, Gamification, Personalization, OpenSource)
      }}
    >
      <div style={{ height: '100vh', scrollSnapAlign: 'start' }}>
        <Hero />
      </div>
      <div style={{ height: '100vh', scrollSnapAlign: 'start' }}>
        <Features />
      </div>
      <div style={{ height: '100vh', scrollSnapAlign: 'start' }}>
        <AlgorithmInfographic />
      </div>
      <div style={{ height: '100vh', scrollSnapAlign: 'start' }}>
        <GamificationSection />
      </div>
      <div style={{ height: '100vh', scrollSnapAlign: 'start' }}>
        <PersonalizationSection />
      </div>
      <div style={{ height: '100vh', scrollSnapAlign: 'start' }}>
        <OpenSource />
      </div>
    </motion.div>
  );
} // end component RootPage
