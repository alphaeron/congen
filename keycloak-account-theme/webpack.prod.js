const path = require('path');
const webpack = require('webpack');
const { merge } = require('webpack-merge');
const common = require('./webpack.common.js');

const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const CssMinimizerPlugin = require('css-minimizer-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');

module.exports = merge(common, {
  mode: 'production',
  devtool: 'source-map',

  plugins: [
    new webpack.DefinePlugin({
      'process.env.PUBLIC_URL': JSON.stringify(''),
      'process.env.REACT_APP_FRONTEND_URL': JSON.stringify(
        process.env.REACT_APP_FRONTEND_URL || 'http://localhost:3000'
      ),
    }),
    new MiniCssExtractPlugin({
      filename: 'css/[name].[contenthash:8].css',
      chunkFilename: 'css/[name].[contenthash:8].chunk.css',
    }),
    new HtmlWebpackPlugin({
      template: path.resolve(__dirname, 'public/index.html'),
      filename: 'index.html',
      inject: true,
      minify: {
        removeComments: true,
        collapseWhitespace: true,
        removeRedundantAttributes: true,
        useShortDoctype: true,
        removeEmptyAttributes: true,
        removeStyleLinkTypeAttributes: true,
        keepClosingSlash: true,
        minifyJS: true,
        minifyCSS: true,
        minifyURLs: true,
      },
    }),
  ],

  module: {
    rules: [
      {
        test: /\.css$/,
        use: [
          MiniCssExtractPlugin.loader,
          {
            loader: 'css-loader',
            options: {
              importLoaders: 1,
              sourceMap: false,
              url: true, // Enable URL processing for font files
            },
          },
        ],
      },
    ],
  },

  optimization: {
    minimize: true,
    minimizer: [
      new TerserPlugin({
        parallel: true,
        terserOptions: {
          parse: {
            ecma: 2020,
          },
          compress: {
            ecma: 5,
            warnings: false,
            comparisons: false,
            inline: 2,
            drop_console: true,
            drop_debugger: true,
            pure_funcs: ['console.log', 'console.info', 'console.debug', 'console.warn'],
            // Additional optimizations
            dead_code: true,
            unused: true,
            side_effects: false,
          },
          mangle: {
            safari10: true,
            properties: {
              regex: /^_/,
            },
          },
          output: {
            ecma: 5,
            comments: false,
            ascii_only: true,
          },
        },
        extractComments: false,
      }),
      new CssMinimizerPlugin({
        minimizerOptions: {
          preset: [
            'default',
            {
              discardComments: { removeAll: true },
              normalizeWhitespace: true,
              minifyFontValues: true,
              minifySelectors: true,
            },
          ],
        },
      }),
    ],

    splitChunks: {
      chunks: 'all',
      minSize: 10000,
      minRemainingSize: 0,
      minChunks: 1,
      maxAsyncRequests: 20,
      maxInitialRequests: 20,
      enforceSizeThreshold: 30000,
      cacheGroups: {
        // Separate React and ReactDOM (highest priority)
        react: {
          test: /[\\/]node_modules[\\/](react|react-dom|scheduler)[\\/]/,
          name: 'react',
          chunks: 'all',
          priority: 50,
          enforce: true,
        },
        // Separate Material-UI (high priority) - split by package
        mui: {
          test: /[\\/]node_modules[\\/]@mui[\\/]/,
          name: 'mui',
          chunks: 'all',
          priority: 40,
          enforce: true,
        },
        // Separate Emotion (MUI dependency)
        emotion: {
          test: /[\\/]node_modules[\\/]@emotion[\\/]/,
          name: 'emotion',
          chunks: 'all',
          priority: 35,
          enforce: true,
        },
        // Keycloakify specific
        keycloakify: {
          test: /[\\/]node_modules[\\/]keycloakify[\\/]/,
          name: 'keycloakify',
          chunks: 'all',
          priority: 30,
          enforce: true,
        },
        // Other vendor libraries
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendors',
          chunks: 'all',
          priority: 10,
          reuseExistingChunk: true,
        },
        // Default chunk
        default: {
          minChunks: 2,
          priority: -20,
          reuseExistingChunk: true,
        },
      },
    },

    runtimeChunk: {
      name: 'runtime',
    },

    // Tree shaking optimization
    usedExports: true,
    sideEffects: false,

    // Additional optimizations
    concatenateModules: true,
    flagIncludedChunks: true,
    providedExports: true,
    removeAvailableModules: true,
    removeEmptyChunks: true,
  },

  performance: {
    hints: 'warning',
    maxEntrypointSize: 1024000, // Increased to 1MB for better UX
    maxAssetSize: 1024000, // Increased to 1MB for better UX
    assetFilter: function (assetFilename) {
      // Only check JS and CSS files
      return /\.(js|css)$/.test(assetFilename);
    },
  },

  output: {
    filename: 'js/[name].[contenthash:8].js',
    chunkFilename: 'js/[name].[contenthash:8].chunk.js',
    assetModuleFilename: 'assets/[name].[contenthash:8][ext]',
    publicPath: './',
    clean: true,
  },

  // Security: Prevent eval and other dangerous features
  experiments: {
    topLevelAwait: true,
  },

  // Additional security measures
  resolve: {
    ...common.resolve,
    fallback: {
      ...common.resolve.fallback,
      crypto: false,
      stream: false,
      util: false,
      buffer: false,
      process: false,
    },
  },
});
