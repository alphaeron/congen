import { createCongenNivoTheme, getCongenNivoTooltipStyle } from './nivoTheme';

describe('getCongenNivoTooltipStyle', () => {
  it('maps nivo tooltip container theme into inline styles', () => {
    const theme = createCongenNivoTheme('dark');
    const style = getCongenNivoTooltipStyle(theme, { zIndex: 2000 });

    expect(style.color).toBe(theme.tooltip.container.color);
    expect(style.background).toBe(theme.tooltip.container.background);
    expect(style.zIndex).toBe(2000);
    expect(style.whiteSpace).toBe('nowrap');
  });
});
