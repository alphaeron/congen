import { capitalizeFirstLetter, capitalizeEachWord } from './utils';

describe('capitalizeFirstLetter', () => {
  it('Only capitalizes the first word', () => {
    expect(capitalizeFirstLetter('one two')).toBe('One two');
  });
});

describe('capitalizeEachWord', () => {
  it('Capitalizes all words', () => {
    expect(capitalizeEachWord('one two')).toBe('One Two');
  });
});
