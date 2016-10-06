// var webpack = require('webpack');
var config  = require('./webpack.config');

config.output.path = '../webapp';
config.devtool = 'false';

// config.plugins = config.plugins.concat([
// 	new webpack.optimize.UglifyJsPlugin({
// 		mangle: false,
// 		sourceMap: true,
// 		compress: {
// 			warnings: false
// 		}
// 	})
// ]);

module.exports = config;