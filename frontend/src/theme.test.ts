import { getTheme, brand, secondary, gray, green } from './theme';

describe('theme', () => {
  describe('color palettes', () => {
    describe('brand', () => {
      it('should have correct brand color values', () => {
        expect(brand[50]).toBe('#f0f4ff');
        expect(brand[100]).toBe('#d6e0fa');
        expect(brand[200]).toBe('#b3c6f7');
        expect(brand[300]).toBe('#7a9cf0');
        expect(brand[400]).toBe('#4d6fe0');
        expect(brand[500]).toBe('#2236cc');
        expect(brand[600]).toBe('#1b2a9e');
        expect(brand[700]).toBe('#16227a');
        expect(brand[800]).toBe('#10195a');
        expect(brand[900]).toBe('#0a113a');
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
        expect(secondary[50]).toBe('#f3f2fa');
        expect(secondary[100]).toBe('#e0def7');
        expect(secondary[200]).toBe('#c2bff0');
        expect(secondary[300]).toBe('#a39ee0');
        expect(secondary[400]).toBe('#7a6fd6');
        expect(secondary[500]).toBe('#5a4fc2');
        expect(secondary[600]).toBe('#473d9e');
        expect(secondary[700]).toBe('#352c7a');
        expect(secondary[800]).toBe('#241b5a');
        expect(secondary[900]).toBe('#18113a');
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
        expect(gray[50]).toBe('#f9fafc');
        expect(gray[100]).toBe('#e3e6ea');
        expect(gray[200]).toBe('#c8ccd2');
        expect(gray[300]).toBe('#a2a6ad');
        expect(gray[400]).toBe('#7a7e87');
        expect(gray[500]).toBe('#55585e');
        expect(gray[600]).toBe('#3a3c40');
        expect(gray[700]).toBe('#28292c');
        expect(gray[800]).toBe('#202020');
        expect(gray[900]).toBe('#141417');
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
        expect(green[50]).toBe('#F6FEF6');
        expect(green[100]).toBe('#E3FBE3');
        expect(green[200]).toBe('#C7F7C7');
        expect(green[300]).toBe('#A1E8A1');
        expect(green[400]).toBe('#51BC51');
        expect(green[500]).toBe('#1F7A1F');
        expect(green[600]).toBe('#136C13');
        expect(green[700]).toBe('#0A470A');
        expect(green[800]).toBe('#042F04');
        expect(green[900]).toBe('#021D02');
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
      expect(darkPrimary?.main).toBe(brand[400]);
    });

    it('should have different contrast text colors for light and dark modes', () => {
      const lightTheme = getTheme('light');
      const darkTheme = getTheme('dark');

      const lightPrimary = lightTheme.palette?.primary;
      const darkPrimary = darkTheme.palette?.primary;

      expect(lightPrimary?.contrastText).toBe(brand[50]);
      expect(darkPrimary?.contrastText).toBe(brand[100]);
    });

    it('should have different light colors for light and dark modes', () => {
      const lightTheme = getTheme('light');
      const darkTheme = getTheme('dark');

      const lightPrimary = lightTheme.palette?.primary;
      const darkPrimary = darkTheme.palette?.primary;

      expect(lightPrimary?.light).toBe(brand[200]);
      expect(darkPrimary?.light).toBe(brand[300]);
    });

    it('should have same dark color for both modes', () => {
      const lightTheme = getTheme('light');
      const darkTheme = getTheme('dark');

      const lightPrimary = lightTheme.palette?.primary;
      const darkPrimary = darkTheme.palette?.primary;

      expect(lightPrimary?.dark).toBe(brand[800]);
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
