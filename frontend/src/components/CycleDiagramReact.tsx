import { motion } from 'framer-motion';
import React, { useEffect, useRef, useState } from 'react';

import type { CycleDiagramProps } from './CycleDiagram';
import { CycleDiagram } from './CycleDiagram';

export const CycleDiagramReact: React.FC<CycleDiagramProps> = props => {
  const containerRef = useRef<HTMLDivElement>(null);
  const diagramRef = useRef<CycleDiagram | null>(null);
  const [isInView, setIsInView] = useState(false);
  const [showText, setShowText] = useState(false);

  // Intersection Observer for viewport detection
  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting && !isInView) {
          setIsInView(true);
          // Delay text animation to happen after chart spin animation completes
          setTimeout(() => {
            setShowText(true);
          }, 1000); // 1.0s delay to allow chart spin animation to complete
          observer.disconnect(); // Only animate once
        }
      },
      { threshold: 0.9 }
    );

    if (containerRef.current) {
      observer.observe(containerRef.current);
    }

    return () => {
      if (containerRef.current) {
        observer.unobserve(containerRef.current);
      }
    };
  }, [isInView]);

  useEffect(() => {
    if (containerRef.current) {
      // Create new instance of the class-based CycleDiagram with showText initially false
      diagramRef.current = new CycleDiagram(containerRef.current, { ...props, showText: false });
    }

    return () => {
      // Clean up the diagram when component unmounts
      if (diagramRef.current) {
        diagramRef.current.destroy();
        diagramRef.current = null;
      }
    };
  }, []);

  // Update diagram when props change
  useEffect(() => {
    if (diagramRef.current) {
      diagramRef.current.update(props);
    }
  }, [props]);

  // Update text visibility when showText state changes
  useEffect(() => {
    if (diagramRef.current) {
      diagramRef.current.updateTextVisibility(showText);
    }
  }, [showText]);

  return (
    <motion.div
      ref={containerRef}
      initial={{
        opacity: 0,
        scale: 0.8,
        rotate: -360,
      }}
      animate={
        isInView
          ? {
              opacity: 1,
              scale: 1,
              rotate: 0,
            }
          : {
              opacity: 0,
              scale: 0.8,
              rotate: -360,
            }
      }
      transition={{
        duration: 1.0,
        ease: 'easeOut',
        rotate: {
          duration: 1.0,
          ease: [0.25, 0.46, 0.45, 0.94], // Smooth deceleration for spin
        },
        scale: {
          duration: 0.6,
          ease: 'easeOut',
        },
        opacity: {
          duration: 0.4,
          ease: 'easeOut',
        },
      }}
      style={{
        position: 'relative',
        overflow: 'visible',
      }}
    ></motion.div>
  );
};
