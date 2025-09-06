import { createTheme } from '@mui/material/styles';
import { getTheme } from './theme';

describe('Theme Configuration', () => {
  it('creates light theme with correct colors', () => {
    const theme = createTheme(getTheme('light'));

    expect(theme.palette.mode).toBe('light');
    expect(theme.palette.primary.main).toBe('#0ea5e9');
    expect(theme.palette.secondary.main).toBe('#f97316');
    expect(theme.palette.background.default).toBe('#ffffff');
    expect(theme.palette.background.paper).toBe('#fafafa');
  });

  it('creates dark theme with correct colors', () => {
    const theme = createTheme(getTheme('dark'));

    expect(theme.palette.mode).toBe('dark');
    expect(theme.palette.primary.main).toBe('#0ea5e9');
    expect(theme.palette.secondary.main).toBe('#f97316');
    expect(theme.palette.background.default).toBe('#0a0a0a');
    expect(theme.palette.background.paper).toBe('#111111');
  });

  it('has correct typography configuration', () => {
    const theme = createTheme(getTheme('light'));

    expect(theme.typography.fontFamily).toContain('Inter');
    expect(theme.typography.h1.fontSize).toBe('clamp(2.5rem, 8vw, 4rem)');
    expect(theme.typography.h1.fontWeight).toBe(700);
    expect(theme.typography.button.textTransform).toBe('none');
  });

  it('has correct spacing configuration', () => {
    const theme = createTheme(getTheme('light'));

    expect(theme.spacing(1)).toBe('8px');
    expect(theme.spacing(2)).toBe('16px');
    expect(theme.spacing(3)).toBe('24px');
  });

  it('has correct shape configuration', () => {
    const theme = createTheme(getTheme('light'));

    expect(theme.shape.borderRadius).toBe(12);
  });

  it('has correct shadows configuration', () => {
    const theme = createTheme(getTheme('light'));

    expect(theme.shadows).toHaveLength(25);
    expect(theme.shadows[1]).toBeDefined();
    expect(theme.shadows[4]).toBeDefined();
  });

  it('has correct breakpoints configuration', () => {
    const theme = createTheme(getTheme('light'));

    expect(theme.breakpoints.values.xs).toBe(0);
    expect(theme.breakpoints.values.sm).toBe(600);
    expect(theme.breakpoints.values.md).toBe(900);
    expect(theme.breakpoints.values.lg).toBe(1200);
    expect(theme.breakpoints.values.xl).toBe(1536);
  });

  it('maintains consistent theme structure between light and dark', () => {
    const lightTheme = createTheme(getTheme('light'));
    const darkTheme = createTheme(getTheme('dark'));

    // Both themes should have the same structure
    expect(lightTheme.palette.primary.main).toBe(darkTheme.palette.primary.main);
    expect(lightTheme.palette.secondary.main).toBe(darkTheme.palette.secondary.main);
    expect(lightTheme.typography.fontFamily).toBe(darkTheme.typography.fontFamily);
    expect(lightTheme.shape.borderRadius).toBe(darkTheme.shape.borderRadius);
  });

  it('has correct component overrides', () => {
    const theme = createTheme(getTheme('light'));

    // Check for MUI component overrides
    expect(theme.components?.MuiButton).toBeDefined();
    expect(theme.components?.MuiCard).toBeDefined();
    expect(theme.components?.MuiPaper).toBeDefined();
  });
});
