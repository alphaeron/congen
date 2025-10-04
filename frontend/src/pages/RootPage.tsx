import * as React from 'react';
import { motion, useElementScroll, useTransform } from 'framer-motion';

import AlgorithmInfographic from '../components/AlgorithmInfographic';
import { Features } from '../components/Features';
import { GamificationSection } from '../components/GamificationSection';
import { Hero } from '../components/Hero';
import { OpenSource } from '../components/OpenSource';
import { PersonalizationSection } from '../components/PersonalizationSection';

// Window size hook
const useWindowSize = () => {
  const [windowSize, setWindowSize] = React.useState({
    width: undefined as number | undefined,
    height: undefined as number | undefined
  });

  React.useEffect(() => {
    function handleResize() {
      setWindowSize({
        width: window.innerWidth,
        height: window.innerHeight
      });
    }

    window.addEventListener("resize", handleResize);
    handleResize();

    return () => window.removeEventListener("resize", handleResize);
  }, []);

  return windowSize;
};

const LINE_VARIANTS = {
  visible: { height: "75vh", transition: { duration: 2 } },
  hidden: { height: "0vh" }
};

const SnapParent = React.forwardRef<HTMLDivElement, React.HTMLProps<HTMLDivElement>>(({ ...props }, ref) => (
  <div 
    ref={ref} 
    {...props} 
    className="snap-parent-y-mandatory"
  >
    {props.children}
  </div>
));

const Container = ({ children }: { children: React.ReactNode }) => {
  const ref = React.useRef<HTMLDivElement>(null);

  return (
    <div
      style={{
        position: "relative"
      }}
    >
      <SnapParent
        ref={ref}
        style={{
          position: "absolute",
          width: "100%",
          top: 0,
          left: 0
        }}
      >
        {children}
      </SnapParent>
    </div>
  );
};

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
