import { motion, MotionProps } from 'framer-motion';
import React, { ReactNode } from 'react';
import {
  fadeInVariants,
  slideInLeftVariants,
  slideInRightVariants,
  slideInUpVariants,
  scaleInVariants,
  staggerContainerVariants,
  hoverLiftVariants,
  hoverScaleVariants,
  hoverCardVariants,
  pulseVariants,
  bounceVariants,
  pageTransitionVariants,
  spinnerVariants,
  buttonPressVariants,
  magneticVariants,
} from '../utils/animations';

export type AnimationType =
  | 'fadeIn'
  | 'slideInLeft'
  | 'slideInRight'
  | 'slideInUp'
  | 'scaleIn'
  | 'staggerContainer'
  | 'hoverLift'
  | 'hoverScale'
  | 'hoverCard'
  | 'pulse'
  | 'bounce'
  | 'pageTransition'
  | 'spinner'
  | 'buttonPress'
  | 'magnetic';

interface AnimatedWrapperProps extends Omit<MotionProps, 'variants'> {
  children: ReactNode;
  animation?: AnimationType;
  className?: string;
  style?: React.CSSProperties;
  as?: keyof typeof motion;
}

const animationVariants = {
  fadeIn: fadeInVariants,
  slideInLeft: slideInLeftVariants,
  slideInRight: slideInRightVariants,
  slideInUp: slideInUpVariants,
  scaleIn: scaleInVariants,
  staggerContainer: staggerContainerVariants,
  hoverLift: hoverLiftVariants,
  hoverScale: hoverScaleVariants,
  hoverCard: hoverCardVariants,
  pulse: pulseVariants,
  bounce: bounceVariants,
  pageTransition: pageTransitionVariants,
  spinner: spinnerVariants,
  buttonPress: buttonPressVariants,
  magnetic: magneticVariants,
};

export function AnimatedWrapper({
  children,
  animation = 'fadeIn',
  className,
  style,
  as = 'div',
  ...motionProps
}: AnimatedWrapperProps) {
  const MotionComponent = motion[as];
  const variants = animationVariants[animation];

  // For continuous animations, use animate prop
  const continuousAnimations = ['pulse', 'bounce', 'spinner'];
  const isContinuous = continuousAnimations.includes(animation);

  if (isContinuous) {
    return (
      <MotionComponent
        className={className}
        style={style}
        variants={variants}
        animate="animate"
        {...motionProps}
      >
        {children}
      </MotionComponent>
    );
  }

  // For hover animations, use whileHover and whileTap
  const hoverAnimations = ['hoverLift', 'hoverScale', 'hoverCard', 'buttonPress', 'magnetic'];
  const isHover = hoverAnimations.includes(animation);

  if (isHover) {
    return (
      <MotionComponent
        className={className}
        style={style}
        variants={variants}
        initial="rest"
        whileHover="hover"
        whileTap="press"
        {...motionProps}
      >
        {children}
      </MotionComponent>
    );
  }

  // For entrance animations, use initial and animate
  return (
    <MotionComponent
      className={className}
      style={style}
      variants={variants}
      initial="hidden"
      animate="visible"
      {...motionProps}
    >
      {children}
    </MotionComponent>
  );
}

// Specialized components for common use cases
export function FadeIn({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="fadeIn" {...props}>
      {children}
    </AnimatedWrapper>
  );
}

export function SlideInLeft({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="slideInLeft" {...props}>
      {children}
    </AnimatedWrapper>
  );
}

export function SlideInRight({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="slideInRight" {...props}>
      {children}
    </AnimatedWrapper>
  );
}

export function SlideInUp({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="slideInUp" {...props}>
      {children}
    </AnimatedWrapper>
  );
}

export function ScaleIn({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="scaleIn" {...props}>
      {children}
    </AnimatedWrapper>
  );
}

export function HoverLift({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="hoverLift" {...props}>
      {children}
    </AnimatedWrapper>
  );
}

export function HoverScale({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="hoverScale" {...props}>
      {children}
    </AnimatedWrapper>
  );
}

export function HoverCard({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="hoverCard" {...props}>
      {children}
    </AnimatedWrapper>
  );
}

export function Pulse({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="pulse" {...props}>
      {children}
    </AnimatedWrapper>
  );
}

export function Bounce({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="bounce" {...props}>
      {children}
    </AnimatedWrapper>
  );
}

export function PageTransition({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="pageTransition" {...props}>
      {children}
    </AnimatedWrapper>
  );
}

export function Spinner({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="spinner" {...props}>
      {children}
    </AnimatedWrapper>
  );
}

export function ButtonPress({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="buttonPress" {...props}>
      {children}
    </AnimatedWrapper>
  );
}

export function Magnetic({ children, ...props }: Omit<AnimatedWrapperProps, 'animation'>) {
  return (
    <AnimatedWrapper animation="magnetic" {...props}>
      {children}
    </AnimatedWrapper>
  );
}
