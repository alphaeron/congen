import { render, screen } from '@testing-library/react';
import React from 'react';

import { AnimatedWrapper, FadeIn, SlideInLeft, SlideInRight, SlideInUp, ScaleIn, HoverLift, HoverScale, HoverCard, Pulse, Bounce, Floating, QuestGlow, ProgramPulse, Shimmer, ProgramShimmer, PageTransition, Spinner, ButtonPress, Magnetic } from './AnimatedWrapper';

// Mock framer-motion
jest.mock('framer-motion', () => ({
  motion: {
    div: ({ children, animate, initial, variants, whileHover, whileTap, whileInView, whileFocus, whileDrag, drag, dragConstraints, dragElastic, dragMomentum, dragPropagation, dragSnapToOrigin, dragTransition, dragControls, onDrag, onDragStart, onDragEnd, layout, layoutId, layoutDependency, layoutScroll, layoutRoot, transition, custom, inherit, ...props }: any) => (
      <div data-testid="motion-div" {...props}>{children}</div>
    ),
    span: ({ children, animate, initial, variants, whileHover, whileTap, whileInView, whileFocus, whileDrag, drag, dragConstraints, dragElastic, dragMomentum, dragPropagation, dragSnapToOrigin, dragTransition, dragControls, onDrag, onDragStart, onDragEnd, layout, layoutId, layoutDependency, layoutScroll, layoutRoot, transition, custom, inherit, ...props }: any) => (
      <span data-testid="motion-span" {...props}>{children}</span>
    ),
  },
}));

// Mock animations
jest.mock('../utils/animations', () => ({
  fadeInVariants: { hidden: { opacity: 0 }, visible: { opacity: 1 } },
  slideInLeftVariants: { hidden: { x: -100 }, visible: { x: 0 } },
  slideInRightVariants: { hidden: { x: 100 }, visible: { x: 0 } },
  slideInUpVariants: { hidden: { y: 100 }, visible: { y: 0 } },
  scaleInVariants: { hidden: { scale: 0 }, visible: { scale: 1 } },
  programSlideInVariants: { hidden: { x: -50 }, visible: { x: 0 } },
  staggerContainerVariants: { hidden: {}, visible: {} },
  hoverLiftVariants: { rest: { y: 0 }, hover: { y: -5 } },
  hoverScaleVariants: { rest: { scale: 1 }, hover: { scale: 1.05 } },
  hoverCardVariants: { rest: { y: 0 }, hover: { y: -2 } },
  pulseVariants: { animate: { scale: [1, 1.05, 1] } },
  bounceVariants: { animate: { y: [0, -10, 0] } },
  programBounceVariants: { animate: { y: [0, -5, 0] } },
  floatingVariants: { animate: { y: [0, -10, 0] } },
  questGlowVariants: { animate: { boxShadow: ['0 0 0px', '0 0 20px', '0 0 0px'] } },
  programPulseVariants: { animate: { scale: [1, 1.02, 1] } },
  shimmerVariants: { animate: { backgroundPosition: ['0%', '100%'] } },
  programShimmerVariants: { animate: { backgroundPosition: ['0%', '100%'] } },
  pageTransitionVariants: { hidden: { opacity: 0 }, visible: { opacity: 1 } },
  spinnerVariants: { animate: { rotate: 360 } },
  buttonPressVariants: { rest: { scale: 1 }, press: { scale: 0.95 } },
  magneticVariants: { rest: { x: 0, y: 0 }, hover: { x: 2, y: -2 } },
}));

