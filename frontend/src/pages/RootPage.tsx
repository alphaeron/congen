import * as React from 'react';

import AlgorithmInfographic from '../components/AlgorithmInfographic';
import { Features } from '../components/Features';
import { GamificationSection } from '../components/GamificationSection';
import { Hero } from '../components/Hero';
import { OpenSource } from '../components/OpenSource';
import { PersonalizationSection } from '../components/PersonalizationSection';

const SnapParent = React.forwardRef<HTMLDivElement, React.HTMLProps<HTMLDivElement>>(
  ({ ...props }, ref) => (
    <div ref={ref} {...props} className="snap-parent-y-mandatory">
      {props.children}
    </div>
  )
);
SnapParent.displayName = 'SnapParent';

/**
 * Root page with viewport-locked fullscreen sections using proper scroll snap.
 *
 * @return The root page.
 */
export function RootPage(): React.ReactElement {
  return (
    <SnapParent>
      <div id="hero-section" className="section">
        <Hero />
      </div>
      <div id="features-section" className="section">
        <Features />
      </div>
      <div id="algorithm-section" className="section">
        <AlgorithmInfographic />
      </div>
      <div id="gamification-section" className="section">
        <GamificationSection />
      </div>
      <div id="personalization-section" className="section">
        <PersonalizationSection />
      </div>
      <div id="opensource-section" className="section">
        <OpenSource />
      </div>
    </SnapParent>
  );
} // end component RootPage
