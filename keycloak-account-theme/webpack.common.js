const path = require('path');
const webpack = require('webpack');
const CopyWebpackPlugin = require('copy-webpack-plugin');

const TerserPlugin = require('terser-webpack-plugin');
const FaviconsWebpackPlugin = require('favicons-webpack-plugin');

module.exports = {
  entry: {
    'keycloak-account-theme': path.resolve(__dirname, 'src/main.tsx'),
  },

  plugins: [
    new webpack.ProvidePlugin({
      process: 'process/browser.js',
      React: 'react',
    }),
    new webpack.ProgressPlugin(),
    new FaviconsWebpackPlugin(path.resolve(__dirname, 'public/logo.png')),
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV || 'development'),
      'process.env.REACT_APP_FRONTEND_URL': JSON.stringify(
        process.env.REACT_APP_FRONTEND_URL || 'http://localhost:3000'
      ),
    }),
    new CopyWebpackPlugin({
      patterns: [
        {
          from: 'public/favicon.ico',
          to: 'img/favicon.ico',
        },
      ],
    }),
  ],

  module: {
    noParse: /\/node_modules\/process\//,
    rules: [
      {
        test: /.(js|jsx|ts|tsx)?$/,
        use: {
          loader: 'babel-loader',
          options: {
            targets: 'defaults',
            presets: ['@babel/preset-env', '@babel/preset-react', '@babel/preset-typescript'],
          },
        },
        exclude: [/node_modules/],
      },
      {
        test: /.html$/,
        use: 'html-loader',
      },
      {
        // write image files under 10k to inline or copy image files over 10k
        test: /\.(jpg|jpeg|gif|png|svg|ico)?$/,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 10000,
              fallback: 'file-loader',
              name: 'img/[name].[ext]',
            },
          },
        ],
      },
      {
        test: /\.json$/,
        type: 'json',
      },
    ],
  },

  resolve: {
    extensions: ['.tsx', '.ts', '.js', '.jsx'],
    modules: ['src', 'node_modules'],
  },

  optimization: {
    minimizer: [
      new TerserPlugin({
        parallel: true,
        terserOptions: {
          parse: {
            ecma: 8,
          },
          compress: {
            comparisons: false,
            ecma: 5,
            inline: 2,
          },
          output: {
            ascii_only: true,
            ecma: 5,
          },
        },
      }),
    ],
  },

  output: {
    filename: '[name].js',
    chunkFilename: '[name].js',
    path: path.resolve(__dirname, 'dist'),
    publicPath: './',
    clean: true,
  },
};
