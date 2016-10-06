var config  = require('./webpack.config');

config.output.path = '../webapp';
config.devtool = 'source-map';

module.exports = config;