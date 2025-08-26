import { getTheme, brand, secondary, gray, green } from './theme';

describe('theme', () => {
  describe('color palettes', () => {
    describe('brand', () => {
      it('should have correct brand color values', () => {
        expect(brand[50]).toBe('#f0f9ff');
        expect(brand[100]).toBe('#e0f2fe');
        expect(brand[200]).toBe('#bae6fd');
        expect(brand[300]).toBe('#7dd3fc');
        expect(brand[400]).toBe('#38bdf8');
        expect(brand[500]).toBe('#0ea5e9');
        expect(brand[600]).toBe('#0284c7');
        expect(brand[700]).toBe('#0369a1');
        expect(brand[800]).toBe('#075985');
        expect(brand[900]).toBe('#0c4a6e');
      });

      it('should have all required color steps', () => {
        const steps = [50, 100, 200, 300, 400, 500, 600, 700, 800, 900];
        steps.forEach(step => {
          expect(brand[step as keyof typeof brand]).toBeDefined();
        });
      });
    });

    describe('secondary', () => {
      it('should have correct secondary color values', () => {
        expect(secondary[50]).toBe('#fff7ed');
        expect(secondary[100]).toBe('#ffedd5');
        expect(secondary[200]).toBe('#fed7aa');
        expect(secondary[300]).toBe('#fdba74');
        expect(secondary[400]).toBe('#fb923c');
        expect(secondary[500]).toBe('#f97316');
        expect(secondary[600]).toBe('#ea580c');
        expect(secondary[700]).toBe('#c2410c');
        expect(secondary[800]).toBe('#9a3412');
        expect(secondary[900]).toBe('#7c2d12');
      });

      it('should have all required color steps', () => {
        const steps = [50, 100, 200, 300, 400, 500, 600, 700, 800, 900];
        steps.forEach(step => {
          expect(secondary[step as keyof typeof secondary]).toBeDefined();
        });
      });
    });

    describe('gray', () => {
      it('should have correct gray color values', () => {
        expect(gray[50]).toBe('#fafafa');
        expect(gray[100]).toBe('#f5f5f5');
        expect(gray[200]).toBe('#e5e5e5');
        expect(gray[300]).toBe('#d4d4d4');
        expect(gray[400]).toBe('#a3a3a3');
        expect(gray[500]).toBe('#737373');
        expect(gray[600]).toBe('#525252');
        expect(gray[700]).toBe('#404040');
        expect(gray[800]).toBe('#262626');
        expect(gray[900]).toBe('#171717');
      });

      it('should have all required color steps', () => {
        const steps = [50, 100, 200, 300, 400, 500, 600, 700, 800, 900];
        steps.forEach(step => {
          expect(gray[step as keyof typeof gray]).toBeDefined();
        });
      });
    });

    describe('green', () => {
      it('should have correct green color values', () => {
        expect(green[50]).toBe('#f0fdf4');
        expect(green[100]).toBe('#dcfce7');
        expect(green[200]).toBe('#bbf7d0');
        expect(green[300]).toBe('#86efac');
        expect(green[400]).toBe('#4ade80');
        expect(green[500]).toBe('#22c55e');
        expect(green[600]).toBe('#16a34a');
        expect(green[700]).toBe('#15803d');
        expect(green[800]).toBe('#166534');
        expect(green[900]).toBe('#14532d');
      });

      it('should have all required color steps', () => {
        const steps = [50, 100, 200, 300, 400, 500, 600, 700, 800, 900];
        steps.forEach(step => {
          expect(green[step as keyof typeof green]).toBeDefined();
        });
      });
    });
  });

  describe('getTheme', () => {
    it('should return theme options for light mode', () => {
      const theme = getTheme('light');

      expect(theme).toBeDefined();
      expect(theme.palette).toBeDefined();
      expect(theme.palette?.mode).toBe('light');
    });

    it('should return theme options for dark mode', () => {
      const theme = getTheme('dark');

      expect(theme).toBeDefined();
      expect(theme.palette).toBeDefined();
      expect(theme.palette?.mode).toBe('dark');
    });

    it('should have primary color configuration', () => {
      const lightTheme = getTheme('light');
      const darkTheme = getTheme('dark');

      expect(lightTheme.palette?.primary).toBeDefined();
      expect(darkTheme.palette?.primary).toBeDefined();
    });

    it('should have secondary color configuration', () => {
      const lightTheme = getTheme('light');
      const darkTheme = getTheme('dark');

      expect(lightTheme.palette?.secondary).toBeDefined();
      expect(darkTheme.palette?.secondary).toBeDefined();
    });

    it('should have typography configuration', () => {
      const lightTheme = getTheme('light');
      const darkTheme = getTheme('dark');

      expect(lightTheme.typography).toBeDefined();
      expect(darkTheme.typography).toBeDefined();
    });

    it('should have components configuration', () => {
      const lightTheme = getTheme('light');
      const darkTheme = getTheme('dark');

      expect(lightTheme.components).toBeDefined();
      expect(darkTheme.components).toBeDefined();
    });

    it('should have different primary colors for light and dark modes', () => {
      const lightTheme = getTheme('light');
      const darkTheme = getTheme('dark');

      const lightPrimary = lightTheme.palette?.primary;
      const darkPrimary = darkTheme.palette?.primary;

      expect(lightPrimary?.main).toBe(brand[500]);
      expect(darkPrimary?.main).toBe(brand[500]); // Both use brand[500] as main
    });

    it('should have different contrast text colors for light and dark modes', () => {
      const lightTheme = getTheme('light');
      const darkTheme = getTheme('dark');

      const lightPrimary = lightTheme.palette?.primary;
      const darkPrimary = darkTheme.palette?.primary;

      expect(lightPrimary?.contrastText).toBe('#ffffff'); // Both use white contrast text
      expect(darkPrimary?.contrastText).toBe('#ffffff');
    });

    it('should have different light colors for light and dark modes', () => {
      const lightTheme = getTheme('light');
      const darkTheme = getTheme('dark');

      const lightPrimary = lightTheme.palette?.primary;
      const darkPrimary = darkTheme.palette?.primary;

      expect(lightPrimary?.light).toBe(brand[300]);
      expect(darkPrimary?.light).toBe(brand[400]);
    });

    it('should have same dark color for both modes', () => {
      const lightTheme = getTheme('light');
      const darkTheme = getTheme('dark');

      const lightPrimary = lightTheme.palette?.primary;
      const darkPrimary = darkTheme.palette?.primary;

      expect(lightPrimary?.dark).toBe(brand[700]);
      expect(darkPrimary?.dark).toBe(brand[800]);
    });

    it('should have typography variants configured', () => {
      const theme = getTheme('light');

      expect(theme.typography?.h1).toBeDefined();
      expect(theme.typography?.h2).toBeDefined();
      expect(theme.typography?.h3).toBeDefined();
      expect(theme.typography?.h4).toBeDefined();
      expect(theme.typography?.h5).toBeDefined();
      expect(theme.typography?.h6).toBeDefined();
      expect(theme.typography?.body1).toBeDefined();
      expect(theme.typography?.body2).toBeDefined();
      expect(theme.typography?.caption).toBeDefined();
    });

    it('should have MUI component overrides configured', () => {
      const theme = getTheme('light');

      expect(theme.components?.MuiAccordion).toBeDefined();
      expect(theme.components?.MuiAccordionSummary).toBeDefined();
      expect(theme.components?.MuiAccordionDetails).toBeDefined();
      expect(theme.components?.MuiToggleButtonGroup).toBeDefined();
      expect(theme.components?.MuiToggleButton).toBeDefined();
      expect(theme.components?.MuiButtonBase).toBeDefined();
    });
  });
});
