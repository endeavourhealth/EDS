var webpack = require('webpack');
var config  = require('./webpack.config');

config.devtool = 'false';

config.plugins = config.plugins.concat([
	new webpack.DefinePlugin({
		PRODUCTION: JSON.stringify(true)
	})
]);

module.exports = config;