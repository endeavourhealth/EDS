var webpack = require("webpack");
var HtmlWebpackPlugin = require('html-webpack-plugin');
var CopyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
	entry: {
		'shim' : 'core-js/fn/object/assign',
		'vendor' : './app/vendor.ts',
		'app' : './app/dataServiceManager.app.ts'
	},
	output: {
		filename: './[name].bundle.js',
		path: '../webapp'
	},
	resolve: {
		extensions: ['', '.webpack.js', '.web.js', '.ts', '.js']
	},
	module: {
		loaders: [
			{ test: /\.ts$/, loader: 'ts' },
			{ test: /\.html/, loader: 'raw' },
			{ test: /\.css$/, loader: "style-loader!css-loader" },
			{ test: /\.less$/, loader: "style!css!less" },
			{	test: /\.(eot|svg|ttf|woff(2)?)(\?v=\d+\.\d+\.\d+)?/, loader: 'url' },
			{ test:  /\.(jpe?g|png|gif|svg)$/i, loader: 'file' }
		]
	},
	externals: {
		"jquery": "jQuery"
	},
	plugins: [
		new HtmlWebpackPlugin(
		{
			template: 'index.ejs',
			inject: 'body'
		}),
		new CopyWebpackPlugin([
			{ from: './api-docs', to: './api-docs' },
			{ from: './node_modules/swagger-ui/dist', to: './api-docs/swagger-ui' }
		]),
		new webpack.optimize.DedupePlugin(),
		new webpack.ProvidePlugin({
			'moment': 'moment'
		})
	],
	devServer: {
		inline: true,
		contentBase: '..\\webapp',
		watch: true,
		progress: true,
		colors: true,

		proxy: {
			'/api': { target: 'http://localhost:8000' },
			'/public': { target: 'http://localhost:8000'}
		}
	}
};