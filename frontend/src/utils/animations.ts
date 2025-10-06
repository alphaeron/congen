import type { Variants } from 'framer-motion';

// Common animation variants for consistent animations across the app
export const fadeInVariants: Variants = {
  hidden: {
    opacity: 0,
    y: 20,
  },
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.6,
      ease: 'easeOut',
    },
  },
};

export const slideInLeftVariants: Variants = {
  hidden: {
    opacity: 0,
    x: -30,
  },
  visible: {
    opacity: 1,
    x: 0,
    transition: {
      duration: 0.6,
      ease: 'easeOut',
    },
  },
};

export const slideInRightVariants: Variants = {
  hidden: {
    opacity: 0,
    x: 30,
  },
  visible: {
    opacity: 1,
    x: 0,
    transition: {
      duration: 0.6,
      ease: 'easeOut',
    },
  },
};

export const slideInUpVariants: Variants = {
  hidden: {
    opacity: 0,
    y: 30,
  },
  visible: {
    opacity: 1,
    y: 0,
    transition: {
      duration: 0.6,
      ease: 'easeOut',
    },
  },
};

export const scaleInVariants: Variants = {
  hidden: {
    opacity: 0,
    scale: 0.95,
  },
  visible: {
    opacity: 1,
    scale: 1,
    transition: {
      duration: 0.6,
      ease: 'easeOut',
    },
  },
};

export const programSlideInVariants: Variants = {
  hidden: {
    opacity: 0,
    x: -50,
    scale: 0.95,
  },
  visible: {
    opacity: 1,
    x: 0,
    scale: 1,
    transition: {
      duration: 0.6,
      ease: 'easeOut',
    },
  },
};

// Stagger container variants
export const staggerContainerVariants: Variants = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1,
      delayChildren: 0.1,
    },
  },
};

// Hover animations
export const hoverLiftVariants = {
  rest: {
    y: 0,
    transition: {
      duration: 0.2,
      ease: 'easeInOut',
    },
  },
  hover: {
    y: -2,
    transition: {
      duration: 0.2,
      ease: 'easeInOut',
    },
  },
};

export const hoverScaleVariants = {
  rest: {
    scale: 1,
    transition: {
      duration: 0.2,
      ease: 'easeInOut',
    },
  },
  hover: {
    scale: 1.05,
    transition: {
      duration: 0.2,
      ease: 'easeInOut',
    },
  },
};

export const hoverCardVariants = {
  rest: {
    y: 0,
    boxShadow: '0 8px 32px rgba(0, 0, 0, 0.12)',
    transition: {
      duration: 0.3,
      ease: 'easeInOut',
    },
  },
  hover: {
    y: -4,
    boxShadow: '0 20px 40px rgba(14, 165, 233, 0.15)',
    transition: {
      duration: 0.3,
      ease: 'easeInOut',
    },
  },
};

// Continuous animations
export const pulseVariants = {
  animate: {
    opacity: [1, 0.5, 1],
    transition: {
      duration: 2,
      repeat: Infinity,
      ease: 'easeInOut',
    },
  },
};

export const bounceVariants = {
  animate: {
    y: [0, -3, 0],
    transition: {
      duration: 2,
      repeat: Infinity,
      ease: 'easeInOut',
    },
  },
};

export const programBounceVariants = {
  animate: {
    y: [0, -2, 0],
    transition: {
      duration: 2,
      repeat: Infinity,
      ease: 'easeInOut',
    },
  },
};

export const floatingVariants = {
  animate: {
    y: [-10, 10, -10],
    transition: {
      duration: 6,
      repeat: Infinity,
      ease: 'easeInOut',
    },
  },
};

// Game-specific animations

export const questGlowVariants = {
  animate: {
    boxShadow: [
      '0 0 5px rgba(0, 188, 212, 0.3)',
      '0 0 20px rgba(0, 188, 212, 0.6), 0 0 30px rgba(0, 188, 212, 0.4)',
      '0 0 5px rgba(0, 188, 212, 0.3)',
    ],
    transition: {
      duration: 2,
      repeat: Infinity,
      ease: 'easeInOut',
    },
  },
};

export const programPulseVariants = {
  animate: {
    boxShadow: [
      '0 0 0 0 rgba(0, 188, 212, 0.4)',
      '0 0 0 10px rgba(0, 188, 212, 0)',
      '0 0 0 0 rgba(0, 188, 212, 0.4)',
    ],
    transition: {
      duration: 2,
      repeat: Infinity,
      ease: 'easeInOut',
    },
  },
};

// Shimmer effect (using background position animation)
export const shimmerVariants = {
  animate: {
    backgroundPosition: ['-200px 0', 'calc(200px + 100%) 0'],
    transition: {
      duration: 2,
      repeat: Infinity,
      ease: 'linear',
    },
  },
};

export const programShimmerVariants = {
  animate: {
    backgroundPosition: ['-200px 0', 'calc(200px + 100%) 0'],
    transition: {
      duration: 3,
      repeat: Infinity,
      ease: 'linear',
    },
  },
};

// Page transition variants
export const pageTransitionVariants: Variants = {
  hidden: {
    opacity: 0,
  },
  visible: {
    opacity: 1,
    transition: {
      duration: 0.3,
      ease: 'easeInOut',
    },
  },
  exit: {
    opacity: 0,
    transition: {
      duration: 0.3,
      ease: 'easeInOut',
    },
  },
};

// Loading spinner variants
export const spinnerVariants = {
  animate: {
    rotate: 360,
    transition: {
      duration: 1,
      repeat: Infinity,
      ease: 'linear',
    },
  },
};

// Button press animation
export const buttonPressVariants = {
  rest: { scale: 1 },
  press: { scale: 0.95 },
};

// Magnetic hover effect
export const magneticVariants = {
  rest: {
    scale: 1,
    rotate: 0,
    transition: {
      type: 'spring',
      stiffness: 300,
      damping: 20,
    },
  },
  hover: {
    scale: 1.05,
    rotate: 2,
    transition: {
      type: 'spring',
      stiffness: 300,
      damping: 20,
    },
  },
  tap: { scale: 0.95 },
};
