var webpack = require("webpack");
var HtmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
	entry: {
		'users' : './app/users/user.module.ts',

		'app' : './app/usermanager.app.ts',

		'vendor' : './app/vendor.ts',
		'shim' : 'core-js/fn/object/assign'
	},
	output: {
		filename: './[name].bundle.js',
		path: __dirname + '/../webapp'
	},
	resolve: {
		extensions: ['.webpack.js', '.web.js', '.ts', '.js']
	},
	module: {
		loaders: [
			{ test: /\.ts$/, loader: 'awesome-typescript-loader' },
			{ test: /\.html/, loader: 'raw-loader' },
			{ test: /\.css$/, loader: "style-loader!css-loader" },
			{ test: /\.less$/, loader: "style-loader!css-loader!less-loader" },
			{	test: /\.(eot|svg|ttf|woff(2)?)(\?v=\d+\.\d+\.\d+)?/, loader: 'url-loader' },
			{ test:  /\.(jpe?g|png|gif|svg)$/i, loader: 'file-loader' }
		]
	},
	externals: {
		"jquery": "jQuery"
	},
	plugins: [
		new webpack.optimize.CommonsChunkPlugin({
			name: [
				'app',
				'layout',
				'users',
				'dialogs', 'security', 'common',
				'shim', 'vendor'
			]
		}),
		new webpack.ContextReplacementPlugin(
			/angular(\\|\/)core(\\|\/)(esm(\\|\/)src|src)(\\|\/)linker/,
			__dirname
		),
		new HtmlWebpackPlugin(
		{
			template: 'index.ejs',
			inject: 'body'
		}),
		new webpack.ProvidePlugin({
			'moment': 'moment'
		}),
		new webpack.optimize.UglifyJsPlugin({
			compress: { warnings: false }
		})
	],
	devServer: {
		inline: true,
		contentBase: '..\\webapp',

		proxy: {
			'/api': { target: 'http://localhost:8000' },
			'/public': { target: 'http://localhost:8000'}
		}
	}
};