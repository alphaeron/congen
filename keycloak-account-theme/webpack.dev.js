const path = require('path');
const { merge } = require('webpack-merge');
const common = require('./webpack.common.js');
const HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = merge(common, {
  mode: 'development',

  // Better source maps for development
  devtool: 'source-map',

  // React 19 DevTools compatibility
  resolve: {
    ...common.resolve,
    alias: {
      ...common.resolve.alias,
    },
  },

  output: {
    sourceMapFilename: '[name].map',
    // Development-specific output settings
    filename: '[name].js',
    chunkFilename: '[name].chunk.js',
    publicPath: '/',
  },

  // Enhanced development server configuration
  devServer: {
    static: {
      directory: path.resolve(__dirname, 'dist'),
      publicPath: '/',
    },
    port: 3001, // Different port from frontend
    host: 'localhost',
    open: true,
    hot: true, // Enable hot module replacement
    liveReload: true,
    compress: true, // Enable gzip compression for dev server
    historyApiFallback: true, // Enable SPA routing
    client: {
      overlay: {
        errors: true,
        warnings: false, // Don't overlay warnings
      },
      progress: true,
    },
    // Development-specific headers
    headers: {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, PATCH, OPTIONS',
      'Access-Control-Allow-Headers': 'X-Requested-With, content-type, Authorization',
    },
    // Better error reporting
    setupMiddlewares: (middlewares, devServer) => {
      if (!devServer) {
        throw new Error('webpack-dev-server is not defined');
      }

      // Log when server starts
      console.log('🚀 Keycloak Account Theme Development server starting...');

      return middlewares;
    },
  },

  module: {
    rules: [
      {
        test: /\.css$/,
        use: [
          'style-loader',
          {
            loader: 'css-loader',
            options: {
              sourceMap: true, // Enable source maps for CSS
              importLoaders: 1,
              url: true, // Enable URL processing for font files
            },
          },
        ],
      },
    ],
  },

  // Development-specific optimizations
  optimization: {
    // Disable minification in development for faster builds
    minimize: false,
    // Keep module names for better debugging
    moduleIds: 'named',
    chunkIds: 'named',
    // Split chunks for better development experience
    splitChunks: {
      chunks: 'all',
      cacheGroups: {
        // Separate vendor chunks for faster rebuilds
        vendor: {
          test: /[\\/]node_modules[\\/]/,
          name: 'vendors',
          chunks: 'all',
          priority: 10,
        },
        // Separate React for faster development
        react: {
          test: /[\\/]node_modules[\\/](react|react-dom)[\\/]/,
          name: 'react',
          chunks: 'all',
          priority: 20,
        },
      },
    },
    // Keep runtime in a separate chunk
    runtimeChunk: 'single',
  },

  // Development-specific performance settings
  performance: {
    hints: false, // Disable performance hints in development
  },

  // Development-specific resolve settings
  resolve: {
    ...common.resolve,
    // Add development-specific aliases if needed
    alias: {
      ...common.resolve.alias,
      // Add any development-specific aliases here
    },
  },

  // Development-specific experiments
  experiments: {
    // Enable top-level await for development
    topLevelAwait: true,
  },

  plugins: [
    ...(common.plugins || []),
    new HtmlWebpackPlugin({
      template: path.resolve(__dirname, 'public/index.html'),
      filename: 'index.html',
      inject: true,
      minify: false,
    }),
    // Add a plugin to help with React DevTools detection
    new (require('webpack').DefinePlugin)({
      'process.env.NODE_ENV': JSON.stringify('development'),
      __REACT_DEVTOOLS_GLOBAL_HOOK__: 'window.__REACT_DEVTOOLS_GLOBAL_HOOK__',
    }),
  ],
});
