module.exports = {
  presets: [
    [
      '@babel/preset-typescript',
      {
        targets: {
          node: 'current',
        },
      },
    ],
    '@babel/preset-env',
    '@babel/preset-react',
  ],
};