describe('AnimatedWrapper', () => {
  const testContent = <div data-testid="test-content">Test Content</div>;

  it('renders with default fadeIn animation', () => {
    render(<AnimatedWrapper>{testContent}</AnimatedWrapper>);
    
    const motionDiv = screen.getByTestId('motion-div');
    expect(motionDiv).toBeInTheDocument();
    expect(screen.getByTestId('test-content')).toBeInTheDocument();
  });

  it('renders with custom animation type', () => {
    render(<AnimatedWrapper animation="slideInLeft">{testContent}</AnimatedWrapper>);
    
    const motionDiv = screen.getByTestId('motion-div');
    expect(motionDiv).toBeInTheDocument();
  });

  it('renders with custom className', () => {
    render(<AnimatedWrapper className="custom-class">{testContent}</AnimatedWrapper>);
    
    const motionDiv = screen.getByTestId('motion-div');
    expect(motionDiv).toHaveClass('custom-class');
  });

  it('renders with custom style', () => {
    const customStyle = { color: 'red' };
    render(<AnimatedWrapper style={customStyle}>{testContent}</AnimatedWrapper>);
    
    const motionDiv = screen.getByTestId('motion-div');
    expect(motionDiv).toHaveStyle('color: red');
  });

  it('renders with custom element type', () => {
    render(<AnimatedWrapper as="span">{testContent}</AnimatedWrapper>);
    
    const motionSpan = screen.getByTestId('motion-span');
    expect(motionSpan).toBeInTheDocument();
  });

  it('renders continuous animations with animate prop', () => {
    render(<AnimatedWrapper animation="pulse">{testContent}</AnimatedWrapper>);
    
    const motionDiv = screen.getByTestId('motion-div');
    expect(motionDiv).toBeInTheDocument();
  });

  it('renders hover animations with whileHover and whileTap', () => {
    render(<AnimatedWrapper animation="hoverLift">{testContent}</AnimatedWrapper>);
    
    const motionDiv = screen.getByTestId('motion-div');
    expect(motionDiv).toBeInTheDocument();
  });
});

describe('Specialized Animation Components', () => {
  const testContent = <div data-testid="test-content">Test Content</div>;

  it('FadeIn renders correctly', () => {
    render(<FadeIn>{testContent}</FadeIn>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
    expect(screen.getByTestId('test-content')).toBeInTheDocument();
  });

  it('SlideInLeft renders correctly', () => {
    render(<SlideInLeft>{testContent}</SlideInLeft>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('SlideInRight renders correctly', () => {
    render(<SlideInRight>{testContent}</SlideInRight>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('SlideInUp renders correctly', () => {
    render(<SlideInUp>{testContent}</SlideInUp>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('ScaleIn renders correctly', () => {
    render(<ScaleIn>{testContent}</ScaleIn>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('HoverLift renders correctly', () => {
    render(<HoverLift>{testContent}</HoverLift>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('HoverScale renders correctly', () => {
    render(<HoverScale>{testContent}</HoverScale>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('HoverCard renders correctly', () => {
    render(<HoverCard>{testContent}</HoverCard>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('Pulse renders correctly', () => {
    render(<Pulse>{testContent}</Pulse>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('Bounce renders correctly', () => {
    render(<Bounce>{testContent}</Bounce>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('Floating renders correctly', () => {
    render(<Floating>{testContent}</Floating>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('QuestGlow renders correctly', () => {
    render(<QuestGlow>{testContent}</QuestGlow>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('ProgramPulse renders correctly', () => {
    render(<ProgramPulse>{testContent}</ProgramPulse>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('Shimmer renders correctly', () => {
    render(<Shimmer>{testContent}</Shimmer>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('ProgramShimmer renders correctly', () => {
    render(<ProgramShimmer>{testContent}</ProgramShimmer>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('PageTransition renders correctly', () => {
    render(<PageTransition>{testContent}</PageTransition>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('Spinner renders correctly', () => {
    render(<Spinner>{testContent}</Spinner>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('ButtonPress renders correctly', () => {
    render(<ButtonPress>{testContent}</ButtonPress>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });

  it('Magnetic renders correctly', () => {
    render(<Magnetic>{testContent}</Magnetic>);
    expect(screen.getByTestId('motion-div')).toBeInTheDocument();
  });
});
