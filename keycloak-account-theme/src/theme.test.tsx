import { createTheme } from '@mui/material/styles';
import { getTheme } from './theme';

describe('theme', () => {
  describe('getTheme', () => {
    it('creates light theme with correct palette', () => {
      const theme = createTheme(getTheme('light'));

      expect(theme.palette.mode).toBe('light');
      expect(theme.palette.primary.main).toBe('#0ea5e9');
      expect(theme.palette.secondary.main).toBe('#f97316');
      expect(theme.palette.background.default).toBe('#ffffff');
      expect(theme.palette.background.paper).toBe('#fafafa');
    });

    it('creates dark theme with correct palette', () => {
      const theme = createTheme(getTheme('dark'));

      expect(theme.palette.mode).toBe('dark');
      expect(theme.palette.primary.main).toBe('#0ea5e9');
      expect(theme.palette.secondary.main).toBe('#f97316');
      expect(theme.palette.background.default).toBe('#0a0a0a');
      expect(theme.palette.background.paper).toBe('#111111');
    });

    it('includes correct typography settings', () => {
      const theme = createTheme(getTheme('light'));

      expect(theme.typography.fontFamily).toContain('Inter');
      expect(theme.typography.h1.fontSize).toContain('clamp');
      expect(theme.typography.button.textTransform).toBe('none');
    });

    it('includes correct shape settings', () => {
      const theme = createTheme(getTheme('light'));

      expect(theme.shape.borderRadius).toBe(12);
    });

    it('includes custom component overrides', () => {
      const theme = createTheme(getTheme('light'));

      expect(theme.components?.MuiButton).toBeDefined();
      expect(theme.components?.MuiCard).toBeDefined();
      expect(theme.components?.MuiTextField).toBeDefined();
    });

    it('includes correct shadows array', () => {
      const theme = createTheme(getTheme('light'));

      expect(theme.shadows).toHaveLength(26);
      expect(theme.shadows[0]).toBe('none');
      expect(theme.shadows[1]).toContain('rgba(0, 0, 0, 0.05)');
    });

    it('includes success and warning colors', () => {
      const theme = createTheme(getTheme('light'));

      expect(theme.palette.success.main).toBe('#22c55e');
      expect(theme.palette.warning.main).toBe('#f59e0b');
    });

    it('includes error color', () => {
      const theme = createTheme(getTheme('light'));

      expect(theme.palette.error.main).toBeDefined();
      expect(theme.palette.error.light).toBeDefined();
      expect(theme.palette.error.dark).toBeDefined();
    });

    it('includes grey color palette', () => {
      const theme = createTheme(getTheme('light'));

      expect(theme.palette.grey[50]).toBe('#fafafa');
      expect(theme.palette.grey[500]).toBe('#737373');
      expect(theme.palette.grey[900]).toBe('#171717');
    });

    it('includes action colors', () => {
      const theme = createTheme(getTheme('light'));

      expect(theme.palette.action.selected).toBeDefined();
      expect(theme.palette.action.hover).toBeDefined();
    });

    it('includes text colors', () => {
      const theme = createTheme(getTheme('light'));

      expect(theme.palette.text.primary).toBe('#171717');
      expect(theme.palette.text.secondary).toBe('#525252');
    });

    it('includes divider color', () => {
      const theme = createTheme(getTheme('light'));

      expect(theme.palette.divider).toBeDefined();
    });
  });
});
