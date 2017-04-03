var webpack = require("webpack");
var config  = require('./webpack.config');

config.devtool = 'source-map';

config.plugins = config.plugins.concat([
	new webpack.DefinePlugin({
		PRODUCTION: JSON.stringify(false)
	})
]);

module.exports = config;