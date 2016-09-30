var webpack = require("webpack");
var HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
	entry: './app/app.module.ts',
	output: {
		filename: './bundle.js'
	},
	resolve: {
		extensions: ['', '.webpack.js', '.web.js', '.ts', '.js']
	},
	module: {
		loaders: [
			{ test: /\.ts$/, loader: 'ts' },
			{ test: /\.html/, loader: 'raw' },
			{ test: /\.css$/, loader: "style-loader!css-loader" },
			{	test: /\.(eot|svg|ttf|woff(2)?)(\?v=\d+\.\d+\.\d+)?/, loader: 'url' },
			{ test:  /\.(jpe?g|png|gif|svg)$/i, loader: 'file' }
		]
	},
	plugins: [
		new HtmlWebpackPlugin(
		{
			template: 'index.ejs',
			inject: 'body'
		}),
		new webpack.optimize.DedupePlugin()
	],
	devServer: {
		inline: true,
		contentBase: '..\\webapp',
		watch: true,
		progress: true,
		colors: true,

		proxy: {
			'/api': {
				target: 'http://localhost:8000'
			}
		}
	}
}