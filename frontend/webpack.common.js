const path = require('path');
const webpack = require('webpack');

const TerserPlugin = require('terser-webpack-plugin');
const FaviconsWebpackPlugin = require('favicons-webpack-plugin');
const RobotstxtPlugin = require('robotstxt-webpack-plugin');
const { BundleAnalyzerPlugin } = require('webpack-bundle-analyzer');

module.exports = {
  entry: {
    'congen-ui': path.resolve(__dirname, 'src/index.tsx'),
  },

  plugins: [
    new webpack.ProvidePlugin({
      process: 'process/browser.js',
    }),
    new webpack.ProgressPlugin(),
    new FaviconsWebpackPlugin(path.resolve(__dirname, 'public/logo.png')),
    new RobotstxtPlugin({
      filePath: path.resolve(__dirname, 'public/robots.txt'),
    }),

    // Bundle analyzer (always enabled)
    new BundleAnalyzerPlugin({
      analyzerMode: 'static',
      analyzerHost: '127.0.0.1',
      analyzerPort: 8888,
      reportFilename: 'bundle-report.html',
      defaultSizes: 'parsed',
      openAnalyzer: false,
      generateStatsFile: true,
      statsFilename: 'bundle-stats.json',
      statsOptions: {
        source: false,
        modules: true,
        chunks: true,
        assets: true,
        children: false,
      },
      logLevel: 'info',
    }),

    // Common bundle size reporter
    new (class BundleSizeReporter {
      apply(compiler) {
        compiler.hooks.done.tap('BundleSizeReporter', stats => {
          const chunkGraph = stats.compilation.chunkGraph;
          const chunks = Array.from(stats.compilation.chunks);
          const isProduction = stats.compilation.options.mode === 'production';

          console.log(`\n📦 ${isProduction ? 'Production' : 'Development'} Bundle Size Report:`);
          console.log('='.repeat(50));

          // Report main chunks
          chunks.forEach(chunk => {
            if (chunk.name) {
              const size = chunkGraph.getChunkSize(chunk);
              if (isProduction) {
                const gzippedSize = this.getGzippedSize(size);
                console.log(
                  `📄 ${chunk.name}: ${this.formatSize(size)} (${this.formatSize(gzippedSize)} gzipped)`
                );
              } else {
                console.log(`📄 ${chunk.name}: ${this.formatSize(size)}`);
              }
            }
          });

          // Report total
          const totalSize = chunks.reduce((sum, chunk) => sum + chunkGraph.getChunkSize(chunk), 0);
          if (isProduction) {
            const totalGzipped = this.getGzippedSize(totalSize);
            console.log('─'.repeat(50));
            console.log(
              `📊 Total: ${this.formatSize(totalSize)} (${this.formatSize(totalGzipped)} gzipped)`
            );
          } else {
            console.log('─'.repeat(50));
            console.log(`📊 Total: ${this.formatSize(totalSize)}`);
          }
          console.log('='.repeat(50));
        });
      }

      getGzippedSize(size) {
        return Math.round(size * 0.3); // Rough estimate
      }

      formatSize(bytes) {
        if (bytes === 0) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
      }
    })(),
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
    publicPath: '/',
    clean: true,
  },
};
